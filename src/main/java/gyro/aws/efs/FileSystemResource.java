/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws.efs;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.kms.KmsKeyResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.DependsOn;
import gyro.core.validation.Min;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.efs.EfsClient;
import software.amazon.awssdk.services.efs.model.CreateFileSystemRequest;
import software.amazon.awssdk.services.efs.model.CreateFileSystemResponse;
import software.amazon.awssdk.services.efs.model.EfsException;
import software.amazon.awssdk.services.efs.model.FileSystemDescription;
import software.amazon.awssdk.services.efs.model.LifeCycleState;
import software.amazon.awssdk.services.efs.model.PerformanceMode;
import software.amazon.awssdk.services.efs.model.PolicyNotFoundException;
import software.amazon.awssdk.services.efs.model.Tag;
import software.amazon.awssdk.services.efs.model.ThroughputMode;
import software.amazon.awssdk.services.efs.model.UpdateFileSystemRequest;
import software.amazon.awssdk.utils.IoUtils;

/**
 * Creates an EFS File System.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::efs-file-system efs-file-system
 *         encrypted: false
 *         performance-mode: GENERAL_PURPOSE
 *         throughput-mode: BURSTING
 *         policy: "policy.json"
 *
 *         lifecycle-policy
 *             transition-to-ia-rules: AFTER_30_DAYS
 *         end
 *
 *         backup-policy
 *             status: DISABLED
 *         end
 *
 *         tags: {
 *             "Name": "example-efs-file-system"
 *         }
 *     end
 */
@Type("efs-file-system")
public class FileSystemResource extends AwsResource implements Copyable<FileSystemDescription> {

    private Boolean encrypted;
    private KmsKeyResource key;
    private PerformanceMode performanceMode;
    private Double provisionedThroughput;
    private ThroughputMode throughputMode;
    private EfsBackupPolicy backupPolicy;
    private String policy;
    private List<EfsLifecyclePolicy> lifecyclePolicy;
    private Map<String, String> tags;

    // Output
    private String id;

    /**
     * The option which decides whether to create an encrypted file system.
     */
    public Boolean getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(Boolean encrypted) {
        this.encrypted = encrypted;
    }

    /**
     * The key to be used to protect the encrypted file system.
     */
    @DependsOn("encrypted")
    public KmsKeyResource getKey() {
        return key;
    }

    public void setKey(KmsKeyResource key) {
        this.key = key;
    }

    /**
     * The performance mode of the file system. Valid values are ``GENERAL_PURPOSE`` or `MAX_IO``.
     */
    public PerformanceMode getPerformanceMode() {
        return performanceMode;
    }

    public void setPerformanceMode(PerformanceMode performanceMode) {
        this.performanceMode = performanceMode;
    }

    /**
     * The throughput in MiB/s, that you want to provision for a file system that you're creating. Minimum value of ``0``.
     */
    @Min(0)
    @Updatable
    public Double getProvisionedThroughput() {
        return provisionedThroughput;
    }

    public void setProvisionedThroughput(Double provisionedThroughput) {
        this.provisionedThroughput = provisionedThroughput;
    }

    /**
     * The throughput mode for the file system to be created. Valid values are ``BURSTING`` or ``PROVISIONED``.
     */
    @Updatable
    public ThroughputMode getThroughputMode() {
        return throughputMode;
    }

    public void setThroughputMode(ThroughputMode throughputMode) {
        this.throughputMode = throughputMode;
    }

    /**
     * The backup policy for the file system.
     *
     * @subresource gyro.aws.efs.EfsBackupPolicy
     */
    @Updatable
    public EfsBackupPolicy getBackupPolicy() {
        return backupPolicy;
    }

    public void setBackupPolicy(EfsBackupPolicy backupPolicy) {
        this.backupPolicy = backupPolicy;
    }

    /**
     * The policy for the file system.
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
     * The LifeCycle policy for the file system.
     *
     * @subresource gyro.aws.efs.EfsLifecyclePolicy
     */
    @Updatable
    public List<EfsLifecyclePolicy> getLifecyclePolicy() {
        if (lifecyclePolicy == null) {
            lifecyclePolicy = new ArrayList<>();
        }

        return lifecyclePolicy;
    }

    public void setLifecyclePolicy(List<EfsLifecyclePolicy> lifecyclePolicy) {
        this.lifecyclePolicy = lifecyclePolicy;
    }

    /**
     * The tags for the file system.
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
     * The ID of the file system.
     */
    @Output
    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(FileSystemDescription model) {
        EfsClient client = createClient(EfsClient.class);

        setEncrypted(model.encrypted());
        setKey(findById(KmsKeyResource.class, model.kmsKeyId()));
        setPerformanceMode(model.performanceMode());
        setProvisionedThroughput(model.provisionedThroughputInMibps());
        setThroughputMode(model.throughputMode());
        setId(model.fileSystemId());

        try {
            setPolicy(client.describeFileSystemPolicy(r -> r.fileSystemId(getId())).policy());

        } catch (PolicyNotFoundException ex) {
            // ignore
        }

        EfsBackupPolicy efsBackupPolicy = newSubresource(EfsBackupPolicy.class);
        efsBackupPolicy.copyFrom(client.describeBackupPolicy(r -> r.fileSystemId(getId())).backupPolicy());
        setBackupPolicy(efsBackupPolicy);

        setLifecyclePolicy(client.describeLifecycleConfiguration(r -> r.fileSystemId(getId()))
            .lifecyclePolicies().stream().map(p -> {
                EfsLifecyclePolicy efsLifecyclePolicy = newSubresource(EfsLifecyclePolicy.class);
                efsLifecyclePolicy.copyFrom(p);

                return efsLifecyclePolicy;
            })
            .collect(Collectors.toList()));

        getTags().clear();
        for (Tag tag : model.tags()) {
            getTags().put(tag.key(), tag.value());
        }
    }

    @Override
    public boolean refresh() {
        EfsClient client = createClient(EfsClient.class);

        FileSystemDescription fileSystem = getFileSystem(client);

        if (fileSystem == null) {
            return false;
        }

        copyFrom(fileSystem);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        EfsClient client = createClient(EfsClient.class);

        CreateFileSystemRequest.Builder builder = CreateFileSystemRequest.builder().encrypted(getEncrypted())
            .performanceMode(getPerformanceMode())
            .provisionedThroughputInMibps(getProvisionedThroughput())
            .throughputMode(getThroughputMode())
            .tags(getTags().entrySet()
                .stream()
                .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
                .collect(Collectors.toList()));

        if (getKey() != null) {
            builder = builder.kmsKeyId(getKey().getId());
        }

        CreateFileSystemResponse fileSystemResponse = client.createFileSystem(builder.build());

        waitForAvailability(client);

        setId(fileSystemResponse.fileSystemId());
        state.save();

        if (getBackupPolicy() != null) {
            client.putBackupPolicy(r -> r.fileSystemId(getId()).backupPolicy(getBackupPolicy().toBackupPolicy()));
            waitForAvailability(client);
        }

        if (getPolicy() != null) {
            client.putFileSystemPolicy(r -> r.fileSystemId(getId()).policy(getPolicy()));
            waitForAvailability(client);
        }

        if (!getLifecyclePolicy().isEmpty()) {
            client.putLifecycleConfiguration(r -> r.fileSystemId(getId())
                .lifecyclePolicies(getLifecyclePolicy().stream()
                    .map(EfsLifecyclePolicy::toLifecyclePolicy)
                    .collect(Collectors.toList())));
            waitForAvailability(client);
        }
    }

    private void waitForAvailability(EfsClient client) {
        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> {
                FileSystemDescription fileSystem = getFileSystem(client);
                return (fileSystem != null && fileSystem.lifeCycleState().equals(LifeCycleState.AVAILABLE));
            });
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        EfsClient client = createClient(EfsClient.class);

        UpdateFileSystemRequest.Builder builder = UpdateFileSystemRequest.builder().fileSystemId(getId());

        if (changedFieldNames.contains("throughput-mode")) {
            builder = builder.throughputMode(getThroughputMode());
        }

        if (changedFieldNames.contains("provisioned-throughput")) {
            builder = builder.provisionedThroughputInMibps(getProvisionedThroughput());
        }

        client.updateFileSystem(builder.build());

        waitForAvailability(client);

        if (changedFieldNames.contains("backup-policy")) {
            client.putBackupPolicy(r -> r.fileSystemId(getId()).backupPolicy(getBackupPolicy().toBackupPolicy()));
            waitForAvailability(client);
        }

        if (changedFieldNames.contains("policy")) {
            client.putFileSystemPolicy(r -> r.fileSystemId(getId()).policy(getPolicy()));
            waitForAvailability(client);
        }

        if (changedFieldNames.contains("lifecycle-policy")) {
            client.putLifecycleConfiguration(r -> r.fileSystemId(getId())
                .lifecyclePolicies(getLifecyclePolicy().stream()
                    .map(EfsLifecyclePolicy::toLifecyclePolicy)
                    .collect(Collectors.toList())));
            waitForAvailability(client);
        }

        if (changedFieldNames.contains("tags")) {
            FileSystemResource currentResource = (FileSystemResource) current;

            if (changedFieldNames.contains("tags")) {
                if (!currentResource.getTags().isEmpty()) {
                    client.deleteTags(r -> r.fileSystemId(currentResource.getId())
                        .tagKeys(new ArrayList<String>(currentResource.getTags().keySet())));
                }

                client.tagResource(r -> r.resourceId(getId()).tags(getTags().entrySet().stream()
                    .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
                    .collect(Collectors.toList())));
            }
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        EfsClient client = createClient(EfsClient.class);

        client.deleteFileSystem(r -> r.fileSystemId(getId()));

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> {
                FileSystemDescription fileSystem = getFileSystem(client);
                return (fileSystem == null || fileSystem.lifeCycleState().equals(LifeCycleState.DELETED));
            });
    }

    private FileSystemDescription getFileSystem(EfsClient client) {
        FileSystemDescription fileSystem = null;

        try {
            List<FileSystemDescription> fileSystems = client.describeFileSystems(r -> r.fileSystemId(getId()))
                .fileSystems();
            if (!fileSystems.isEmpty()) {
                fileSystem = fileSystems.get(0);
            }

        } catch (EfsException ex) {
            if (!ex.awsErrorDetails().errorCode().equals("FileSystemNotFound")) {
                throw ex;
            }
        }

        return fileSystem;
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

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        ArrayList<ValidationError> errors = new ArrayList<>();

        if (!getThroughputMode().equals(ThroughputMode.PROVISIONED) && configuredFields.contains(
            "provisioned-throughput")) {
            errors.add(new ValidationError(
                this,
                null,
                "'provisioned-throughput' can only be set when 'throughput-mode' is set to 'PROVISIONED'."));
        }

        return errors;
    }
}
