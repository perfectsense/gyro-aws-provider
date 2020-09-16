package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.codebuild.model.CloudWatchLogsConfig;
import software.amazon.awssdk.services.codebuild.model.LogsConfig;
import software.amazon.awssdk.services.codebuild.model.S3LogsConfig;

public class CodebuildLogsConfig extends Diffable implements Copyable<LogsConfig> {

    private CodebuildCloudWatchLogsConfig cloudWatchLogs;
    private CodebuildS3LogsConfig s3Logs;

    @Updatable
    public CodebuildCloudWatchLogsConfig getCloudWatchLogs() {
        return cloudWatchLogs;
    }

    public void setCloudWatchLogs(CodebuildCloudWatchLogsConfig cloudWatchLogs) {
        this.cloudWatchLogs = cloudWatchLogs;
    }

    @Updatable
    public CodebuildS3LogsConfig getS3Logs() {
        return s3Logs;
    }

    public void setS3Logs(CodebuildS3LogsConfig s3Logs) {
        this.s3Logs = s3Logs;
    }

    @Override
    public void copyFrom(LogsConfig model) {

        if (model.cloudWatchLogs() != null) {
            CodebuildCloudWatchLogsConfig cloudWatchLogsConfig = newSubresource(CodebuildCloudWatchLogsConfig.class);
            cloudWatchLogsConfig.copyFrom(model.cloudWatchLogs());
            setCloudWatchLogs(cloudWatchLogsConfig);
        }

        if (model.s3Logs() != null) {
            CodebuildS3LogsConfig s3LogsConfig = newSubresource(CodebuildS3LogsConfig.class);
            s3LogsConfig.copyFrom(model.s3Logs());
            setS3Logs(s3LogsConfig);
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public LogsConfig toProjectLogsConfig() {
        CodebuildCloudWatchLogsConfig cloudWatchLogs = getCloudWatchLogs();
        CodebuildS3LogsConfig s3Logs = getS3Logs();

        CloudWatchLogsConfig cloudWatchLogsConfig = CloudWatchLogsConfig.builder()
            .groupName(cloudWatchLogs.getGroupName())
            .status(cloudWatchLogs.getStatus())
            .streamName(cloudWatchLogs.getStreamName())
            .build();

        S3LogsConfig s3LogsConfig = S3LogsConfig.builder()
            .encryptionDisabled(s3Logs.getEncryptionDisabled())
            .location(s3Logs.getLocation())
            .status(s3Logs.getStatus())
            .build();

        return LogsConfig.builder()
            .cloudWatchLogs(cloudWatchLogsConfig)
            .s3Logs(s3LogsConfig)
            .build();
    }
}
