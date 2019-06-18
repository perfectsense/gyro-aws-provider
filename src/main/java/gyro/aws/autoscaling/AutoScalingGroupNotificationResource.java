package gyro.aws.autoscaling;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.sns.TopicResource;
import gyro.core.GyroException;
import gyro.core.resource.Updatable;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.NotificationConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AutoScalingGroupNotificationResource extends AwsResource implements Copyable<NotificationConfiguration> {

    private TopicResource topic;
    private String autoScalingGroupName;
    private List<String> notificationTypes;

    /**
     * The SNS topic to notify. (Required)
     */
    public TopicResource getTopic() {
        return topic;
    }

    public void setTopic(TopicResource topic) {
        this.topic = topic;
    }

    /**
     * The name of the parent auto scaling group. (Auto populated)
     */
    public String getAutoScalingGroupName() {
        return autoScalingGroupName;
    }

    public void setAutoScalingGroupName(String autoScalingGroupName) {
        this.autoScalingGroupName = autoScalingGroupName;
    }

    /**
     * The event on which to notify. Valid values [ 'autoscaling:EC2_INSTANCE_LAUNCH', 'autoscaling:EC2_INSTANCE_LAUNCH_ERROR'
     * 'autoscaling:EC2_INSTANCE_TERMINATE', 'autoscaling:EC2_INSTANCE_TERMINATE_ERROR' ].
     */
    @Updatable
    public List<String> getNotificationTypes() {
        if (notificationTypes == null) {
            notificationTypes = new ArrayList<>();
        }

        return notificationTypes;
    }

    public void setNotificationTypes(List<String> notificationTypes) {
        this.notificationTypes = notificationTypes;
    }

    @Override
    public void copyFrom(NotificationConfiguration notificationConfiguration) {
        setAutoScalingGroupName(notificationConfiguration.autoScalingGroupName());
        setTopic(!ObjectUtils.isBlank(notificationConfiguration.topicARN()) ? findById(TopicResource.class, notificationConfiguration.topicARN()) : null);
        setNotificationTypes(Collections.singletonList(notificationConfiguration.notificationType()));
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        validate();
        setAutoScalingGroupName(getParentId());
        saveNotification(client);
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        validate();
        saveNotification(client);
    }

    @Override
    public void delete() {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        client.deleteNotificationConfiguration(
            r -> r.autoScalingGroupName(getAutoScalingGroupName())
            .topicARN(getTopic().getTopicArn())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("auto scale notification");

        if (getTopic() != null && !ObjectUtils.isBlank(getTopic().getTopicArn())) {
            sb.append(" - ").append(getTopic().getTopicArn());
        }

        return sb.toString();
    }

    @Override
    public String primaryKey() {
        return getTopic().getTopicArn();
    }

    private String getParentId() {
        AutoScalingGroupResource parent = (AutoScalingGroupResource) parentResource();
        if (parent == null) {
            throw new GyroException("Parent Auto Scale Group resource not found.");
        }
        return parent.getAutoScalingGroupName();
    }

    private void saveNotification(AutoScalingClient client) {
        client.putNotificationConfiguration(
            r -> r.autoScalingGroupName(getAutoScalingGroupName())
                .notificationTypes(getNotificationTypes())
                .topicARN(getTopic().getTopicArn())
        );
    }

    private void validate() {
        HashSet<String> validNotificationSet = new HashSet<>(
            Arrays.asList(
                "autoscaling:EC2_INSTANCE_LAUNCH",
                "autoscaling:EC2_INSTANCE_LAUNCH_ERROR",
                "autoscaling:EC2_INSTANCE_TERMINATE",
                "autoscaling:EC2_INSTANCE_TERMINATE_ERROR"
            )
        );

        if (getNotificationTypes().isEmpty() || getNotificationTypes().size() > 1 || !validNotificationSet.contains(getNotificationTypes().get(0))) {
            throw new GyroException("The param 'notification-types' needs one value."
                + " Valid values [ '" + String.join("', '", validNotificationSet) + "' ].");
        }
    }
}
