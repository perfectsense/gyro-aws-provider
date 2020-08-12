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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.DescribeSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;
import software.amazon.awssdk.services.secretsmanager.model.SecretListEntry;

/**
 * Query secrets manager.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 * secrets: $(external-query aws::secret {})
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
        try {
            List<DescribeSecretResponse> list = new ArrayList<>();
            list.add(client.describeSecret(r -> r.secretId(filters.get("arn"))));
            return list;
        } catch (ResourceNotFoundException ex) {
            return Collections.emptyList();
        }
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
