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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.Type;

import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.GetSubscriptionAttributesResponse;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;
import software.amazon.awssdk.services.sns.model.Subscription;

import software.amazon.awssdk.utils.IoUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Creates a SNS subscription to a topic.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::sns-subscription sns-subscription-example
 *         protocol: "sqs"
 *         endpoint: $(aws::sqs-queue sqs-example | arn)
 *         topic: $(aws::sns-topic sns-topic-example)
 *         filter-policy: "filter-policy.json"
 *         raw-message-delivery: true
 *     end
 */
@Type("sns-subscription")
public class SubscriptionResource extends AwsResource implements Copyable<Subscription> {

    private String endpoint;
    private String protocol;
    private String subscriptionArn;
    private TopicResource topic;
    private String deliveryPolicy;
    private String filterPolicy;
    private Boolean rawMessageDelivery;

    /**
     * The endpoint of the resource subscribed to the topic. (Required)
     */
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * The protocol associated with the endpoint. (Required)
     */
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * The topic resource to subscribe to. (Required)
     */
    public TopicResource getTopic() {
        return topic;
    }

    public void setTopic(TopicResource topic) {
        this.topic = topic;
    }

    /**
     * The delivery retry policy. Can be json file or json blob.
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
     * The filter policy. Can be json file or json blob.
     */
    @Updatable
    public String getFilterPolicy() {
        filterPolicy = getProcessedPolicy(filterPolicy);

        return filterPolicy;
    }

    public void setFilterPolicy(String filterPolicy) {
        this.filterPolicy = filterPolicy;
    }

    /**
     * Allow raw message delivery. Defaults to false.
     */
    @Updatable
    public Boolean getRawMessageDelivery() {
        if (rawMessageDelivery == null) {
            rawMessageDelivery = false;
        }

        return rawMessageDelivery;
    }

    public void setRawMessageDelivery(Boolean rawMessageDelivery) {
        this.rawMessageDelivery = rawMessageDelivery;
    }

    /**
     * The arn of the subscription.
     */
    @Output
    @Id
    public String getSubscriptionArn() {
        return subscriptionArn;
    }

    public void setSubscriptionArn(String subscriptionArn) {
        this.subscriptionArn = subscriptionArn;
    }

    @Override
    public void copyFrom(Subscription subscription) {
        SnsClient client = createClient(SnsClient.class);

        GetSubscriptionAttributesResponse response = client.getSubscriptionAttributes(r -> r.subscriptionArn(subscription.subscriptionArn()));

        //The list of attributes is much larger than what can be set.
        //Only those that can be set are extracted out of the list of attributes.
        setDeliveryPolicy(response.attributes().getOrDefault("DeliveryPolicy", null));
        setFilterPolicy(response.attributes().getOrDefault("FilterPolicy", null));
        setRawMessageDelivery(response.attributes().get("RawMessageDelivery").equalsIgnoreCase("true"));

        setTopic(findById(TopicResource.class, response.attributes().get("TopicArn")));
        setSubscriptionArn(subscription.subscriptionArn());
        setEndpoint(subscription.endpoint());
        setProtocol(subscription.protocol());
    }

    @Override
    public boolean refresh() {
        SnsClient client = createClient(SnsClient.class);

        Subscription subscription = client.listSubscriptionsPaginator()
            .subscriptions().stream()
            .filter(o -> o.subscriptionArn().equals(getSubscriptionArn()))
            .findFirst()
            .orElse(null);

        if (subscription == null) {
            return false;
        }

        copyFrom(subscription);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        SnsClient client = createClient(SnsClient.class);

        SubscribeResponse subscribeResponse = client.subscribe(r -> r.attributes(getSubscriptionAttributes())
                .endpoint(getEndpoint())
                .protocol(getProtocol())
                .topicArn(getTopic().getArn()));

        setSubscriptionArn(subscribeResponse.subscriptionArn());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        SnsClient client = createClient(SnsClient.class);

        if (changedFieldNames.contains("raw-message-delivery")) {
            client.setSubscriptionAttributes(r -> r.attributeName("RawMessageDelivery")
                .attributeValue(getRawMessageDelivery() != null ? getRawMessageDelivery().toString() : null)
                .subscriptionArn(getSubscriptionArn()));
        }

        if (changedFieldNames.contains("delivery-policy")) {
            if (ObjectUtils.isBlank(getDeliveryPolicy())) {
                throw new GyroException("delivery-policy cannot be set to blank.");
            }

            client.setSubscriptionAttributes(r -> r.attributeName("DeliveryPolicy")
                .attributeValue(getDeliveryPolicy())
                .subscriptionArn(getSubscriptionArn()));
        }

        if (changedFieldNames.contains("filter-policy")) {
            if (ObjectUtils.isBlank(getFilterPolicy())) {
                throw new GyroException("filter-policy cannot be set to blank.");
            }

            client.setSubscriptionAttributes(r -> r.attributeName("FilterPolicy")
                .attributeValue(getFilterPolicy())
                .subscriptionArn(getSubscriptionArn()));
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        SnsClient client = createClient(SnsClient.class);

        client.unsubscribe(r -> r.subscriptionArn(getSubscriptionArn()));
    }

    private Map<String, String> getSubscriptionAttributes() {
        Map<String, String> attributes = new HashMap<>();

        if (getRawMessageDelivery() != null) {
            attributes.put("RawMessageDelivery", getRawMessageDelivery().toString());
        }

        if (!ObjectUtils.isBlank(getDeliveryPolicy())) {
            attributes.put("DeliveryPolicy", getDeliveryPolicy());
        }

        if (!ObjectUtils.isBlank(getFilterPolicy())) {
            attributes.put("FilterPolicy", getFilterPolicy());
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
