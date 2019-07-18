package gyro.aws.sqs;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsCredentials;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.psddev.dari.util.CompactMap;
import com.psddev.dari.util.JsonProcessor;

import gyro.core.scope.State;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesResponse;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 * aws::sqs-queue sqs-example996556
 *      name : "testRedrive3345itest12345"
 *      visibility-timeout : 400
 *      message-retention-period : 864000
 *      maximum-message-size : 258048
 *      delay-seconds : 140
 *      receive-message-wait-time-seconds : 5
 *      kms-master-key-id : 23
 *      kms-data-key-reuse-period-seconds : 200
 *      policy: 'policy.json'
 *      dead-letter-queue-name : "testRedrive3345itest123"
 *      max-receive-count : "5"
 * end
 *
 * aws::sqs-queue sqs-example2.fifo
 *      name : "testxyz"
 *      visibility-timeout : 400
 *      message-retention-period : 864000
 *      maximum-message-size : 258048
 *      delay-seconds : 140
 *      receive-message-wait-time-seconds : 5
 *      kms-master-key-id : 23
 *      kms-data-key-reuse-period-seconds : 200
 *      policy: 'policy.json'
 *      dead-letter-target-arn : "arn:aws:sqs:us-east-2:242040583208:testRedrive3345.fifo"
 *      max-receive-count : "5"
 *      content-based-deduplication : 'true'
 * end
 */

@Type("sqs-queue")
public class SqsResource extends AwsResource implements Copyable<String> {

    private String name;
    private String arn;
    private String queueUrl;
    private Integer visibilityTimeout;
    private Integer messageRetentionPeriod;
    private Integer delaySeconds;
    private Integer maximumMessageSize;
    private Integer receiveMessageWaitTimeSeconds;
    private String deadLetterQueueName;
    private String deadLetterTargetArn;
    private String maxReceiveCount;
    private String contentBasedDeduplication;
    private Integer kmsMasterKeyId;
    private Integer kmsDataKeyReusePeriodSeconds;
    private String policy;

    /**
     * The name of the queue. The name of a FIFO queue must end with the .fifo suffix. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The visibility timeout for the queue, in seconds. Valid values include any integer from ``0`` to ``43200``. Defaults to ``30``.
     */
    @Updatable
    public Integer getVisibilityTimeout() {
        if (visibilityTimeout == null) {
            visibilityTimeout = 30;
        }

        return visibilityTimeout;
    }

    public void setVisibilityTimeout(Integer visibilityTimeout) {
        this.visibilityTimeout = visibilityTimeout;
    }

    /**
     * The length of time, in seconds, for which thw queue retains a message. Valid values include any integer from ``60`` to ``1209600``. Defaults to ``345600``.
     */
    @Updatable
    public Integer getMessageRetentionPeriod() {
        if (messageRetentionPeriod == null){
            messageRetentionPeriod = 345600;
        }

        return messageRetentionPeriod;
    }

    public void setMessageRetentionPeriod(Integer messageRetentionPeriod) {
        this.messageRetentionPeriod = messageRetentionPeriod;
    }

    /**
     * The length of time, in seconds, for which the delivery of all messages in the queue is delayed. Valid values include any integer from ``0`` to ``900``. Defaults to 0.
     */
    @Updatable
    public Integer getDelaySeconds() {
        if (delaySeconds == null) {
            delaySeconds = 0;
        }

        return delaySeconds;
    }

    public void setDelaySeconds(Integer delaySeconds) {
        this.delaySeconds = delaySeconds;
    }

    /**
     * The limit of how many bytes a message can contain before the queue rejects it. Valid values include any integer from ``1024`` to ``262144``. Defaults to ``262144``.
     */
    @Updatable
    public Integer getMaximumMessageSize() {
        if (maximumMessageSize == null) {
            maximumMessageSize = 262144;
        }

        return maximumMessageSize;
    }

    public void setMaximumMessageSize(Integer maximumMessageSize) {
        this.maximumMessageSize = maximumMessageSize;
    }

    /**
     * The length of time, in seconds, for which a ReceiveMessage action waits for a message to arrive. Valid values include any integer from ``0`` to ``20``. Defaults to ``0``.
     */
    @Updatable
    public Integer getReceiveMessageWaitTimeSeconds() {
        if (receiveMessageWaitTimeSeconds == null) {
            receiveMessageWaitTimeSeconds = 0;
        }

        return receiveMessageWaitTimeSeconds;
    }

    public void setReceiveMessageWaitTimeSeconds(Integer receiveMessageWaitTimeSeconds) {
        this.receiveMessageWaitTimeSeconds = receiveMessageWaitTimeSeconds;
    }

    /**
     * The ARN of the dead-letter queue to which Amazon SQS moves messages after the value of maxReceiveCount is exceeded. (Optional) See `<https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-dead-letter-queues.html>`_.
     */
    @Updatable
    public String getDeadLetterTargetArn() {
        return deadLetterTargetArn;
    }

    public void setDeadLetterTargetArn(String deadLetterTargetArn) {
        this.deadLetterTargetArn = deadLetterTargetArn;
    }

    /**
     * The number of times a message is received before being moved to the dead-letter queue. (Optional)
     */
    @Updatable
    public String getMaxReceiveCount() {
        return maxReceiveCount;
    }

    public void setMaxReceiveCount(String maxReceiveCount) {
        this.maxReceiveCount = maxReceiveCount;
    }

    /**
     * The name of the dead-letter queue. (Optional)
     */
    @Updatable
    public String getDeadLetterQueueName() {
        return deadLetterQueueName;
    }

    public void setDeadLetterQueueName(String deadLetterQueueName) {
        this.deadLetterQueueName = deadLetterQueueName;
    }

    /**
     * Enables content-based deduplication for FIFO Queues. See `<https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/FIFO-queues.html#FIFO-queues-exactly-once-processing>`_.
     */
    @Updatable
    public String getContentBasedDeduplication() {
        return contentBasedDeduplication;
    }

    public void setContentBasedDeduplication(String contentBasedDeduplication) {
        this.contentBasedDeduplication = contentBasedDeduplication;
    }

    /**
     * The ID of an AWS-managed customer master key (CMK) for Amazon SQS or a custom CMK. (Optional)
     */
    @Updatable
    public Integer getKmsMasterKeyId() {
        return kmsMasterKeyId;
    }

    public void setKmsMasterKeyId(Integer kmsMasterKeyId) {
        this.kmsMasterKeyId = kmsMasterKeyId;
    }

    /**
     * The length of time, in seconds, for which Amazon SQS can reuse a data key to encrypt or decrypt messages before calling AWS KMS again. Valid valus are any integer ``60`` to ``86400``. Defaults to ``300``
     */
    @Updatable
    public Integer getKmsDataKeyReusePeriodSeconds() {
        if (kmsDataKeyReusePeriodSeconds == null) {
            kmsDataKeyReusePeriodSeconds = 300;
        }

        return kmsDataKeyReusePeriodSeconds;
    }

    public void setKmsDataKeyReusePeriodSeconds(Integer kmsDataKeyReusePeriodSeconds) {
        this.kmsDataKeyReusePeriodSeconds = kmsDataKeyReusePeriodSeconds;
    }

    /**
     * The policy document. A policy path or policy string is allowed. (Required)
     */
    @Updatable
    public String getPolicy() {
        if (this.policy != null && this.policy.contains(".json")) {
            try (InputStream input = openInput(this.policy)) {
                this.policy = formatPolicy(IoUtils.toUtf8String(input));
                return this.policy;
            } catch (IOException err) {
                throw new GyroException(MessageFormat
                    .format("Queue - {0} policy error. Unable to read policy from path [{1}]", getName(), policy));
            }
        } else {
            return this.policy;
        }
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    /**
     * The arn of the queue.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The queue url.
     */
    @Output
    @Id
    public String getQueueUrl() {
        return queueUrl;
    }

    public void setQueueUrl(String queueUrl) {
        this.queueUrl = queueUrl;
    }

    @Override
    public void copyFrom(String sqs) {
        SqsClient client = createClient(SqsClient.class);

        GetQueueAttributesResponse response = client.getQueueAttributes(r -> r.queueUrl(sqs)
                .attributeNames(QueueAttributeName.ALL));

        setArn(response.attributes().get(QueueAttributeName.QUEUE_ARN));

        setVisibilityTimeout(Integer.valueOf(response.attributes()
                .get(QueueAttributeName.VISIBILITY_TIMEOUT)));

        setMessageRetentionPeriod(Integer.valueOf(response.attributes()
                .get(QueueAttributeName.MESSAGE_RETENTION_PERIOD)));

        setDelaySeconds(Integer.valueOf(response.attributes()
                .get(QueueAttributeName.DELAY_SECONDS)));

        setMaximumMessageSize(Integer.valueOf(response.attributes()
                .get(QueueAttributeName.MAXIMUM_MESSAGE_SIZE)));

        setReceiveMessageWaitTimeSeconds(Integer.valueOf(response.attributes()
                .get(QueueAttributeName.RECEIVE_MESSAGE_WAIT_TIME_SECONDS)));

        if (response.attributes().get(QueueAttributeName.POLICY) != null) {
            setPolicy(response.attributes().get(QueueAttributeName.POLICY));
        }

        if (response.attributes().get(QueueAttributeName.CONTENT_BASED_DEDUPLICATION) != null) {
            setContentBasedDeduplication(response.attributes()
                    .get(QueueAttributeName.CONTENT_BASED_DEDUPLICATION));
        }

        if (response.attributes().get(QueueAttributeName.REDRIVE_POLICY) != null) {
            String policy = response.attributes().get(QueueAttributeName.REDRIVE_POLICY);
            JsonProcessor obj = new JsonProcessor();
            Object parse = obj.parse(policy);

            setDeadLetterTargetArn(((CompactMap) parse).get("deadLetterTargetArn").toString());
            setMaxReceiveCount(((CompactMap) parse).get("maxReceiveCount").toString());
        }

        if (response.attributes().get(QueueAttributeName.KMS_MASTER_KEY_ID) != null) {
            setKmsMasterKeyId(Integer.valueOf(response.attributes().get(QueueAttributeName.KMS_MASTER_KEY_ID)));

            if (response.attributes().get(QueueAttributeName.KMS_DATA_KEY_REUSE_PERIOD_SECONDS) != null) {
                setKmsDataKeyReusePeriodSeconds(Integer.valueOf(response.attributes()
                        .get(QueueAttributeName.KMS_DATA_KEY_REUSE_PERIOD_SECONDS)));
            }
        }
    }

    @Override
    public boolean refresh() {
        SqsClient client = createClient(SqsClient.class);

        String queue = getQueue(client);

        if (ObjectUtils.isBlank(queue)) {
            return false;
        }

        copyFrom(queue);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        SqsClient client = createClient(SqsClient.class);

        if (ObjectUtils.isBlank(getQueue(client))) {
            createQueue(client);

            state.save();

            // Wait for the queue to be created.
            boolean waitResult = Wait.atMost(4, TimeUnit.MINUTES)
                .checkEvery(10, TimeUnit.SECONDS)
                .until(() -> !ObjectUtils.isBlank(getQueue(client)));

            if (!waitResult) {
                throw new GyroException("Unable to create sqs queue - " + getName());
            }
        } else {
            throw new GyroException("A queue with the name " + getName() + " already exists.");
        }
    }

    private void createQueue(SqsClient client) {
        Map<QueueAttributeName, String> attributeMap = new HashMap<>();

        if (getName().substring(getName().lastIndexOf(".") + 1).equals("fifo")) {
            attributeMap.put(QueueAttributeName.FIFO_QUEUE, "true");
        }

        addAttributeEntry(attributeMap, QueueAttributeName.VISIBILITY_TIMEOUT, getVisibilityTimeout());
        addAttributeEntry(attributeMap, QueueAttributeName.DELAY_SECONDS, getDelaySeconds());
        addAttributeEntry(attributeMap, QueueAttributeName.MESSAGE_RETENTION_PERIOD, getMessageRetentionPeriod());
        addAttributeEntry(attributeMap, QueueAttributeName.MAXIMUM_MESSAGE_SIZE, getMaximumMessageSize());
        addAttributeEntry(attributeMap, QueueAttributeName.RECEIVE_MESSAGE_WAIT_TIME_SECONDS, getReceiveMessageWaitTimeSeconds());
        addAttributeEntry(attributeMap, QueueAttributeName.CONTENT_BASED_DEDUPLICATION, getContentBasedDeduplication());
        addAttributeEntry(attributeMap, QueueAttributeName.KMS_MASTER_KEY_ID, getKmsMasterKeyId());
        addAttributeEntry(attributeMap, QueueAttributeName.KMS_DATA_KEY_REUSE_PERIOD_SECONDS, getKmsDataKeyReusePeriodSeconds());

        if (!ObjectUtils.isBlank(getDeadLetterTargetArn()) && !ObjectUtils.isBlank(getMaxReceiveCount())) {

            String policy = String.format("{\"maxReceiveCount\": \"%s\", \"deadLetterTargetArn\": \"%s\"}",
                getMaxReceiveCount(), getDeadLetterTargetArn());

            attributeMap.put(QueueAttributeName.REDRIVE_POLICY, policy);

        } else if (!ObjectUtils.isBlank(getDeadLetterQueueName()) && !ObjectUtils.isBlank(getMaxReceiveCount())) {

            String policy = String.format("{\"maxReceiveCount\": \"%s\", \"deadLetterTargetArn\": \"%s\"}",
                getMaxReceiveCount(), createQueueArn(getDeadLetterQueueName()));

            attributeMap.put(QueueAttributeName.REDRIVE_POLICY, policy);
        }

        attributeMap.put(QueueAttributeName.POLICY, getPolicy());

        CreateQueueResponse queueResponse = client.createQueue(r -> r.queueName(getName()).attributes(attributeMap));
        setQueueUrl(queueResponse.queueUrl());

        GetQueueAttributesResponse response = client.getQueueAttributes(r -> r.queueUrl(getQueueUrl())
                .attributeNames(QueueAttributeName.QUEUE_ARN));

        setArn(response.attributes().get(QueueAttributeName.QUEUE_ARN));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {

        Map<QueueAttributeName, String> attributeUpdate = new HashMap<>();

        attributeUpdate.put(QueueAttributeName.VISIBILITY_TIMEOUT, getVisibilityTimeout().toString());
        attributeUpdate.put(QueueAttributeName.MESSAGE_RETENTION_PERIOD, getMessageRetentionPeriod().toString());
        attributeUpdate.put(QueueAttributeName.DELAY_SECONDS, getDelaySeconds().toString());
        attributeUpdate.put(QueueAttributeName.MAXIMUM_MESSAGE_SIZE, getMaximumMessageSize().toString());
        attributeUpdate.put(QueueAttributeName.RECEIVE_MESSAGE_WAIT_TIME_SECONDS, getReceiveMessageWaitTimeSeconds().toString());
        attributeUpdate.put(QueueAttributeName.KMS_MASTER_KEY_ID, getKmsMasterKeyId() != null ? getKmsMasterKeyId().toString() : null);

        attributeUpdate.put(QueueAttributeName.KMS_DATA_KEY_REUSE_PERIOD_SECONDS, getKmsDataKeyReusePeriodSeconds().toString());
        attributeUpdate.put(QueueAttributeName.POLICY, getPolicy());

        if (getName().contains(".fifo")) {
            attributeUpdate.put(QueueAttributeName.CONTENT_BASED_DEDUPLICATION, getContentBasedDeduplication());
        }

        SqsClient client = createClient(SqsClient.class);

        client.setQueueAttributes(r -> r.attributes(attributeUpdate).queueUrl(getQueueUrl()));
    }

    @Override
    public void delete(GyroUI ui, State state) {
        SqsClient client = createClient(SqsClient.class);

        client.deleteQueue(r -> r.queueUrl(getQueueUrl()));
    }

    private String getQueue(SqsClient client) {
        ListQueuesResponse response = client.listQueues(r -> r.queueNamePrefix(getName()));

        return response.queueUrls().stream().filter(o -> o.endsWith(getName())).findFirst().orElse(null);
    }

    private String createQueueArn(String deadLetterQueueName) {
        AwsCredentials awsCredentials = credentials(AwsCredentials.class);

        return "arn:aws:sqs:" + awsCredentials.getRegion() + ":" + getAccountNumber() + ":" + deadLetterQueueName;
    }

    private String getAccountNumber() {
        StsClient client = createClient(StsClient.class);
        GetCallerIdentityResponse response = client.getCallerIdentity();
        return response.account();
    }

    private void addAttributeEntry(Map<QueueAttributeName, String> request, QueueAttributeName name, Object value) {
        if (value != null) {
            request.put(name, value.toString());
        }
    }

    private String formatPolicy(String policy) {
        return policy != null ? policy.replaceAll(System.lineSeparator(), " ").replaceAll("\t", " ").trim().replaceAll(" ", "") : policy;
    }
}
