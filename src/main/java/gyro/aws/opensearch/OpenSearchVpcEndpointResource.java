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

import java.util.Set;
import java.util.concurrent.TimeUnit;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.TimeoutSettings;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.opensearch.OpenSearchClient;
import software.amazon.awssdk.services.opensearch.model.CreateVpcEndpointResponse;
import software.amazon.awssdk.services.opensearch.model.DescribeVpcEndpointsResponse;
import software.amazon.awssdk.services.opensearch.model.ResourceNotFoundException;
import software.amazon.awssdk.services.opensearch.model.VpcEndpoint;
import software.amazon.awssdk.services.opensearch.model.VpcEndpointStatus;

/**
 * Create an OpenSearch VPC endpoint.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::opensearch-vpc-endpoint open-search-vpc-endpoint-example
 *         domain: $(aws::opensearch-domain open-search-domain-example)
 *         vpc-options
 *             subnets: [
 *                 $(aws::subnet subnet-example-1),
 *                 $(aws::subnet subnet-example-2)
 *             ]
 *             security-groups: [
 *                 $(aws::security-group security-group-example-1),
 *                 $(aws::security-group security-group-example-2)
 *             ]
 *         end
 *     end
 */
@Type("opensearch-vpc-endpoint")
public class OpenSearchVpcEndpointResource extends AwsResource implements Copyable<VpcEndpoint> {

    private OpenSearchDomainResource domain;
    private OpenSearchVpcOptions vpcOptions;

    // Read-only
    private String endpoint;
    private String vpcEndpointId;

    /**
     * The domain for the VPC endpoint.
     */
    @Required
    public OpenSearchDomainResource getDomain() {
        return domain;
    }

    public void setDomain(OpenSearchDomainResource domain) {
        this.domain = domain;
    }

    /**
     * The VPC options for the VPC endpoint.
     *
     * @subresource gyro.aws.opensearch.OpenSearchVpcOptions
     */
    @Required
    @Updatable
    public OpenSearchVpcOptions getVpcOptions() {
        return vpcOptions;
    }

    public void setVpcOptions(OpenSearchVpcOptions vpcOptions) {
        this.vpcOptions = vpcOptions;
    }

    /**
     * The endpoint connection id for the VPC endpoint.
     */
    @Output
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * The VPC endpoint id.
     */
    @Id
    @Output
    public String getVpcEndpointId() {
        return vpcEndpointId;
    }

    public void setVpcEndpointId(String vpcEndpointId) {
        this.vpcEndpointId = vpcEndpointId;
    }

    @Override
    public void copyFrom(VpcEndpoint model) {
        setEndpoint(model.endpoint());
        setDomain(findById(OpenSearchDomainResource.class, model.domainArn()));
        setVpcEndpointId(model.vpcEndpointId());

        setVpcOptions(null);
        if (model.vpcOptions() != null) {
            OpenSearchVpcOptions options = newSubresource(OpenSearchVpcOptions.class);
            options.copyFrom(model.vpcOptions());
            setVpcOptions(options);
        }
    }

    @Override
    public boolean refresh() {
        OpenSearchClient client = createClient(OpenSearchClient.class);
        VpcEndpoint vpcEndpoint = getVpcEndpoint(client);

        if (vpcEndpoint == null) {
            return false;
        }

        copyFrom(vpcEndpoint);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        try (OpenSearchClient client = createClient(OpenSearchClient.class)) {
            CreateVpcEndpointResponse response = client.createVpcEndpoint(
                r -> r.domainArn(getDomain().getArn()).vpcOptions(getVpcOptions().toVPCOptions()));

            setVpcEndpointId(response.vpcEndpoint().vpcEndpointId());

            Wait.atMost(10, TimeUnit.MINUTES)
                .checkEvery(30, TimeUnit.SECONDS)
                .resourceOverrides(this, TimeoutSettings.Action.CREATE)
                .prompt(false)
                .until(() -> {
                    VpcEndpoint vpcEndpoint = getVpcEndpoint(client);
                    return vpcEndpoint != null && vpcEndpoint.status().equals(VpcEndpointStatus.ACTIVE);
                });
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        try (OpenSearchClient client = createClient(OpenSearchClient.class)) {
            client.updateVpcEndpoint(r -> r.vpcEndpointId(getVpcEndpointId())
                .vpcOptions(getVpcOptions().toVPCOptions()));

            Wait.atMost(10, TimeUnit.MINUTES)
                .checkEvery(30, TimeUnit.SECONDS)
                .resourceOverrides(this, TimeoutSettings.Action.UPDATE)
                .prompt(false)
                .until(() -> {
                    VpcEndpoint vpcEndpoint = getVpcEndpoint(client);
                    return vpcEndpoint != null && vpcEndpoint.status().equals(VpcEndpointStatus.ACTIVE);
                });
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        try (OpenSearchClient client = createClient(OpenSearchClient.class)) {
            client.deleteVpcEndpoint(r -> r.vpcEndpointId(getVpcEndpointId()));

            Wait.atMost(10, TimeUnit.MINUTES)
                .checkEvery(30, TimeUnit.SECONDS)
                .resourceOverrides(this, TimeoutSettings.Action.DELETE)
                .prompt(false)
                .until(() -> getVpcEndpoint(client) == null);
        }
    }

    private VpcEndpoint getVpcEndpoint(OpenSearchClient client) {
        VpcEndpoint vpcEndpoint = null;

        try {
            DescribeVpcEndpointsResponse response =
                client.describeVpcEndpoints(r -> r.vpcEndpointIds(getVpcEndpointId()));

            if (response.hasVpcEndpoints() && !response.vpcEndpoints().isEmpty()) {
                vpcEndpoint = response.vpcEndpoints().get(0);
            }

        } catch (ResourceNotFoundException ex) {
            // ignore
        }

        return vpcEndpoint;
    }
}
