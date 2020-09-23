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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.GetTopicAttributesResponse;
import software.amazon.awssdk.services.sns.model.Tag;
import software.amazon.awssdk.services.sns.model.TagResourceRequest;
import software.amazon.awssdk.services.sns.model.Topic;
import software.amazon.awssdk.services.sns.model.UntagResourceRequest;
import software.amazon.awssdk.utils.IoUtils;

/**
 * Creates a SNS topic.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::sns-topic sns-topic-example
 *         name: "sns-topic"
 *         display-name: "sns-topic-example-ex"
 *         policy: "sns-policy.json"
 *     end
 */
@Type("sns-topic")
public class TopicResource extends AwsResource implements Copyable<Topic> {

    private String name;
    private String deliveryPolicy;
    private String policy;
    private String displayName;
    private Map<String, String> tags;

    // Output
    private String arn;

    /**
     * The name of the topic. May contain alphanumeric characters, hyphens and underscores and it maybe up to 256 characters long.
     */
    @Required
    @Regex(value = "[a-zA-Z0-9_.-]{1,256}", message = "1-256 alphanumeric characters, hyphens and underscores.")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The delivery retry policy for the sns topic. May be json file or json blob.
     */
    @Updatable
    public String getDeliveryPolicy() {
        deliveryPolicy = getProcessedPolicy(deliveryPolicy);

        return deliveryPolicy;
    }

    public void setDeliveryPolicy(String deliveryPolicy) {
        this.deliveryPolicy = deliveryPolicy;
    }

    /**
     * The access policy for the sns topic. May be json file or json blob.
     */
    @Updatable
    public String getPolicy() {
        policy = getProcessedPolicy(policy);

        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    /**
     * The sns topic display name. May contain alphanumeric characters, hyphens and underscores and it maybe up to 100 characters long.
     */
    @Updatable
    @Regex(value = "[a-zA-Z0-9_.-]{1,100}", message = "1-100 alphanumeric characters, hyphens and underscores.")
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * The tags for the sns topic.
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The arn of the sns topic.
     */
    @Output
    @Id
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }
    
    @Override
    public void copyFrom(Topic topic) {
        SnsClient client = createClient(SnsClient.class);

        GetTopicAttributesResponse attributesResponse = client.getTopicAttributes(r -> r.topicArn(topic.topicArn()));

        //The list of attributes is much larger than what can be set.
        //Only those that can be set are extracted out of the list of attributes.
        setDisplayName(attributesResponse.attributes().get("DisplayName"));
        setPolicy(attributesResponse.attributes().get("Policy"));
        setDeliveryPolicy(attributesResponse.attributes().get("DeliveryPolicy"));

        setArn(attributesResponse.attributes().get("TopicArn"));
        setName(getArn().split(":")[getArn().split(":").length - 1]);

        getTags().clear();
        client.listTagsForResource(r -> r.resourceArn(getArn())).tags().forEach(t -> getTags().put(t.key(), t.value()));

    }

    @Override
    public boolean refresh() {
        SnsClient client = createClient(SnsClient.class);

        Topic topic = client.listTopicsPaginator().topics().stream().filter(o -> o.topicArn().equals(getArn())).findFirst().orElse(null);

        if (topic == null) {
            return false;
        }

        copyFrom(topic);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        SnsClient client = createClient(SnsClient.class);

        CreateTopicResponse response = client.createTopic(
            r -> r.attributes(getAttributes())
                .name(getName())
                .tags(getTags().entrySet()
                    .stream()
                    .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
                    .collect(Collectors.toList()))
        );

        setArn(response.topicArn());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        SnsClient client = createClient(SnsClient.class);

        if (changedFieldNames.contains("display-name")) {
            client.setTopicAttributes(r -> r.attributeName("DisplayName")
                .attributeValue(getDisplayName())
                .topicArn(getArn()));
        }

        if (changedFieldNames.contains("policy")) {
            if (ObjectUtils.isBlank(getPolicy())) {
                throw new GyroException("policy cannot be set to blank.");
            }

            client.setTopicAttributes(r -> r.attributeName("Policy")
                .attributeValue(getPolicy())
                .topicArn(getArn()));
        }

        if (changedFieldNames.contains("delivery-policy")) {
            if (ObjectUtils.isBlank(getDeliveryPolicy())) {
                throw new GyroException("delivery-policy cannot be set to blank.");
            }

            client.setTopicAttributes(r -> r.attributeName("DeliveryPolicy")
                .attributeValue(getDeliveryPolicy())
                .topicArn(getArn()));
        }

        if (changedFieldNames.contains("tags")) {
            TopicResource currentResource = (TopicResource) current;

            if (!currentResource.getTags().isEmpty()) {
                client.untagResource(UntagResourceRequest.builder()
                    .resourceArn(getArn())
                    .tagKeys(currentResource.getTags().keySet())
                    .build());
            }

            client.tagResource(TagResourceRequest.builder()
                .resourceArn(getArn())
                .tags(getTags().entrySet()
                    .stream()
                    .map(t -> Tag.builder().key(t.getKey()).value(t.getValue()).build())
                    .collect(Collectors.toList()))
                .build());
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        SnsClient client = createClient(SnsClient.class);

        client.deleteTopic(r -> r.topicArn(getArn()));
    }

    private Map<String, String> getAttributes() {
        Map<String, String> attributes = new HashMap<>();

        attributes.put("DisplayName", getDisplayName());

        if (!ObjectUtils.isBlank(getPolicy())) {
            attributes.put("Policy", getPolicy());
        }

        if (!ObjectUtils.isBlank(getDeliveryPolicy())) {
            attributes.put("DeliveryPolicy", getDeliveryPolicy());
        }

        return attributes;
    }

    private String getProcessedPolicy(String policy) {
        if (policy == null) {
            return null;
        } else if (policy.endsWith(".json")) {
            try (InputStream input = openInput(policy)) {
                policy = IoUtils.toUtf8String(input);

            } catch (IOException ex) {
                throw new GyroException(String.format("File at path '%s' not found.", policy));
            }
        }

        ObjectMapper obj = new ObjectMapper();
        try {
            JsonNode jsonNode = obj.readTree(policy);
            return jsonNode.toString();
        } catch (IOException ex) {
            throw new GyroException(String.format("Could not read the json `%s`",policy),ex);
        }
    }
}
