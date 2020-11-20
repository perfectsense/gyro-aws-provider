package gyro.aws.dax;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.dax.model.NotificationConfiguration;

public class DaxNotificationConfiguration extends Diffable implements Copyable<NotificationConfiguration> {

    private String topicArn;
    private String topicStatus;

    /**
     * The ARN of the topic.
     */
    public String getTopicArn() {
        return topicArn;
    }

    public void setTopicArn(String topicArn) {
        this.topicArn = topicArn;
    }

    /**
     * The status of the topic.
     */
    public String getTopicStatus() {
        return topicStatus;
    }

    public void setTopicStatus(String topicStatus) {
        this.topicStatus = topicStatus;
    }

    @Override
    public void copyFrom(NotificationConfiguration model) {
        setTopicArn(model.topicArn());
        setTopicStatus(model.topicStatus());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
