package gyro.aws.cloudfront;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.cloudfront.model.CacheBehavior;
import software.amazon.awssdk.services.cloudfront.model.DefaultCacheBehavior;
import software.amazon.awssdk.services.cloudfront.model.ForwardedValues;
import software.amazon.awssdk.services.cloudfront.model.LambdaFunctionAssociation;
import software.amazon.awssdk.services.cloudfront.model.LambdaFunctionAssociations;
import software.amazon.awssdk.services.cloudfront.model.TrustedSigners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CloudFrontCacheBehavior extends Diffable implements Copyable<CacheBehavior> {

    private String targetOriginId;
    private String pathPattern;
    private String viewerProtocolPolicy;
    private Long minTtl;
    private List<String> allowedMethods;
    private List<String> cachedMethods;
    private List<String> headers;
    private String forwardCookies;
    private List<String> cookies;
    private boolean smoothStreaming;
    private Long defaultTtl;
    private Long maxTtl;
    private boolean compress;
    private boolean queryString;
    private List<String> queryStringCacheKeys;
    private List<String> trustedSigners;
    private String fieldLevelEncryptionId;
    private List<CloudFrontCacheBehaviorLambdaFunction> lambdaFunctions;

    /**
     * The ID for the origin to route requests to when the path pattern matches this cache behavior.
     */
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
    public String getViewerProtocolPolicy() {
        return viewerProtocolPolicy;
    }

    public void setViewerProtocolPolicy(String viewerProtocolPolicy) {
        this.viewerProtocolPolicy = viewerProtocolPolicy;
    }

    /**
     * The minimum time objects will be cached in this distribution.
     */
    @Updatable
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
    public List<String> getAllowedMethods() {
        if (allowedMethods == null) {
            allowedMethods = new ArrayList<>();
        }

        List<String> sorted = new ArrayList<>(allowedMethods);
        Collections.sort(sorted);

        return sorted;
    }

    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    /**
     * HTTP methods (i.e. ``GET``, ``POST``) that you want to cache responses from.
     */
    @Updatable
    public List<String> getCachedMethods() {
        if (cachedMethods == null || cachedMethods.isEmpty()) {
            return getAllowedMethods();
        }

        List<String> sorted = new ArrayList<>(cachedMethods);
        Collections.sort(sorted);

        return sorted;
    }

    public void setCachedMethods(List<String> cachedMethods) {
        this.cachedMethods = cachedMethods;
    }

    /**
     * Headers to include the cache key for an object.
     */
    @Updatable
    public List<String> getHeaders() {
        if (headers == null) {
            headers = new ArrayList<>();
        }

        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    /**
     * Whether to forward to cookies to the origin.
     */
    @Updatable
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
     */
    @Updatable
    public List<String> getCookies() {
        if (cookies == null) {
            cookies = new ArrayList<>();
        }

        return cookies;
    }

    public void setCookies(List<String> cookies) {
        this.cookies = cookies;
    }

    /**
     * Whether you want to distribute media files in the Microsoft Smooth Streaming format.
     */
    @Updatable
    public boolean getSmoothStreaming() {
        return smoothStreaming;
    }

    public void setSmoothStreaming(boolean smoothStreaming) {
        this.smoothStreaming = smoothStreaming;
    }

    /**
     * The time objects will be cached in this distribution. Only applies when one of ``Cache-Control: max-age``, ``Cache-Control: s-maxage``, or ``Expires`` are not returned by the origin.
     */
    @Updatable
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
     */
    @Updatable
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
    public boolean getCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    /**
     * Whether to forward query strings to origin. If true, query string parameters become part of the cache key.
     */
    @Updatable
    public boolean getQueryString() {
        return queryString;
    }

    public void setQueryString(boolean queryString) {
        this.queryString = queryString;
    }

    /**
     * Query string parameters that should be used in the cache key.
     */
    @Updatable
    public List<String> getQueryStringCacheKeys() {
        if (queryStringCacheKeys == null) {
            queryStringCacheKeys = new ArrayList<>();
        }

        return queryStringCacheKeys;
    }

    public void setQueryStringCacheKeys(List<String> queryStringCacheKeys) {
        this.queryStringCacheKeys = queryStringCacheKeys;
    }

    /**
     * A list of AWS account numbers that are allowed to generate signed URLs for private content.
     */
    @Updatable
    public List<String> getTrustedSigners() {
        if (trustedSigners == null) {
            trustedSigners = new ArrayList<>();
        }

        return trustedSigners;
    }

    public void setTrustedSigners(List<String> trustedSigners) {
        this.trustedSigners = trustedSigners;
    }

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

    @Updatable
    public List<CloudFrontCacheBehaviorLambdaFunction> getLambdaFunctions() {
        if (lambdaFunctions == null) {
            lambdaFunctions = new ArrayList<>();
        }
        return lambdaFunctions;
    }

    public void setLambdaFunctions(List<CloudFrontCacheBehaviorLambdaFunction> lambdaFunctions) {
        this.lambdaFunctions = lambdaFunctions;
    }

    public DefaultCacheBehavior toDefaultCacheBehavior() {
        ForwardedValues forwardedValues = ForwardedValues.builder()
            .headers(h -> h.items(getHeaders()).quantity(getHeaders().size()))
            .cookies(c -> c.forward(getForwardCookies()).whitelistedNames(w -> w.items(getCookies()).quantity(getCookies().size())))
            .queryString(getQueryString())
            .queryStringCacheKeys(q -> q.items(getQueryStringCacheKeys()).quantity(getQueryStringCacheKeys().size()))
            .build();

        TrustedSigners trustedSigners = TrustedSigners.builder()
            .items(getTrustedSigners())
            .quantity(getTrustedSigners().size())
            .enabled(!getTrustedSigners().isEmpty())
            .build();

        LambdaFunctionAssociations lambdaFunctionAssociations = LambdaFunctionAssociations.builder()
            .items(getLambdaFunctions().stream().map(l -> l.toLambdaFunctionAssociation()).collect(Collectors.toList()))
            .quantity(getLambdaFunctions().size())
            .build();

        return DefaultCacheBehavior.builder()
            .allowedMethods(am -> am.itemsWithStrings(getAllowedMethods())
                .quantity(getAllowedMethods().size())
                .cachedMethods(cm -> cm.itemsWithStrings(getCachedMethods()).quantity(getCachedMethods().size()))
            )
            .defaultTTL(getDefaultTtl())
            .maxTTL(getMaxTtl())
            .minTTL(getMinTtl())
            .smoothStreaming(getSmoothStreaming())
            .targetOriginId(getTargetOriginId())
            .forwardedValues(forwardedValues)
            .trustedSigners(trustedSigners)
            .lambdaFunctionAssociations(lambdaFunctionAssociations)
            .viewerProtocolPolicy(getViewerProtocolPolicy())
            .fieldLevelEncryptionId(getFieldLevelEncryptionId())
            .compress(getCompress())
            .build();
    }

    public CacheBehavior toCachBehavior() {
        ForwardedValues forwardedValues = ForwardedValues.builder()
            .headers(h -> h.items(getHeaders()).quantity(getHeaders().size()))
            .cookies(c -> c.forward(getForwardCookies()).whitelistedNames(w -> w.items(getCookies()).quantity(getCookies().size())))
            .queryString(getQueryString())
            .queryStringCacheKeys(q -> q.items(getQueryStringCacheKeys()).quantity(getQueryStringCacheKeys().size()))
            .build();

        TrustedSigners trustedSigners = TrustedSigners.builder()
            .items(getTrustedSigners())
            .quantity(getTrustedSigners().size())
            .enabled(!getTrustedSigners().isEmpty())
            .build();

        LambdaFunctionAssociations lambdaFunctionAssociations = LambdaFunctionAssociations.builder()
            .items(getLambdaFunctions().stream().map(l -> l.toLambdaFunctionAssociation()).collect(Collectors.toList()))
            .quantity(getLambdaFunctions().size())
            .build();

        return CacheBehavior.builder()
            .allowedMethods(am -> am.itemsWithStrings(getAllowedMethods())
                .quantity(getAllowedMethods().size())
                .cachedMethods(cm -> cm.itemsWithStrings(getCachedMethods()).quantity(getCachedMethods().size()))
            )
            .defaultTTL(getDefaultTtl())
            .maxTTL(getMaxTtl())
            .minTTL(getMinTtl())
            .smoothStreaming(getSmoothStreaming())
            .targetOriginId(getTargetOriginId())
            .pathPattern(getPathPattern())
            .forwardedValues(forwardedValues)
            .trustedSigners(trustedSigners)
            .lambdaFunctionAssociations(lambdaFunctionAssociations)
            .viewerProtocolPolicy(getViewerProtocolPolicy())
            .fieldLevelEncryptionId(getFieldLevelEncryptionId())
            .compress(getCompress())
            .build();
    }

    @Override
    public String primaryKey() {
        return getPathPattern();
    }

    @Override
    public String toDisplayString() {
        if (getPathPattern() != null && getPathPattern().equals("*")) {
            return "default cache behavior";
        }

        return "cache behavior";
    }

    @Override
    public void copyFrom(CacheBehavior cacheBehavior) {
        setTargetOriginId(cacheBehavior.targetOriginId());
        setPathPattern(cacheBehavior.pathPattern());
        setViewerProtocolPolicy(cacheBehavior.viewerProtocolPolicyAsString());
        setTrustedSigners(new ArrayList<>(cacheBehavior.trustedSigners().items()));

        // -- TTLs
        setDefaultTtl(cacheBehavior.defaultTTL());
        setMinTtl(cacheBehavior.minTTL());
        setMaxTtl(cacheBehavior.maxTTL());

        // -- Forwarded Values
        setForwardCookies(cacheBehavior.forwardedValues().cookies().forwardAsString());
        if (!getForwardCookies().equals("none")) {
            setCookies(new ArrayList<>(cacheBehavior.forwardedValues().cookies().whitelistedNames().items()));
        }
        setHeaders(new ArrayList<>(cacheBehavior.forwardedValues().headers().items()));
        setQueryString(cacheBehavior.forwardedValues().queryString());
        setQueryStringCacheKeys(new ArrayList<>(cacheBehavior.forwardedValues().queryStringCacheKeys().items()));

        setAllowedMethods(cacheBehavior.allowedMethods().itemsAsStrings());
        setCachedMethods(cacheBehavior.allowedMethods().cachedMethods().itemsAsStrings());
        setCompress(cacheBehavior.compress());
        setFieldLevelEncryptionId(cacheBehavior.fieldLevelEncryptionId());
        setSmoothStreaming(cacheBehavior.smoothStreaming());

        getLambdaFunctions().clear();
        if (cacheBehavior.lambdaFunctionAssociations() != null && !cacheBehavior.lambdaFunctionAssociations().items().isEmpty()) {
            for (LambdaFunctionAssociation lambdaFunctionAssociation : cacheBehavior.lambdaFunctionAssociations().items()) {
                CloudFrontCacheBehaviorLambdaFunction cloudFrontCacheBehaviorLambdaFunction = newSubresource(CloudFrontCacheBehaviorLambdaFunction.class);
                cloudFrontCacheBehaviorLambdaFunction.copyFrom(lambdaFunctionAssociation);
                getLambdaFunctions().add(cloudFrontCacheBehaviorLambdaFunction);
            }
        }
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
            .lambdaFunctionAssociations(defaultCacheBehavior.lambdaFunctionAssociations()).build();
    }
}
