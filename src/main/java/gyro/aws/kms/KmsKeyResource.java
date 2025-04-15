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
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import com.psddev.dari.util.CompactMap;

import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.AliasListEntry;
import software.amazon.awssdk.services.kms.model.AlreadyExistsException;
import software.amazon.awssdk.services.kms.model.CreateKeyResponse;
import software.amazon.awssdk.services.kms.model.DescribeKeyResponse;
import software.amazon.awssdk.services.kms.model.GetKeyPolicyResponse;
import software.amazon.awssdk.services.kms.model.KeyMetadata;
import software.amazon.awssdk.services.kms.model.KeySpec;
import software.amazon.awssdk.services.kms.model.KmsException;
import software.amazon.awssdk.services.kms.model.KmsInvalidStateException;
import software.amazon.awssdk.services.kms.model.ListAliasesResponse;
import software.amazon.awssdk.services.kms.model.NotFoundException;
import software.amazon.awssdk.services.kms.model.Tag;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::kms-key kms-example
 *         aliases: ["alias/kmsExample", "alias/kmsSecondExample"]
 *         bypass-policy-lockout-safety-check: "false"
 *         description: "sample kms key"
 *         enabled: "true"
 *         key-manager: "CUSTOMER"
 *         key-rotation: "false"
 *         key-usage: "ENCRYPT_DECRYPT"
 *         multi-region: "false"
 *         origin: "AWS_KMS"
 *         pending-window: "7"
 *         policy: "gyro-providers/gyro-aws-provider/examples/kms/kms-policy.json"
 *         tags: {
 *             Name: "kms-example"
 *         }
 *     end
 */

@Type("kms-key")
public class KmsKeyResource extends AwsResource implements Copyable<KeyMetadata> {

    private Set<String> aliases;
    private Boolean bypassPolicyLockoutSafetyCheck;
    private String description;
    private Boolean enabled;
    private String arn;
    private String id;
    private String keyManager;
    private Boolean keyRotation;
    private String keyState;
    private String keyUsage;
    private KeySpec keySpec;
    private Boolean multiRegion;
    private String origin;
    private String pendingWindow;
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
     * Determines whether to bypass the key policy lockout safety check. Defaults to false.
     */
    public Boolean getBypassPolicyLockoutSafetyCheck() {
        if (bypassPolicyLockoutSafetyCheck == null) {
            bypassPolicyLockoutSafetyCheck = false;
        }

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
     * Determines whether the key is enabled. Defaults to ``enabled``.
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
     * Determines whether the backing key is rotated each year. Defaults to ``false``.
     */
    @Updatable
    public Boolean getKeyRotation() {
        if (keyRotation == null) {
            keyRotation = false;
        }

        return keyRotation;
    }

    public void setKeyRotation(Boolean keyRotation) {
        this.keyRotation = keyRotation;
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
    @ValidStrings({"RSA_2048","RSA_3072","RSA_4096","ECC_NIST_P256","ECC_NIST_P384","ECC_NIST_P521","ECC_SECG_P256K1","SYMMETRIC_DEFAULT","HMAC_224","HMAC_256","HMAC_384","HMAC_512","SM2"})
    public KeySpec getKeySpec() {
        return keySpec;
    }

    public void setKeySpec(KeySpec keySpec) {
        this.keySpec = keySpec;
    }

    /**
     * The capability of cross-region replication of the key. Defaults to ``false``
     */
    public Boolean getMultiRegion() {
        if (multiRegion == null) {
            multiRegion = false;
        }
        return multiRegion;
    }

    public void setMultiRegion(Boolean multiRegion) {
        this.multiRegion = multiRegion;
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
     * The number of days until the key will be deleted. Defaults to 30.
     */
    public String getPendingWindow() {
        if (pendingWindow == null) {
            pendingWindow = "30";
        }

        return pendingWindow;
    }

    public void setPendingWindow(String pendingWindow) {
        this.pendingWindow = pendingWindow;
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
        setDescription(keyMetadata.description());
        setEnabled(keyMetadata.enabled());
        setArn(keyMetadata.arn());
        setId(keyMetadata.keyId());
        setKeyManager(keyMetadata.keyManagerAsString());
        setKeyState(keyMetadata.keyStateAsString());
        setKeyUsage(keyMetadata.keyUsageAsString());
        setMultiRegion(keyMetadata.multiRegion());
        setOrigin(keyMetadata.originAsString());

        KmsClient client = createClient(KmsClient.class);

        getAliases().clear();
        ListAliasesResponse aliasResponse = client.listAliases(r -> r.keyId(getId()));
        if (aliasResponse != null) {
            for (AliasListEntry alias : aliasResponse.aliases()) {
                getAliases().add(alias.aliasName());
            }
        }

        GetKeyPolicyResponse policyResponse = client.getKeyPolicy(r -> r.keyId(getId()).policyName("default"));
        if (policyResponse != null) {
            setPolicy(policyResponse.policy());
        }
    }

    @Override
    public boolean refresh() {
        KmsClient client = createClient(KmsClient.class);

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

        if (getAliases().isEmpty()) {
            throw new GyroException("At least one alias must be provided.");
        }

        List<String> newList = getAliases().stream()
                .distinct()
                .collect(Collectors.toList());

        if (newList.size() == getAliases().size()) {

            CreateKeyResponse response = client.createKey(
                r -> r.bypassPolicyLockoutSafetyCheck(getBypassPolicyLockoutSafetyCheck())
                            .description(getDescription())
                            .keyUsage(getKeyUsage())
                            .origin(getOrigin())
                            .multiRegion(getMultiRegion())
                            .policy(getPolicy())
                            .keySpec(getKeySpec() != null ? getKeySpec() : KeySpec.SYMMETRIC_DEFAULT)
                            .tags(toTag())
            );

            setArn(response.keyMetadata().arn());
            setId(response.keyMetadata().keyId());
            setKeyManager(response.keyMetadata().keyManagerAsString());
            setKeyState(response.keyMetadata().keyStateAsString());

            state.save();

            try {
                if (getAliases() != null) {
                    for (String alias : getAliases()) {
                        client.createAlias(r -> r.aliasName(alias).targetKeyId(getId()));
                    }
                }

            } catch (AlreadyExistsException ex) {
                delete(ui, state);
                throw new GyroException(ex.getMessage());
            }

            if (getKeyRotation() != null && getKeyRotation()) {
                client.enableKeyRotation(r -> r.keyId(getId()));
            }

            if (getEnabled() != null && !getEnabled()) {
                client.disableKey(r -> r.keyId(getId()));
            }
        } else {
            throw new GyroException("Duplicate aliases are not allowed in the same region");
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        KmsClient client = createClient(KmsClient.class);
        KmsKeyResource currentResource = (KmsKeyResource) current;

        try {
            if (getEnabled() && !currentResource.getEnabled()) {
                client.enableKey(r -> r.keyId(getId()));
            } else if (!getEnabled() && currentResource.getEnabled()) {
                client.disableKey(r -> r.keyId(getId()));
            }
        } catch (KmsInvalidStateException ex) {
            throw new GyroException("This key is either pending import or pending deletion. It must be "
                    + "disabled or enabled to perform this operation");
        }

        try {
            if (getKeyRotation() && !currentResource.getKeyRotation()) {
                client.enableKeyRotation(r -> r.keyId(getId()));
            } else if (!getKeyRotation() && currentResource.getKeyRotation()) {
                client.disableKeyRotation(r -> r.keyId(getId()));
            }
        } catch (KmsException ex) {
            throw new GyroException("This key must be enabled to update key rotation.");
        }

        try {
            try {
                List<String> aliasSubtractions = new ArrayList<>(currentResource.getAliases());
                aliasSubtractions.removeAll(getAliases());

                aliasSubtractions.forEach(alias -> client.deleteAlias(r -> r.aliasName(alias)));

                List<String> aliasAdditions = new ArrayList<>(getAliases());
                aliasAdditions.removeAll(currentResource.getAliases());

                aliasAdditions.forEach(alias -> client.createAlias(r -> r.aliasName(alias).targetKeyId(getId())));

            } catch (AlreadyExistsException ex) {
                throw new GyroException(ex.getMessage());
            }

            client.tagResource(r -> r.tags(toTag())
                    .keyId(getArn())
            );

            client.updateKeyDescription(r -> r.description(getDescription())
                    .keyId(getId()));

        } catch (KmsInvalidStateException ex) {
            throw new GyroException("This key is pending deletion. This operation is not supported in this state");
        }

        client.putKeyPolicy(r -> r.policy(getPolicy())
                .policyName("default")
                .keyId(getId()));
    }

    @Override
    public void delete(GyroUI ui, State state) {
        KmsClient client = createClient(KmsClient.class);
        client.scheduleKeyDeletion(r -> r.keyId(getId()).pendingWindowInDays(Integer.valueOf(getPendingWindow())));
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
            throw new GyroException(String.format("Could not read the json `%s`",policy),ex);
        }
    }
}
