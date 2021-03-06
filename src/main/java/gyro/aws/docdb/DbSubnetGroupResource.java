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

package gyro.aws.docdb;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.aws.ec2.SubnetResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.CreateDbSubnetGroupResponse;
import software.amazon.awssdk.services.docdb.model.DBSubnetGroup;
import software.amazon.awssdk.services.docdb.model.DbSubnetGroupNotFoundException;
import software.amazon.awssdk.services.docdb.model.DescribeDbSubnetGroupsResponse;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates an Document db subnet group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::db-subnet-group db-subnet-group-example
 *         name: "db-subnet-group-example"
 *         description: "db-subnet-group-example-description"
 *         subnets: [
 *             $(aws::subnet subnet-db-subnet-group-example-1),
 *             $(aws::subnet subnet-db-subnet-group-example-2)
 *         ]
 *
 *         tags: {
 *             Name: "db-subnet-group-example"
 *         }
 *     end
 */
@Type("docdb-subnet-group")
public class DbSubnetGroupResource extends DocDbTaggableResource implements Copyable<DBSubnetGroup> {

    private String description;
    private String name;
    private Set<SubnetResource> subnets;

    //-- Read-only Attributes

    private String arn;
    private String status;

    /**
     * Description of the db subnet group.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Name of the db subnet group.
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
     * A list of associated subnets.
     */
    @Required
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
     * The arn of the db subnet group.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The status of this subnet group.
     */
    @Output
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    protected String getResourceId() {
        return getArn();
    }

    @Override
    protected boolean doRefresh() {
        DocDbClient client = createClient(DocDbClient.class);

        DBSubnetGroup dbSubnetGroup = getDbSubnetGroup(client);

        if (dbSubnetGroup == null) {
            return false;
        }

        copyFrom(dbSubnetGroup);

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        DocDbClient client = createClient(DocDbClient.class);

        CreateDbSubnetGroupResponse response = client.createDBSubnetGroup(
            r -> r.dbSubnetGroupDescription(getDescription())
                .dbSubnetGroupName(getName())
                .subnetIds(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList()))
        );

        setArn(response.dbSubnetGroup().dbSubnetGroupArn());
    }

    @Override
    protected void doUpdate(Resource current, Set changedProperties) {
        DocDbClient client = createClient(DocDbClient.class);

        client.modifyDBSubnetGroup(
            r -> r.dbSubnetGroupName(getName())
                .dbSubnetGroupDescription(getDescription())
                .subnetIds(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList()))
        );
    }

    @Override
    public void delete(GyroUI ui, State state) {
        DocDbClient client = createClient(DocDbClient.class);

        client.deleteDBSubnetGroup(
            r -> r.dbSubnetGroupName(getName())
        );
    }

    @Override
    public void copyFrom(DBSubnetGroup dbSubnetGroup) {
        setArn(dbSubnetGroup.dbSubnetGroupArn());
        setDescription(dbSubnetGroup.dbSubnetGroupDescription());
        setName(dbSubnetGroup.dbSubnetGroupName());
        setStatus(dbSubnetGroup.subnetGroupStatus());
        setSubnets(dbSubnetGroup.subnets().stream().map(s -> findById(SubnetResource.class, s.subnetIdentifier())).collect(Collectors.toSet()));

        loadTags();
    }

    private DBSubnetGroup getDbSubnetGroup(DocDbClient client) {
        DBSubnetGroup dbSubnetGroup = null;

        if (ObjectUtils.isBlank(getName())) {
            throw new GyroException("name is missing, unable to load db subnet group.");
        }

        try {
            DescribeDbSubnetGroupsResponse response = client.describeDBSubnetGroups(
                r -> r.dbSubnetGroupName(getName())
            );

            if (!response.dbSubnetGroups().isEmpty()) {
                dbSubnetGroup = response.dbSubnetGroups().get(0);
            }
        } catch (DbSubnetGroupNotFoundException ex) {

        }

        return dbSubnetGroup;
    }

}
