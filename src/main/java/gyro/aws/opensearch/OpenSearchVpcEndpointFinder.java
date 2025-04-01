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

package gyro.aws.opensearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.opensearch.OpenSearchClient;
import software.amazon.awssdk.services.opensearch.model.DescribeVpcEndpointsResponse;
import software.amazon.awssdk.services.opensearch.model.ListVpcEndpointsResponse;
import software.amazon.awssdk.services.opensearch.model.ResourceNotFoundException;
import software.amazon.awssdk.services.opensearch.model.VpcEndpoint;
import software.amazon.awssdk.services.opensearch.model.VpcEndpointSummary;
import software.amazon.awssdk.utils.builder.SdkBuilder;

/**
 * Query OpenSearch VPC endpoint.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    vpc-endpoint: $(external-query aws::opensearch-vpc-endpoint { id: ''})
 */
@Type("opensearch-vpc-endpoint")
public class OpenSearchVpcEndpointFinder
    extends AwsFinder<OpenSearchClient, VpcEndpoint, OpenSearchVpcEndpointResource> {

    @Override
    protected List<VpcEndpoint> findAllAws(OpenSearchClient client) {
        List<VpcEndpoint> vpcEndpoints = new ArrayList<>();
        ListVpcEndpointsResponse response = client.listVpcEndpoints(SdkBuilder::build);
        if (response.hasVpcEndpointSummaryList() && !response.vpcEndpointSummaryList().isEmpty()) {
            vpcEndpoints.addAll(client.describeVpcEndpoints(r -> r
                .vpcEndpointIds(response.vpcEndpointSummaryList().stream().map(VpcEndpointSummary::vpcEndpointId)
                    .collect(Collectors.toList()))).vpcEndpoints());
        }

        return vpcEndpoints;
    }

    @Override
    protected List<VpcEndpoint> findAws(OpenSearchClient client, Map<String, String> filters) {
        List<VpcEndpoint> vpcEndpoints = new ArrayList<>();
        if (filters.containsKey("id")) {
            try {
                DescribeVpcEndpointsResponse response = client.describeVpcEndpoints(r -> r
                    .vpcEndpointIds(filters.get("id")));

                if (response.hasVpcEndpoints() && !response.vpcEndpoints().isEmpty()) {
                    vpcEndpoints.addAll(response.vpcEndpoints());
                }
            } catch (ResourceNotFoundException ex) {
                // Ignore
            }
        }

        return vpcEndpoints;
    }
}
