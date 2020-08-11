package gyro.aws.secretsmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.DescribeSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretListEntry;

/**
 * Query secrets manager.
 *
 * Example -------
 *
 * .. code-block:: gyro
 *
 * secrets: $(external-query aws::secret {"arn":"arn:aws:secretsmanager:Region:User-ARN:secret:'Secret-example-for-arn'"})
 */
@Type("secret")
public class SecretFinder extends AwsFinder<SecretsManagerClient, DescribeSecretResponse, SecretResource> {

    @Override
    protected List<DescribeSecretResponse> findAllAws(SecretsManagerClient client) {
        return client.listSecretsPaginator().stream().flatMap(list ->
            list.secretList().stream().map(this::convertEntry)).collect(Collectors.toList());
    }

    @Override
    protected List<DescribeSecretResponse> findAws(
        SecretsManagerClient client, Map<String, String> filters) {
        List<DescribeSecretResponse> list = new ArrayList<>();
        list.add(client.describeSecret(r -> r.secretId(filters.get("arn"))));

        return list;
    }

    private DescribeSecretResponse convertEntry(SecretListEntry entry) {
        return DescribeSecretResponse.builder()
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
    }
}
