/*
 * Copyright 2021, Brightspot.
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

package gyro.aws.backup;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gyro.aws.AwsCredentials;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.kms.KmsKeyResource;
import gyro.aws.sns.TopicResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.DependsOn;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.backup.BackupClient;
import software.amazon.awssdk.services.backup.model.BackupException;
import software.amazon.awssdk.services.backup.model.BackupVaultEvent;
import software.amazon.awssdk.services.backup.model.CreateBackupVaultResponse;
import software.amazon.awssdk.services.backup.model.DescribeBackupVaultResponse;
import software.amazon.awssdk.services.backup.model.GetBackupVaultNotificationsResponse;
import software.amazon.awssdk.services.backup.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;
import software.amazon.awssdk.utils.IoUtils;

/**
 * Creates a backup vault.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::backup-vault destination-vault
 *         name: destination-vault
 *         access-policy: "vault-access-policy.json"
 *         encryption-key: $(aws::kms-key kms-example)
 *
 *         tags: {
 *             "example-tag": "example-value"
 *         }
 *     end
 */
@Type("backup-vault")
public class BackupVaultResource extends AwsResource implements Copyable<DescribeBackupVaultResponse> {

    private String name;
    private Map<String, String> tags;
    private String creatorRequestId;
    private KmsKeyResource encryptionKey;
    private String accessPolicy;
    private List<BackupVaultEvent> backupVaultEvents;
    private TopicResource snsTopic;

    // Read-only
    private String arn;

    /**
     * The name of a logical container where backups are stored.
     */
    @Required
    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Metadata that you can assign to help organize the resources that you create.
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
     * A unique string that identifies the request and allows failed requests to be retried without the risk of running the operation twice.
     */
    public String getCreatorRequestId() {
        return creatorRequestId;
    }

    public void setCreatorRequestId(String creatorRequestId) {
        this.creatorRequestId = creatorRequestId;
    }

    /**
     * The server-side encryption key that is used to protect your backups; for example, arn:aws:kms:us-west-2:111122223333:key/1234abcd-12ab-34cd-56ef-1234567890ab.
     */
    public KmsKeyResource getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(KmsKeyResource encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    /**
     * The backup vault access policy document in JSON format.
     */
    @Updatable
    public String getAccessPolicy() {
        accessPolicy = getProcessedPolicy(accessPolicy);

        return accessPolicy;
    }

    public void setAccessPolicy(String accessPolicy) {
        this.accessPolicy = accessPolicy;
    }

    /**
     * The list of events that indicate the status of jobs to back up resources to the backup vault.
     */
    @DependsOn("sns-topic")
    public List<BackupVaultEvent> getBackupVaultEvents() {
        if (backupVaultEvents == null) {
            backupVaultEvents = new ArrayList<>();
        }

        return backupVaultEvents;
    }

    public void setBackupVaultEvents(List<BackupVaultEvent> backupVaultEvents) {
        this.backupVaultEvents = backupVaultEvents;
    }

    /**
     * The SNS topic for a backup vault’s events.
     */
    @DependsOn("backup-vault-events")
    public TopicResource getSnsTopic() {
        return snsTopic;
    }

    public void setSnsTopic(TopicResource snsTopic) {
        this.snsTopic = snsTopic;
    }

    /**
     * The arn of the backup vault.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public void copyFrom(DescribeBackupVaultResponse model) {
        setName(model.backupVaultName());
        setArn(model.backupVaultArn());
        setCreatorRequestId(getCreatorRequestId());
        setEncryptionKey(findById(KmsKeyResource.class, model.encryptionKeyArn()));

        BackupClient client = createClient(BackupClient.class);
        setTags(client.listTags(r -> r.resourceArn(getArn())).tags());

        try {
            setAccessPolicy(client.getBackupVaultAccessPolicy(r -> r.backupVaultName(getName())).policy());

            GetBackupVaultNotificationsResponse notifications = client.getBackupVaultNotifications(r -> r.backupVaultName(
                getName()));
            setBackupVaultEvents(notifications.backupVaultEvents());
            setSnsTopic(findById(TopicResource.class, notifications.snsTopicArn()));

        } catch (ResourceNotFoundException ex) {
            // ignore
        }
    }

    @Override
    public boolean refresh() {
        BackupClient client = createClient(BackupClient.class);

        try {
            DescribeBackupVaultResponse response = client.describeBackupVault(r -> r.backupVaultName(getName()));

            copyFrom(response);

            return true;

        } catch (BackupException ex) {
            if (ex.awsErrorDetails().errorCode().contains("AccessDeniedException")) {
                return false;
            }

            throw ex;
        }
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        BackupClient client = createClient(BackupClient.class);

        CreateBackupVaultResponse backupVault = client.createBackupVault(r -> r.backupVaultName(getName())
            .backupVaultTags(getTags())
            .creatorRequestId(getCreatorRequestId())
            .encryptionKeyArn(getEncryptionKey().getArn()));

        setArn(backupVault.backupVaultArn());
        state.save();

        if (getAccessPolicy() != null) {
            client.putBackupVaultAccessPolicy(r -> r.backupVaultName(getName()).policy(getAccessPolicy()));
        }

        if (getSnsTopic() != null) {
            client.putBackupVaultNotifications(r -> r.backupVaultEvents(getBackupVaultEvents())
                .backupVaultName(getName())
                .snsTopicArn(getSnsTopic().getArn()));
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        BackupClient client = createClient(BackupClient.class);
        BackupVaultResource resource = (BackupVaultResource) current;

        if (changedFieldNames.contains("access-policy")) {
            client.deleteBackupVaultAccessPolicy(r -> r.backupVaultName(getName()));
            if (getAccessPolicy() != null) {
                client.putBackupVaultAccessPolicy(r -> r.backupVaultName(getName())
                    .policy(getAccessPolicy()));
            }
        }

        if (changedFieldNames.contains("tags")) {
            client.untagResource(r -> r.resourceArn(getArn()).tagKeyList(resource.getTags().keySet()));
            client.tagResource(r -> r.resourceArn(getArn()).tags(getTags()));
        }

        if (changedFieldNames.contains("sns-topic") || changedFieldNames.contains("backup-vault-events")) {
            client.deleteBackupVaultNotifications(r -> r.backupVaultName(getName()));
            client.putBackupVaultNotifications(r -> r.backupVaultEvents(getBackupVaultEvents())
                .backupVaultName(getName())
                .snsTopicArn(getSnsTopic().getArn()));
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        BackupClient client = createClient(BackupClient.class);

        client.deleteBackupVault(r -> r.backupVaultName(getName()));
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
            throw new GyroException(String.format("Could not read the json `%s`", policy), ex);
        }
    }

    protected String getArnFromName() {
        return String.format(
            "arn:aws:backup:%s:%s:backup-vault:%s",
            credentials(AwsCredentials.class).getRegion(),
            getAccountNumber(),
            getName());
    }

    private String getAccountNumber() {
        StsClient client = createClient(StsClient.class);
        GetCallerIdentityResponse response = client.getCallerIdentity();
        return response.account();
    }
}
