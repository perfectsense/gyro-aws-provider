package gyro.aws.elb;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.aws.s3.BucketResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.elasticloadbalancing.model.AccessLog;

public class LoadBalancerAccessLog extends Diffable implements Copyable<AccessLog> {
    private Integer emitInterval;
    private Boolean enabled;
    private BucketResource bucket;
    private String bucketPrefix;

    /**
     * The interval for publishing the access logs. Valid values are ``5`` or ``60`` in minutes. Required if enabled set to ``true``.
     */
    @Updatable
    public Integer getEmitInterval() {
        return emitInterval;
    }

    public void setEmitInterval(Integer emitInterval) {
        this.emitInterval = emitInterval;
    }

    /**
     * If set to ``true``, the load balancer captures detailed information of all requests and delivers the information to the Amazon S3 bucket that you specify. Defaults to ``false``.
     */
    @Updatable
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
     * The Amazon S3 bucket where the access logs are stored. Required if enabled set to ``true``.
     */
    @Updatable
    public BucketResource getBucket() {
        return bucket;
    }

    public void setBucket(BucketResource bucket) {
        this.bucket = bucket;
    }

    /**
     * The folder path in the bucket where to keep the logs.
     */
    @Updatable
    public String getBucketPrefix() {
        if (!ObjectUtils.isBlank(bucketPrefix) && bucketPrefix.startsWith("/")) {
            bucketPrefix = bucketPrefix.replaceFirst("/", "");
        }

        return bucketPrefix;
    }

    public void setBucketPrefix(String bucketPrefix) {
        this.bucketPrefix = bucketPrefix;
    }

    @Override
    public void copyFrom(AccessLog accessLog) {
        setEmitInterval(accessLog.emitInterval());
        setEnabled(accessLog.enabled());
        setBucket(!ObjectUtils.isBlank(accessLog.s3BucketName()) ? findById(BucketResource.class, accessLog.s3BucketName()) : null);
        setBucketPrefix(accessLog.s3BucketPrefix());
    }

    @Override
    public String primaryKey() {
        return "access log";
    }

    AccessLog toAccessLog() {
        return AccessLog.builder()
            .emitInterval(getEmitInterval())
            .enabled(getEnabled())
            .s3BucketName(getBucket() != null ? getBucket().getName() : null)
            .s3BucketPrefix(getBucketPrefix())
            .build();
    }
}
