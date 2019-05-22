package gyro.aws.sqs;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsCredentials;
import gyro.aws.AwsResource;
import gyro.core.GyroException;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.psddev.dari.util.CompactMap;
import com.psddev.dari.util.JsonProcessor;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesResponse;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro with the standard queue
 *
 *aws::sqs sqs-example996556
 *      name : "testRedrive3345itest12345"
 *      visibility-timeout : 400
 *      message-retention-period : 864000 (in seconds)
 *      maximum-message-size : 258048 (bytes)
 *      delay-seconds : 140
 *      receive-message-wait-time-seconds : 5
 *      kms-master-key-id : 23
 *      kms-data-key-reuse-period-seconds : 200
 *      policy-doc-path: 'policy.json'
 *      dead-letter-queue-name : "testRedrive3345itest123"
 *      max-receive-count : "5"
 *      account-no : 242040583208 (needed when dead letter queue name is provided instead of the dead letter target ARN)
 * end
 *
 *.. code-block:: gyro with the fifo queue
 *
 * aws::sqs sqs-example2.fifo
 *      name : "testxyz"
 *      visibility-timeout : 400
 *      message-retention-period : 864000 (in seconds)
 *      maximum-message-size : 258048 (bytes)
 *      delay-seconds : 140
 *      receive-message-wait-time-seconds : 5
 *      kms-master-key-id : 23
 *      kms-data-key-reuse-period-seconds : 200
 *      policy-doc-path: 'policy.json'
 *      dead-letter-target-arn : "arn:aws:sqs:us-east-2:242040583208:testRedrive3345.fifo"
 *      max-receive-count : "5"
 *      content-based-deduplication : 'true'
 * end
 */

@Type("sqs")
public class SqsResource extends AwsResource {
    private String name;
    private String queueArn;
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
    private transient String policyDocPath;
    private String policy;
    private String accountNo;

    /**
     * Enables setting the name of the queue. The name of a FIFO queue must end with the .fifo suffix.
     * Example for standard queue : test123
     * Example for Fifo queue : test123.fifo
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Enable setting up the attributes for the queue. See `<https://docs.aws.amazon.com/AWSSimpleQueueService/latest/APIReference/API_GetQueueAttributes.html>`_.
     * Check default values for the attributes of the queue here `<https://docs.aws.amazon.com/AWSSimpleQueueService/latest/APIReference/API_SetQueueAttributes.html>`_.
     * Valid values :
     * delay-seconds : 0-900 seconds
     * visibility-timeout : 0-12 hrs
     * message-retention-period : 1 minute - 14 days
     * maximum-message-size : 1 KiB - 256 Kib
     * receive-message-wait-time-seconds : 0-20 seconds
     */

    @Updatable
    public Integer getVisibilityTimeout() {
        return visibilityTimeout;
    }

    public void setVisibilityTimeout(Integer visibilityTimeout) {
        this.visibilityTimeout = visibilityTimeout;
    }

    @Updatable
    public Integer getMessageRetentionPeriod() {
        return messageRetentionPeriod;
    }

    public void setMessageRetentionPeriod(Integer messageRetentionPeriod) {
        this.messageRetentionPeriod = messageRetentionPeriod;
    }

    @Updatable
    public Integer getDelaySeconds() {
        return delaySeconds;
    }

    public void setDelaySeconds(Integer delaySeconds) {
        this.delaySeconds = delaySeconds;
    }

    @Updatable
    public Integer getMaximumMessageSize() {
        return maximumMessageSize;
    }

    public void setMaximumMessageSize(Integer maximumMessageSize) {
        this.maximumMessageSize = maximumMessageSize;
    }

    @Updatable
    public Integer getReceiveMessageWaitTimeSeconds() {
        return receiveMessageWaitTimeSeconds;
    }

    public void setReceiveMessageWaitTimeSeconds(Integer receiveMessageWaitTimeSeconds) {
        this.receiveMessageWaitTimeSeconds = receiveMessageWaitTimeSeconds;
    }

    public String getQueueArn() {
        return queueArn;
    }

    public void setQueueArn(String queueArn) {
        this.queueArn = queueArn;
    }

    public String getQueueUrl() {
        return queueUrl;
    }

    public void setQueueUrl(String queueUrl) {
        this.queueUrl = queueUrl;
    }

    /**
     * Enables moving messages to the dead letter queue. See `<https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-dead-letter-queues.html>`_.
     */
    @Updatable
    public String getDeadLetterTargetArn() {
        return deadLetterTargetArn;
    }

    public void setDeadLetterTargetArn(String deadLetterTargetArn) {
        this.deadLetterTargetArn = deadLetterTargetArn;
    }

    @Updatable
    public String getMaxReceiveCount() {
        return maxReceiveCount;
    }

    public void setMaxReceiveCount(String maxReceiveCount) {
        this.maxReceiveCount = maxReceiveCount;
    }

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
     * Enables server side encryption on queues. See `<https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-server-side-encryption.html#sqs-sse-key-terms>`_.
     */
    @Updatable
    public Integer getKmsMasterKeyId() {
        return kmsMasterKeyId;
    }

    public void setKmsMasterKeyId(Integer kmsMasterKeyId) {
        this.kmsMasterKeyId = kmsMasterKeyId;
    }

    @Updatable
    public Integer getKmsDataKeyReusePeriodSeconds() {
        return kmsDataKeyReusePeriodSeconds;
    }

    public void setKmsDataKeyReusePeriodSeconds(Integer kmsDataKeyReusePeriodSeconds) {
        this.kmsDataKeyReusePeriodSeconds = kmsDataKeyReusePeriodSeconds;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    /**
     * Enables setting up the valid IAM policies and permissions for the queue.
     */
    @Updatable
    public String getPolicyDocPath() {
        return policyDocPath;
    }

    public void setPolicyDocPath(String policyDocPath) {
        this.policyDocPath = policyDocPath;

        if (!ObjectUtils.isBlank(policyDocPath)) {
            setPolicyFromPath();
        }
    }

    /**
     * Need to change when code changes
     * This method removes all the spaces from the policy content even from the values, the better way to do it would be by using the Jackson's API
     */
    @Updatable
    public String getPolicy() {
        return policy != null ? policy.replaceAll(System.lineSeparator(), " ").replaceAll("\t", " ").trim().replaceAll(" ", "") : policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    @Override
    public boolean refresh() {
        SqsClient client = createClient(SqsClient.class);

        ListQueuesResponse response1 = client.listQueues(r -> r.queueNamePrefix(getName()));
        if (!response1.queueUrls().isEmpty()) {
            setQueueUrl(response1.queueUrls() != null ? response1.queueUrls().get(0) : null);
        }

        if (getQueueUrl() != null && !getQueueUrl().isEmpty()) {

            GetQueueAttributesResponse response = client.getQueueAttributes(r -> r.queueUrl(getQueueUrl())
                .attributeNames(QueueAttributeName.ALL));

            setQueueArn(response.attributes().get(QueueAttributeName.QUEUE_ARN));

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
            return true;
        }
        return false;
    }

    @Override
    public void create() {
        SqsClient client = createClient(SqsClient.class);

        ListQueuesResponse listName = client.listQueues(r -> r.queueNamePrefix(getName()));

        if (listName.queueUrls().isEmpty()) {
            createQueue(client);
        } else {
            throw new GyroException("A queue with the name " + getName() + " already exists.");
        }
    }

    private void createQueue(SqsClient client) {
        Map<QueueAttributeName, String> attributeMap = new HashMap<>();

        if (name.substring(name.lastIndexOf(".") + 1).equals("fifo")) {
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

        if (getDeadLetterTargetArn() != null && getMaxReceiveCount() != null) {

            String policy = String.format("{\"maxReceiveCount\": \"%s\", \"deadLetterTargetArn\": \"%s\"}",
                getMaxReceiveCount(), getDeadLetterTargetArn());

            attributeMap.put(QueueAttributeName.REDRIVE_POLICY, policy);

        } else if (getDeadLetterQueueName() != null && getMaxReceiveCount() != null) {

            String policy = String.format("{\"maxReceiveCount\": \"%s\", \"deadLetterTargetArn\": \"%s\"}",
                getMaxReceiveCount(), createQueueArn(deadLetterQueueName));

            attributeMap.put(QueueAttributeName.REDRIVE_POLICY, policy);
        }

        if (policyDocPath != null) {
            setPolicyFromPath();
            attributeMap.put(QueueAttributeName.POLICY, getPolicy());
        }

        client.createQueue(r -> r.queueName(getName()).attributes(attributeMap));
        setQueueUrl(client.createQueue(r -> r.queueName(getName()).attributes(attributeMap)).queueUrl());

        GetQueueAttributesResponse response = client.getQueueAttributes(r -> r.queueUrl(getQueueUrl())
                .attributeNames(QueueAttributeName.QUEUE_ARN));

        setQueueArn(response.attributes().get(QueueAttributeName.QUEUE_ARN));
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {

        Map<QueueAttributeName, String> attributeUpdate = new HashMap<>();

        attributeUpdate.put(QueueAttributeName.VISIBILITY_TIMEOUT, getVisibilityTimeout().toString());
        attributeUpdate.put(QueueAttributeName.MESSAGE_RETENTION_PERIOD, getMessageRetentionPeriod().toString());
        attributeUpdate.put(QueueAttributeName.DELAY_SECONDS, getDelaySeconds().toString());
        attributeUpdate.put(QueueAttributeName.MAXIMUM_MESSAGE_SIZE, getMaximumMessageSize().toString());
        attributeUpdate.put(QueueAttributeName.RECEIVE_MESSAGE_WAIT_TIME_SECONDS, getReceiveMessageWaitTimeSeconds().toString());
        attributeUpdate.put(QueueAttributeName.KMS_MASTER_KEY_ID, getKmsMasterKeyId().toString());
        attributeUpdate.put(QueueAttributeName.KMS_DATA_KEY_REUSE_PERIOD_SECONDS, getKmsDataKeyReusePeriodSeconds().toString());
        attributeUpdate.put(QueueAttributeName.POLICY, getPolicy());
        attributeUpdate.put(QueueAttributeName.CONTENT_BASED_DEDUPLICATION, getContentBasedDeduplication());

        SqsClient client = createClient(SqsClient.class);

        client.setQueueAttributes(r -> r.attributes(attributeUpdate).queueUrl(getQueueUrl()));
    }

    @Override
    public void delete() {
        SqsClient client = createClient(SqsClient.class);

        client.deleteQueue(r -> r.queueUrl(getQueueUrl()));

    }

    @Override
    public String toDisplayString() {
        return getName();
    }

    private void setPolicyFromPath() {
        try (InputStream input = openInput(getPolicyDocPath())) {
            setPolicy(IoUtils.toUtf8String(input));

        } catch (IOException ioex) {
            throw new GyroException(MessageFormat
                .format("Queue - {0} policy error. Unable to read policy from path [{1}]", getName(), getPolicyDocPath()));
        }
    }

    /**
     * Adding the account number in the config is a temporary fix, need to change when code changes
     */
    private String createQueueArn(String deadLetterQueueName) {
        AwsCredentials awsCredentials = credentials(AwsCredentials.class);

        return "arn:aws:sqs:" + awsCredentials.getRegion() + ":" + getAccountNo() + ":" + deadLetterQueueName;
    }

    private void addAttributeEntry(Map<QueueAttributeName, String> request, QueueAttributeName name, Object value) {
        if (value != null) {
            request.put(name, value.toString());
        }
    }
}
