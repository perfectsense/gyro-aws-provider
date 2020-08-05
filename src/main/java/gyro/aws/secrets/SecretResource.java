package gyro.aws.secrets;

import java.util.List;
import java.util.Set;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.DescribeSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretListEntry;
import software.amazon.awssdk.services.secretsmanager.model.UpdateSecretResponse;

@Type("secret")
public class SecretResource extends AwsResource implements Copyable<DescribeSecretResponse> {

    private String arn;
    private String description;
    private String name;
    private String id;

    @Id
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean refresh() {
        SecretsManagerClient client = createClient(SecretsManagerClient.class);

        DescribeSecretResponse response = client.describeSecret(r -> r.secretId(getId()));

        if (response == null) {
            return false;
        }

        SecretListEntry entry = SecretListEntry.builder()
            .arn(response.arn())
            .name(response.name())
            .description(response.description())
            .kmsKeyId(response.kmsKeyId())
            .tags(response.tags())
            .build();

        copyFrom(entry);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        SecretsManagerClient client = createClient(SecretsManagerClient.class);

        CreateSecretRequest request = CreateSecretRequest.builder()
            .name(getName())
            .description(getDescription())
            .build();

        CreateSecretResponse response = client.createSecret(request);

        setArn(response.arn());
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        SecretsManagerClient client = createClient(SecretsManagerClient.class);

        UpdateSecretResponse response = client.updateSecret(r -> r.secretId(getId()));

        if (changedFieldNames.isEmpty() || changedFieldNames.contains("name")) {

        }

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        SecretsManagerClient client = createClient(SecretsManagerClient.class);

        client.deleteSecret(r -> r.secretId(getId()));
    }

    @Override
    public void copyFrom(SecretListEntry model) {
        setArn(model.arn());
        setName(model.name());
        setDescription(model.description());
        setId(getId());
    }
}
