/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.dax;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.ec2.SubnetResource;
import gyro.aws.ec2.VpcResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.dax.DaxClient;
import software.amazon.awssdk.services.dax.model.DescribeSubnetGroupsResponse;
import software.amazon.awssdk.services.dax.model.SubnetGroup;

/**
 * Creates a DAX subnet group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::dax-subnet-group subnet-group
 *        name: "subnet-group-example"
 *        description: "subnet-group-description"
 *        subnets: [
 *            $(aws::subnet example-subnet-1),
 *            $(aws::subnet example-subnet-2)
 *        ]
 *    end
 */
@Type("dax-subnet-group")
public class DaxSubnetGroupResource extends AwsResource implements Copyable<SubnetGroup> {

    private String description;
    private String name;
    private List<SubnetResource> subnets;
    private VpcResource vpc;

    /**
     * The description of the subnet group.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The name of the subnet group.
     */
    @Id
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The list of subnets of the subnet group.
     */
    @Required
    @Updatable
    public List<SubnetResource> getSubnets() {
        if (subnets != null) {
            subnets = new ArrayList<>();
        }

        return subnets;
    }

    public void setSubnets(List<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    /**
     * The VPC of the subnet group.
     */
    @Output
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    @Override
    public void copyFrom(SubnetGroup model) {
        setDescription(model.description());
        setName(model.subnetGroupName());
        setVpc(findById(VpcResource.class, model.vpcId()));

        getSubnets().clear();
        if (model.subnets() != null) {
            model.subnets().forEach(subnet -> {
                getSubnets().add(findById(SubnetResource.class, subnet.subnetIdentifier()));
            });
        }
    }

    @Override
    public boolean refresh() {
        DaxClient client = createClient(DaxClient.class);

        DescribeSubnetGroupsResponse response = client.describeSubnetGroups(r -> r.subnetGroupNames(getName()));

        if (response == null || response.subnetGroups().isEmpty()) {
            return false;
        }

        copyFrom(response.subnetGroups().get(0));
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        DaxClient client = createClient(DaxClient.class);

        client.createSubnetGroup(r -> r
            .subnetGroupName(getName())
            .subnetIds(getSubnets() != null
                ? getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList())
                : null)
            .description(getDescription())
            .build()
        );

        refresh();
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        DaxClient client = createClient(DaxClient.class);

        client.updateSubnetGroup(r -> r
            .subnetGroupName(getName())
            .subnetIds(getSubnets() != null
                ? getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList())
                : null)
            .description(getDescription())
        );
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        DaxClient client = createClient(DaxClient.class);

        client.deleteSubnetGroup(r -> r.subnetGroupName(getName()));
    }
}
