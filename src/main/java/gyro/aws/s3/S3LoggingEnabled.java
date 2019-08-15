package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

import software.amazon.awssdk.services.s3.model.LoggingEnabled;

public class S3LoggingEnabled extends Diffable implements Copyable<LoggingEnabled> {
    private BucketResource bucket;
    private String prefix;

    /**
     * The target destination bucket for the logs. (Required)
     */
    @Updatable
    public BucketResource getBucket() {
        return bucket;
    }

    public void setBucket(BucketResource targetBucket) {
        this.bucket = targetBucket;
    }

    /**
     * The destination prefix on the bucket to place logs.
     */
    @Updatable
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String primaryKey() {
        return "logging enabled";
    }

    @Override
    public void copyFrom(LoggingEnabled loggingEnabled) {
        setBucket(findById(BucketResource.class, loggingEnabled.targetBucket()));
        setPrefix(loggingEnabled.targetPrefix());
    }

    LoggingEnabled toLoggingEnabled() {
        String prefix = getPrefix() == null ? "" : getPrefix();

        return LoggingEnabled.builder()
                .targetBucket(getBucket().getName())
                .targetPrefix(prefix)
                .build();
    }
}
