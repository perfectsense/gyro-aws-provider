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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.cloudfront.model.CacheBehavior;
import software.amazon.awssdk.services.cloudfront.model.DefaultCacheBehavior;
import software.amazon.awssdk.services.cloudfront.model.ForwardedValues;
import software.amazon.awssdk.services.cloudfront.model.FunctionAssociation;
import software.amazon.awssdk.services.cloudfront.model.FunctionAssociations;
import software.amazon.awssdk.services.cloudfront.model.ItemSelection;
import software.amazon.awssdk.services.cloudfront.model.LambdaFunctionAssociation;
import software.amazon.awssdk.services.cloudfront.model.LambdaFunctionAssociations;
import software.amazon.awssdk.services.cloudfront.model.TrustedSigners;

public class CloudFrontCacheBehavior extends Diffable implements Copyable<CacheBehavior> {

    private String targetOriginId;
    private String pathPattern;
    private String viewerProtocolPolicy;
    private Long minTtl;
    private Set<String> allowedMethods;
    private Set<String> cachedMethods;
    private Set<String> headers;
    private String forwardCookies;
    private Set<String> cookies;
    private Boolean smoothStreaming;
    private Long defaultTtl;
    private Long maxTtl;
    private Boolean compress;
    private Boolean queryString;
    private Set<String> queryStringCacheKeys;
    private Set<String> trustedSigners;
    private String fieldLevelEncryptionId;
    private Set<CloudFrontCacheBehaviorLambdaFunction> lambdaFunctions;
    private Set<CloudFrontCacheBehaviorFunctionAssociation> functionAssociations;
    private CachePolicyResource cachePolicy;
    private OriginRequestPolicyResource originRequestPolicy;

    /**
     * The ID for the origin to route requests to when the path pattern matches this cache behavior.
     */
    @Updatable
    public String getTargetOriginId() {
        return targetOriginId;
    }

    public void setTargetOriginId(String targetOriginId) {
        this.targetOriginId = targetOriginId;
    }

    /**
     * The URL pattern to match against this pattern. (i.e. ``/dims?/*``).
     */
    @Updatable
    public String getPathPattern() {
        return pathPattern;
    }

    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    /**
     * The protocol the user is allowed to access resources that match this cache behavior.
     */
    @Updatable
    @ValidStrings({"allow-all", "redirect-to-https", "https-only"})
    public String getViewerProtocolPolicy() {
        if (viewerProtocolPolicy == null) {
            viewerProtocolPolicy = "allow-all";
        }

        return viewerProtocolPolicy;
    }

    public void setViewerProtocolPolicy(String viewerProtocolPolicy) {
        this.viewerProtocolPolicy = viewerProtocolPolicy;
    }

    /**
     * The minimum time objects will be cached in this distribution.
     * Deprecated in favor of {@link CachePolicyResource}. For more information, See
     * `Creating cache policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/controlling-the-cache-key.html#cache-key-create-cache-policy">` or
     * `Using the managed cache policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/using-managed-cache-policies.html">`
     */
    @Updatable
    @Deprecated
    @ConflictsWith("cache-policy")
    public Long getMinTtl() {
        if (minTtl == null) {
            minTtl = 0L;
        }

        return minTtl;
    }

    public void setMinTtl(Long minTtl) {
        this.minTtl = minTtl;
    }

    /**
     * HTTP methods (i.e. ``GET``, ``POST``) that you want to forward to the origin.
     */
    @Updatable
    public Set<String> getAllowedMethods() {
        if (allowedMethods == null) {
            allowedMethods = new HashSet<>();
        }

        return allowedMethods;
    }

    public void setAllowedMethods(Set<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    /**
     * HTTP methods (i.e. ``GET``, ``POST``) that you want to cache responses from.
     */
    @Updatable
    public Set<String> getCachedMethods() {
        if (cachedMethods == null || cachedMethods.isEmpty()) {
            return getAllowedMethods();
        }

        return cachedMethods;
    }

    public void setCachedMethods(Set<String> cachedMethods) {
        this.cachedMethods = cachedMethods;
    }

    /**
     * Headers to include the cache key for an object.
     * Deprecated in favor of {@link CachePolicyResource} or {@link OriginRequestPolicyResource}
     * If you want to include values in the cache key, use a cache policy. For more information, See
     * `Creating cache policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/controlling-the-cache-key.html#cache-key-create-cache-policy">` or
     * `Using the managed cache policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/using-managed-cache-policies.html">`
     * If you want to send values to the origin but not include them in the cache key, use an origin request policy.
     * For more information, See
     * `Creating origin request policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/controlling-origin-requests.html#origin-request-create-origin-request-policy">` or
     * `Using the managed origin request policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/using-managed-origin-request-policies.html">`
     */
    @Updatable
    @Deprecated
    @ConflictsWith("cache-policy")
    public Set<String> getHeaders() {
        if (headers == null) {
            headers = new HashSet<>();
        }

        return headers;
    }

    public void setHeaders(Set<String> headers) {
        this.headers = headers;
    }

    /**
     * Whether to forward to cookies to the origin.
     * Deprecated in favor of {@link CachePolicyResource} or {@link OriginRequestPolicyResource}
     * If you want to include values in the cache key, use a cache policy. For more information, See
     * `Creating cache policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/controlling-the-cache-key.html#cache-key-create-cache-policy">` or
     * `Using the managed cache policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/using-managed-cache-policies.html">`
     * If you want to send values to the origin but not include them in the cache key, use an origin request policy.
     * For more information, See
     * `Creating origin request policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/controlling-origin-requests.html#origin-request-create-origin-request-policy">` or
     * `Using the managed origin request policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/using-managed-origin-request-policies.html">`
     */
    @Updatable
    @Deprecated
    @ConflictsWith("cache-policy")
    public String getForwardCookies() {
        if (forwardCookies != null) {
            return forwardCookies.toLowerCase();
        }

        return "none";
    }

    public void setForwardCookies(String forwardCookies) {
        this.forwardCookies = forwardCookies;
    }

    /**
     * Whitelist of cookies to include the cache key for an object.
     * Deprecated in favor of {@link CachePolicyResource} or {@link OriginRequestPolicyResource}
     * If you want to include values in the cache key, use a cache policy. For more information, See
     * `Creating cache policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/controlling-the-cache-key.html#cache-key-create-cache-policy">` or
     * `Using the managed cache policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/using-managed-cache-policies.html">`
     * If you want to send values to the origin but not include them in the cache key, use an origin request policy.
     * For more information, See
     * `Creating origin request policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/controlling-origin-requests.html#origin-request-create-origin-request-policy">` or
     * `Using the managed origin request policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/using-managed-origin-request-policies.html">`
     */
    @Updatable
    @Deprecated
    @ConflictsWith("cache-policy")
    public Set<String> getCookies() {
        if (cookies == null) {
            cookies = new HashSet<>();
        }

        return cookies;
    }

    public void setCookies(Set<String> cookies) {
        this.cookies = cookies;
    }

    /**
     * Whether you want to distribute media files in the Microsoft Smooth Streaming format.
     */
    @Updatable
    public Boolean getSmoothStreaming() {
        if (smoothStreaming == null) {
            smoothStreaming = false;
        }

        return smoothStreaming;
    }

    public void setSmoothStreaming(Boolean smoothStreaming) {
        this.smoothStreaming = smoothStreaming;
    }

    /**
     * The time objects will be cached in this distribution. Only applies when one of ``Cache-Control: max-age``, ``Cache-Control: s-maxage``, or ``Expires`` are not returned by the origin.
     * Deprecated in favor of {@link CachePolicyResource}. For more information, See
     * `Creating cache policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/controlling-the-cache-key.html#cache-key-create-cache-policy">` or
     * `Using the managed cache policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/using-managed-cache-policies.html">`
     */
    @Updatable
    @Deprecated
    @ConflictsWith("cache-policy")
    public Long getDefaultTtl() {
        if (defaultTtl == null) {
            defaultTtl = 86400L;
        }

        return defaultTtl;
    }

    public void setDefaultTtl(Long defaultTtl) {
        this.defaultTtl = defaultTtl;
    }

    /**
     * The maximum time objects will be cached in this distribution.
     * Deprecated in favor of {@link CachePolicyResource}. For more information, See
     * `Creating cache policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/controlling-the-cache-key.html#cache-key-create-cache-policy">` or
     * `Using the managed cache policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/using-managed-cache-policies.html">`
     */
    @Updatable
    @Deprecated
    @ConflictsWith("cache-policy")
    public Long getMaxTtl() {
        if (maxTtl == null) {
            maxTtl = 31536000L;
        }

        return maxTtl;
    }

    public void setMaxTtl(Long maxTtl) {
        this.maxTtl = maxTtl;
    }

    /**
     * Whether to compress files from origin.
     */
    @Updatable
    public Boolean getCompress() {
        if (compress == null) {
            compress = false;
        }

        return compress;
    }

    public void setCompress(Boolean compress) {
        this.compress = compress;
    }

    /**
     * Whether to forward query strings to origin. If true, query string parameters become part of the cache key.
     * Deprecated in favor of {@link CachePolicyResource} or {@link OriginRequestPolicyResource}
     * If you want to include values in the cache key, use a cache policy. For more information, See
     * `Creating cache policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/controlling-the-cache-key.html#cache-key-create-cache-policy">` or
     * `Using the managed cache policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/using-managed-cache-policies.html">`
     * If you want to send values to the origin but not include them in the cache key, use an origin request policy.
     * For more information, See
     * `Creating origin request policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/controlling-origin-requests.html#origin-request-create-origin-request-policy">` or
     * `Using the managed origin request policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/using-managed-origin-request-policies.html">`
     */
    @Updatable
    @Deprecated
    @ConflictsWith("cache-policy")
    public Boolean getQueryString() {
        if (queryString == null) {
            queryString = false;
        }

        return queryString;
    }

    public void setQueryString(Boolean queryString) {
        this.queryString = queryString;
    }

    /**
     * Query string parameters that should be used in the cache key.
     * Deprecated in favor of {@link CachePolicyResource} or {@link OriginRequestPolicyResource}
     * If you want to include values in the cache key, use a cache policy. For more information, See
     * `Creating cache policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/controlling-the-cache-key.html#cache-key-create-cache-policy">` or
     * `Using the managed cache policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/using-managed-cache-policies.html">`
     * If you want to send values to the origin but not include them in the cache key, use an origin request policy.
     * For more information, See
     * `Creating origin request policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/controlling-origin-requests.html#origin-request-create-origin-request-policy">` or
     * `Using the managed origin request policies <"https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/using-managed-origin-request-policies.html">`
     */
    @Updatable
    @Deprecated
    @ConflictsWith("cache-policy")
    public Set<String> getQueryStringCacheKeys() {
        if (queryStringCacheKeys == null) {
            queryStringCacheKeys = new HashSet<>();
        }

        return queryStringCacheKeys;
    }

    public void setQueryStringCacheKeys(Set<String> queryStringCacheKeys) {
        this.queryStringCacheKeys = queryStringCacheKeys;
    }

    /**
     * A list of AWS account numbers that are allowed to generate signed URLs for private content.
     */
    @Updatable
    public Set<String> getTrustedSigners() {
        if (trustedSigners == null) {
            trustedSigners = new HashSet<>();
        }

        return trustedSigners;
    }

    public void setTrustedSigners(Set<String> trustedSigners) {
        this.trustedSigners = trustedSigners;
    }

    /**
     * The Field Level Encryption configuration ID.
     */
    @Updatable
    public String getFieldLevelEncryptionId() {
        if (fieldLevelEncryptionId == null) {
            return "";
        }

        return fieldLevelEncryptionId;
    }

    public void setFieldLevelEncryptionId(String fieldLevelEncryptionId) {
        this.fieldLevelEncryptionId = fieldLevelEncryptionId;
    }

    /**
     * The lambda functions associated with this cache behavior.
     *
     * @subresource gyro.aws.cloudfront.CloudFrontCacheBehaviorLambdaFunction
     */
    @Updatable
    public Set<CloudFrontCacheBehaviorLambdaFunction> getLambdaFunctions() {
        if (lambdaFunctions == null) {
            lambdaFunctions = new HashSet<>();
        }
        return lambdaFunctions;
    }

    public void setLambdaFunctions(Set<CloudFrontCacheBehaviorLambdaFunction> lambdaFunctions) {
        this.lambdaFunctions = lambdaFunctions;
    }

    /**
     * The cloudfront function associations with this cache behavior.
     *
     * @subresource gyro.aws.cloudfront.CloudFrontCacheBehaviorFunctionAssociation
     */
    @Updatable
    public Set<CloudFrontCacheBehaviorFunctionAssociation> getFunctionAssociations() {
        if (functionAssociations == null) {
            functionAssociations = new HashSet<>();
        }

        return functionAssociations;
    }

    public void setFunctionAssociations(Set<CloudFrontCacheBehaviorFunctionAssociation> functionAssociations) {
        this.functionAssociations = functionAssociations;
    }

    /**
     * The cache policy that is attached to this cache behavior.
     */
    @Updatable
    public CachePolicyResource getCachePolicy() {
        return cachePolicy;
    }

    public void setCachePolicy(CachePolicyResource cachePolicy) {
        this.cachePolicy = cachePolicy;
    }

    /**
     * The origin request policy that is attached to this cache behavior.
     */
    @Updatable
    public OriginRequestPolicyResource getOriginRequestPolicy() {
        return originRequestPolicy;
    }

    public void setOriginRequestPolicy(OriginRequestPolicyResource originRequestPolicy) {
        this.originRequestPolicy = originRequestPolicy;
    }

    @Override
    public void copyFrom(CacheBehavior cacheBehavior) {
        setTargetOriginId(cacheBehavior.targetOriginId());
        setPathPattern(cacheBehavior.pathPattern());
        setViewerProtocolPolicy(cacheBehavior.viewerProtocolPolicyAsString());
        setTrustedSigners(new HashSet<>(cacheBehavior.trustedSigners().items()));

        // -- TTLs
        setDefaultTtl(cacheBehavior.defaultTTL());
        setMinTtl(cacheBehavior.minTTL());
        setMaxTtl(cacheBehavior.maxTTL());

        // -- Forwarded Values
        if (cacheBehavior.forwardedValues() != null) {
            setForwardCookies(cacheBehavior.forwardedValues().cookies().forwardAsString());
            if (cacheBehavior.forwardedValues().cookies().forward().equals(ItemSelection.WHITELIST)) {
                setCookies(new HashSet<>(cacheBehavior.forwardedValues().cookies().whitelistedNames().items()));
            } else {
                setCookies(new HashSet<>());
            }

            setHeaders(new HashSet<>(cacheBehavior.forwardedValues().headers().items()));
            setQueryString(cacheBehavior.forwardedValues().queryString());
            setQueryStringCacheKeys(new HashSet<>(cacheBehavior.forwardedValues().queryStringCacheKeys().items()));
        } else {
            setCookies(new HashSet<>());
        }

        setAllowedMethods(new HashSet<>(cacheBehavior.allowedMethods().itemsAsStrings()));
        setCachedMethods(new HashSet<>(cacheBehavior.allowedMethods().cachedMethods().itemsAsStrings()));
        setCompress(cacheBehavior.compress());
        setFieldLevelEncryptionId(cacheBehavior.fieldLevelEncryptionId());
        setSmoothStreaming(cacheBehavior.smoothStreaming());

        getLambdaFunctions().clear();
        if (cacheBehavior.lambdaFunctionAssociations() != null &&
            !cacheBehavior.lambdaFunctionAssociations().items().isEmpty()) {
            for (LambdaFunctionAssociation lambdaFunctionAssociation : cacheBehavior.lambdaFunctionAssociations()
                .items()) {
                CloudFrontCacheBehaviorLambdaFunction cloudFrontCacheBehaviorLambdaFunction =
                    newSubresource(CloudFrontCacheBehaviorLambdaFunction.class);
                cloudFrontCacheBehaviorLambdaFunction.copyFrom(lambdaFunctionAssociation);
                getLambdaFunctions().add(cloudFrontCacheBehaviorLambdaFunction);
            }
        }

        getFunctionAssociations().clear();
        if (cacheBehavior.functionAssociations() != null && !cacheBehavior.functionAssociations().items().isEmpty()) {
            for (FunctionAssociation functionAssociation : cacheBehavior.functionAssociations().items()) {
                CloudFrontCacheBehaviorFunctionAssociation cloudFrontCacheBehaviorFunctionAssociation =
                    newSubresource(CloudFrontCacheBehaviorFunctionAssociation.class);
                cloudFrontCacheBehaviorFunctionAssociation.copyFrom(functionAssociation);
                getFunctionAssociations().add(cloudFrontCacheBehaviorFunctionAssociation);
            }
        }

        setCachePolicy(findById(CachePolicyResource.class, cacheBehavior.cachePolicyId()));
        setOriginRequestPolicy(findById(OriginRequestPolicyResource.class, cacheBehavior.originRequestPolicyId()));
    }

    @Override
    public String primaryKey() {
        return getPathPattern();
    }

    static CacheBehavior getCacheBehaviorFromDefault(DefaultCacheBehavior defaultCacheBehavior) {
        return CacheBehavior.builder().targetOriginId(defaultCacheBehavior.targetOriginId())
            .pathPattern("*")
            .viewerProtocolPolicy(defaultCacheBehavior.viewerProtocolPolicy())
            .trustedSigners(defaultCacheBehavior.trustedSigners())
            .defaultTTL(defaultCacheBehavior.defaultTTL())
            .minTTL(defaultCacheBehavior.minTTL())
            .maxTTL(defaultCacheBehavior.maxTTL())
            .forwardedValues(defaultCacheBehavior.forwardedValues())
            .allowedMethods(defaultCacheBehavior.allowedMethods())
            .compress(defaultCacheBehavior.compress())
            .fieldLevelEncryptionId(defaultCacheBehavior.fieldLevelEncryptionId())
            .smoothStreaming(defaultCacheBehavior.smoothStreaming())
            .lambdaFunctionAssociations(defaultCacheBehavior.lambdaFunctionAssociations())
            .functionAssociations(defaultCacheBehavior.functionAssociations())
            .cachePolicyId(defaultCacheBehavior.cachePolicyId())
            .originRequestPolicyId(defaultCacheBehavior.originRequestPolicyId())
            .build();
    }

    DefaultCacheBehavior toDefaultCacheBehavior() {
        TrustedSigners trustedSigners = TrustedSigners.builder()
            .items(getTrustedSigners())
            .quantity(getTrustedSigners().size())
            .enabled(!getTrustedSigners().isEmpty())
            .build();

        LambdaFunctionAssociations lambdaFunctionAssociations = LambdaFunctionAssociations.builder()
            .items(getLambdaFunctions().stream().map(l -> l.toLambdaFunctionAssociation()).collect(Collectors.toList()))
            .quantity(getLambdaFunctions().size())
            .build();

        FunctionAssociations functionAssociations = FunctionAssociations.builder()
            .items(getFunctionAssociations().stream().map(f -> f.toFunctionAssociation()).collect(Collectors.toList()))
            .quantity(getFunctionAssociations().size())
            .build();

        DefaultCacheBehavior.Builder builder = DefaultCacheBehavior.builder()
            .allowedMethods(am -> am.itemsWithStrings(getAllowedMethods())
                .quantity(getAllowedMethods().size())
                .cachedMethods(cm -> cm.itemsWithStrings(getCachedMethods()).quantity(getCachedMethods().size()))
            )
            .smoothStreaming(getSmoothStreaming())
            .targetOriginId(getTargetOriginId())
            .trustedSigners(trustedSigners)
            .lambdaFunctionAssociations(lambdaFunctionAssociations)
            .functionAssociations(functionAssociations)
            .viewerProtocolPolicy(getViewerProtocolPolicy())
            .fieldLevelEncryptionId(getFieldLevelEncryptionId())
            .compress(getCompress())
            .cachePolicyId(getCachePolicy() != null ? getCachePolicy().getId() : null)
            .originRequestPolicyId(getOriginRequestPolicy() != null ? getOriginRequestPolicy().getId() : null);

        if (getCachePolicy() == null) {
            ForwardedValues forwardedValues = ForwardedValues.builder()
                .headers(h -> h.items(getHeaders()).quantity(getHeaders().size()))
                .cookies(c -> c.forward(getForwardCookies())
                    .whitelistedNames(w -> w.items(getCookies()).quantity(getCookies().size())))
                .queryString(getQueryString())
                .queryStringCacheKeys(
                    q -> q.items(getQueryStringCacheKeys()).quantity(getQueryStringCacheKeys().size()))
                .build();
            builder.forwardedValues(forwardedValues)
                .defaultTTL(getDefaultTtl())
                .maxTTL(getMaxTtl())
                .minTTL(getMinTtl());
        }

        return builder.build();
    }

    CacheBehavior toCachBehavior() {

        TrustedSigners trustedSigners = TrustedSigners.builder()
            .items(getTrustedSigners())
            .quantity(getTrustedSigners().size())
            .enabled(!getTrustedSigners().isEmpty())
            .build();

        LambdaFunctionAssociations lambdaFunctionAssociations = LambdaFunctionAssociations.builder()
            .items(getLambdaFunctions().stream().map(l -> l.toLambdaFunctionAssociation()).collect(Collectors.toList()))
            .quantity(getLambdaFunctions().size())
            .build();

        FunctionAssociations functionAssociations = FunctionAssociations.builder()
            .items(getFunctionAssociations().stream().map(f -> f.toFunctionAssociation()).collect(Collectors.toList()))
            .quantity(getFunctionAssociations().size())
            .build();

        CacheBehavior.Builder builder = CacheBehavior.builder()
            .allowedMethods(am -> am.itemsWithStrings(getAllowedMethods())
                .quantity(getAllowedMethods().size())
                .cachedMethods(cm -> cm.itemsWithStrings(getCachedMethods()).quantity(getCachedMethods().size()))
            )
            .smoothStreaming(getSmoothStreaming())
            .targetOriginId(getTargetOriginId())
            .pathPattern(getPathPattern())
            .trustedSigners(trustedSigners)
            .lambdaFunctionAssociations(lambdaFunctionAssociations)
            .functionAssociations(functionAssociations)
            .viewerProtocolPolicy(getViewerProtocolPolicy())
            .fieldLevelEncryptionId(getFieldLevelEncryptionId())
            .compress(getCompress())
            .cachePolicyId(getCachePolicy() != null ? getCachePolicy().getId() : null)
            .originRequestPolicyId(getOriginRequestPolicy() != null ? getOriginRequestPolicy().getId() : null);

        if (getCachePolicy() == null) {
            ForwardedValues forwardedValues = ForwardedValues.builder()
                .headers(h -> h.items(getHeaders()).quantity(getHeaders().size()))
                .cookies(c -> c.forward(getForwardCookies())
                    .whitelistedNames(w -> w.items(getCookies()).quantity(getCookies().size())))
                .queryString(getQueryString())
                .queryStringCacheKeys(
                    q -> q.items(getQueryStringCacheKeys()).quantity(getQueryStringCacheKeys().size()))
                .build();
            builder.forwardedValues(forwardedValues)
                .defaultTTL(getDefaultTtl())
                .maxTTL(getMaxTtl())
                .minTTL(getMinTtl());
        }

        return builder.build();
    }
}
