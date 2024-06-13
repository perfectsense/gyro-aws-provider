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

package gyro.aws.cloudfront;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.ListOriginRequestPoliciesRequest;
import software.amazon.awssdk.services.cloudfront.model.ListOriginRequestPoliciesResponse;
import software.amazon.awssdk.services.cloudfront.model.OriginRequestPolicy;
import software.amazon.awssdk.services.cloudfront.model.OriginRequestPolicySummary;
import software.amazon.awssdk.services.cloudfront.model.GetOriginRequestPolicyRequest;
import software.amazon.awssdk.services.cloudfront.model.GetOriginRequestPolicyResponse;
import software.amazon.awssdk.services.cloudfront.model.NoSuchOriginRequestPolicyException;

/**
 * Query origin request policy.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    origin-request-policy: $(external-query aws::cloudfront-origin-request-policy { id: '' })
 */
@Type("cloudfront-origin-request-policy")
public class OriginRequestPolicyFinder extends AwsFinder<CloudFrontClient, OriginRequestPolicy, OriginRequestPolicyResource> {

    private String id;

    /**
     * The ID of the origin request policy.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<OriginRequestPolicy> findAllAws(CloudFrontClient client) {
        List<OriginRequestPolicy> cachePolicies = new ArrayList<>();

        String marker = null;
        ListOriginRequestPoliciesResponse response = client.listOriginRequestPolicies(
            ListOriginRequestPoliciesRequest.builder().marker(marker).build());

        if (response.originRequestPolicyList() != null && response.originRequestPolicyList().items() != null) {
            cachePolicies.addAll(response.originRequestPolicyList().items().stream()
                .map(OriginRequestPolicySummary::originRequestPolicy)
                .collect(Collectors.toList()));
        }

        return cachePolicies;
    }

    @Override
    protected List<OriginRequestPolicy> findAws(CloudFrontClient client, Map<String, String> filters) {
        List<OriginRequestPolicy> cachePolicies = new ArrayList<>();

        if (filters.containsKey("id")) {
            try {
                GetOriginRequestPolicyResponse response = client.getOriginRequestPolicy(GetOriginRequestPolicyRequest.builder()
                    .id(filters.get("id"))
                    .build());

                if (response.originRequestPolicy() != null) {
                    cachePolicies.add(response.originRequestPolicy());
                }

            } catch (NoSuchOriginRequestPolicyException e) {
                // Ignore
            }

        } else {
            cachePolicies.addAll(findAllAws(client));
        }

        return cachePolicies;
    }
}
