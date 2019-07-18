package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

import software.amazon.awssdk.services.s3.model.LoggingEnabled;

public class S3LoggingEnabled extends Diffable implements Copyable<LoggingEnabled> {
    private String targetBucket;
    private String targetPrefix;

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

    @Override
    public String toDisplayString() {
        return "server access logging";
    }

    @Updatable
    public String getTargetBucket() {
        return targetBucket;
    }

    @Updatable
    public void setTargetBucket(String targetBucket) {
        this.targetBucket = targetBucket;
    }

    @Updatable
    public String getTargetPrefix() {
        return targetPrefix;
    }

    @Updatable
    public void setTargetPrefix(String targetPrefix) {
        this.targetPrefix = targetPrefix;
    }
}
