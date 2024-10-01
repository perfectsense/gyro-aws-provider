/*
 * Copyright 2019, Perfect Sense, Inc.
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

package gyro.aws.rds;

import gyro.aws.Copyable;
import gyro.aws.ec2.SubnetResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.CreateDbSubnetGroupResponse;
import software.amazon.awssdk.services.rds.model.DBSubnetGroup;
import software.amazon.awssdk.services.rds.model.DbSubnetGroupNotFoundException;
import software.amazon.awssdk.services.rds.model.DescribeDbSubnetGroupsResponse;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Create a db subnet group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::db-subnet-group db-subnet-group
 *        name: "db-subnet-group-example"
 *        description: "db subnet group description"
 *        subnets: [
 *            $(aws::subnet subnet-us-east-2a),
 *            $(aws::subnet subnet-us-east-2b)
 *        ]
 *
 *        tags: {
 *            Name: "db-subnet-group-example"
 *        }
 *    end
 */
@Type("db-subnet-group")
public class DbSubnetGroupResource extends RdsTaggableResource implements Copyable<DBSubnetGroup> {

    private String description;
    private String name;
    private List<SubnetResource> subnets;

    /**
     * The description for the DB subnet group.
     */
    @Required
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The name for the DB subnet group.
     */
    @Required
    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The list of Subnets for the DB subnet group.
     */
    @Required
    @Updatable
    public List<SubnetResource> getSubnets() {
        if (subnets == null) {
            return new ArrayList<>();
        }

        return subnets.stream()
            .sorted(Comparator.comparing(SubnetResource::getName))
            .collect(Collectors.toList());
    }

    public void setSubnets(List<SubnetResource> subnets) {
        this.subnets = new ArrayList<>(subnets);
    }

    @Override
    public void copyFrom(DBSubnetGroup group) {
        setDescription(group.dbSubnetGroupDescription());
        setName(group.dbSubnetGroupName());
        setSubnets(group.subnets().stream().map(s -> findById(SubnetResource.class, s.subnetIdentifier()))
            .collect(Collectors.toList()));
        setArn(group.dbSubnetGroupArn());
    }

    @Override
    public boolean doRefresh() {
        RdsClient client = createClient(RdsClient.class);

        if (ObjectUtils.isBlank(getName())) {
            throw new GyroException("name is missing, unable to load db subnet group.");
        }

        try {
            DescribeDbSubnetGroupsResponse response = client.describeDBSubnetGroups(
                r -> r.dbSubnetGroupName(getName())
            );

            response.dbSubnetGroups().forEach(this::copyFrom);

        } catch (DbSubnetGroupNotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    public void doCreate(GyroUI ui, State state) {
        RdsClient client = createClient(RdsClient.class);
        CreateDbSubnetGroupResponse response = client.createDBSubnetGroup(
            r -> r.dbSubnetGroupDescription(getDescription())
                .dbSubnetGroupName(getName())
                .subnetIds(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList()))
        );

        setArn(response.dbSubnetGroup().dbSubnetGroupArn());
    }

    @Override
    public void doUpdate(Resource current, Set<String> changedProperties) {
        RdsClient client = createClient(RdsClient.class);
        client.modifyDBSubnetGroup(
            r -> r.dbSubnetGroupName(getName())
                .dbSubnetGroupDescription(getDescription())
                .subnetIds(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList()))
        );
    }

    @Override
    public void delete(GyroUI ui, State state) {
        RdsClient client = createClient(RdsClient.class);
        client.deleteDBSubnetGroup(
            r -> r.dbSubnetGroupName(getName())
        );
    }
}
