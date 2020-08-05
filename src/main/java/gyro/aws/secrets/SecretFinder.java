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
                .deletedDate(entry.deletedDate())
                .description(entry.description())
                .kmsKeyId(entry.kmsKeyId())
                .lastAccessedDate(entry.lastAccessedDate())
                .lastChangedDate(entry.lastChangedDate())
                .lastRotatedDate(entry.lastRotatedDate())
                .name(entry.name())
                .owningService(entry.owningService())
                .rotationEnabled(entry.rotationEnabled())
                .rotationLambdaARN(entry.rotationLambdaARN())
                .rotationRules(entry.rotationRules())
                .tags(entry.tags())
                .versionIdsToStages(entry.secretVersionsToStages())
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
