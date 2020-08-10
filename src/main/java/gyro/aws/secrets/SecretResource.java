package gyro.aws.secrets;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import software.amazon.awssdk.services.secretsmanager.model.DeleteSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.DeleteSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.DescribeSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.RotationRulesType;
import software.amazon.awssdk.services.secretsmanager.model.Tag;
import software.amazon.awssdk.services.secretsmanager.model.TagResourceRequest;
import software.amazon.awssdk.services.secretsmanager.model.UpdateSecretRequest;

@Type("secret")
public class SecretResource extends AwsResource implements Copyable<DescribeSecretResponse> {

    private String arn;
    private String clientRequestToken;
    private Instant deletedDate;
    private String description;
    private Boolean forceDeleteWithoutRecovery;
    private String kmsKeyId;
    private Instant lastAccessedDate;
    private Instant lastChangedDate;
    private Instant lastRotatedDate;
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

    public Instant getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(Instant deletedDate) {
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

    public Instant getLastAccessedDate() {
        return lastAccessedDate;
    }

    public void setLastAccessedDate(Instant lastAccessedDate) {
        this.lastAccessedDate = lastAccessedDate;
    }

    public Instant getLastChangedDate() {
        return lastChangedDate;
    }

    public void setLastChangedDate(Instant lastChangedDate) {
        this.lastChangedDate = lastChangedDate;
    }

    public Instant getLastRotatedDate() {
        return lastRotatedDate;
    }

    public void setLastRotatedDate(Instant lastRotatedDate) {
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

        TagResourceRequest tagRequest = TagResourceRequest.builder()
            .secretId(getArn())
            .tags(convertTags(getTags()))
            .build();

        client.updateSecret(updateRequest);
        client.tagResource(tagRequest);
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        SecretsManagerClient client = createClient(SecretsManagerClient.class);

        DeleteSecretRequest request = DeleteSecretRequest.builder()
            .secretId(getArn())
            .forceDeleteWithoutRecovery(getForceDeleteWithoutRecovery())
            .recoveryWindowInDays(getRecoveryWindowInDays())
            .build();

        DeleteSecretResponse response = client.deleteSecret(request);

        setArn(response.arn());
        setDeletedDate(response.deletionDate());
        setName(response.name());
    }

    @Override
    public void copyFrom(DescribeSecretResponse model) {
        setArn(model.arn());
        setDeletedDate(model.deletedDate());
        setDescription(model.description());
        setKmsKeyId(model.kmsKeyId());
        setLastAccessedDate(model.lastAccessedDate());
        setLastChangedDate(model.lastChangedDate());
        setLastRotatedDate(model.lastRotatedDate());
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
