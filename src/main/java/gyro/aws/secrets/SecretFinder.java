package gyro.aws.secrets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.DescribeSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretListEntry;

@Type("secret")
public class SecretFinder extends AwsFinder<SecretsManagerClient, DescribeSecretResponse, SecretResource> {

    @Override
    protected List<DescribeSecretResponse> findAllAws(SecretsManagerClient client) {
        List<DescribeSecretResponse> responseList = new ArrayList<>();

        for (SecretListEntry entry : client.listSecrets().secretList()) {
            DescribeSecretResponse response = DescribeSecretResponse.builder()
                .arn(entry.arn())
                .name(entry.name())
                .description(entry.description())
                .build();

            responseList.add(response);
        }

        return responseList;
    }

    @Override
    protected List<DescribeSecretResponse> findAws(
        SecretsManagerClient client, Map<String, String> filters) {
        return null;
    }
}
