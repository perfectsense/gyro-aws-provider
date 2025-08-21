/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.cloudfront;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.waf.global.WebAclResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.TestValue;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.CacheBehavior;
import software.amazon.awssdk.services.cloudfront.model.CacheBehaviors;
import software.amazon.awssdk.services.cloudfront.model.CloudFrontException;
import software.amazon.awssdk.services.cloudfront.model.CreateDistributionResponse;
import software.amazon.awssdk.services.cloudfront.model.CustomErrorResponse;
import software.amazon.awssdk.services.cloudfront.model.CustomErrorResponses;
import software.amazon.awssdk.services.cloudfront.model.Distribution;
import software.amazon.awssdk.services.cloudfront.model.DistributionConfig;
import software.amazon.awssdk.services.cloudfront.model.GetDistributionResponse;
import software.amazon.awssdk.services.cloudfront.model.GetMonitoringSubscriptionResponse;
import software.amazon.awssdk.services.cloudfront.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.cloudfront.model.NoSuchDistributionException;
import software.amazon.awssdk.services.cloudfront.model.Origin;
import software.amazon.awssdk.services.cloudfront.model.OriginGroup;
import software.amazon.awssdk.services.cloudfront.model.OriginGroups;
import software.amazon.awssdk.services.cloudfront.model.Origins;
import software.amazon.awssdk.services.cloudfront.model.Tag;
import software.amazon.awssdk.services.cloudfront.model.Tags;
import software.amazon.awssdk.services.cloudfront.model.UpdateDistributionResponse;

/**
 * Create a CloudFront distribution.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::cloudfront cloudfront-example
 *        enabled: true
 *        ipv6-enabled: false
 *        comment: "$(project) - static asset cache"
 *
 *        origin
 *            id: "S3-$(project)-brightspot"
 *            domain-name: "$(project)-brightspot.s3.us-east-1.amazonaws.com"
 *        end
 *
 *        origin
 *            id: "elb-$(project)-web"
 *            domain-name: "www.google.com"
 *
 *            custom-origin
 *                http-port: 80
 *            end
 *        end
 *
 *        default-cache-behavior
 *            target-origin-id: "S3-$(project)-brightspot"
 *            viewer-protocol-policy: "allow-all"
 *            allowed-methods: ["GET", "HEAD"]
 *            cached-methods: ["GET", "HEAD"]
 *            cache-policy: $(aws::cloudfront-cache-policy cache-policy-example)
 *            origin-request-policy: $(aws::cloudfront-origin-request-policy origin-request-policy-example)
 *        end
 *
 *        behavior
 *            path-pattern: "/dims?/*"
 *            target-origin-id: "elb-$(project)-web"
 *            viewer-protocol-policy: "allow-all"
 *            allowed-methods: ["GET", "HEAD"]
 *            query-string: true
 *        end
 *
 *        geo-restriction
 *            type: "whitelist"
 *            restrictions: ["US"]
 *        end
 *
 *        @for error-code, ttl -in [400, 0, 403, 5, 404, 5, 500, 0, 502, 0, 503, 0, 504, 0]
 *            custom-error-response
 *                error-code: $(error-code)
 *                ttl: $(ttl)
 *            end
 *        @end
 *
 *        tags: {
 *            Name: "content cache"
 *        }
 *    end
 */
@Type("cloudfront")
public class CloudFrontResource extends AwsResource implements Copyable<Distribution> {

    private Boolean enabled;
    private String comment;
    private List<String> cnames;
    private String httpVersion;
    private String priceClass;
    private String defaultRootObject;
    private String etag;
    private String callerReference;
    private Boolean isIpv6Enabled;
    private WebAclResource webAcl;
    private Map<String, String> tags;
    private Set<CloudFrontOrigin> origin;
    private Set<CloudFrontOriginGroup> originGroup;
    private Set<CloudFrontCacheBehavior> behavior;
    private CloudFrontCacheBehavior defaultCacheBehavior;
    private CloudFrontViewerCertificate viewerCertificate;
    private CloudFrontLogging logging;
    private List<CloudFrontCustomErrorResponse> customErrorResponse;
    private CloudFrontGeoRestriction geoRestriction;
    private MonitoringSubscription monitoringSubscription;

    // -- Read only
    private String id;
    private String arn;
    private String domainName;

    /**
     * The id of this CloudFront distribution.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The arn of this CloudFront distribution.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * Enable or disable this distribution without deleting it.
     */
    @Updatable
    public boolean getEnabled() {
        if (enabled == null) {
            enabled = true;
        }

        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * A comment for this distribution.
     */
    @Updatable
    public String getComment() {
        if (comment == null) {
            return "";
        }

        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * CNAMES (aliases) for which this distribution will listen for.
     */
    @Updatable
    public List<String> getCnames() {
        if (cnames == null) {
            cnames = new ArrayList<>();
        }

        Collections.sort(cnames);

        return cnames;
    }

    public void setCnames(List<String> cnames) {
        this.cnames = cnames;
    }

    /**
     * The maximum http version that users can request on this distribution.
     */
    @Updatable
    @ValidStrings({"http1.1", "http2", "http3", "http2and3"})
    public String getHttpVersion() {
        if (httpVersion == null) {
            httpVersion = "http1.1";
        }

        return httpVersion;
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    /**
     * The maximum price you want to pay for CloudFront. For information on pricing see `Price classes <https://aws.amazon.com/cloudfront/pricing/#On-demand_Pricing>`_.
     */
    @Updatable
    @ValidStrings({"PriceClass_All", "PriceClass_200", "PriceClass_100"})
    public String getPriceClass() {
        if (priceClass == null) {
            return "PriceClass_All";
        }

        return priceClass;
    }

    public void setPriceClass(String priceClass) {
        this.priceClass = priceClass;
    }

    /**
     * The object to request from the origin when a user requests the root URL (i.e. http://www.example.com/).
     */
    @Updatable
    public String getDefaultRootObject() {
        if (defaultRootObject == null) {
            return "";
        }

        return defaultRootObject;
    }

    public void setDefaultRootObject(String defaultRootObject) {
        this.defaultRootObject = defaultRootObject;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getCallerReference() {
        return callerReference;
    }

    public void setCallerReference(String callerReference) {
        this.callerReference = callerReference;
    }

    /**
     * Enable IPv6 support for this distribution.
     */
    @Updatable
    public boolean getIpv6Enabled() {
        if (isIpv6Enabled == null) {
            isIpv6Enabled = false;
        }

        return isIpv6Enabled;
    }

    public void setIpv6Enabled(boolean ipv6Enabled) {
        isIpv6Enabled = ipv6Enabled;
    }

    /**
     * The Web ACL (WAF) ID to associate with this distribution.
     */
    @Updatable
    public WebAclResource getWebAcl() {
        return webAcl;
    }

    public void setWebAcl(WebAclResource webAcl) {
        this.webAcl = webAcl;
    }

    /**
     * The domain name for this distribution (i.e. ``abc123893.cloudfront.net``).
     *
     */
    @Output
    @TestValue("abc123.cloudfront.net")
    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    /**
     * A map of tags to apply to this distribution.
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * List of origins for this distribution.
     *
     * @subresource gyro.aws.cloudfront.CloudFrontOrigin
     */
    @Updatable
    public Set<CloudFrontOrigin> getOrigin() {
        if (origin == null) {
            origin = new HashSet<>();
        }

        return origin;
    }

    public void setOrigin(Set<CloudFrontOrigin> origin) {
        this.origin = origin;
    }

    /**
     * List of origin groups for this distribution.
     *
     * @subresource gyro.aws.cloudfront.CloudFrontOriginGroup
     */
    @Updatable
    public Set<CloudFrontOriginGroup> getOriginGroup() {
        if (originGroup == null) {
            originGroup = new HashSet<>();
        }

        return originGroup;
    }

    public void setOriginGroup(Set<CloudFrontOriginGroup> originGroup) {
        this.originGroup = originGroup;
    }

    /**
     * List of cache behaviors for this distribution.
     *
     * @subresource gyro.aws.cloudfront.CloudFrontCacheBehavior
     */
    @Updatable
    public Set<CloudFrontCacheBehavior> getBehavior() {
        if (behavior == null) {
            behavior = new HashSet<>();
        }

        return behavior;
    }

    public void setBehavior(Set<CloudFrontCacheBehavior> behavior) {
        this.behavior = behavior;
    }

    /**
     * The default cache behavior for this distribution.
     *
     * @subresource gyro.aws.cloudfront.CloudFrontCacheBehavior
     */
    @Updatable
    public CloudFrontCacheBehavior getDefaultCacheBehavior() {
        return defaultCacheBehavior;
    }

    public void setDefaultCacheBehavior(CloudFrontCacheBehavior defaultCacheBehavior) {
        this.defaultCacheBehavior = defaultCacheBehavior;

        defaultCacheBehavior.setPathPattern("*");
    }

    /**
     * SSL certificate configuration.
     *
     * @subresource gyro.aws.cloudfront.CloudFrontViewerCertificate
     */
    @Updatable
    public CloudFrontViewerCertificate getViewerCertificate() {
        if (viewerCertificate == null) {
            viewerCertificate = newSubresource(CloudFrontViewerCertificate.class);
        }

        return viewerCertificate;
    }

    public void setViewerCertificate(CloudFrontViewerCertificate viewerCertificate) {
        this.viewerCertificate = viewerCertificate;
    }

    /**
     * Configure logging access logs to S3.
     *
     * @subresource gyro.aws.cloudfront.CloudFrontLogging
     */
    @Updatable
    public CloudFrontLogging getLogging() {
        return logging;
    }

    public void setLogging(CloudFrontLogging logging) {
        this.logging = logging;
    }

    /**
     * Replace HTTP codes with custom error responses as well as define cache TTLs for error responses.
     *
     * @subresource gyro.aws.cloudfront.CloudFrontCustomErrorResponse
     */
    @Updatable
    public List<CloudFrontCustomErrorResponse> getCustomErrorResponse() {
        if (customErrorResponse == null) {
            customErrorResponse = new ArrayList<>();
        }

        customErrorResponse.sort(Comparator.comparing(CloudFrontCustomErrorResponse::getErrorCode));

        return customErrorResponse;
    }

    public void setCustomErrorResponse(List<CloudFrontCustomErrorResponse> customErrorResponses) {
        this.customErrorResponse = customErrorResponses;
    }

    /**
     * Restrict or allow access to this distribution by country.
     *
     * @subresource gyro.aws.cloudfront.CloudFrontGeoRestriction
     */
    @Updatable
    public CloudFrontGeoRestriction getGeoRestriction() {
        return geoRestriction;
    }

    public void setGeoRestriction(CloudFrontGeoRestriction geoRestriction) {
        this.geoRestriction = geoRestriction;
    }

    /**
     * Monitoring subscription configuration for cloudfront.
     *
     * @subresource gyro.aws.cloudfront.MonitoringSubscription
     */
    @Updatable
    public MonitoringSubscription getMonitoringSubscription() {
        return monitoringSubscription;
    }

    public void setMonitoringSubscription(MonitoringSubscription monitoringSubscription) {
        this.monitoringSubscription = monitoringSubscription;
    }

    @Override
    public void copyFrom(Distribution distribution) {
        setArn(distribution.arn());
        setId(distribution.id());
        DistributionConfig config = distribution.distributionConfig();

        setEnabled(config.enabled());
        setComment(config.comment());
        setCnames(config.aliases().items().isEmpty() ? new ArrayList<>() : new ArrayList<>(config.aliases().items()));
        setHttpVersion(config.httpVersionAsString());
        setPriceClass(config.priceClassAsString());
        setCallerReference(config.callerReference());
        setIpv6Enabled(config.isIPV6Enabled());
        setDomainName(distribution.domainName());
        setWebAcl(!ObjectUtils.isBlank(config.webACLId()) ? findById(WebAclResource.class, config.webACLId()) : null);

        getOrigin().clear();
        if (config.origins() != null) {
            for (Origin origin : config.origins().items()) {
                CloudFrontOrigin originResource = newSubresource(CloudFrontOrigin.class);
                originResource.copyFrom(origin);
                getOrigin().add(originResource);
            }
        }

        getOriginGroup().clear();
        if (config.originGroups() != null) {
            for (OriginGroup originGroup : config.originGroups().items()) {
                CloudFrontOriginGroup originGroupResource = newSubresource(CloudFrontOriginGroup.class);
                originGroupResource.copyFrom(originGroup);
                getOriginGroup().add(originGroupResource);
            }
        }

        getBehavior().clear();
        if (config.cacheBehaviors() != null) {
            for (CacheBehavior behavior : config.cacheBehaviors().items()) {
                CloudFrontCacheBehavior cacheBehavior = newSubresource(CloudFrontCacheBehavior.class);
                cacheBehavior.copyFrom(behavior);
                getBehavior().add(cacheBehavior);
            }
        }

        CloudFrontCacheBehavior defaultCacheBehavior = newSubresource(CloudFrontCacheBehavior.class);
        defaultCacheBehavior.copyFrom(CloudFrontCacheBehavior.getCacheBehaviorFromDefault(config.defaultCacheBehavior()));
        setDefaultCacheBehavior(defaultCacheBehavior);


        CloudFrontLogging logging = null;

        if (config.logging() != null && config.logging().enabled()) {
            logging = newSubresource(CloudFrontLogging.class);
            logging.copyFrom(config.logging());
        }

        setLogging(logging);


        CloudFrontViewerCertificate viewerCertificate = newSubresource(CloudFrontViewerCertificate.class);

        if (config.viewerCertificate() != null) {
            viewerCertificate.copyFrom(config.viewerCertificate());
        }

        setViewerCertificate(viewerCertificate);


        CloudFrontGeoRestriction geoRestriction = newSubresource(CloudFrontGeoRestriction.class);
        geoRestriction.copyFrom(config.restrictions().geoRestriction());
        setGeoRestriction(geoRestriction);

        getCustomErrorResponse().clear();
        setCustomErrorResponse(config.customErrorResponses().items().stream().map(errorResponse -> {
            CloudFrontCustomErrorResponse customErrorResponse = newSubresource(CloudFrontCustomErrorResponse.class);
            customErrorResponse.copyFrom(errorResponse);

            return customErrorResponse;
        }).collect(Collectors.toList()));

        CloudFrontClient client = createClient(CloudFrontClient.class, "us-east-1", "https://cloudfront.amazonaws.com");

        ListTagsForResourceResponse tagsForResource = client.listTagsForResource(r -> r.resource(getArn()));
        getTags().clear();
        for (Tag tag: tagsForResource.tags().items()) {
            getTags().put(tag.key(), tag.value());
        }

        GetDistributionResponse response = client.getDistribution(r -> r.id(getId()));
        setEtag(response.eTag());

        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> {
                try {
                    GetMonitoringSubscriptionResponse monitoringSubscription = client.getMonitoringSubscription(r -> r.distributionId(getId()));

                    setMonitoringSubscription(null);
                    if (monitoringSubscription != null) {
                        MonitoringSubscription monitoringSubscriptionObj = newSubresource(MonitoringSubscription.class);
                        monitoringSubscriptionObj.copyFrom(monitoringSubscription.monitoringSubscription());
                        setMonitoringSubscription(monitoringSubscriptionObj);
                    }

                    return true;
                } catch (CloudFrontException ex) {
                    if (ex.retryable() || ex.getMessage().contains("try again"))  {
                        return false;
                    } else if (ex.statusCode() == 404) {
                        MonitoringSubscription monitoringSubscriptionObj = newSubresource(MonitoringSubscription.class);
                        monitoringSubscriptionObj.setDisabledObj();
                        setMonitoringSubscription(monitoringSubscriptionObj);
                        return true;
                    } else {
                        throw ex;
                    }
                }
            });


    }

    @Override
    public boolean refresh() {
        CloudFrontClient client = createClient(CloudFrontClient.class, "us-east-1", "https://cloudfront.amazonaws.com");

        try {
            GetDistributionResponse response = client.getDistribution(r -> r.id(getId()));

            Distribution distribution = response.distribution();

            copyFrom(distribution);

        } catch (NoSuchDistributionException ex) {
            return false;
        }

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        CloudFrontClient client = createClient(CloudFrontClient.class, "us-east-1", "https://cloudfront.amazonaws.com");

        CreateDistributionResponse response = client.createDistribution(c -> c.distributionConfig(distributionConfig()));
        setId(response.distribution().id());
        setArn(response.distribution().arn());
        setDomainName(response.distribution().domainName());
        setEtag(response.eTag());

        applyTags(client, Collections.emptyMap());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        CloudFrontClient client = createClient(CloudFrontClient.class, "us-east-1", "https://cloudfront.amazonaws.com");

        UpdateDistributionResponse response = client.updateDistribution(r -> r.distributionConfig(distributionConfig())
            .id(getId())
            .ifMatch(getEtag()));

        setEtag(response.eTag());

        if (changedFieldNames.contains("tags")) {
            CloudFrontResource currentCf = (CloudFrontResource) current;

            applyTags(client, currentCf.getTags());
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        CloudFrontClient client = createClient(CloudFrontClient.class, "us-east-1", "https://cloudfront.amazonaws.com");

        if (getEnabled()) {
            setEnabled(false);

            client.updateDistribution(r -> r.distributionConfig(distributionConfig())
                .id(getId())
                .ifMatch(getEtag()));

            boolean deploying = true;
            do {
                GetDistributionResponse response = client.getDistribution(r -> r.id(getId()));
                setEtag(response.eTag());

                if (response.distribution().status().equals("Deployed")) {
                    deploying = false;
                } else {
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException ie) {
                        throw new GyroException(ie.getMessage());
                    }
                }
            } while (deploying);
        }

        client.deleteDistribution(r -> r.id(getId()).ifMatch(getEtag()));
    }

    private void applyTags(CloudFrontClient client, Map<String, String> oldTags) {
        if (!oldTags.isEmpty()) {
            client.untagResource(r -> r.resource(getArn()).tagKeys(t -> t.items(oldTags.keySet())));
        }

        List<Tag> tags = new ArrayList<>();
        for (String key : getTags().keySet()) {
            Tag tag = Tag.builder().key(key).value(getTags().get(key)).build();
            tags.add(tag);
        }

        client.tagResource(r -> r.tags(Tags.builder().items(tags).build()).resource(getArn()));
    }

    private DistributionConfig distributionConfig() {
        DistributionConfig.Builder builder = DistributionConfig.builder();

        List<CustomErrorResponse> errorResponses = getCustomErrorResponse()
            .stream()
            .map(CloudFrontCustomErrorResponse::toCustomErrorResponse)
            .collect(Collectors.toList());

        CustomErrorResponses customErrorResponses = CustomErrorResponses.builder()
            .items(errorResponses)
            .quantity(errorResponses.size())
            .build();

        List<CacheBehavior> behaviors = getBehavior()
            .stream()
            .map(CloudFrontCacheBehavior::toCachBehavior)
            .collect(Collectors.toList());

        CacheBehaviors cacheBehaviors = CacheBehaviors.builder()
            .items(behaviors)
            .quantity(behaviors.size())
            .build();

        List<Origin> origin = getOrigin()
            .stream()
            .map(CloudFrontOrigin::toOrigin)
            .collect(Collectors.toList());

        Origins origins = Origins.builder()
            .items(origin)
            .quantity(origin.size())
            .build();

        List<OriginGroup> originGroups = getOriginGroup()
            .stream()
            .map(CloudFrontOriginGroup::toOriginGroup)
            .collect(Collectors.toList());

        OriginGroups originGroupsConfig = OriginGroups.builder()
            .items(originGroups)
            .quantity(originGroups.size())
            .build();

        CloudFrontViewerCertificate viewerCertificate = getViewerCertificate();
        if (viewerCertificate == null) {
            viewerCertificate = newSubresource(CloudFrontViewerCertificate.class);
            viewerCertificate.setCloudfrontDefaultCertificate(true);
        }

        CloudFrontCacheBehavior defaultCacheBehavior = getDefaultCacheBehavior();
        if (defaultCacheBehavior == null) {
            defaultCacheBehavior = newSubresource(CloudFrontCacheBehavior.class);
        }

        builder.enabled(getEnabled())
            .comment(getComment())
            .httpVersion(getHttpVersion())
            .priceClass(getPriceClass())
            .defaultRootObject(getDefaultRootObject())
            .isIPV6Enabled(getIpv6Enabled())
            .webACLId(getWebAcl() != null ? getWebAcl().getWebAclId() : "")
            .aliases(a -> a.items(getCnames()).quantity(getCnames().size()))
            .customErrorResponses(customErrorResponses)
            .defaultCacheBehavior(defaultCacheBehavior.toDefaultCacheBehavior())
            .cacheBehaviors(cacheBehaviors)
            .origins(origins)
            .originGroups(originGroupsConfig)
            .logging(getLogging() != null ? getLogging().toLoggingConfig() : CloudFrontLogging.defaultLoggingConfig())
            .viewerCertificate(viewerCertificate.toViewerCertificate())
            .callerReference(getCallerReference() != null ? getCallerReference() : Long.toString(new Date().getTime()));

        if (getGeoRestriction() != null) {
            builder.restrictions(getGeoRestriction().toRestrictions());
        } else {
            builder.restrictions(new CloudFrontGeoRestriction().toRestrictions());
        }

        return builder.build();
    }
}
