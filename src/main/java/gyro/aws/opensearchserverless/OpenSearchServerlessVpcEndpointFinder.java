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
import software.amazon.awssdk.services.opensearchserverless.model.ResourceNotFoundException;
import software.amazon.awssdk.services.opensearchserverless.model.VpcEndpointDetail;
import software.amazon.awssdk.services.opensearchserverless.model.VpcEndpointSummary;
import software.amazon.awssdk.utils.builder.SdkBuilder;

/**
 * Query OpenSearch Serverless VPC endpoint.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    vpc-endpoint: $(external-query aws::opensearch-serverless-vpc-endpoint { vpc-endpoint-id: ''})
 */
@Type("opensearch-serverless-vpc-endpoint")
public class OpenSearchServerlessVpcEndpointFinder
    extends AwsFinder<OpenSearchServerlessClient, VpcEndpointDetail, OpenSearchServerlessVpcEndpointResource> {

    private String vpcEndpointId;

    /**
     * The ID of the VPC endpoint.
     */
    public String getVpcEndpointId() {
        return vpcEndpointId;
    }

    public void setVpcEndpointId(String vpcEndpointId) {
        this.vpcEndpointId = vpcEndpointId;
    }

    @Override
    protected List<VpcEndpointDetail> findAllAws(OpenSearchServerlessClient client) {
        List<VpcEndpointDetail> vpcEndpoints = new ArrayList<>();
        List<String> vpcEndpointIds = client.listVpcEndpoints(SdkBuilder::build).vpcEndpointSummaries().stream()
            .map(VpcEndpointSummary::id)
            .collect(Collectors.toList());

        if (!vpcEndpointIds.isEmpty()) {
            vpcEndpoints = client.batchGetVpcEndpoint(r -> r.ids(vpcEndpointIds).build()).vpcEndpointDetails();
        }

        return vpcEndpoints;
    }

    @Override
    protected List<VpcEndpointDetail> findAws(OpenSearchServerlessClient client, Map<String, String> filters) {
        List<VpcEndpointDetail> vpcEndpoints = new ArrayList<>();

        if (filters.containsKey("vpc-endpoint-id")) {
            try {
                vpcEndpoints = client.batchGetVpcEndpoint(r -> r.ids(filters.get("vpc-endpoint-id")))
                    .vpcEndpointDetails();
            } catch (ResourceNotFoundException ex) {
                // Ignore
            }
        }

        return vpcEndpoints;
    }

}
