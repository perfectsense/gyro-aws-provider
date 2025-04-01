/*
 * Copyright 2024, Brightspot.
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

package gyro.aws.opensearchserverless;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.opensearchserverless.OpenSearchServerlessClient;
import software.amazon.awssdk.services.opensearchserverless.model.ListSecurityConfigsResponse;
import software.amazon.awssdk.services.opensearchserverless.model.ResourceNotFoundException;
import software.amazon.awssdk.services.opensearchserverless.model.SecurityConfigDetail;
import software.amazon.awssdk.services.opensearchserverless.model.SecurityConfigSummary;
import software.amazon.awssdk.services.opensearchserverless.model.SecurityConfigType;

/**
 * Query OpenSearch Serverless security configuration.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    security-config: $(external-query aws::opensearch-serverless-security-config { security-config-id: ''})
 */
@Type("opensearch-serverless-security-config")
public class OpenSearchServerlessSecurityConfigFinder
    extends AwsFinder<OpenSearchServerlessClient, SecurityConfigDetail, OpenSearchServerlessSecurityConfigResource> {

    private String securityConfigId;

    /**
     * The id of the security configuration.
     */
    public String getSecurityConfigId() {
        return securityConfigId;
    }

    public void setSecurityConfigId(String securityConfigId) {
        this.securityConfigId = securityConfigId;
    }

    @Override
    protected List<SecurityConfigDetail> findAllAws(OpenSearchServerlessClient client) {
        List<SecurityConfigDetail> securityConfigurations = new ArrayList<>();
        for (SecurityConfigType type : SecurityConfigType.knownValues()) {
            ListSecurityConfigsResponse response = client.listSecurityConfigs(r -> r.type(type));
            List<String> securityConfigIds = response.securityConfigSummaries()
                .stream()
                .map(SecurityConfigSummary::id)
                .collect(Collectors.toList());

            for (String securityConfigId : securityConfigIds) {
                securityConfigurations.add(client.getSecurityConfig(r -> r.id(securityConfigId))
                    .securityConfigDetail());
            }
        }

        return securityConfigurations;
    }

    @Override
    protected List<SecurityConfigDetail> findAws(OpenSearchServerlessClient client, Map<String, String> filters) {
        List<SecurityConfigDetail> securityConfigurations = new ArrayList<>();
        if (filters.containsKey("security-config-id")) {
            try {
                securityConfigurations.add(client.getSecurityConfig(r -> r.id(filters.get("security-config-id")))
                    .securityConfigDetail());
            } catch (ResourceNotFoundException ex) {
                // Ignore
            }
        }

        return securityConfigurations;
    }

}
