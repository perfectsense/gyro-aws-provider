package gyro.aws.cloudfront;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.waf.global.WebAclResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.TestValue;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.diff.Context;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.CacheBehavior;
import software.amazon.awssdk.services.cloudfront.model.CacheBehaviors;
import software.amazon.awssdk.services.cloudfront.model.CreateDistributionResponse;
import software.amazon.awssdk.services.cloudfront.model.CustomErrorResponse;
import software.amazon.awssdk.services.cloudfront.model.CustomErrorResponses;
import software.amazon.awssdk.services.cloudfront.model.Distribution;
import software.amazon.awssdk.services.cloudfront.model.DistributionConfig;
import software.amazon.awssdk.services.cloudfront.model.GetDistributionResponse;
import software.amazon.awssdk.services.cloudfront.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.cloudfront.model.NoSuchDistributionException;
import software.amazon.awssdk.services.cloudfront.model.Origin;
import software.amazon.awssdk.services.cloudfront.model.Origins;
import software.amazon.awssdk.services.cloudfront.model.Tag;
import software.amazon.awssdk.services.cloudfront.model.Tags;
import software.amazon.awssdk.services.cloudfront.model.UpdateDistributionResponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Create a CloudFront distribution.
 *
 * .. code-block:: gyro
 *
 *    aws::cloudfront cloudfront-example
 *        enabled: true
 *        ipv6-enabled: false
 *        comment: "cloudfront-example - static asset cache"
 *
 *        origin
 *            id: $(aws::s3-bucket bucket).name
 *            domain-name: "www.google.com"
 *
 *            custom-origin
 *                http-port: 80
 *            end
 *        end
 *
 *        default-cache-behavior
 *            target-origin-id: $(aws::s3-bucket bucket).name
 *            viewer-protocol-policy: "allow-all"
 *            allowed-methods: ["GET", "HEAD"]
 *            cached-methods: ["GET", "HEAD"]
 *            headers: ["Origin"]
 *        end
 *
 *        behavior
 *            path-pattern: "/dims?/*"
 *            target-origin-id: $(aws::s3-bucket bucket).name
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
 *        custom-error-response
 *            error-code: 400
 *            ttl: 0
 *        end
 *
 *        logging
 *            bucket: $(aws::s3-bucket bucket)
 *            bucket-prefix: "my-bucket/logs"
 *            include-cookies: false
 *        end
 *
 *        tags: {
 *            Name: "content cache"
 *        }
 *     end
 */
@Type("cloudfront")
public class CloudFrontResource extends AwsResource implements Copyable<Distribution> {

    private Boolean enabled;
    private String comment;
    private Set<String> cnames;
    private String httpVersion;
    private String priceClass;
    private String defaultRootObject;
    private String etag;
    private String callerReference;
    private Boolean isIpv6Enabled;
    private WebAclResource webAcl;
    private Map<String, String> tags;
    private Set<CloudFrontOrigin> origin;
    private Set<CloudFrontCacheBehavior> behavior;
    private CloudFrontCacheBehavior defaultCacheBehavior;
    private CloudFrontViewerCertificate viewerCertificate;
    private CloudFrontLogging logging;
    private Set<CloudFrontCustomErrorResponse> customErrorResponse;
    private CloudFrontGeoRestriction geoRestriction;

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
    public Set<String> getCnames() {
        if (cnames == null) {
            cnames = new HashSet<>();
        }

        return cnames;
    }

    public void setCnames(Set<String> cnames) {
        this.cnames = cnames;
    }

    /**
     * The maximum http version that users can request on this distribution. Valid values are ``HTTP1_1`` or ``HTTP2``.
     */
    @Updatable
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
     * The maximum price you want to pay for CloudFront. Valid values are ``PriceClass_All``, ``PriceClass_200`` and ``PriceClass_100``. For information on pricing see `Price classes <https://aws.amazon.com/cloudfront/pricing/#On-demand_Pricing>`_.
     */
    @Updatable
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
    public Set<CloudFrontCustomErrorResponse> getCustomErrorResponse() {
        if (customErrorResponse == null) {
            customErrorResponse = new HashSet<>();
        }

        return customErrorResponse;
    }

    public void setCustomErrorResponse(Set<CloudFrontCustomErrorResponse> customErrorResponses) {
        this.customErrorResponse = customErrorResponses;
    }

    /**
     * Restrict or allow access to this distribution by country.
     *
     * @subresource gyro.aws.cloudfront.CloudFrontGeoRestriction
     */
    @Updatable
    public CloudFrontGeoRestriction getGeoRestriction() {
        if (geoRestriction == null) {
            geoRestriction = newSubresource(CloudFrontGeoRestriction.class);
        }

        return geoRestriction;
    }

    public void setGeoRestriction(CloudFrontGeoRestriction geoRestriction) {
        this.geoRestriction = geoRestriction;
    }

    @Override
    public void copyFrom(Distribution distribution) {
        setArn(distribution.arn());
        setId(distribution.id());
        DistributionConfig config = distribution.distributionConfig();

        setEnabled(config.enabled());
        setComment(config.comment());
        setCnames(config.aliases().items().isEmpty() ? new HashSet<>() : new HashSet<>(config.aliases().items()));
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
        for (CustomErrorResponse errorResponse : config.customErrorResponses().items()) {
            CloudFrontCustomErrorResponse customErrorResponse = newSubresource(CloudFrontCustomErrorResponse.class);
            customErrorResponse.copyFrom(errorResponse);
            getCustomErrorResponse().add(customErrorResponse);
        }

        CloudFrontClient client = createClient(CloudFrontClient.class, "us-east-1", "https://cloudfront.amazonaws.com");

        ListTagsForResourceResponse tagsForResource = client.listTagsForResource(r -> r.resource(getArn()));
        getTags().clear();
        for (Tag tag: tagsForResource.tags().items()) {
            getTags().put(tag.key(), tag.value());
        }

        GetDistributionResponse response = client.getDistribution(r -> r.id(getId()));
        setEtag(response.eTag());
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
    public void create(GyroUI ui, Context context) {
        CloudFrontClient client = createClient(CloudFrontClient.class, "us-east-1", "https://cloudfront.amazonaws.com");

        CreateDistributionResponse response = client.createDistribution(c -> c.distributionConfig(distributionConfig()));
        setId(response.distribution().id());
        setArn(response.distribution().arn());
        setDomainName(response.distribution().domainName());
        setEtag(response.eTag());

        applyTags(client);
    }

    @Override
    public void update(GyroUI ui, Context context, Resource current, Set<String> changedFieldNames) {
        CloudFrontClient client = createClient(CloudFrontClient.class, "us-east-1", "https://cloudfront.amazonaws.com");

        UpdateDistributionResponse response = client.updateDistribution(r -> r.distributionConfig(distributionConfig())
            .id(getId())
            .ifMatch(getEtag()));

        setEtag(response.eTag());

        if (changedFieldNames.contains("tags")) {
            applyTags(client);
        }
    }

    @Override
    public void delete(GyroUI ui, Context context) {
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

    private void applyTags(CloudFrontClient client) {
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
            .restrictions(getGeoRestriction().toRestrictions())
            .customErrorResponses(customErrorResponses)
            .defaultCacheBehavior(defaultCacheBehavior.toDefaultCacheBehavior())
            .cacheBehaviors(cacheBehaviors)
            .origins(origins)
            .logging(getLogging() != null ? getLogging().toLoggingConfig() : CloudFrontLogging.defaultLoggingConfig())
            .viewerCertificate(viewerCertificate.toViewerCertificate())
            .callerReference(getCallerReference() != null ? getCallerReference() : Long.toString(new Date().getTime()));

        return builder.build();
    }
}
