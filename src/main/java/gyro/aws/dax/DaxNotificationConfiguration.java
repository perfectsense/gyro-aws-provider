/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.dax;

import gyro.aws.Copyable;
import gyro.aws.sns.TopicResource;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.dax.model.NotificationConfiguration;

public class DaxNotificationConfiguration extends Diffable implements Copyable<NotificationConfiguration> {

    private TopicResource topic;
    private String topicStatus;

    /**
     * The notification the topic.
     */
    @Required
    public TopicResource getTopic() {
        return topic;
    }

    public void setTopic(TopicResource topic) {
        this.topic = topic;
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
        setTopic(findById(TopicResource.class, model.topicArn()));
        setTopicStatus(model.topicStatus());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
