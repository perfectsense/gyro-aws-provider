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

package gyro.aws.sns;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.Topic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query SNS topics.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    sns: $(external-query aws::sns-topic { arn: ''})
 */
@Type("sns-topic")
public class TopicFinder extends AwsFinder<SnsClient, Topic, TopicResource> {

    private String arn;

    /**
     * The arn of the topic.
     */
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    protected List<Topic> findAws(SnsClient client, Map<String, String> filters) {
        List<Topic> topics = new ArrayList<>();

        if (filters.containsKey("arn") && !ObjectUtils.isBlank(filters.get("arn"))) {
            topics.addAll(client.listTopicsPaginator().topics().stream().filter(o -> o.topicArn().equals(filters.get("arn"))).collect(Collectors.toList()));
        }

        return topics;
    }

    @Override
    protected List<Topic> findAllAws(SnsClient client) {
        return client.listTopicsPaginator().topics().stream().collect(Collectors.toList());
    }
}
