package gyro.aws.kms;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import com.psddev.dari.util.CompactMap;

import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.AliasListEntry;
import software.amazon.awssdk.services.kms.model.AlreadyExistsException;
import software.amazon.awssdk.services.kms.model.CreateKeyResponse;
import software.amazon.awssdk.services.kms.model.DescribeKeyResponse;
import software.amazon.awssdk.services.kms.model.GetKeyPolicyResponse;
import software.amazon.awssdk.services.kms.model.KeyMetadata;
import software.amazon.awssdk.services.kms.model.KmsException;
import software.amazon.awssdk.services.kms.model.KmsInvalidStateException;
import software.amazon.awssdk.services.kms.model.ListAliasesResponse;
import software.amazon.awssdk.services.kms.model.NotFoundException;
import software.amazon.awssdk.services.kms.model.Tag;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
 *     aws::kms kms-example
 *         aliases: ["alias/kmsExample", "alias/kmsSecondExample"]
 *         bypass-policy-lockout-safety-check: "false"
 *         description: "sample kms key"
 *         enabled: "true"
 *         key-manager: "CUSTOMER"
 *         key-rotation: "false"
 *         key-usage: "ENCRYPT_DECRYPT"
 *         origin: "AWS_KMS"
 *         pending-window: "7"
 *         policy: "gyro-providers/gyro-aws-provider/examples/kms/kms-policy.json"
 *         tags: {
 *             Name: "kms-example"
 *         }
 * end
 */

@Type("kms")
public class KmsResource extends AwsResource implements Copyable<KeyMetadata> {

    private Set<String> aliases;
    private Boolean bypassPolicyLockoutSafetyCheck;
    private String description;
    private Boolean enabled;
    private String keyArn;
    private String keyId;
    private String keyManager;
    private Boolean keyRotation;
    private String keyState;
    private String keyUsage;
    private String origin;
    private String pendingWindow;
    private String policy;
    private String policyContents;
    private Map<String, String> tags;

    /**
     * The set of aliases associated with the key. (Required)
     */
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
     * Determines whether to bypass the key policy lockout safety check. Defaults to false. (Optional)
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
     * The description of the key. (Optional)
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Determines whether the key is enabled. Defaults to ``enabled``. (Optional)
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
     * Determines whether the backing key is rotated each year. Defaults to ``false``. (Optional)
     */
    @Updatable
    public Boolean getKeyRotation() {
        if (keyRotation == null) {
            keyRotation = true;
        }

        return keyRotation;
    }

    public void setKeyRotation(Boolean keyRotation) {
        this.keyRotation = keyRotation;
    }

    /**
     * The arn for this key.
     */
    @Output
    public String getKeyArn() {
        return keyArn;
    }

    public void setKeyArn(String keyArn) {
        this.keyArn = keyArn;
    }

    /**
     * The id for this key.
     */
    @Id
    @Output
    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
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
     * The usage of the key. The only valid value is ``ENCRYPT_DECRYPT``. Defaults to ``ENCRYPT_DECRYPT``. (Required)
     */
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
     * The source of the key material. Defaults to ``AWS_KMS``. (Optional)
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
     * The number of days until the key will be deleted. Defaults to 30. (Optional)
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
     * The path to the policy associated with the key. (Optional)
     */
    @Updatable
    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    /**
     * The path to the policy associated with the key.
     */
    @Updatable
    public String getPolicyContents() {
        if (policyContents != null) {
            return this.policyContents;
        } else {
            if (getPolicy() != null) {
                try {
                    String encode = new String(Files.readAllBytes(Paths.get(getPolicy())), StandardCharsets.UTF_8);
                    return formatPolicy(encode);
                } catch (Exception err) {
                    throw new GyroException(err.getMessage());
                }
            } else {
                return null;
            }
        }
    }

    public void setPolicyContents(String policyContents) {
        this.policyContents = policyContents;
    }

    /**
     * The tags associated with the key. (Optional)
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
        setKeyArn(keyMetadata.arn());
        setKeyId(keyMetadata.keyId());
        setKeyManager(keyMetadata.keyManagerAsString());
        setKeyState(keyMetadata.keyStateAsString());
        setKeyUsage(keyMetadata.keyUsageAsString());
        setOrigin(keyMetadata.originAsString());

        KmsClient client = createClient(KmsClient.class);

        getAliases().clear();
        ListAliasesResponse aliasResponse = client.listAliases(r -> r.keyId(getKeyId()));
        if (aliasResponse != null) {
            for (AliasListEntry alias : aliasResponse.aliases()) {
                getAliases().add(alias.aliasName());
            }
        }

        GetKeyPolicyResponse policyResponse = client.getKeyPolicy(r -> r.keyId(getKeyId()).policyName("default"));
        if (policyResponse != null) {
            setPolicyContents(formatPolicy(policyResponse.policy()));
        }
    }

    @Override
    public boolean refresh() {
        KmsClient client = createClient(KmsClient.class);

        try {
            DescribeKeyResponse keyResponse = client.describeKey(r -> r.keyId(getKeyId()));
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
    public void create() {
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
                            .policy(getPolicyContents())
                            .tags(toTag())
            );

            setKeyArn(response.keyMetadata().arn());
            setKeyId(response.keyMetadata().keyId());
            setKeyManager(response.keyMetadata().keyManagerAsString());
            setKeyState(response.keyMetadata().keyStateAsString());

            try {
                if (getAliases() != null) {
                    for (String alias : getAliases()) {
                        client.createAlias(r -> r.aliasName(alias).targetKeyId(getKeyId()));
                    }
                }

            } catch (AlreadyExistsException ex) {
                delete();
                throw new GyroException(ex.getMessage());
            }

            if (getKeyRotation() != null && getKeyRotation()) {
                client.enableKeyRotation(r -> r.keyId(getKeyId()));
            }

            if (getEnabled() != null && !getEnabled()) {
                client.disableKey(r -> r.keyId(getKeyId()));
            }
        } else {
            throw new GyroException("Duplicate aliases are not allowed in the same region");
        }
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        KmsClient client = createClient(KmsClient.class);
        KmsResource currentResource = (KmsResource) current;

        try {
            if (getEnabled() && !currentResource.getEnabled()) {
                client.enableKey(r -> r.keyId(getKeyId()));
            } else if (!getEnabled() && currentResource.getEnabled()) {
                client.disableKey(r -> r.keyId(getKeyId()));
            }
        } catch (KmsInvalidStateException ex) {
            throw new GyroException("This key is either pending import or pending deletion. It must be "
                    + "disabled or enabled to perform this operation");
        }

        try {
            if (getKeyRotation() && !currentResource.getKeyRotation()) {
                client.enableKeyRotation(r -> r.keyId(getKeyId()));
            } else if (!getKeyRotation() && currentResource.getKeyRotation()) {
                client.disableKeyRotation(r -> r.keyId(getKeyId()));
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

                aliasAdditions.forEach(alias -> client.createAlias(r -> r.aliasName(alias).targetKeyId(getKeyId())));

            } catch (AlreadyExistsException ex) {
                throw new GyroException(ex.getMessage());
            }

            client.tagResource(r -> r.tags(toTag())
                    .keyId(getKeyArn())
            );

            client.updateKeyDescription(r -> r.description(getDescription())
                    .keyId(getKeyId()));

        } catch (KmsInvalidStateException ex) {
            throw new GyroException("This key is pending deletion. This operation is not supported in this state");
        }

        client.putKeyPolicy(r -> r.policy(getPolicyContents())
                .policyName("default")
                .keyId(getKeyId()));
    }

    @Override
    public void delete() {
        KmsClient client = createClient(KmsClient.class);
        client.scheduleKeyDeletion(r -> r.keyId(getKeyId()).pendingWindowInDays(Integer.valueOf(getPendingWindow())));
    }

    @Override
    public String toDisplayString() {
        return "kms key with alias " + getAliases();
    }

    private List<Tag> toTag() {
        List<Tag> tag = new ArrayList<>();
        getTags().forEach((key, value) -> tag.add(Tag.builder().tagKey(key).tagValue(value).build()));
        return tag;
    }

    private String formatPolicy(String policy) {
        return policy != null ? policy.replaceAll(System.lineSeparator(), " ").replaceAll("\t", " ").trim().replaceAll(" ", "") : policy;
    }
}
