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

    @Updatable
    public Integer getEmitInterval() {
        return emitInterval;
    }

    public void setEmitInterval(Integer emitInterval) {
        this.emitInterval = emitInterval;
    }

    @Updatable
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Updatable
    public BucketResource getBucket() {
        return bucket;
    }

    public void setBucket(BucketResource bucket) {
        this.bucket = bucket;
    }

    @Updatable
    public String getBucketPrefix() {
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
    public String toDisplayString() {
        return "access log";
    }

    @Override
    public String primaryKey() {
        return "access log";
    }

    AccessLog toAccessLog() {
        return AccessLog.builder()
            .emitInterval(getEmitInterval())
            .enabled(getEnabled())
            .s3BucketName(getBucket().getName())
            .s3BucketPrefix(getBucketPrefix())
            .build();
    }
}
