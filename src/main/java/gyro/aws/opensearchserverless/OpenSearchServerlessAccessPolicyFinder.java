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
import software.amazon.awssdk.services.opensearchserverless.model.AccessPolicyDetail;
import software.amazon.awssdk.services.opensearchserverless.model.AccessPolicySummary;
import software.amazon.awssdk.services.opensearchserverless.model.AccessPolicyType;
import software.amazon.awssdk.services.opensearchserverless.model.ListAccessPoliciesResponse;
import software.amazon.awssdk.services.opensearchserverless.model.ResourceNotFoundException;

/**
 * Query OpenSearch Serverless access policy.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    access-policy: $(external-query aws::opensearch-serverless-access-policy { name: ''})
 */
@Type("opensearch-serverless-access-policy")
public class OpenSearchServerlessAccessPolicyFinder
    extends AwsFinder<OpenSearchServerlessClient, AccessPolicyDetail, OpenSearchServerlessAccessPolicyResource> {

    private String name;
    private String type;

    /**
     * The name of the access policy.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The type of the access policy.
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    protected List<AccessPolicyDetail> findAllAws(OpenSearchServerlessClient client) {
        List<AccessPolicyDetail> accessPolicyDetails = new ArrayList<>();
        for (AccessPolicyType type : AccessPolicyType.knownValues()) {
            ListAccessPoliciesResponse response = client.listAccessPolicies(r -> r.type(type));
            List<String> collect = response.accessPolicySummaries()
                .stream()
                .map(AccessPolicySummary::name)
                .collect(Collectors.toList());

            for (String name : collect) {
                accessPolicyDetails.add(client.getAccessPolicy(r -> r.name(name).type(type)).accessPolicyDetail());
            }
        }

        return accessPolicyDetails;
    }

    @Override
    protected List<AccessPolicyDetail> findAws(OpenSearchServerlessClient client, Map<String, String> filters) {
        List<AccessPolicyDetail> accessPolicyDetails = new ArrayList<>();

        if (filters.containsKey("name") && filters.containsKey("type")) {
            try {
                accessPolicyDetails.add(client.getAccessPolicy(r -> r
                        .name(filters.get("name"))
                        .type(AccessPolicyType.fromValue(filters.get("type"))).build())
                    .accessPolicyDetail());
            } catch (ResourceNotFoundException ex) {
                // Ignore
            }
        }

        return accessPolicyDetails;
    }

}
