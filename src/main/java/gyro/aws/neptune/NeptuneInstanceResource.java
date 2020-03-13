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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.neptune.NeptuneClient;
import software.amazon.awssdk.services.neptune.model.CreateDbInstanceRequest;
import software.amazon.awssdk.services.neptune.model.CreateDbInstanceResponse;
import software.amazon.awssdk.services.neptune.model.DBInstance;
import software.amazon.awssdk.services.neptune.model.DbInstanceNotFoundException;
import software.amazon.awssdk.services.neptune.model.DescribeDbInstancesResponse;
import software.amazon.awssdk.services.neptune.model.ModifyDbInstanceRequest;

/**
 * Create a Neptune instance.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 * aws::neptune-instance neptune-instance-example
 *     engine: "neptune"
 *     db-instance-class: "db.r4.large"
 *     db-instance-identifier: "neptune-instance-example"
 *     db-cluster: $(aws::neptune-cluster neptune-cluster-example)
 *     db-parameter-group: $(aws::neptune-parameter-group neptune-parameter-group)
 *
 *     tags: {
 *             Name: "neptune instance example"
 *         }
 * end
 */
@Type("neptune-instance")
public class NeptuneInstanceResource extends NeptuneTaggableResource implements Copyable<DBInstance> {

    public String engine;
    public String dbInstanceClass;
    public String dbInstanceIdentifier;
    public NeptuneClusterResource dbCluster;
    public String masterUsername;
    public String masterUserPassword;
    private Set<SecurityGroupResource> vpcSecurityGroups;
    private NeptuneSubnetGroupResource dbSubnetGroup;
    private NeptuneParameterGroupResource dbParameterGroup;

    /**
     * The name of the database engine. The only valid value is ``neptune`` (Required).
     */
    @ValidStrings("neptune")
    @Required
    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    /**
     * The compute and memory capacity of the Neptune instance (Required).
     */
    @Updatable
    @Required
    public String getDbInstanceClass() {
        return dbInstanceClass;
    }

    public void setDbInstanceClass(String dbInstanceClass) {
        this.dbInstanceClass = dbInstanceClass;
    }

    /**
     * The unique name of the Neptune instance (Required).
     */
    @Id
    @Required
    public String getDbInstanceIdentifier() {
        return dbInstanceIdentifier;
    }

    public void setDbInstanceIdentifier(String dbInstanceIdentifier) {
        this.dbInstanceIdentifier = dbInstanceIdentifier;
    }

    /**
     * The Neptune cluster that this instance will belong to (Required).
     */
    @Required
    public NeptuneClusterResource getDbCluster() {
        return dbCluster;
    }

    public void setDbCluster(NeptuneClusterResource dbCluster) {
        this.dbCluster = dbCluster;
    }

    /**
     * The name for the master user.
     */
    public String getMasterUsername() {
        return masterUsername;
    }

    public void setMasterUsername(String masterUsername) {
        this.masterUsername = masterUsername;
    }

    /**
     * The password for the master user.
     */
    @Updatable
    public String getMasterUserPassword() {
        return masterUserPassword;
    }

    public void setMasterUserPassword(String masterUserPassword) {
        this.masterUserPassword = masterUserPassword;
    }

    /**
     * A list of EC2 VPC security groups to associate with this Neptune instance.
     */
    @Updatable
    public Set<SecurityGroupResource> getVpcSecurityGroups() {
        if (vpcSecurityGroups == null) {
            vpcSecurityGroups = new HashSet<>();
        }

        return vpcSecurityGroups;
    }

    public void setVpcSecurityGroups(Set<SecurityGroupResource> vpcSecurityGroups) {
        this.vpcSecurityGroups = vpcSecurityGroups;
    }

    /**
     * A Neptune subnet group to associate with this Neptune instance.
     * If there is no Neptune subnet group, then it is a non-VPC Neptune instance.
     */
    @Updatable
    public NeptuneSubnetGroupResource getDbSubnetGroup() {
        return dbSubnetGroup;
    }

    public void setDbSubnetGroup(NeptuneSubnetGroupResource dbSubnetGroup) {
        this.dbSubnetGroup = dbSubnetGroup;
    }

    /**
     * The Neptune parameter group to associate with this Neptune instance.
     * If this argument is omitted, the default parameter group for the specified engine is used.
     */
    @Updatable
    public NeptuneParameterGroupResource getDbParameterGroup() {
        return dbParameterGroup;
    }

    public void setDbParameterGroup(NeptuneParameterGroupResource dbParameterGroup) {
        this.dbParameterGroup = dbParameterGroup;
    }

    @Override
    public void copyFrom(DBInstance model) {
        setEngine(model.engine());
        setDbInstanceClass(model.dbInstanceClass());
        setDbInstanceIdentifier(model.dbInstanceIdentifier());
        setDbCluster(findById(NeptuneClusterResource.class, model.dbClusterIdentifier()));
        setMasterUsername(model.masterUsername());
        setVpcSecurityGroups(
            model.vpcSecurityGroups().stream()
                .map(o -> findById(SecurityGroupResource.class, o.vpcSecurityGroupId()))
                .collect(Collectors.toSet())
        );
        setDbSubnetGroup(
            findById(NeptuneSubnetGroupResource.class, model.dbSubnetGroup().dbSubnetGroupName())
        );
        setDbParameterGroup(
            model.hasDbParameterGroups()
                ? findById(NeptuneParameterGroupResource.class, model.dbParameterGroups().get(0).dbParameterGroupName())
                : null
        );
        setArn(model.dbInstanceArn());
    }

    @Override
    protected boolean doRefresh() {
        NeptuneClient client = createClient(NeptuneClient.class);

        DBInstance instance = getDbInstance(client);

        if (instance != null) {
            copyFrom(instance);
            return true;
        }

        return false;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        NeptuneClient client = createClient(NeptuneClient.class);

        CreateDbInstanceRequest.Builder builder = CreateDbInstanceRequest.builder()
            .engine(getEngine())
            .dbInstanceClass(getDbInstanceClass())
            .dbInstanceIdentifier(getDbInstanceIdentifier())
            .dbClusterIdentifier(getDbCluster() != null ? getDbCluster().getDbClusterIdentifier() : null)
            .masterUsername(getMasterUsername())
            .masterUserPassword(getMasterUserPassword());

        if (!getVpcSecurityGroups().isEmpty()) {
            builder = builder.vpcSecurityGroupIds(
                getVpcSecurityGroups().stream()
                    .map(SecurityGroupResource::getId)
                    .collect(Collectors.toList())
            );
        }

        if (getDbSubnetGroup() != null) {
            builder = builder.dbSubnetGroupName(getDbSubnetGroup().getName());
        }

        if (getDbParameterGroup() != null) {
            builder = builder.dbParameterGroupName(getDbParameterGroup().getName());
        }

        CreateDbInstanceResponse response = client.createDBInstance(builder.build());

        setArn(response.dbInstance().dbInstanceArn());

        Wait.atMost(20, TimeUnit.MINUTES)
            .checkEvery(1, TimeUnit.MINUTES)
            .prompt(false)
            .until(() -> isAvailable(client));
    }

    @Override
    protected void doUpdate(Resource current, Set<String> changedProperties) {
        NeptuneClient client = createClient(NeptuneClient.class);

        ModifyDbInstanceRequest.Builder builder = ModifyDbInstanceRequest.builder()
            .dbInstanceIdentifier(getDbInstanceIdentifier())
            .dbInstanceClass(getDbInstanceClass())
            .masterUserPassword(getMasterUserPassword())
            .applyImmediately(true);

        if (changedProperties.contains("vpc-security-groups")) {
            builder = builder.vpcSecurityGroupIds(
                getVpcSecurityGroups().stream()
                    .map(SecurityGroupResource::getId)
                    .collect(Collectors.toList()));
        }

        if (changedProperties.contains("db-subnet-group") && getDbSubnetGroup() != null) {
            builder = builder.dbSubnetGroupName(getDbSubnetGroup().getName());
        }

        if (changedProperties.contains("db-parameter-group") && getDbParameterGroup() != null) {
            builder = builder.dbParameterGroupName(getDbParameterGroup().getName());
        }

        client.modifyDBInstance(builder.build());

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> isAvailable(client));
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        NeptuneClient client = createClient(NeptuneClient.class);

        client.deleteDBInstance(
            r -> r.dbInstanceIdentifier(getDbInstanceIdentifier()).skipFinalSnapshot(true)
        );

        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> isDeleted(client));
    }

    private DBInstance getDbInstance(NeptuneClient client) {
        DBInstance instance = null;

        try {
            DescribeDbInstancesResponse response = client.describeDBInstances(
                r -> r.dbInstanceIdentifier(getDbInstanceIdentifier())
            );

            if (response.hasDbInstances()) {
                instance = response.dbInstances().get(0);
            }

        } catch (DbInstanceNotFoundException ex) {
            // instance not found - ignore exception and return null
        }

        return instance;
    }

    private boolean isAvailable(NeptuneClient client) {
        DBInstance instance = getDbInstance(client);

        if (instance == null) {
            return false;
        }

        return instance.dbInstanceStatus().equals("available");
    }

    private boolean isDeleted(NeptuneClient client) {
        DBInstance instance = getDbInstance(client);

        return instance == null;
    }
}
