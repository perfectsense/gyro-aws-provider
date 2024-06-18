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
import software.amazon.awssdk.services.cloudfront.model.CachePolicy;
import software.amazon.awssdk.services.cloudfront.model.CachePolicySummary;
import software.amazon.awssdk.services.cloudfront.model.GetCachePolicyRequest;
import software.amazon.awssdk.services.cloudfront.model.GetCachePolicyResponse;
import software.amazon.awssdk.services.cloudfront.model.ListCachePoliciesRequest;
import software.amazon.awssdk.services.cloudfront.model.ListCachePoliciesResponse;
import software.amazon.awssdk.services.cloudfront.model.NoSuchCachePolicyException;

/**
 * Query cache policy.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    cache-policy: $(external-query aws::cloudfront-cache-policy { id: '' })
 */
@Type("cloudfront-cache-policy")
public class CachePolicyFinder extends AwsFinder<CloudFrontClient, CachePolicy, CachePolicyResource> {

    private String id;

    /**
     * The ID of the cache policy.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<CachePolicy> findAllAws(CloudFrontClient client) {
        List<CachePolicy> cachePolicies = new ArrayList<>();

        String marker = null;
        ListCachePoliciesResponse response = client.listCachePolicies(ListCachePoliciesRequest.builder().marker(marker).build());

        if (response.cachePolicyList() != null && response.cachePolicyList().items() != null) {
            cachePolicies.addAll(response.cachePolicyList().items().stream()
                .map(CachePolicySummary::cachePolicy)
                .collect(Collectors.toList()));
        }

        return cachePolicies;
    }

    @Override
    protected List<CachePolicy> findAws(CloudFrontClient client, Map<String, String> filters) {
        List<CachePolicy> cachePolicies = new ArrayList<>();

        if (filters.containsKey("id")) {
            try {
                GetCachePolicyResponse response = client.getCachePolicy(GetCachePolicyRequest.builder()
                    .id(filters.get("id"))
                    .build());

                if (response.cachePolicy() != null) {
                    cachePolicies.add(response.cachePolicy());
                }

            } catch (NoSuchCachePolicyException e) {
                // Ignore
            }

        } else {
            cachePolicies.addAll(findAllAws(client));
        }

        return cachePolicies;
    }
}
