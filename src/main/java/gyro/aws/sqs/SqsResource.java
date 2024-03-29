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

package gyro.aws.sqs;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.psddev.dari.util.CompactMap;
import com.psddev.dari.util.JsonProcessor;
import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsCredentials;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.PolicyResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.TimeoutSettings;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Range;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesResponse;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;
import software.amazon.awssdk.utils.IoUtils;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::sqs-queue sqs-example996556
 *          name : "testRedrive3345itest12345"
 *          visibility-timeout : 400
 *          message-retention-period : 864000
 *          maximum-message-size : 258048
 *          delay-seconds : 140
 *          receive-message-wait-time-seconds : 5
 *          kms-master-key-id : 23
 *          kms-data-key-reuse-period-seconds : 200
 *          policy: 'policy.json'
 *          dead-letter-queue-name : "testRedrive3345itest123"
 *          max-receive-count : "5"
 *     end
 *
 * .. code-block:: gyro
 *
 *     aws::sqs-queue sqs-example2.fifo
 *          name : "testxyz"
 *          visibility-timeout : 400
 *          message-retention-period : 864000
 *          maximum-message-size : 258048
 *          delay-seconds : 140
 *          receive-message-wait-time-seconds : 5
 *          kms-master-key-id : 23
 *          kms-data-key-reuse-period-seconds : 200
 *          policy: 'policy.json'
 *          dead-letter-target-arn : "arn:aws:sqs:us-east-2:242040583208:testRedrive3345.fifo"
 *          max-receive-count : "5"
 *          content-based-deduplication : 'true'
 *     end
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
    private String kmsMasterKeyId;
    private Integer kmsDataKeyReusePeriodSeconds;
    private String policy;

    /**
     * The name of the queue. The name of a FIFO queue must end with the .fifo suffix.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The visibility timeout for the queue, in seconds. Defaults to ``30``.
     */
    @Updatable
    @Range(min = 0, max = 43200)
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
     * The length of time, in seconds, for which thw queue retains a message. Defaults to ``345600``.
     */
    @Updatable
    @Range(min = 60, max = 1209600)
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
     * The length of time, in seconds, for which the delivery of all messages in the queue is delayed. Defaults to 0.
     */
    @Updatable
    @Range(min = 0, max = 900)
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
     * The limit of how many bytes a message can contain before the queue rejects it. Defaults to ``262144``.
     */
    @Updatable
    @Range(min = 1024, max = 262144)
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
     * The length of time, in seconds, for which a ReceiveMessage action waits for a message to arrive. Defaults to ``0``.
     */
    @Updatable
    @Range(min = 0, max = 20)
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
     * The ARN of the dead-letter queue to which Amazon SQS moves messages after the value of maxReceiveCount is exceeded. See `<https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-dead-letter-queues.html>`_.
     */
    @Updatable
    public String getDeadLetterTargetArn() {
        return deadLetterTargetArn;
    }

    public void setDeadLetterTargetArn(String deadLetterTargetArn) {
        this.deadLetterTargetArn = deadLetterTargetArn;
    }

    /**
     * The number of times a message is received before being moved to the dead-letter queue.
     */
    @Updatable
    public String getMaxReceiveCount() {
        return maxReceiveCount;
    }

    public void setMaxReceiveCount(String maxReceiveCount) {
        this.maxReceiveCount = maxReceiveCount;
    }

    /**
     * The name of the dead-letter queue.
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
     * The ID of an AWS-managed customer master key (CMK) for Amazon SQS or a custom CMK.
     */
    @Updatable
    public String getKmsMasterKeyId() {
        return kmsMasterKeyId;
    }

    public void setKmsMasterKeyId(String kmsMasterKeyId) {
        this.kmsMasterKeyId = kmsMasterKeyId;
    }

    /**
     * The length of time, in seconds, for which Amazon SQS can reuse a data key to encrypt or decrypt messages before calling AWS KMS again. Defaults to ``300``
     */
    @Updatable
    @Range(min = 60, max = 86400)
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
     * The policy document. A policy path or policy string is allowed.
     */
    @Required
    @Updatable
    public String getPolicy() {
        if (this.policy != null && this.policy.contains(".json")) {
            try (InputStream input = openInput(this.policy)) {
                this.policy = PolicyResource.formatPolicy(IoUtils.toUtf8String(input));
                return this.policy;
            } catch (IOException err) {
                throw new GyroException(MessageFormat
                    .format("Queue - {0} policy error. Unable to read policy from path [{1}]", getName(), policy));
            }
        } else {
            return PolicyResource.formatPolicy(this.policy);
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

        setQueueUrl(sqs);

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
            setDeadLetterQueueName(getDeadLetterTargetArn().substring(getDeadLetterTargetArn().lastIndexOf(':') + 1));
        }

        if (response.attributes().get(QueueAttributeName.KMS_MASTER_KEY_ID) != null) {
            setKmsMasterKeyId(response.attributes().get(QueueAttributeName.KMS_MASTER_KEY_ID));

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
                .resourceOverrides(this, TimeoutSettings.Action.CREATE)
                .prompt(false)
                .until(() -> !ObjectUtils.isBlank(getQueue(client)));

            if (!waitResult) {
                throw new GyroException(String .format("Sqs queue - %s, not available for use. ", getName()));
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
        attributeUpdate.put(QueueAttributeName.KMS_MASTER_KEY_ID, getKmsMasterKeyId() != null ? getKmsMasterKeyId() : null);

        attributeUpdate.put(QueueAttributeName.KMS_DATA_KEY_REUSE_PERIOD_SECONDS, getKmsDataKeyReusePeriodSeconds().toString());
        attributeUpdate.put(QueueAttributeName.POLICY, getPolicy());

        if (getName().contains(".fifo")) {
            attributeUpdate.put(QueueAttributeName.CONTENT_BASED_DEDUPLICATION, getContentBasedDeduplication());
        }

        attributeUpdate.put(QueueAttributeName.REDRIVE_POLICY, null);
        if (!ObjectUtils.isBlank(getDeadLetterTargetArn()) && !ObjectUtils.isBlank(getMaxReceiveCount())) {

            String policy = String.format("{\"maxReceiveCount\": \"%s\", \"deadLetterTargetArn\": \"%s\"}",
                getMaxReceiveCount(), getDeadLetterTargetArn());

            attributeUpdate.put(QueueAttributeName.REDRIVE_POLICY, policy);

        } else if (!ObjectUtils.isBlank(getDeadLetterQueueName()) && !ObjectUtils.isBlank(getMaxReceiveCount())) {

            String policy = String.format("{\"maxReceiveCount\": \"%s\", \"deadLetterTargetArn\": \"%s\"}",
                getMaxReceiveCount(), createQueueArn(getDeadLetterQueueName()));

            attributeUpdate.put(QueueAttributeName.REDRIVE_POLICY, policy);
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
}
