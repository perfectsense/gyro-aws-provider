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

package gyro.aws.secretsmanager;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.kms.KmsKeyResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.Required;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.DescribeSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;
import software.amazon.awssdk.services.secretsmanager.model.RotationRulesType;
import software.amazon.awssdk.services.secretsmanager.model.Tag;
import software.amazon.awssdk.services.secretsmanager.model.TagResourceRequest;
import software.amazon.awssdk.services.secretsmanager.model.UntagResourceRequest;
import software.amazon.awssdk.services.secretsmanager.model.UpdateSecretRequest;

/**
 * Creates a Secret with the Name, Description, and Tags.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::secret secret
 *         name: 'secret-example'
 *         description: 'secret-example-description'
 *         tags: {
 *             "secret-example-tag" : "secret-example-tag-value"
 *         }
 *     end
 */
@Type("secret")
public class SecretResource extends AwsResource implements Copyable<DescribeSecretResponse> {

    private String clientRequestToken;
    private String description;
    private KmsKeyResource kmsKey;
    private String secretBinary;
    private String secretString;
    private Map<String, String> tags;

    // Read-only
    private String arn;
    private String deletedDate;
    private Boolean forceDeleteWithoutRecovery;
    private String lastAccessedDate;
    private String lastChangedDate;
    private String lastRotatedDate;
    private String name;
    private String owningService;
    private Long recoveryWindowInDays;
    private Boolean rotationEnabled;
    private String rotationLambdaARN;
    private RotationRulesType rotationRules;
    private String versionId;
    private Map<String, List<String>> versionIdsToStages;

    /**
     * Specifies a unique identifier for the new version that helps ensure idempotency. See `Client Request Token Info
     * <https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_UpdateSecret.html#SecretsManager-UpdateSecret-request-ClientRequestToken/>`_.
     */
    @Updatable
    public String getClientRequestToken() {
        return clientRequestToken;
    }

    public void setClientRequestToken(String clientRequestToken) {
        this.clientRequestToken = clientRequestToken;
    }

    /**
     * The description of the secret. See `Description Info
     * <https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_UpdateSecret.html#SecretsManager-UpdateSecret-request-Description/>`_.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Specifies an updated ARN or alias of the AWS KMS customer master key (CMK) to be used to encrypt the protected
     * text in new versions of this secret. See `Kms Key Id Info <Specifies an updated ARN or alias of the AWS KMS
     * customer master key (CMK) to be used to encrypt the protected text in new versions of this secret./>`_.
     */
    @Updatable
    public KmsKeyResource getKmsKey() {
        return kmsKey;
    }

    public void setKmsKey(KmsKeyResource kmsKey) {
        this.kmsKey = kmsKey;
    }

    /**
     * Specifies updated binary data that you want to encrypt and store in the new version of the secret. See `Secret
     * Binary Info <https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_UpdateSecret.html#SecretsManager-UpdateSecret-request-SecretBinary/>`_.
     */
    @Updatable
    public String getSecretBinary() {
        return secretBinary;
    }

    public void setSecretBinary(String secretBinary) {
        this.secretBinary = secretBinary;
    }

    /**
     * Specifies updated text data that you want to encrypt and store in this new version of the secret. See `Secret
     * String Info <https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_UpdateSecret.html#SecretsManager-UpdateSecret-request-SecretString/>`_.
     */
    @Updatable
    public String getSecretString() {
        return secretString;
    }

    public void setSecretString(String secretString) {
        this.secretString = secretString;
    }

    /**
     * Specifies a list of tags that are attached to the secret
     */
    @Updatable
    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The Amazon Resource Name (ARN) of the secret. This is unique.
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
     * This value exists if the secret is scheduled for deletion and specifies the date.
     */
    @Output
    public String getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(String deletedDate) {
        this.deletedDate = deletedDate;
    }

    /**
     * Specifies that the secret is to be deleted without any recovery window. Cannot use both this parameter and the
     * RecoveryWindowInDays parameter in the same API call. See `Force Delete Without Recovery Info
     * <https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_DeleteSecret.html#SecretsManager-DeleteSecret-request-ForceDeleteWithoutRecovery/>`_.
     */
    @ConflictsWith("recovery-window-in-days")
    public Boolean getForceDeleteWithoutRecovery() {
        return forceDeleteWithoutRecovery;
    }

    public void setForceDeleteWithoutRecovery(Boolean forceDeleteWithoutRecovery) {
        this.forceDeleteWithoutRecovery = forceDeleteWithoutRecovery;
    }

    /**
     * The last date that this secret was accessed.
     */
    @Output
    public String getLastAccessedDate() {
        return lastAccessedDate;
    }

    public void setLastAccessedDate(String lastAccessedDate) {
        this.lastAccessedDate = lastAccessedDate;
    }

    /**
     * The last date and time that this secret was modified in any way.
     */
    @Output
    public String getLastChangedDate() {
        return lastChangedDate;
    }

    public void setLastChangedDate(String lastChangedDate) {
        this.lastChangedDate = lastChangedDate;
    }

    /**
     * The most recent date and time that the Secrets Manager rotation process was successfully completed.
     */
    @Output
    public String getLastRotatedDate() {
        return lastRotatedDate;
    }

    public void setLastRotatedDate(String lastRotatedDate) {
        this.lastRotatedDate = lastRotatedDate;
    }

    /**
     * The name of the secret. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The name of the owning service.
     */
    @Output
    public String getOwningService() {
        return owningService;
    }

    public void setOwningService(String owningService) {
        this.owningService = owningService;
    }

    /**
     * Specifies the number of days that Secrets Manager waits before it can delete the secret. Cannot use both this
     * parameter and the ForceDeleteWithoutRecovery parameter in the same API call.
     */
    @ConflictsWith("force-delete-without-recovery")
    public Long getRecoveryWindowInDays() {
        return recoveryWindowInDays;
    }

    public void setRecoveryWindowInDays(Long recoveryWindowInDays) {
        this.recoveryWindowInDays = recoveryWindowInDays;
    }

    /**
     * Specifies whether automatic rotation is enabled for this secret.
     */
    @Output
    public Boolean getRotationEnabled() {
        return rotationEnabled;
    }

    public void setRotationEnabled(Boolean rotationEnabled) {
        this.rotationEnabled = rotationEnabled;
    }

    /**
     * Specifies the ARN of a Lambda function that's invoked by Secrets Manager to rotate the secret either
     * automatically per the schedule or manually by a call to RotateSecret.
     */
    @Output
    public String getRotationLambdaARN() {
        return rotationLambdaARN;
    }

    public void setRotationLambdaARN(String rotationLambdaARN) {
        this.rotationLambdaARN = rotationLambdaARN;
    }

    /**
     * Specifies a structure that contains the rotation configuration for this secret.
     */
    @Output
    public RotationRulesType getRotationRules() {
        return rotationRules;
    }

    public void setRotationRules(RotationRulesType rotationRules) {
        this.rotationRules = rotationRules;
    }

    /**
     * The unique identifier associated with the version of the generated secret.
     */
    @Output
    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    /**
     * A list of all of the currently assigned VersionStage staging labels and the VersionId that each is attached to.
     * Staging labels are used to keep track of the different versions during the rotation process.
     */
    @Output
    public Map<String, List<String>> getVersionIdsToStages() {
        return versionIdsToStages;
    }

    public void setVersionIdsToStages(Map<String, List<String>> versionIdsToStages) {
        this.versionIdsToStages = versionIdsToStages;
    }

    @Override
    public boolean refresh() {
        SecretsManagerClient client = createClient(SecretsManagerClient.class);

        try {
            DescribeSecretResponse response = client.describeSecret(r -> r.secretId(getArn()));

            if (response == null) {
                return false;
            }

            copyFrom(response);
        } catch (ResourceNotFoundException ex) {
            // No Resource found
            return false;
        }

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        SecretsManagerClient client = createClient(SecretsManagerClient.class);

        CreateSecretRequest request = CreateSecretRequest.builder()
            .clientRequestToken(getClientRequestToken())
            .description(getDescription())
            .kmsKeyId(getKmsKey() != null ? getKmsKey().getId() : null)
            .name(getName())
            .secretBinary(getSecretBinary() != null ? SdkBytes.fromUtf8String(getSecretBinary()) : null)
            .secretString(getSecretString())
            .tags(convertTags(getTags()))
            .build();

        CreateSecretResponse response = client.createSecret(request);

        setVersionId(response.versionId());
        copyFrom(client.describeSecret(r -> r.secretId(response.arn())));
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        SecretsManagerClient client = createClient(SecretsManagerClient.class);

        UpdateSecretRequest updateRequest = UpdateSecretRequest.builder()
            .secretId(getArn())
            .clientRequestToken(getClientRequestToken())
            .description(getDescription())
            .kmsKeyId(getKmsKey() != null ? getKmsKey().getId() : null)
            .secretBinary(getSecretBinary() != null ? SdkBytes.fromUtf8String(getSecretBinary()) : null)
            .secretString(getSecretString())
            .build();

        if (changedFieldNames.contains("tags")) {
            SecretResource oldResource = (SecretResource) current;
            Map<String, String> oldTags = oldResource.getTags();

            if (!oldTags.isEmpty() || !getTags().isEmpty()) {
                MapDifference<String, String> diff = Maps.difference(oldTags, getTags());

                TagResourceRequest tagRequest = null;
                UntagResourceRequest untagRequest = null;

                if (getTags().isEmpty()) {
                    untagRequest = UntagResourceRequest.builder()
                        .secretId(getArn())
                        .tagKeys(diff.entriesOnlyOnLeft().keySet())
                        .build();
                } else if (diff.entriesOnlyOnLeft().isEmpty()) {
                    tagRequest = TagResourceRequest.builder()
                        .secretId(getArn())
                        .tags(convertTags(getTags()))
                        .build();
                } else {
                    tagRequest = TagResourceRequest.builder()
                        .secretId(getArn())
                        .tags(convertTags(getTags()))
                        .build();

                    untagRequest = UntagResourceRequest.builder()
                        .secretId(getArn())
                        .tagKeys(diff.entriesOnlyOnLeft().keySet())
                        .build();
                }

                if (tagRequest != null) {
                    client.tagResource(tagRequest);
                }

                if (untagRequest != null) {
                    client.untagResource(untagRequest);
                }
            }
        }
        client.updateSecret(updateRequest);
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        SecretsManagerClient client = createClient(SecretsManagerClient.class);

        client.deleteSecret(r -> r.secretId(getArn())
            .forceDeleteWithoutRecovery(getForceDeleteWithoutRecovery())
            .recoveryWindowInDays(getRecoveryWindowInDays()));
    }

    @Override
    public void copyFrom(DescribeSecretResponse model) {
        setArn(model.arn());
        setDeletedDate(model.deletedDate() != null ? model.deletedDate().toString() : null);
        setDescription(model.description());
        setKmsKey(findById(KmsKeyResource.class, model.kmsKeyId()));
        setLastAccessedDate(model.lastAccessedDate() != null ? model.lastAccessedDate().toString() : null);
        setLastChangedDate(model.lastAccessedDate() != null ? model.lastChangedDate().toString() : null);
        setLastRotatedDate(model.lastRotatedDate() != null ? model.lastRotatedDate().toString() : null);
        setName(model.name());
        setOwningService(model.owningService());
        setRotationEnabled(model.rotationEnabled());
        setRotationLambdaARN(model.rotationLambdaARN());
        setRotationRules(model.rotationRules());
        setTags(model.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value)));
        setVersionIdsToStages(model.versionIdsToStages());
    }

    private List<Tag> convertTags(Map<String, String> tags) {
        return tags.entrySet().stream()
            .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
            .collect(Collectors.toList());
    }
}