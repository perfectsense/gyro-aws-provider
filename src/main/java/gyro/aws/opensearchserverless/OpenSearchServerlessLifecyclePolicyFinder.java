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
import software.amazon.awssdk.services.opensearchserverless.model.BatchGetLifecyclePolicyResponse;
import software.amazon.awssdk.services.opensearchserverless.model.LifecyclePolicyDetail;
import software.amazon.awssdk.services.opensearchserverless.model.LifecyclePolicyIdentifier;
import software.amazon.awssdk.services.opensearchserverless.model.LifecyclePolicySummary;
import software.amazon.awssdk.services.opensearchserverless.model.LifecyclePolicyType;
import software.amazon.awssdk.services.opensearchserverless.model.ListLifecyclePoliciesResponse;
import software.amazon.awssdk.services.opensearchserverless.model.ResourceNotFoundException;

/**
 * Query OpenSearch Serverless lifecycle policy.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    lifecycle-policy: $(external-query aws::opensearch-serverless-lifecycle-policy { name: ''})
 */
@Type("opensearch-serverless-lifecycle-policy")
public class OpenSearchServerlessLifecyclePolicyFinder
    extends AwsFinder<OpenSearchServerlessClient, LifecyclePolicyDetail, OpenSearchServerlessLifecyclePolicyResource> {

    private String name;
    private String type;

    /**
     * The name of the lifecycle policy.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The type of the lifecycle policy.
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    protected List<LifecyclePolicyDetail> findAllAws(OpenSearchServerlessClient client) {
        List<LifecyclePolicyDetail> lifecyclePolicyDetails = new ArrayList<>();
        for (LifecyclePolicyType type : LifecyclePolicyType.knownValues()) {
            ListLifecyclePoliciesResponse response = client.listLifecyclePolicies(r -> r.type(type).build());
            List<String> policyNames = response.lifecyclePolicySummaries()
                .stream()
                .map(LifecyclePolicySummary::name)
                .collect(Collectors.toList());

            if (!policyNames.isEmpty()) {
                List<LifecyclePolicyIdentifier> identifiers = policyNames.stream()
                    .map(policyName -> LifecyclePolicyIdentifier.builder().name(policyName).type(type).build())
                    .collect(Collectors.toList());
                BatchGetLifecyclePolicyResponse batchGetLifecyclePolicyResponse = client.batchGetLifecyclePolicy(r -> r.identifiers(
                        identifiers)
                    .build());
                lifecyclePolicyDetails.addAll(batchGetLifecyclePolicyResponse.lifecyclePolicyDetails());
            }
        }

        return lifecyclePolicyDetails;
    }

    @Override
    protected List<LifecyclePolicyDetail> findAws(OpenSearchServerlessClient client, Map<String, String> filters) {
        List<LifecyclePolicyDetail> lifecyclePolicyDetails = new ArrayList<>();

        if (filters.containsKey("name") && filters.containsKey("type")) {
            try {
                lifecyclePolicyDetails = client.batchGetLifecyclePolicy(r -> r.identifiers(
                    LifecyclePolicyIdentifier.builder().name(filters.get("name"))
                        .type(LifecyclePolicyType.fromValue(filters.get("type")))
                        .build()).build()).lifecyclePolicyDetails();
            } catch (ResourceNotFoundException ex) {
                // Ignore
            }
        }

        return lifecyclePolicyDetails;
    }

}
