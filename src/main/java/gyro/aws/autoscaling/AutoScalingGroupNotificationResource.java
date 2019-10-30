/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.autoscaling;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.sns.TopicResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Updatable;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.scope.State;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.NotificationConfiguration;

import java.util.Set;

public class AutoScalingGroupNotificationResource extends AwsResource implements Copyable<NotificationConfiguration> {

    private TopicResource topic;
    private String notificationType;

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
     * The event on which to notify. Valid values are ``autoscaling:EC2_INSTANCE_LAUNCH`` or ``autoscaling:EC2_INSTANCE_LAUNCH_ERROR`` or ``autoscaling:EC2_INSTANCE_TERMINATE`` or ``autoscaling:EC2_INSTANCE_TERMINATE_ERROR``. (Required)
     */
    @Updatable
    @ValidStrings({
        "autoscaling:EC2_INSTANCE_LAUNCH",
        "autoscaling:EC2_INSTANCE_LAUNCH_ERROR",
        "autoscaling:EC2_INSTANCE_TERMINATE",
        "autoscaling:EC2_INSTANCE_TERMINATE_ERROR"
    })
    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    @Override
    public void copyFrom(NotificationConfiguration notificationConfiguration) {
        setTopic(!ObjectUtils.isBlank(notificationConfiguration.topicARN()) ? findById(TopicResource.class, notificationConfiguration.topicARN()) : null);
        setNotificationType(notificationConfiguration.notificationType());
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, State state) {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        validate();
        saveNotification(client);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        validate();
        saveNotification(client);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        client.deleteNotificationConfiguration(
            r -> r.autoScalingGroupName(getParentId())
            .topicARN(getTopic().getArn())
        );
    }

    @Override
    public String primaryKey() {
        return getTopic().getArn();
    }

    private String getParentId() {
        AutoScalingGroupResource parent = (AutoScalingGroupResource) parentResource();
        if (parent == null) {
            throw new GyroException("Parent Auto Scale Group resource not found.");
        }
        return parent.getName();
    }

    private void saveNotification(AutoScalingClient client) {
        client.putNotificationConfiguration(
            r -> r.autoScalingGroupName(getParentId())
                .notificationTypes(getNotificationType())
                .topicARN(getTopic().getArn())
        );
    }

}
