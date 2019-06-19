package gyro.aws.sns;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import com.psddev.dari.util.CompactMap;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.AuthorizationErrorException;
import software.amazon.awssdk.services.sns.model.GetSubscriptionAttributesResponse;
import software.amazon.awssdk.services.sns.model.InvalidParameterException;
import software.amazon.awssdk.services.sns.model.NotFoundException;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;
import software.amazon.awssdk.services.sns.model.Subscription;

import org.json.JSONObject;
import software.amazon.awssdk.utils.IoUtils;
import java.io.IOException;
import java.io.InputStream;
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

    private String endpoint;
    private String protocol;
    private String subscriptionArn;
    private Map<String, String> subscriptionAttributes;
    private TopicResource topic;

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
    public Map<String, String> getSubscriptionAttributes() {
        if (subscriptionAttributes == null) {
            subscriptionAttributes = new CompactMap<>();
        }

        if (subscriptionAttributes.get("DeliveryPolicy") != null && subscriptionAttributes.get("DeliveryPolicy").endsWith(".json")) {
            try {
                String encode = policyDocument(subscriptionAttributes.get("DeliveryPolicy"));
                subscriptionAttributes.put("DeliveryPolicy", formatPolicy(encode));
            } catch (Exception err) {
                throw new GyroException(err.getMessage());
            }
        }

        if (subscriptionAttributes.get("FilterPolicy") != null && subscriptionAttributes.get("FilterPolicy").endsWith(".json")) {
            try {
                String encode = policyDocument(subscriptionAttributes.get("FilterPolicy"));
                subscriptionAttributes.put("FilterPolicy", formatPolicy(encode));
            } catch (Exception err) {
                throw new GyroException(err.getMessage());
            }
        }

        return subscriptionAttributes;
    }

    public void setSubscriptionAttributes(Map<String, String> subscriptionAttributes) {
        if (this.subscriptionAttributes != null && subscriptionAttributes != null) {
            this.subscriptionAttributes.putAll(subscriptionAttributes);
        } else {
            this.subscriptionAttributes = subscriptionAttributes;
        }
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

        //The list of attributes is much larger than what can be set.
        //Only those that can be set are extracted out of the list of attributes.
        getSubscriptionAttributes().clear();
        if (response.attributes().get("DeliveryPolicy") != null) {
            getSubscriptionAttributes().put("DeliveryPolicy", (response.attributes().get("DeliveryPolicy")));
        }
        if (response.attributes().get("FilterPolicy") != null) {
            getSubscriptionAttributes().put("FilterPolicy", (response.attributes().get("FilterPolicy")));
        }
        if (response.attributes().get("RawMessageDelivery") != null) {
            getSubscriptionAttributes().put("RawMessageDelivery", (response.attributes().get("RawMessageDelivery")));
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

        SubscribeResponse subscribeResponse = client.subscribe(r -> r.attributes(getSubscriptionAttributes())
                .endpoint(getEndpoint())
                .protocol(getProtocol())
                .topicArn(getTopic().getArn()));

        setSubscriptionArn(subscribeResponse.subscriptionArn());
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        SnsClient client = createClient(SnsClient.class);

        JSONObject map = new JSONObject();

        for (Map.Entry<String, String> attr : getSubscriptionAttributes().entrySet()) {
            if (attr.getKey().contains("Policy")) {
                map.put(attr.getKey(), attr.getValue());
                client.setSubscriptionAttributes(r -> r.attributeName(attr.getKey())
                        .attributeValue(map.get(attr.getKey()).toString())
                        .subscriptionArn(getSubscriptionArn()));
            }
        }

        if (getSubscriptionAttributes().containsKey("RawMessageDelivery")) {
            client.setSubscriptionAttributes(r -> r.attributeName("RawMessageDelivery")
                    .attributeValue(getSubscriptionAttributes().get("RawMessageDelivery"))
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

    private String policyDocument(String policy) {
        if (policy != null && policy.contains(".json")) {
            try (InputStream input = openInput(policy)) {
                policy = formatPolicy(IoUtils.toUtf8String(input));
                return policy;
            } catch (IOException err) {
                throw new GyroException(err.getMessage());
            }
        } else {
            return policy;
        }
    }

    private String formatPolicy(String policy) {
        return policy != null ? policy.replaceAll(System.lineSeparator(), " ").replaceAll("\t", " ").trim().replaceAll(" ", "") : policy;
    }
}
