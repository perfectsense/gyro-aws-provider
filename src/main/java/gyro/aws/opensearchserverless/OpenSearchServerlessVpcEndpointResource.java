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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.aws.ec2.SubnetResource;
import gyro.aws.ec2.VpcResource;
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
import software.amazon.awssdk.services.opensearchserverless.OpenSearchServerlessClient;
import software.amazon.awssdk.services.opensearchserverless.model.BatchGetVpcEndpointResponse;
import software.amazon.awssdk.services.opensearchserverless.model.CreateVpcEndpointResponse;
import software.amazon.awssdk.services.opensearchserverless.model.ResourceNotFoundException;
import software.amazon.awssdk.services.opensearchserverless.model.UpdateVpcEndpointRequest;
import software.amazon.awssdk.services.opensearchserverless.model.VpcEndpointDetail;
import software.amazon.awssdk.services.opensearchserverless.model.VpcEndpointStatus;

/**
 * Create an OpenSearch Serverless VPC endpoint.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::opensearch-serverless-vpc-endpoint vpc-endpoint
 *         name: "opensearch-serverless-vpc-endpoint-example"
 *         vpc: $(aws::vpc vpc-example)
 *         subnets: [
 *             $(aws::subnet subnet-example-1),
 *             $(aws::subnet subnet-example-2)
 *         ]
 *         security-groups: [
 *             $(aws::security-group security-group-example-1),
 *             $(aws::security-group security-group-example-2)
 *         ]
 *     end
 */
@Type("opensearch-serverless-vpc-endpoint")
public class OpenSearchServerlessVpcEndpointResource extends AwsResource implements Copyable<VpcEndpointDetail> {

    private String name;
    private Set<SubnetResource> subnets;
    private Set<SecurityGroupResource> securityGroups;
    private VpcResource vpc;

    // Read-only
    private String id;

    /**
     * The name of the VPC endpoint.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The subnets in which the VPC endpoint will be created.
     */
    @Updatable
    public Set<SubnetResource> getSubnets() {
        if (subnets == null) {
            subnets = new HashSet<>();
        }

        return subnets;
    }

    public void setSubnets(Set<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    /**
     * The security groups that will be associated with the VPC endpoint.
     */
    @Updatable
    public Set<SecurityGroupResource> getSecurityGroups() {
        if (securityGroups == null) {
            securityGroups = new HashSet<>();
        }

        return securityGroups;
    }

    public void setSecurityGroups(Set<SecurityGroupResource> securityGroups) {
        this.securityGroups = securityGroups;
    }

    /**
     * The VPC in which the VPC endpoint will be created.
     */
    @Required
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    /**
     * The ID of the VPC endpoint.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(VpcEndpointDetail model) {
        setName(model.name());
        setId(model.id());
        setVpc(findById(VpcResource.class, model.vpcId()));

        getSubnets().clear();
        if (model.hasSubnetIds()) {
            setSubnets(
                model.subnetIds().stream().map(s -> findById(SubnetResource.class, s)).collect(Collectors.toSet()));
        }

        getSecurityGroups().clear();
        if (model.hasSecurityGroupIds()) {
            setSecurityGroups(model.securityGroupIds()
                .stream()
                .map(s -> findById(SecurityGroupResource.class, s))
                .collect(Collectors.toSet()));
        }
    }

    @Override
    public boolean refresh() {
        OpenSearchServerlessClient client = createClient(OpenSearchServerlessClient.class);
        VpcEndpointDetail endpoint = getVpcEndpoint(client);

        if (endpoint != null) {
            copyFrom(endpoint);

            return true;
        }

        return false;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        OpenSearchServerlessClient client = createClient(OpenSearchServerlessClient.class);
        String token = UUID.randomUUID().toString();

        CreateVpcEndpointResponse response = client.createVpcEndpoint(r -> r.clientToken(token)
            .name(getName())
            .subnetIds(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList()))
            .securityGroupIds(getSecurityGroups().stream()
                .map(SecurityGroupResource::getId)
                .collect(Collectors.toList()))
            .vpcId(getVpc().getId())
        );

        setId(response.createVpcEndpointDetail().id());

        Wait.atMost(20, TimeUnit.MINUTES)
            .checkEvery(1, TimeUnit.MINUTES)
            .resourceOverrides(this, TimeoutSettings.Action.CREATE)
            .prompt(false)
            .until(() -> {
                VpcEndpointDetail endpoint = getVpcEndpoint(client);
                return endpoint != null && endpoint.status().equals(VpcEndpointStatus.ACTIVE);
            });
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        OpenSearchServerlessClient client = createClient(OpenSearchServerlessClient.class);
        String token = UUID.randomUUID().toString();
        UpdateVpcEndpointRequest.Builder builder = UpdateVpcEndpointRequest.builder()
            .clientToken(token)
            .id(getId());

        OpenSearchServerlessVpcEndpointResource old = (OpenSearchServerlessVpcEndpointResource) current;

        if (changedFieldNames.contains("subnets")) {
            Optional.of(old.getSubnets().stream()
                    .map(SubnetResource::getId)
                    .filter(id -> getSubnets().stream()
                        .map(SubnetResource::getId)
                        .noneMatch(id::equals))
                    .collect(Collectors.toList()))
                .filter(list -> !list.isEmpty())
                .ifPresent(builder::removeSubnetIds);

            Optional.of(getSubnets().stream()
                    .map(SubnetResource::getId)
                    .filter(id -> old.getSubnets().stream()
                        .map(SubnetResource::getId)
                        .noneMatch(id::equals))
                    .collect(Collectors.toList()))
                .filter(list -> !list.isEmpty())
                .ifPresent(builder::addSubnetIds);

        }

        if (changedFieldNames.contains("security-groups")) {
            Optional.of(old.getSecurityGroups().stream()
                    .map(SecurityGroupResource::getId)
                    .filter(id -> getSecurityGroups().stream()
                        .map(SecurityGroupResource::getId)
                        .noneMatch(id::equals))
                    .collect(Collectors.toList()))
                .filter(list -> !list.isEmpty())
                .ifPresent(builder::removeSecurityGroupIds);

            Optional.of(getSecurityGroups().stream()
                    .map(SecurityGroupResource::getId)
                    .filter(id -> old.getSecurityGroups().stream()
                        .map(SecurityGroupResource::getId)
                        .noneMatch(id::equals))
                    .collect(Collectors.toList()))
                .filter(list -> !list.isEmpty())
                .ifPresent(builder::addSecurityGroupIds);
        }

        client.updateVpcEndpoint(builder.build());

        Wait.atMost(20, TimeUnit.MINUTES)
            .checkEvery(1, TimeUnit.MINUTES)
            .resourceOverrides(this, TimeoutSettings.Action.CREATE)
            .prompt(false)
            .until(() -> {
                VpcEndpointDetail endpoint = getVpcEndpoint(client);
                return endpoint != null && endpoint.status().equals(VpcEndpointStatus.ACTIVE);
            });
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        try {
            OpenSearchServerlessClient client = createClient(OpenSearchServerlessClient.class);
            String token = UUID.randomUUID().toString();
            client.deleteVpcEndpoint(r -> r.clientToken(token).id(getId())
            );

            Wait.atMost(20, TimeUnit.MINUTES)
                .checkEvery(1, TimeUnit.MINUTES)
                .resourceOverrides(this, TimeoutSettings.Action.UPDATE)
                .prompt(false)
                .until(() -> getVpcEndpoint(client) == null);
        } catch (ResourceNotFoundException ex) {
            // ignore
        }
    }

    private VpcEndpointDetail getVpcEndpoint(OpenSearchServerlessClient client) {
        VpcEndpointDetail vpcEndpoint = null;

        try {
            BatchGetVpcEndpointResponse response = client.batchGetVpcEndpoint(r -> r.ids(getId()));

            if (response.hasVpcEndpointDetails() && !response.vpcEndpointDetails().isEmpty()) {
                vpcEndpoint = response.vpcEndpointDetails().get(0);
            }

        } catch (ResourceNotFoundException ex) {
            // Ignore
        }

        return vpcEndpoint;
    }
}
