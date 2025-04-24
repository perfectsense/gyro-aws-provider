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

package gyro.aws.kms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psddev.dari.util.CompactMap;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.*;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * Replicate a Multi-Region KMS Key to another region. `See KMS Replication <https://docs.aws.amazon.com/kms/latest/APIReference/API_ReplicateKey.html>`_.
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::kms-replicate-key kms-replicate-example
 *         key-id: "arn:aws:kms:us-east-1:123456789012:key/mrk-*"
 *         alias: ["alias/kms-example"]
 *         replica-region: "us-west-1"
 *         bypass-policy-lockout-safety-check: "false"
 *         description: "sample kms key"
 *         policy: "gyro-providers/gyro-aws-provider/examples/kms/kms-policy.json"
 *         tags: {
 *             Name: "kms-example"
 *         }
 *     end
 */

@Type("kms-replicate-key")
public class KmsReplicateKeyResource extends AwsResource implements Copyable<KeyMetadata> {
    private Set<String> aliases;
    private String id;
    private String arn;
    private Boolean enabled;
    private Boolean bypassPolicyLockoutSafetyCheck;
    private String replicaRegion;
    private String description;
    private String primaryKeyArn;
    private String keyManager;
    private KeySpec keySpec;
    private String keyState;
    private String keyUsage;
    private String origin;
    private String policy;
    private Map<String, String> tags;

    /**
     * The set of aliases associated with the key.
     */
    @Required
    @Updatable
    public Set<String> getAliases() {
        if (aliases == null) {
            aliases = new LinkedHashSet<>();
        }

        return aliases;
    }

    public void setAliases(Set<String> aliases) {
        this.aliases = aliases;
    }

    /**
     * The arn of the primary KMS key to replicate.
     */
    @Required
    public String getPrimaryKeyArn() {
        return primaryKeyArn;
    }

    public void setPrimaryKeyArn(String primaryKeyArn) {
        this.primaryKeyArn = primaryKeyArn;
    }

    /**
     * The region to replicate the primary KMS Key.
     */
    public String getReplicaRegion() {
        return replicaRegion;
    }

    public void setReplicaRegion(String replicaRegion) {
        this.replicaRegion = replicaRegion;
    }

    public Boolean getBypassPolicyLockoutSafetyCheck() {
        return bypassPolicyLockoutSafetyCheck;
    }

    public void setBypassPolicyLockoutSafetyCheck(Boolean bypassPolicyLockoutSafetyCheck) {
        this.bypassPolicyLockoutSafetyCheck = bypassPolicyLockoutSafetyCheck;
    }

    /**
     * The description of the key.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The arn for this key.
     */
    @Id
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * Determines whether the key is enabled. Defaults to ``true``.
     */
    @Updatable
    public Boolean getEnabled() {
        if (enabled == null) {
            enabled = true;
        }
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * The id for this key.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The manager of the key, either AWS or customer.
     */
    @Output
    public String getKeyManager() {
        return keyManager;
    }

    public void setKeyManager(String keyManager) {
        this.keyManager = keyManager;
    }

    /**
     * The current state of the key.
     */
    @Output
    public String getKeyState() {
        return keyState;
    }

    public void setKeyState(String keyState) {
        this.keyState = keyState;
    }

    /**
     * The usage of the key. Defaults to ``ENCRYPT_DECRYPT``.
     */
    @Required
    public String getKeyUsage() {
        if (keyUsage == null) {
            keyUsage = "ENCRYPT_DECRYPT";
        }

        return keyUsage;
    }

    public void setKeyUsage(String keyUsage) {
        this.keyUsage = keyUsage;
    }

    /**
     * The spec for the key.
     */
    @ValidStrings({"RSA_2048", "RSA_3072", "RSA_4096", "ECC_NIST_P256", "ECC_NIST_P384", "ECC_NIST_P521", "ECC_SECG_P256K1", "SYMMETRIC_DEFAULT", "HMAC_224", "HMAC_256", "HMAC_384", "HMAC_512", "SM2"})
    public KeySpec getKeySpec() {
        return keySpec;
    }

    public void setKeySpec(KeySpec keySpec) {
        this.keySpec = keySpec;
    }

    /**
     * The source of the key material. Defaults to ``AWS_KMS``.
     */
    public String getOrigin() {
        if (origin == null) {
            origin = "AWS_KMS";
        }

        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    /**
     * The path to the policy associated with the key.
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
     * The tags associated with the key.
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new CompactMap<>();
        }
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        if (this.tags != null && tags != null) {
            this.tags.putAll(tags);

        } else {
            this.tags = tags;
        }
    }

    @Override
    public void copyFrom(KeyMetadata keyMetadata) {
        setArn(keyMetadata.arn());
        setId(keyMetadata.keyId());
        setKeyManager(keyMetadata.keyManagerAsString());
        setKeyState(keyMetadata.keyStateAsString());
        setKeyUsage(keyMetadata.keyUsageAsString());
        setOrigin(keyMetadata.originAsString());
        setDescription(keyMetadata.description());
        setPrimaryKeyArn(keyMetadata.multiRegionConfiguration().primaryKey().arn());

        ListAliasesResponse aliasResponse;
        GetKeyPolicyResponse policyResponse;
        KmsClient client = createClient(KmsClient.class, getReplicaRegion(), null);

        getAliases().clear();
        aliasResponse = client.listAliases(r -> r.keyId(getId()));
        if (aliasResponse != null) {
            for (AliasListEntry alias : aliasResponse.aliases()) {
                getAliases().add(alias.aliasName());
            }
        }

        policyResponse = client.getKeyPolicy(r -> r.keyId(getId()).policyName("default"));
        if (policyResponse != null) {
            setPolicy(policyResponse.policy());
        }
    }

    @Override
    public boolean refresh() {
        KmsClient client = createClient(KmsClient.class, getReplicaRegion(), null);
        try {
            DescribeKeyResponse keyResponse = client.describeKey(r -> r.keyId(getId()));
            KeyMetadata keyMetadata = keyResponse.keyMetadata();

            if (!keyMetadata.keyStateAsString().equals("PENDING_DELETION")) {
                this.copyFrom(keyMetadata);
            }

            return true;

        } catch (NotFoundException ex) {
            return false;
        }
    }

    @Override
    public void create(GyroUI ui, State state) {
        KmsClient client = createClient(KmsClient.class);
        try {

            if (getPrimaryKeyArn().isEmpty()) {
                throw new GyroException("Primary Key ARN is missing. Must be provided.");
            }

            if (getReplicaRegion().isEmpty()) {
                throw new GyroException("Replica region is missing. Must be provided.");
            }

            if (getAliases().isEmpty()) {
                throw new GyroException("At least one alias must be provided.");
            }

            List<String> newList = getAliases().stream()
                .distinct()
                .collect(Collectors.toList());

            if (newList.size() != getAliases().size()) {
                throw new GyroException("Duplicate aliases are not allowed in the same region");
            }

            DescribeKeyResponse primaryKeyResponse = client.describeKey(r -> r.keyId(getPrimaryKeyArn()));

            ReplicateKeyResponse replicateKeyResponse = client.replicateKey(r -> r.keyId(getPrimaryKeyArn())
                .replicaRegion(getReplicaRegion())
                .bypassPolicyLockoutSafetyCheck(getBypassPolicyLockoutSafetyCheck() != null ? getBypassPolicyLockoutSafetyCheck() : null)
                .description(getDescription() != null ? getDescription() : primaryKeyResponse.keyMetadata().description())
                .policy(getPolicy() != null ? getPolicy() : null)
                .tags(toTag())
            );

            setArn(replicateKeyResponse.replicaKeyMetadata().arn());
            setId(replicateKeyResponse.replicaKeyMetadata().keyId());
            setKeyManager(replicateKeyResponse.replicaKeyMetadata().keyManagerAsString());
            setKeyState(replicateKeyResponse.replicaKeyMetadata().keyStateAsString());
            setDescription(replicateKeyResponse.replicaKeyMetadata().description());
            setPrimaryKeyArn(replicateKeyResponse.replicaKeyMetadata().multiRegionConfiguration().primaryKey().arn());

            state.save();

            if (getAliases() != null) {
                KmsClient replicaRegionClient = createClient(KmsClient.class, getReplicaRegion(), null);
                for (String alias : getAliases()) {
                    replicaRegionClient.createAlias(r -> r.aliasName(alias).targetKeyId(getId()));
                }
            }
        } catch (AlreadyExistsException ex) {
            delete(ui, state);
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        KmsClient client = createClient(KmsClient.class, getReplicaRegion(), null);
        try {
            KmsReplicateKeyResource currentResource = (KmsReplicateKeyResource) current;

            if (getEnabled() && !currentResource.getEnabled()) {
                client.enableKey(r -> r.keyId(getId()));
            } else if (!getEnabled() && currentResource.getEnabled()) {
                client.disableKey(r -> r.keyId(getId()));
            }

            client.updateKeyDescription(r -> r.description(getDescription()).keyId(getId()));
            client.tagResource(r -> r.tags(toTag()).keyId(getId()));

            List<String> aliasSubtractions = new ArrayList<>(currentResource.getAliases());
            aliasSubtractions.removeAll(getAliases());
            aliasSubtractions.forEach(alias -> client.deleteAlias(r -> r.aliasName(alias)));
            List<String> aliasAdditions = new ArrayList<>(getAliases());
            aliasAdditions.removeAll(currentResource.getAliases());
            aliasAdditions.forEach(alias -> client.createAlias(r -> r.aliasName(alias).targetKeyId(getId())));

            client.tagResource(r -> r.tags(toTag())
                .keyId(getArn())
            );

            client.updateKeyDescription(r -> r.description(getDescription())
                .keyId(getId()));

            client.putKeyPolicy(r -> r.policy(getPolicy())
                .policyName("default")
                .keyId(getId()));
        } catch (AlreadyExistsException ex) {
            throw new GyroException(ex.getMessage());
        } catch (KmsInvalidStateException ex) {
            throw new GyroException("This key is pending deletion. This operation is not supported in this state");
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        KmsClient client = createClient(KmsClient.class, getReplicaRegion(), null);
        client.scheduleKeyDeletion(r -> r.keyId(getId()));
    }

    private List<Tag> toTag() {
        List<Tag> tag = new ArrayList<>();
        getTags().forEach((key, value) -> tag.add(Tag.builder().tagKey(key).tagValue(value).build()));
        return tag;
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
}
