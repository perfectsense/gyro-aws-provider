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
import software.amazon.awssdk.services.opensearchserverless.model.SecurityPolicyDetail;
import software.amazon.awssdk.services.opensearchserverless.model.SecurityPolicySummary;
import software.amazon.awssdk.services.opensearchserverless.model.SecurityPolicyType;

/**
 * Query OpenSearch Serverless security policy.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    security-policy: $(external-query aws::opensearch-serverless-security-policy { name: ''})
 */
@Type("opensearch-serverless-security-policy")
public class OpenSearchServerlessSecurityPolicyFinder
    extends AwsFinder<OpenSearchServerlessClient, SecurityPolicyDetail, OpenSearchServerlessSecurityPolicyResource> {

    private String name;
    private String type;

    /**
     * The name of the security policy.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The type of the security policy.
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    protected List<SecurityPolicyDetail> findAllAws(OpenSearchServerlessClient client) {
        List<SecurityPolicyDetail> securityPolicyDetails = new ArrayList<>();
        for (SecurityPolicyType type : SecurityPolicyType.knownValues()) {
            List<String> securityPolicies = client.listSecurityPolicies(r -> r.type(type))
                .securityPolicySummaries()
                .stream()
                .map(SecurityPolicySummary::name)
                .collect(Collectors.toList());

            for (String securityPolicyName : securityPolicies) {
                securityPolicyDetails.add(client.getSecurityPolicy(r -> r
                        .name(securityPolicyName)
                        .type(type))
                    .securityPolicyDetail());
            }
        }

        return securityPolicyDetails;
    }

    @Override
    protected List<SecurityPolicyDetail> findAws(OpenSearchServerlessClient client, Map<String, String> filters) {
        List<SecurityPolicyDetail> securityPolicyDetails = new ArrayList<>();

        if (filters.containsKey("name") && filters.containsKey("type")) {
            try {
                securityPolicyDetails.add(client.getSecurityPolicy(r -> r
                    .name(filters.get("name"))
                    .type(SecurityPolicyType.fromValue(filters.get("type")))).securityPolicyDetail());
            } catch (Exception ex) {
                // ignore
            }
        }

        return securityPolicyDetails;
    }
}
