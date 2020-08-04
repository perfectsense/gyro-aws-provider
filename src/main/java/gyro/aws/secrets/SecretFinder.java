package gyro.aws.secrets;

import java.util.List;
import java.util.Map;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.SecretListEntry;

@Type("secret")
public class SecretFinder extends AwsFinder<SecretsManagerClient, SecretListEntry, SecretResource> {

    @Override
    protected List<SecretListEntry> findAllAws(SecretsManagerClient client) {
        return client.listSecrets().secretList();
    }

    @Override
    protected List<SecretListEntry> findAws(
        SecretsManagerClient client, Map<String, String> filters) {
        return null;
    }
}
