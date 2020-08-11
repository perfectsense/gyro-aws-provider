package gyro.aws.secrets;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.DescribeSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.RotationRulesType;
import software.amazon.awssdk.services.secretsmanager.model.Tag;
import software.amazon.awssdk.services.secretsmanager.model.TagResourceRequest;
import software.amazon.awssdk.services.secretsmanager.model.UntagResourceRequest;
import software.amazon.awssdk.services.secretsmanager.model.UpdateSecretRequest;

@Type("secret")
public class SecretResource extends AwsResource implements Copyable<DescribeSecretResponse> {

    private String arn;
    private String clientRequestToken;
    private String deletedDate;
    private String description;
    private Boolean forceDeleteWithoutRecovery;
    private String kmsKeyId;
    private String lastAccessedDate;
    private String lastChangedDate;
    private String lastRotatedDate;
    private String name;
    private String owningService;
    private Long recoveryWindowInDays;
    private Boolean rotationEnabled;
    private String rotationLambdaARN;
    private RotationRulesType rotationRules;
    private String secretBinary;
    private String secretString;
    private Map<String, String> tags;
    private String versionId;
    private Map<String, List<String>> versionIdsToStages;

    @Id
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Updatable
    public String getClientRequestToken() {
        return clientRequestToken;
    }

    public void setClientRequestToken(String clientRequestToken) {
        this.clientRequestToken = clientRequestToken;
    }

    public String getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(String deletedDate) {
        this.deletedDate = deletedDate;
    }

    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getForceDeleteWithoutRecovery() {
        return forceDeleteWithoutRecovery;
    }

    public void setForceDeleteWithoutRecovery(Boolean forceDeleteWithoutRecovery) {
        this.forceDeleteWithoutRecovery = forceDeleteWithoutRecovery;
    }

    @Updatable
    public String getKmsKeyId() {
        return kmsKeyId;
    }

    public void setKmsKeyId(String kmsKeyId) {
        this.kmsKeyId = kmsKeyId;
    }

    public String getLastAccessedDate() {
        return lastAccessedDate;
    }

    public void setLastAccessedDate(String lastAccessedDate) {
        this.lastAccessedDate = lastAccessedDate;
    }

    public String getLastChangedDate() {
        return lastChangedDate;
    }

    public void setLastChangedDate(String lastChangedDate) {
        this.lastChangedDate = lastChangedDate;
    }

    public String getLastRotatedDate() {
        return lastRotatedDate;
    }

    public void setLastRotatedDate(String lastRotatedDate) {
        this.lastRotatedDate = lastRotatedDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwningService() {
        return owningService;
    }

    public void setOwningService(String owningService) {
        this.owningService = owningService;
    }

    public Long getRecoveryWindowInDays() {
        return recoveryWindowInDays;
    }

    public void setRecoveryWindowInDays(Long recoveryWindowInDays) {
        this.recoveryWindowInDays = recoveryWindowInDays;
    }

    public Boolean getRotationEnabled() {
        return rotationEnabled;
    }

    public void setRotationEnabled(Boolean rotationEnabled) {
        this.rotationEnabled = rotationEnabled;
    }

    public String getRotationLambdaARN() {
        return rotationLambdaARN;
    }

    public void setRotationLambdaARN(String rotationLambdaARN) {
        this.rotationLambdaARN = rotationLambdaARN;
    }

    public RotationRulesType getRotationRules() {
        return rotationRules;
    }

    public void setRotationRules(RotationRulesType rotationRules) {
        this.rotationRules = rotationRules;
    }

    @Updatable
    public String getSecretBinary() {
        return secretBinary;
    }

    public void setSecretBinary(String secretBinary) {
        this.secretBinary = secretBinary;
    }

    @Updatable
    public String getSecretString() {
        return secretString;
    }

    public void setSecretString(String secretString) {
        this.secretString = secretString;
    }

    @Updatable
    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public Map<String, List<String>> getVersionIdsToStages() {
        return versionIdsToStages;
    }

    public void setVersionIdsToStages(Map<String, List<String>> versionIdsToStages) {
        this.versionIdsToStages = versionIdsToStages;
    }

    @Override
    public boolean refresh() {
        SecretsManagerClient client = createClient(SecretsManagerClient.class);
        DescribeSecretResponse response = client.describeSecret(r -> r.secretId(getArn()));

        if (response == null) {
            return false;
        }

        copyFrom(response);
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        SecretsManagerClient client = createClient(SecretsManagerClient.class);

        CreateSecretRequest request = CreateSecretRequest.builder()
            .clientRequestToken(getClientRequestToken())
            .description(getDescription())
            .kmsKeyId(getKmsKeyId())
            .name(getName())
            .secretBinary(getSecretBinary() != null ? SdkBytes.fromUtf8String(getSecretBinary()) : null)
            .secretString(getSecretString())
            .tags(convertTags(getTags()))
            .build();

        CreateSecretResponse response = client.createSecret(request);

        setArn(response.arn());
        setName(response.name());
        setVersionId(response.versionId());
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        SecretsManagerClient client = createClient(SecretsManagerClient.class);

        UpdateSecretRequest updateRequest = UpdateSecretRequest.builder()
            .secretId(getArn())
            .clientRequestToken(getClientRequestToken())
            .description(getDescription())
            .kmsKeyId(getKmsKeyId())
            .secretBinary(getSecretBinary() != null ? SdkBytes.fromUtf8String(getSecretBinary()) : null)
            .secretString(getSecretString())
            .build();

        if (changedFieldNames.contains("tags")) {
            SecretResource oldResource = (SecretResource) current;
            saveTags(client, oldResource.getTags());
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
        setKmsKeyId(model.kmsKeyId());
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

    private void saveTags(SecretsManagerClient client, Map<String, String> oldTags) {
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
}