/*
 * Copyright 2026, Brightspot.
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

package gyro.aws.cloudwatch;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.PolicyResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidNumbers;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.AssociateKmsKeyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteDataProtectionPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteIndexPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteRetentionPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeIndexPoliciesRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeIndexPoliciesResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.DisassociateKmsKeyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetDataProtectionPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetDataProtectionPolicyResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.IndexPolicy;
import software.amazon.awssdk.services.cloudwatchlogs.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDataProtectionPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutIndexPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutRetentionPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Creates a CloudWatch log group.
 *
 * Example
 * -------
 * .. code-block:: gyro
 *
 *     aws::cloudwatch-log-group example-log-group
 *         name: "my-app-logs"
 *         retention-days: 30
 *
 *         tags: {
 *             "Environment": "dev"
 *         }
 *     end
 *
 *     aws::cloudwatch-log-group encrypted-log-group
 *         name: "/aws/lambda/my-function"
 *         retention-days: 14
 *         kms-key-id: "example-key-id"
 *         log-group-class: "STANDARD"
 *
 *         tags: {
 *             "Environment": "dev"
 *         }
 *
 *         index-policy: "index-policy.json"
 *     end
 *
 *     aws::cloudwatch-log-group protected-log-group
 *         name: "sensitive-logs"
 *         retention-days: 7
 *         log-group-class: "INFREQUENT_ACCESS"
 *
 *         data-protection-policy: "data-protection-policy.json"
 *     end
 */
@Type("cloudwatch-log-group")
public class LogGroupResource extends AwsResource implements Copyable<LogGroup> {

    private String logGroupName;
    private Integer retentionDays;
    private String kmsKeyId;
    private String logGroupClass;
    private Map<String, String> tags;
    private String indexPolicy;
    private String dataProtectionPolicy;

    //-- Read-only Attributes
    private String logGroupArn;
    private Long creationTime;
    private Long storedBytes;
    private Integer metricFilterCount;
    private String indexPolicySource;

    /**
     * The name of the log group.
     * Cannot be changed after creation.
     */
    @Required
    public String getLogGroupName() {
        return logGroupName;
    }

    public void setLogGroupName(String logGroupName) {
        this.logGroupName = logGroupName;
    }

    /**
     * The number of days to retain log events.
     */
    @Updatable
    @ValidNumbers({1, 3, 5, 7, 14, 30, 60, 90, 120, 150, 180, 365, 400, 545, 731, 1096, 1827, 2192, 2557, 2922, 3288, 3653})
    public Integer getRetentionDays() {
        return retentionDays;
    }

    public void setRetentionDays(Integer retentionDays) {
        this.retentionDays = retentionDays;
    }

    /**
     * The KMS key ID or ARN to use for encryption.
     * If not specified, encryption is disabled.
     */
    @Updatable
    public String getKmsKeyId() {
        return kmsKeyId;
    }

    public void setKmsKeyId(String kmsKeyId) {
        this.kmsKeyId = kmsKeyId;
    }

    /**
     * The log group class.
     */
    @ValidStrings({"STANDARD", "INFREQUENT_ACCESS"})
    public String getLogGroupClass() {
        return logGroupClass;
    }

    public void setLogGroupClass(String logGroupClass) {
        this.logGroupClass = logGroupClass;
    }

    /**
     * Tags to apply to the log group.
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
     * The JSON policy document for field indexing
     * Valid for ``STANDARD`` log class only.
     */
    @Updatable
    public String getIndexPolicy() {
        if (indexPolicy != null && indexPolicy.contains(".json")) {
            try (InputStream input = openInput(indexPolicy)) {
                indexPolicy = PolicyResource.formatPolicy(IoUtils.toUtf8String(input));
                return indexPolicy;
            } catch (IOException err) {
                throw new GyroException(err.getMessage());
            }
        } else {
            return PolicyResource.formatPolicy(indexPolicy);
        }
    }

    public void setIndexPolicy(String indexPolicy) {
        this.indexPolicy = indexPolicy;
    }

    /**
     * The JSON policy document for data protection (sensitive data masking).
     */
    @Updatable
    public String getDataProtectionPolicy() {
        if (dataProtectionPolicy != null && dataProtectionPolicy.contains(".json")) {
            try (InputStream input = openInput(dataProtectionPolicy)) {
                dataProtectionPolicy = PolicyResource.formatPolicy(IoUtils.toUtf8String(input));
                return dataProtectionPolicy;
            } catch (IOException err) {
                throw new GyroException(err.getMessage());
            }
        } else {
            return PolicyResource.formatPolicy(dataProtectionPolicy);
        }
    }

    public void setDataProtectionPolicy(String dataProtectionPolicy) {
        this.dataProtectionPolicy = dataProtectionPolicy;
    }

    /**
     * The ARN of the log group.
     */
    @Output
    public String getLogGroupArn() {
        return logGroupArn;
    }

    public void setLogGroupArn(String logGroupArn) {
        this.logGroupArn = logGroupArn;
    }

    /**
     * The creation time of the log group.
     */
    @Output
    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * The total bytes stored in the log group.
     */
    @Output
    public Long getStoredBytes() {
        return storedBytes;
    }

    public void setStoredBytes(Long storedBytes) {
        this.storedBytes = storedBytes;
    }

    /**
     * The number of metric filters for the log group.
     */
    @Output
    public Integer getMetricFilterCount() {
        return metricFilterCount;
    }

    public void setMetricFilterCount(Integer metricFilterCount) {
        this.metricFilterCount = metricFilterCount;
    }

    /**
     * The source of the log group index policy.
     */
    @Output
    public String getIndexPolicySource() {
        return indexPolicySource;
    }

    public void setIndexPolicySource(String indexPolicySource) {
        this.indexPolicySource = indexPolicySource;
    }

    @Override
    public void copyFrom(LogGroup logGroup) {
        setLogGroupName(logGroup.logGroupName());
        setRetentionDays(logGroup.retentionInDays());
        setKmsKeyId(logGroup.kmsKeyId());
        setLogGroupClass(logGroup.logGroupClass() != null ? logGroup.logGroupClass().toString() : null);
        setLogGroupArn(logGroup.logGroupArn());
        setCreationTime(logGroup.creationTime());
        setStoredBytes(logGroup.storedBytes());
        setMetricFilterCount(logGroup.metricFilterCount());

        CloudWatchLogsClient client = createClient(CloudWatchLogsClient.class);

        // Fetch tags
        getTags().clear();
        try {
            ListTagsForResourceResponse tagsResponse = client.listTagsForResource(
                ListTagsForResourceRequest.builder()
                    .resourceArn(logGroup.logGroupArn())
                    .build()
            );
            if (tagsResponse.tags() != null) {
                setTags(new HashMap<>(tagsResponse.tags()));
            }
        } catch (Exception e) {
            // Ignore
        }

        // Fetch index policy
        setIndexPolicy(null);
        setIndexPolicySource(null);
        try {
            DescribeIndexPoliciesResponse indexPolicies = client.describeIndexPolicies(
                DescribeIndexPoliciesRequest.builder()
                    .logGroupIdentifiers(logGroup.logGroupArn())
                    .build()
            );
            if (!indexPolicies.indexPolicies().isEmpty()) {
                IndexPolicy policy = indexPolicies.indexPolicies().get(0);
                setIndexPolicy(policy.policyDocument());
                setIndexPolicySource(policy.sourceAsString());
            }
        } catch (Exception e) {
            // Ignore
        }

        // Fetch data protection policy
        setDataProtectionPolicy(null);
        try {
            GetDataProtectionPolicyResponse dppResponse = client.getDataProtectionPolicy(
                GetDataProtectionPolicyRequest.builder()
                    .logGroupIdentifier(logGroup.logGroupArn())
                    .build()
            );
            setDataProtectionPolicy(dppResponse.policyDocument());
        } catch (Exception e) {
            // Ignore
        }
    }

    @Override
    public boolean refresh() {
        CloudWatchLogsClient client = createClient(CloudWatchLogsClient.class);
        LogGroup logGroup = LogGroupFinder.getLogGroup(client, getLogGroupName());
        if (logGroup == null) {
            return false;
        }
        copyFrom(logGroup);
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        CloudWatchLogsClient client = createClient(CloudWatchLogsClient.class);
        CreateLogGroupRequest.Builder requestBuilder = CreateLogGroupRequest.builder()
            .logGroupName(getLogGroupName());

        if (getKmsKeyId() != null) {
            requestBuilder.kmsKeyId(getKmsKeyId());
        }

        if (getLogGroupClass() != null) {
            requestBuilder.logGroupClass(getLogGroupClass());
        }

        if (!getTags().isEmpty()) {
            requestBuilder.tags(getTags());
        }

        client.createLogGroup(requestBuilder.build());
        state.save();

        if (getRetentionDays() != null) {
            client.putRetentionPolicy(
                PutRetentionPolicyRequest.builder()
                    .logGroupName(getLogGroupName())
                    .retentionInDays(getRetentionDays())
                    .build()
            );
            state.save();
        }

        if (getIndexPolicy() != null) {
            client.putIndexPolicy(
                PutIndexPolicyRequest.builder()
                    .logGroupIdentifier(getLogGroupName())
                    .policyDocument(getIndexPolicy())
                    .build()
            );
            state.save();
        }

        if (getDataProtectionPolicy() != null) {
            client.putDataProtectionPolicy(
                PutDataProtectionPolicyRequest.builder()
                    .logGroupIdentifier(getLogGroupName())
                    .policyDocument(getDataProtectionPolicy())
                    .build()
            );
            state.save();
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        CloudWatchLogsClient client = createClient(CloudWatchLogsClient.class);
        LogGroupResource currentResource = (LogGroupResource) current;

        if (changedFieldNames.contains("retention-days")) {
            if (getRetentionDays() != null) {
                client.putRetentionPolicy(
                    PutRetentionPolicyRequest.builder()
                        .logGroupName(getLogGroupName())
                        .retentionInDays(getRetentionDays())
                        .build()
                );
            } else if (currentResource.getRetentionDays() != null) {
                client.deleteRetentionPolicy(
                    DeleteRetentionPolicyRequest.builder()
                        .logGroupName(getLogGroupName())
                        .build()
                );
            }
        }

        if (changedFieldNames.contains("kms-key-id")) {
            if (getKmsKeyId() != null) {
                client.associateKmsKey(
                    AssociateKmsKeyRequest.builder()
                        .logGroupName(getLogGroupName())
                        .kmsKeyId(getKmsKeyId())
                        .build()
                );
            } else if (currentResource.getKmsKeyId() != null) {
                client.disassociateKmsKey(
                    DisassociateKmsKeyRequest.builder()
                        .logGroupName(getLogGroupName())
                        .build()
                );
            }
        }

        if (changedFieldNames.contains("tags")) {
            if (!currentResource.getTags().isEmpty()) {
                client.untagResource(r -> r.resourceArn(getLogGroupArn()).tagKeys(currentResource.getTags().keySet()));
            }
            if (!getTags().isEmpty()) {
                client.tagResource(r -> r.resourceArn(getLogGroupArn()).tags(getTags()));
            }
        }

        if (changedFieldNames.contains("index-policy")) {
            if (getIndexPolicy() != null) {
                client.putIndexPolicy(
                    PutIndexPolicyRequest.builder()
                        .logGroupIdentifier(getLogGroupArn())
                        .policyDocument(getIndexPolicy())
                        .build()
                );
            } else if (currentResource.getIndexPolicySource() != null) {
                try {
                    client.deleteIndexPolicy(
                        DeleteIndexPolicyRequest.builder()
                            .logGroupIdentifier(getLogGroupArn())
                            .build()
                    );
                } catch (Exception e) {
                    // Continue
                }
            }
        }

        if (changedFieldNames.contains("data-protection-policy")) {
            if (getDataProtectionPolicy() != null) {
                client.putDataProtectionPolicy(
                    PutDataProtectionPolicyRequest.builder()
                        .logGroupIdentifier(getLogGroupArn())
                        .policyDocument(getDataProtectionPolicy())
                        .build()
                );
            } else if (currentResource.getDataProtectionPolicy() != null) {
                try {
                    client.deleteDataProtectionPolicy(
                        DeleteDataProtectionPolicyRequest.builder()
                            .logGroupIdentifier(getLogGroupArn())
                            .build()
                    );
                } catch (Exception e) {
                    // Continue
                }
            }
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        CloudWatchLogsClient client = createClient(CloudWatchLogsClient.class);

        // Delete policies
        if (getIndexPolicySource() != null) {
            try {
                client.deleteIndexPolicy(
                    DeleteIndexPolicyRequest.builder()
                        .logGroupIdentifier(getLogGroupArn())
                        .build()
                );
            } catch (Exception e) {
                // Continue
            }
        }

        if (getDataProtectionPolicy() != null) {
            try {
                client.deleteDataProtectionPolicy(
                    DeleteDataProtectionPolicyRequest.builder()
                        .logGroupIdentifier(getLogGroupArn())
                        .build()
                );
            } catch (Exception e) {
                // Continue
            }
        }

        // Delete log group
        client.deleteLogGroup(
            DeleteLogGroupRequest.builder()
                .logGroupName(getLogGroupName())
                .build()
        );
    }
}
