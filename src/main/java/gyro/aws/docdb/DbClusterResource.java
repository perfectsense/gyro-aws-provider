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
import gyro.aws.ec2.SecurityGroupResource;
import gyro.aws.kms.KmsKeyResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.TimeoutSettings;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Min;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.CreateDbClusterResponse;
import software.amazon.awssdk.services.docdb.model.DBCluster;
import software.amazon.awssdk.services.docdb.model.DbClusterNotFoundException;
import software.amazon.awssdk.services.docdb.model.DeleteDbClusterRequest;
import software.amazon.awssdk.services.docdb.model.DescribeDbClustersResponse;
import software.amazon.awssdk.services.docdb.model.ModifyDbClusterRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Creates an Document db cluster.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::docdb-cluster db-cluster-example
 *         identifier: "db-cluster-example"
 *         db-subnet-group: $(aws::db-subnet-group db-subnet-group-db-cluster-example)
 *         engine: "docdb"
 *         engine-version: "3.6.0"
 *         db-cluster-param-group: $(aws::db-cluster-param-group db-cluster-param-group-db-cluster-example)
 *         master-username: "master"
 *         master-user-password: "masterpassword"
 *         port: 27017
 *         preferred-backup-window: "00:00-00:30"
 *         preferred-maintenance-window: "wed:03:28-wed:03:58"
 *         vpc-security-groups: [
 *             $(aws::security-group security-group-db-cluster-example-1),
 *             $(aws::security-group security-group-db-cluster-example-2)
 *         ]
 *         storage-encrypted: false
 *         backup-retention-period: 1
 *         tags: {
 *             Name: "db-cluster-example"
 *         }
 *         post-delete-snapshot-identifier: "db-cluster-example-backup-snapshot"
 *     end
 */
@Type("docdb-cluster")
public class DbClusterResource extends DocDbTaggableResource implements Copyable<DBCluster> {

    private Integer backupRetentionPeriod;
    private String identifier;
    private DbSubnetGroupResource dbSubnetGroup;
    private String engine;
    private String engineVersion;
    private DbClusterParameterGroupResource dbClusterParamGroup;
    private KmsKeyResource kmsKey;
    private String masterUsername;
    private String masterUserPassword;
    private Integer port;
    private String preferredBackupWindow;
    private String preferredMaintenanceWindow;
    private Set<SecurityGroupResource> vpcSecurityGroups;
    private Boolean storageEncrypted;
    private List<String> enableCloudwatchLogsExports;
    private String postDeleteSnapshotIdentifier;

    //-- Read-only Attributes

    private String id;
    private String status;
    private String arn;

    /**
     * Set backup retention period.
     */
    @Required
    @Updatable
    @Min(1)
    public Integer getBackupRetentionPeriod() {
        return backupRetentionPeriod;
    }

    public void setBackupRetentionPeriod(Integer backupRetentionPeriod) {
        this.backupRetentionPeriod = backupRetentionPeriod;
    }

    /**
     * Name of the cluster.
     */
    @Required
    @Id
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Associated db subnet group.
     */
    @Required
    public DbSubnetGroupResource getDbSubnetGroup() {
        return dbSubnetGroup;
    }

    public void setDbSubnetGroup(DbSubnetGroupResource dbSubnetGroup) {
        this.dbSubnetGroup = dbSubnetGroup;
    }

    /**
     * Engine for the cluster.
     */
    @Required
    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    /**
     * Engine version for the cluster.
     */
    @Required
    @Updatable
    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    /**
     * Associated db cluster parameter group.
     */
    @Required
    @Updatable
    public DbClusterParameterGroupResource getDbClusterParamGroup() {
        return dbClusterParamGroup;
    }

    public void setDbClusterParamGroup(DbClusterParameterGroupResource dbClusterParamGroup) {
        this.dbClusterParamGroup = dbClusterParamGroup;
    }

    /**
     * Associated kms key.
     */
    public KmsKeyResource getKmsKey() {
        return kmsKey;
    }

    public void setKmsKey(KmsKeyResource kmsKey) {
        this.kmsKey = kmsKey;
    }

    /**
     * Master username.
     */
    @Required
    public String getMasterUsername() {
        return masterUsername;
    }

    public void setMasterUsername(String masterUsername) {
        this.masterUsername = masterUsername;
    }

    /**
     * Master user password.
     */
    @Required
    public String getMasterUserPassword() {
        return masterUserPassword;
    }

    public void setMasterUserPassword(String masterUserPassword) {
        this.masterUserPassword = masterUserPassword;
    }

    /**
     * Set the access port.
     */
    @Required
    @Updatable
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * Set preferred backup window.
     */
    @Required
    @Updatable
    public String getPreferredBackupWindow() {
        return preferredBackupWindow;
    }

    public void setPreferredBackupWindow(String preferredBackupWindow) {
        this.preferredBackupWindow = preferredBackupWindow;
    }

    /**
     * Set preferred maintenance window.
     */
    @Required
    @Updatable
    public String getPreferredMaintenanceWindow() {
        return preferredMaintenanceWindow;
    }

    public void setPreferredMaintenanceWindow(String preferredMaintenanceWindow) {
        this.preferredMaintenanceWindow = preferredMaintenanceWindow;
    }

    /**
     * Associated vpc security groups.
     */
    @Required
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
     * Encrypt storage.
     */
    public Boolean getStorageEncrypted() {
        return storageEncrypted;
    }

    public void setStorageEncrypted(Boolean storageEncrypted) {
        this.storageEncrypted = storageEncrypted;
    }

    /**
     * Enabled cloud watch log exports.
     */
    public List<String> getEnableCloudwatchLogsExports() {
        if (enableCloudwatchLogsExports == null) {
            enableCloudwatchLogsExports = new ArrayList<>();
        }

        if (!enableCloudwatchLogsExports.isEmpty() && !enableCloudwatchLogsExports.contains(null)) {
            Collections.sort(enableCloudwatchLogsExports);
        }

        return enableCloudwatchLogsExports;
    }

    public void setEnableCloudwatchLogsExports(List<String> enableCloudwatchLogsExports) {
        this.enableCloudwatchLogsExports = enableCloudwatchLogsExports;
    }

    /**
     * snapshot name to be created post cluster delete.
     */
    @Updatable
    public String getPostDeleteSnapshotIdentifier() {
        return postDeleteSnapshotIdentifier;
    }

    public void setPostDeleteSnapshotIdentifier(String postDeleteSnapshotIdentifier) {
        this.postDeleteSnapshotIdentifier = postDeleteSnapshotIdentifier;
    }

    /**
     * The id for the db cluster.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The status for the db cluster.
     */
    @Output
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * The arn for the db cluster.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    protected String getResourceId() {
        return getArn();
    }

    @Override
    public boolean doRefresh() {
        DocDbClient client = createClient(DocDbClient.class);

        DBCluster dbCluster = getDbCluster(client);

        if (dbCluster == null) {
            return false;
        }

        copyFrom(dbCluster);

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        DocDbClient client = createClient(DocDbClient.class);

        CreateDbClusterResponse response = client.createDBCluster(
            o -> o.backupRetentionPeriod(getBackupRetentionPeriod())
                .dbClusterIdentifier(getIdentifier())
                .dbSubnetGroupName(getDbSubnetGroup().getName())
                .engine(getEngine())
                .engineVersion(getEngineVersion())
                .dbClusterParameterGroupName(getDbClusterParamGroup().getName())
                .kmsKeyId(getKmsKey() != null ? getKmsKey().getId() : null)
                .masterUsername(getMasterUsername())
                .masterUserPassword(getMasterUserPassword())
                .port(getPort())
                .preferredBackupWindow(getPreferredBackupWindow())
                .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
                .vpcSecurityGroupIds(getVpcSecurityGroups().stream().map(SecurityGroupResource::getId).collect(Collectors.toList()))
                .storageEncrypted(getStorageEncrypted())
                .enableCloudwatchLogsExports(getEnableCloudwatchLogsExports())
        );

        setId(response.dbCluster().dbClusterResourceId());
        setArn(response.dbCluster().dbClusterArn());

        state.save();

        boolean waitResult = Wait.atMost(3, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.CREATE)
            .prompt(false)
            .until(() -> isAvailable(client));

        if (!waitResult) {
            throw new GyroException("Unable to reach 'available' state for docdb cluster - " + getIdentifier());
        }
    }

    @Override
    protected void doUpdate(Resource current, Set changedProperties) {
        DocDbClient client = createClient(DocDbClient.class);

        DbClusterResource resource = (DbClusterResource) current;

        ModifyDbClusterRequest.Builder builder = ModifyDbClusterRequest.builder()
            .backupRetentionPeriod(getBackupRetentionPeriod())
            .dbClusterIdentifier(getIdentifier())
            .dbClusterParameterGroupName(getDbClusterParamGroup().getName())
            .masterUserPassword(getMasterUserPassword())
            .port(getPort())
            .preferredBackupWindow(getPreferredBackupWindow())
            .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
            .vpcSecurityGroupIds(getVpcSecurityGroups().stream().map(SecurityGroupResource::getId).collect(Collectors.toList()));

        if (!resource.getEngineVersion().equals(getEngineVersion())) {
            builder.engineVersion(getEngineVersion());
        }

        client.modifyDBCluster(builder.build());

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.UPDATE)
            .prompt(true)
            .until(() -> isAvailable(client));
    }

    @Override
    public void delete(GyroUI ui, State state) {
        DocDbClient client = createClient(DocDbClient.class);

        DeleteDbClusterRequest.Builder builder = DeleteDbClusterRequest
            .builder()
            .dbClusterIdentifier(getIdentifier());

        if (ObjectUtils.isBlank(getPostDeleteSnapshotIdentifier())) {
            builder.skipFinalSnapshot(true);
        } else {
            builder.finalDBSnapshotIdentifier(getPostDeleteSnapshotIdentifier())
                .skipFinalSnapshot(false);
        }

        client.deleteDBCluster(builder.build());

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.DELETE)
            .prompt(true)
            .until(() -> getDbCluster(client) == null);
    }

    @Override
    public void copyFrom(DBCluster dbCluster) {

        setBackupRetentionPeriod(dbCluster.backupRetentionPeriod());
        setIdentifier(dbCluster.dbClusterIdentifier());
        setDbSubnetGroup(findById(DbSubnetGroupResource.class, dbCluster.dbSubnetGroup()));
        setEngine(dbCluster.engine());
        setEngineVersion(dbCluster.engineVersion());
        setDbClusterParamGroup(findById(DbClusterParameterGroupResource.class, dbCluster.dbClusterParameterGroup()));
        setIdentifier(dbCluster.dbClusterIdentifier());
        setKmsKey(findById(KmsKeyResource.class, dbCluster.kmsKeyId()));
        setMasterUsername(dbCluster.masterUsername());
        setPort(dbCluster.port());
        setPreferredBackupWindow(dbCluster.preferredBackupWindow());
        setPreferredMaintenanceWindow(dbCluster.preferredMaintenanceWindow());
        setVpcSecurityGroups(dbCluster.vpcSecurityGroups().stream().map(v -> findById(SecurityGroupResource.class, v.vpcSecurityGroupId())).collect(Collectors.toSet()));
        setStorageEncrypted(dbCluster.storageEncrypted());
        setEnableCloudwatchLogsExports(dbCluster.enabledCloudwatchLogsExports().isEmpty() ? new ArrayList<>() : dbCluster.enabledCloudwatchLogsExports());
        setStatus(dbCluster.status());
        setId(dbCluster.dbClusterResourceId());
        setArn(dbCluster.dbClusterArn());
    }

    private boolean isAvailable(DocDbClient client) {
        DBCluster dbCluster = getDbCluster(client);

        return dbCluster != null && dbCluster.status().equals("available");
    }

    private DBCluster getDbCluster(DocDbClient client) {
        DBCluster dbCluster = null;

        if (ObjectUtils.isBlank(getIdentifier())) {
            throw new GyroException("identifier is missing, unable to load db cluster.");
        }

        try {
            DescribeDbClustersResponse response = client.describeDBClusters(
                r -> r.dbClusterIdentifier(getIdentifier())
            );

            if (!response.dbClusters().isEmpty()) {
                dbCluster = response.dbClusters().get(0);
            }

        } catch (DbClusterNotFoundException ex) {

        }

        return dbCluster;
    }

}
