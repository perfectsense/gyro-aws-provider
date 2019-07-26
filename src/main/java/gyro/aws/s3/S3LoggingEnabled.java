package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

import software.amazon.awssdk.services.s3.model.LoggingEnabled;

public class S3LoggingEnabled extends Diffable implements Copyable<LoggingEnabled> {
    private String targetBucket;
    private String targetPrefix;

    /**
     * The target destination bucket for the logs. (Required)
     */
    @Updatable
    public String getTargetBucket() {
        return targetBucket;
    }

    public void setTargetBucket(String targetBucket) {
        this.targetBucket = targetBucket;
    }

    /**
     * The destination prefix on the bucket to place logs.
     */
    @Updatable
    public String getTargetPrefix() {
        return targetPrefix;
    }

    public void setTargetPrefix(String targetPrefix) {
        this.targetPrefix = targetPrefix;
    }

    @Override
    public void copyFrom(LoggingEnabled loggingEnabled) {
        setTargetBucket(loggingEnabled.targetBucket());
        setTargetPrefix(loggingEnabled.targetPrefix());
    }

    LoggingEnabled toLoggingEnabled(){
        return LoggingEnabled.builder()
                .targetBucket(getTargetBucket())
                .targetPrefix(getTargetPrefix())
                .build();
    }
}
