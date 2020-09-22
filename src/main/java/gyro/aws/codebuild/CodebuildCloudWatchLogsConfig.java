package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.codebuild.model.CloudWatchLogsConfig;

public class CodebuildCloudWatchLogsConfig extends Diffable implements Copyable<CloudWatchLogsConfig> {

    private String status;
    private String groupName;
    private String streamName;

    @Updatable
    @Required
    @ValidStrings({ "ENABLED", "DISABLED" })
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Updatable
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Updatable
    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    @Override
    public void copyFrom(CloudWatchLogsConfig model) {
        setStatus(model.statusAsString());
        setGroupName(model.groupName());
        setStreamName(model.streamName());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public CloudWatchLogsConfig toCloudWatchLogsConfig() {
        return CloudWatchLogsConfig.builder()
            .groupName(getGroupName())
            .status(getStatus())
            .streamName(getStreamName())
            .build();
    }
}
