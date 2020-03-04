/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws.neptune;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.aws.ec2.SubnetResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.neptune.NeptuneClient;
import software.amazon.awssdk.services.neptune.model.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Create a Neptune subnet group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::neptune-subnet-group neptune-subnet-group
 *        name: "neptune-subnet-group-example"
 *        description: "neptune subnet group example description"
 *        subnets: [
 *            $(aws::subnet subnet-us-east-2a),
 *            $(aws::subnet subnet-us-east-2b)
 *        ]
 *
 *        tags: {
 *            Name: "neptune-subnet-group-example"
 *        }
 *    end
 */
@Type("neptune-subnet-group")
public class NeptuneSubnetGroupResource extends NeptuneTaggableResource implements Copyable<DBSubnetGroup> {

    private String description;
    private String name;
    private Set<SubnetResource> subnets;

    /**
     * The description for the Neptune subnet group. (Required)
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The name for the Neptune subnet group. (Required)
     */
    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The list of subnets for the Neptune subnet group. (Required)
     */
    @Updatable
    public Set<SubnetResource> getSubnets() {
        if (subnets == null) {
            return new HashSet<>();
        }
        return subnets;
    }

    @Updatable
    public void setSubnets(Set<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    @Override
    public void copyFrom(DBSubnetGroup model) {
        setDescription(model.dbSubnetGroupDescription());
        setName(model.dbSubnetGroupName());
        setSubnets(model.subnets().stream().map(s -> findById(SubnetResource.class, s.subnetIdentifier())).collect(Collectors.toSet()));
        setArn(model.dbSubnetGroupArn());
    }

    @Override
    protected boolean doRefresh() {
        DBSubnetGroup group = getDBSubnetGroup();
        if (group == null) {
            return false;
        }

        copyFrom(group);
        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        NeptuneClient client = createClient(NeptuneClient.class);
        CreateDbSubnetGroupResponse response = client.createDBSubnetGroup(
                r -> r.dbSubnetGroupName(getName())
                        .dbSubnetGroupDescription(getDescription())
                        .subnetIds(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toSet()))
        );

        setArn(response.dbSubnetGroup().dbSubnetGroupArn());
    }

    @Override
    protected void doUpdate(Resource current, Set<String> changedProperties) {
        NeptuneClient client = createClient(NeptuneClient.class);
        client.modifyDBSubnetGroup(
                r -> r.dbSubnetGroupName(getName())
                        .dbSubnetGroupDescription(getDescription())
                        .subnetIds(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toSet()))
        );
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        NeptuneClient client = createClient(NeptuneClient.class);
        client.deleteDBSubnetGroup(
                r -> r.dbSubnetGroupName(getName())
        );
    }

    private DBSubnetGroup getDBSubnetGroup() {
        NeptuneClient client = createClient(NeptuneClient.class);

        if (ObjectUtils.isBlank(getName())) {
            throw new GyroException("name is missing, unable to load subnet group.");
        }

        DBSubnetGroup group = null;

        try {
            DescribeDbSubnetGroupsResponse response = client.describeDBSubnetGroups(
                    r -> r.dbSubnetGroupName(getName())
            );

            if (response.hasDbSubnetGroups()) {
                group = response.dbSubnetGroups().get(0);
            }
        } catch (DbSubnetGroupNotFoundException ex) {
            //  group not found - ignore exception and return null
        }

        return group;
    }
}
