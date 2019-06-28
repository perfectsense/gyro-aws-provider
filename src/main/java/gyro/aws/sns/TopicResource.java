package gyro.aws.sns;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.GetTopicAttributesResponse;
import software.amazon.awssdk.services.sns.model.Topic;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Creates a sns topic.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::topic sns-topic-example
 *         name: "sns-topic"
 *         display-name: "sns-topic-example-ex"
 *         policy: "sns-policy.json"
 *     end
 */
@Type("topic")
public class TopicResource extends AwsResource implements Copyable<Topic> {

    private String arn;
    private String name;
    private String deliveryPolicy;
    private String policy;
    private String displayName;

    /**
     * The name of the topic. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
     * The access policy. Can be json file or json blob.
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
     * The sns display name.
     */
    @Updatable
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * The arn of the sns.
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
    }

    @Override
    public boolean refresh() {
        SnsClient client = createClient(SnsClient.class);

        Topic topic = client.listTopicsPaginator().topics().stream().findFirst().filter(o -> o.topicArn().equals(getArn())).orElse(null);

        if (topic == null) {
            return false;
        }

        copyFrom(topic);

        return true;
    }

    @Override
    public void create() {
        SnsClient client = createClient(SnsClient.class);

        CreateTopicResponse response = client.createTopic(
            r -> r.attributes(getAttributes()).name(getName())
        );

        setArn(response.topicArn());
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
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
    }

    @Override
    public void delete() {
        SnsClient client = createClient(SnsClient.class);

        client.deleteTopic(r -> r.topicArn(getArn()));
    }

    @Override
    public String toDisplayString() {
        return "sns topic " + getName();
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
