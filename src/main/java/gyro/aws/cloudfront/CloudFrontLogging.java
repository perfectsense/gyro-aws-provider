package gyro.aws.cloudfront;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.aws.s3.BucketResource;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.cloudfront.model.LoggingConfig;

public class CloudFrontLogging extends Diffable implements Copyable<LoggingConfig> {

    private Boolean enabled;
    private BucketResource bucket;
    private String bucketPrefix;
    private Boolean includeCookies;

    /**
     * Enable or disable logging for this distribution.
     */
    public Boolean getEnabled() {
        if (enabled == null) {
            enabled = false;
        }

        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * The bucket to save access logs.
     */
    public BucketResource getBucket() {
        return bucket;
    }

    public void setBucket(BucketResource bucket) {
        this.bucket = bucket;
    }

    /**
     * Directory within bucket ot save access logs.
     */
    public String getBucketPrefix() {
        if (bucketPrefix == null) {
            bucketPrefix = "";
        }

        if (bucketPrefix.startsWith("/")) {
            bucketPrefix = bucketPrefix.replaceFirst("/", "");
        }

        return bucketPrefix;
    }

    public void setBucketPrefix(String bucketPrefix) {
        this.bucketPrefix = bucketPrefix;
    }

    /**
     * Whether to include cookies logs.
     */
    public Boolean getIncludeCookies() {
        if (includeCookies == null) {
            includeCookies = false;
        }

        return includeCookies;
    }

    public void setIncludeCookies(Boolean includeCookies) {
        this.includeCookies = includeCookies;
    }

    @Override
    public void copyFrom(LoggingConfig loggingConfig) {
        setBucket(!ObjectUtils.isBlank(loggingConfig.bucket()) ? findById(BucketResource.class, loggingConfig.bucket()) : null);
        setBucketPrefix(loggingConfig.prefix());
        setEnabled(loggingConfig.enabled());
        setIncludeCookies(loggingConfig.includeCookies());
    }

    @Override
    public String primaryKey() {
        return "logging";
    }

    @Override
    public String toDisplayString() {
        return String.format("logging config - bucket: %s, prefix: %s", getBucket(), getBucketPrefix());
    }

    LoggingConfig toLoggingConfig() {
        return LoggingConfig.builder()
            .bucket(getBucket() != null ? getBucket().getDomainName() : "")
            .prefix(getBucketPrefix())
            .includeCookies(getIncludeCookies())
            .enabled(getEnabled()).build();
    }
}
