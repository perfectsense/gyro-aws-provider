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

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::kms-replicate-key kms-replicate-example
 *         key-id: "arn:aws:kms:us-east-1:123456789012:key/mrk-*"
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

    private String keyId;
    private String replicaRegion;
    private Boolean bypassPolicyLockoutSafetyCheck;
    private String description;
    private String policy;
    private Map<String, String> tags;
    private String arn;
    private String id;
    private String keyManager;
    private String keyState;
    private String keyUsage;
    private KeySpec keySpec;
    private Boolean multiRegion;
    private String origin;

    /**
     * The arn of the primary KMS key to replicate.
     */
    @Required
    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    /**
     * The region to replicate the primary KMS Key.
     * @return
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
        setArn(keyMetadata.arn());
        setId(keyMetadata.keyId());
        setKeyManager(keyMetadata.keyManagerAsString());
        setKeyState(keyMetadata.keyStateAsString());
        setKeyUsage(keyMetadata.keyUsageAsString());
        setMultiRegion(keyMetadata.multiRegion());
        setOrigin(keyMetadata.originAsString());

        KmsClient client = createClient(KmsClient.class);

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

        if (getKeyId().isEmpty()) {
            throw new GyroException("Key id is missing. Must be provided.");
        }

        if (getReplicaRegion().isEmpty()) {
            throw new GyroException("Replica region is missing. Must be provided.");
        }

        try {
            DescribeKeyResponse primaryKeyResponse = client.describeKey(r -> r.keyId(getKeyId()));

            ReplicateKeyResponse response = client.replicateKey(r -> r.keyId(getKeyId())
                .replicaRegion(getReplicaRegion())
                .description(getDescription() != null ? getDescription() : null)
                .policy(getPolicy() != null ? getPolicy() : null)
                .tags(toTag())
            );

            setArn(response.replicaKeyMetadata().arn());
            setId(response.replicaKeyMetadata().keyId());
            setKeyManager(response.replicaKeyMetadata().keyManagerAsString());
            setKeyState(response.replicaKeyMetadata().keyStateAsString());
            setDescription(response.replicaKeyMetadata().description());

            state.save();

        } catch (AlreadyExistsException ex) {
            delete(ui, state);
            throw new GyroException(ex.getMessage());
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        KmsClient client = createClient(KmsClient.class);
        KmsReplicateKeyResource currentResource = (KmsReplicateKeyResource) current;

        try {
            client.tagResource(r -> r.tags(toTag()).keyId(getArn()));
            client.updateKeyDescription(r -> r.description(getDescription()).keyId(getId()));

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
            throw new GyroException(String.format("Could not read the json `%s`",policy),ex);
        }
    }
}
