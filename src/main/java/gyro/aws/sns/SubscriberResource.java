package gyro.aws.sns;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import com.psddev.dari.util.CompactMap;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.AuthorizationErrorException;
import software.amazon.awssdk.services.sns.model.GetSubscriptionAttributesResponse;
import software.amazon.awssdk.services.sns.model.InvalidParameterException;
import software.amazon.awssdk.services.sns.model.NotFoundException;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;
import software.amazon.awssdk.services.sns.model.Subscription;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

/**
 * Creates a subscriber to a topic.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::subscriber subscriber-example
 *         protocol: "sqs"
 *         endpoint: $(aws::sqs sqs-example | queue-arn)
 *         attributes: {
 *             FilterPolicy: "gyro-providers/gyro-aws-provider/examples/sns/filter-policy.json",
 *             RawMessageDelivery: "true"
 *         }
 *         topic: $(aws::topic sns-topic-example)
 *     end
 */
@Type("subscriber")
public class SubscriberResource extends AwsResource implements Copyable<Subscription> {

    private Map<String, String> attributes;
    private String endpoint;
    private String protocol;
    private String subscriptionArn;
    private TopicResource topic;

    /**
     * The attributes for the subscription (Optional)
     *
     * Possible attributes are DeliveryPolicy, FilterPolicy, and RawMessageDelivery
     *
     * DeliveryPolicy can be a json file path or json blob (Optional)
     *
     * FilterPolicy can be a json file path or json blob (Optional)
     *
     * RawMessageDelivery is a boolean (Optional)
     */
    @Updatable
    public Map<String, String> getAttributes() {
        if (attributes == null) {
            attributes = new CompactMap<>();
        }

        if (attributes.get("DeliveryPolicy") != null && attributes.get("DeliveryPolicy").endsWith(".json")) {
            try {
                String encode = new String(Files.readAllBytes(Paths.get(attributes.get("DeliveryPolicy"))), "UTF-8");
                attributes.put("DeliveryPolicy", formatPolicy(encode));
            } catch (Exception err) {
                throw new GyroException(err.getMessage());
            }
        }

        if (attributes.get("FilterPolicy") != null && attributes.get("FilterPolicy").endsWith(".json")) {
            try {
                String encode = new String(Files.readAllBytes(Paths.get(attributes.get("FilterPolicy"))), "UTF-8");
                attributes.put("FilterPolicy", formatPolicy(encode));
            } catch (Exception err) {
                throw new GyroException(err.getMessage());
            }
        }

        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        if (this.attributes != null && attributes != null) {
            this.attributes.putAll(attributes);
        } else {
            this.attributes = attributes;
        }
    }

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

    @Output
    public String getSubscriptionArn() {
        return subscriptionArn;
    }

    public void setSubscriptionArn(String subscriptionArn) {
        this.subscriptionArn = subscriptionArn;
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

    @Override
    public void copyFrom(Subscription subscription) {
        SnsClient client = createClient(SnsClient.class);

        GetSubscriptionAttributesResponse response = client.getSubscriptionAttributes(r -> r.subscriptionArn(subscription.subscriptionArn()));
        getAttributes().clear();

        //The list of attributes is much larger than what can be set.
        //Only those that can be set are extracted out of the list of attributes.
        if (response.attributes().get("DeliveryPolicy") != null) {
            getAttributes().put("DeliveryPolicy", (response.attributes().get("DeliveryPolicy")));
        }
        if (response.attributes().get("FilterPolicy") != null) {
            getAttributes().put("FilterPolicy", (response.attributes().get("FilterPolicy")));
        }
        if (response.attributes().get("RawMessageDelivery") != null) {
            getAttributes().put("RawMessageDelivery", (response.attributes().get("RawMessageDelivery")));
        }

        setTopic(findById(TopicResource.class, response.attributes().get("TopicArn")));
    }

    @Override
    public boolean refresh() {
        SnsClient client = createClient(SnsClient.class);

        try {
            for (Subscription target : client.listSubscriptions().subscriptions()) {
                if (target.subscriptionArn().equals(getSubscriptionArn())) {
                    this.copyFrom(target);
                }
            }
        } catch (AuthorizationErrorException | InvalidParameterException ex) {
            throw new GyroException(ex.getMessage());
        } catch (NotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    public void create() {
        SnsClient client = createClient(SnsClient.class);

        SubscribeResponse subscribeResponse = client.subscribe(r -> r.attributes(getAttributes())
                .endpoint(getEndpoint())
                .protocol(getProtocol())
                .topicArn(getTopic().getArn()));

        setSubscriptionArn(subscribeResponse.subscriptionArn());
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        SnsClient client = createClient(SnsClient.class);

        for (Map.Entry<String, String> entry : getAttributes().entrySet()) {
            client.setSubscriptionAttributes(r -> r.attributeName(entry.getKey())
                    .attributeValue(getAttributes().get(entry.getValue()))
                    .subscriptionArn(getSubscriptionArn()));
        }
    }

    @Override
    public void delete() {
        SnsClient client = createClient(SnsClient.class);

        client.unsubscribe(r -> r.subscriptionArn(getSubscriptionArn()));
    }

    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("subscriber with protocol " + getProtocol());
        if (getEndpoint() != null) {
            sb.append(" and endpoint " + getEndpoint());
        }

        return sb.toString();
    }

    private String formatPolicy(String policy) {
        return policy != null ? policy.replaceAll(System.lineSeparator(), " ").replaceAll("\t", " ").trim().replaceAll(" ", "") : policy;
    }
}
