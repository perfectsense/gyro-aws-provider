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

import gyro.aws.Copyable;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.aws.kms.KmsKeyResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Range;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.neptune.NeptuneClient;
import software.amazon.awssdk.services.neptune.model.CreateDbClusterResponse;
import software.amazon.awssdk.services.neptune.model.DBCluster;
import software.amazon.awssdk.services.neptune.model.DBClusterRole;
import software.amazon.awssdk.services.neptune.model.DbClusterNotFoundException;
import software.amazon.awssdk.services.neptune.model.DescribeDbClustersResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Create a Neptune cluster.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 * aws::neptune-cluster neptune-cluster-example
 *     engine: "neptune"
 *     engine-version: "1.0.2.1"
 *     db-cluster-identifier: "neptune-cluster-example"
 *     vpc-security-groups: [
 *         $(aws::security-group security-group),
 *         $(aws::security-group security-group-2)
 *     ]
 *     db-subnet-group: $(aws::neptune-subnet-group neptune-subnet-group)
 *     db-cluster-parameter-group: $(aws::neptune-cluster-parameter-group neptune-cluster-parameter-group)
 *     backup-retention-period: 7
 *     deletion-protection: false
 *     enable-iam-database-authentication: true
 *     port: 8102
 *     preferred-backup-window: "07:39-08:09"
 *     preferred-maintenance-window: "sun:05:12-sun:05:42"
 *     storage-encrypted: true
 *     kms-key: $(aws::kms-key kms-key-neptune-example)
 *     enable-cloudwatch-logs-exports: ["audit"]
 *
 *     tags: {
 *         Name: "neptune cluster example tag"
 *     }
 * end
 */
@Type("neptune-cluster")
public class NeptuneClusterResource extends NeptuneTaggableResource implements Copyable<DBCluster> {

    private String engine;
    private String engineVersion;
    private String dbClusterIdentifier;
    private Set<SecurityGroupResource> vpcSecurityGroups;
    private NeptuneSubnetGroupResource dbSubnetGroup;
    private NeptuneClusterParameterGroupResource dbClusterParameterGroup;
    private List<String> availabilityZones;
    private Integer backupRetentionPeriod;
    private String databaseName;
    private Boolean deletionProtection;
    private Boolean enableIamDatabaseAuthentication;
    private String masterUsername;
    private Integer port;
    private String preferredBackupWindow;
    private String preferredMaintenanceWindow;
    private Boolean storageEncrypted;
    private KmsKeyResource kmsKey;
    private List<String> enableCloudwatchLogsExports;
    private String replicationSourceIdentifier;
    private List<String> associatedRoles;

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
     * The version number of the database engine to use. Valid values are ``1.0.2.1``, ``1.0.2.0``, and ``1.0.1.0``.
     * The default version number is ``1.0.2.1``.
     */
    @ValidStrings({ "1.0.2.1", "1.0.2.0", "1.0.1.0" })
    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    /**
     * The unique name of the Neptune Cluster. (Required)
     */
    @Id
    @Required
    public String getDbClusterIdentifier() {
        return dbClusterIdentifier;
    }

    public void setDbClusterIdentifier(String dbClusterIdentifier) {
        this.dbClusterIdentifier = dbClusterIdentifier;
    }

    /**
     * A list of Amazon VPC security groups to associate with.
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
     * A DB subnet group to use for this Neptune cluster.
     */
    public NeptuneSubnetGroupResource getDbSubnetGroup() {
        return dbSubnetGroup;
    }

    public void setDbSubnetGroup(NeptuneSubnetGroupResource dbSubnetGroup) {
        this.dbSubnetGroup = dbSubnetGroup;
    }

    /**
     * The Neptune cluster parameter group to associate with. If omitted, ``default.neptune1`` is used.
     */
    @Updatable
    public NeptuneClusterParameterGroupResource getDbClusterParameterGroup() {
        return dbClusterParameterGroup;
    }

    public void setDbClusterParameterGroup(NeptuneClusterParameterGroupResource dbClusterParameterGroup) {
        this.dbClusterParameterGroup = dbClusterParameterGroup;
    }

    /**
     * A list of availability zones in which instances in the Neptune cluster can be created.
     */
    public List<String> getAvailabilityZones() {
        if (availabilityZones == null) {
            availabilityZones = new ArrayList<>();
        }

        return availabilityZones;
    }

    public void setAvailabilityZones(List<String> availabilityZones) {
        this.availabilityZones = availabilityZones;
    }

    /**
     * The number of days to retain backups. Must be a value from ``1`` to ``35``.
     */
    @Range(min = 1, max = 35)
    @Updatable
    public Integer getBackupRetentionPeriod() {
        return backupRetentionPeriod;
    }

    public void setBackupRetentionPeriod(Integer backupRetentionPeriod) {
        this.backupRetentionPeriod = backupRetentionPeriod;
    }

    /**
     * The database name when creating the Neptune cluster. If omitted, no database will be created.
     */
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * Enable deletion protection on the Neptune cluster. The default is false.
     */
    @Updatable
    public Boolean getDeletionProtection() {
        return deletionProtection;
    }

    public void setDeletionProtection(Boolean deletionProtection) {
        this.deletionProtection = deletionProtection;
    }

    /**
     * Enable mapping IAM accounts to database accounts. The default is false.
     */
    @Updatable
    public Boolean getEnableIamDatabaseAuthentication() {
        return enableIamDatabaseAuthentication;
    }

    public void setEnableIamDatabaseAuthentication(Boolean enableIamDatabaseAuthentication) {
        this.enableIamDatabaseAuthentication = enableIamDatabaseAuthentication;
    }

    /**
     * The name of the master user for the Neptune cluster.
     */
    @Output
    public String getMasterUsername() {
        return masterUsername;
    }

    public void setMasterUsername(String masterUsername) {
        this.masterUsername = masterUsername;
    }

    /**
     * The port number on which the instances in the Neptune cluster accept connections. Defaults to ``8182``.
     */
    @Updatable
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * The preferred backup window when automated backups are enabled. Must be provided in UTC using the format ``hh24:mi-hh24:mi`` (i.e. ``01:00-02:00``).
     * The window must be at least 30 minutes long, and must not conflict with the preferred maintenance window.
     */
    @Updatable
    public String getPreferredBackupWindow() {
        return preferredBackupWindow;
    }

    public void setPreferredBackupWindow(String preferredBackupWindow) {
        this.preferredBackupWindow = preferredBackupWindow;
    }

    /**
     * The preferred system maintenance window. Must be provided in UTC using the format ``ddd:hh24:mi-ddd:hh24:mi``` (i.e. ``Mon:01:00-Mon:02:00``).
     * The window must be at least 30 minutes long. Valid days are ``Mon``, ``Tue``, ``Wed``, ``Thu``, ``Fri``, ``Sat``, and ``Sun``.
     */
    @Updatable
    public String getPreferredMaintenanceWindow() {
        return preferredMaintenanceWindow;
    }

    public void setPreferredMaintenanceWindow(String preferredMaintenanceWindow) {
        this.preferredMaintenanceWindow = preferredMaintenanceWindow;
    }

    /**
     * Enable Neptune cluster encryption. The default is false.
     */
    public Boolean getStorageEncrypted() {
        return storageEncrypted;
    }

    public void setStorageEncrypted(Boolean storageEncrypted) {
        this.storageEncrypted = storageEncrypted;
    }

    /**
     * The AWS KMS key to encrypt the Neptune cluster.
     */
    public KmsKeyResource getKmsKey() {
        return kmsKey;
    }

    public void setKmsKey(KmsKeyResource kmsKey) {
        this.kmsKey = kmsKey;
    }

    /**
     * The Amazon Resource Name (ARN) of the source Neptune instance or Neptune cluster if this Neptune cluster is created as a Read Replica.
     */
    public String getReplicationSourceIdentifier() {
        return replicationSourceIdentifier;
    }

    public void setReplicationSourceIdentifier(String replicationSourceIdentifier) {
        this.replicationSourceIdentifier = replicationSourceIdentifier;
    }

    /**
     * The list of log types to export to CloudWatch Logs. Currently, the only valid log type for Neptune is ``audit``.
     */
    @ValidStrings("audit")
    @Updatable
    public List<String> getEnableCloudwatchLogsExports() {
        if (enableCloudwatchLogsExports == null) {
            enableCloudwatchLogsExports = new ArrayList<>();
        }

        return enableCloudwatchLogsExports;
    }

    public void setEnableCloudwatchLogsExports(List<String> enableCloudwatchLogsExports) {
        this.enableCloudwatchLogsExports = enableCloudwatchLogsExports;
    }

    /**
     * The list of roles that are associated with the Neptune cluster.
     */
    @Output
    public List<String> getAssociatedRoles() {
        if (associatedRoles == null) {
            associatedRoles = new ArrayList<>();
        }

        return associatedRoles;
    }

    public void setAssociatedRoles(List<String> associatedRoles) {
        this.associatedRoles = associatedRoles;
    }

    @Override
    public void copyFrom(DBCluster model) {
        setEngine(model.engine());
        setEngineVersion(model.engineVersion());
        setDbClusterIdentifier(model.dbClusterIdentifier());
        setVpcSecurityGroups(
            model.vpcSecurityGroups().stream()
                .map(o -> findById(SecurityGroupResource.class, o.vpcSecurityGroupId()))
                .collect(Collectors.toSet())
        );
        setDbSubnetGroup(
            findById(NeptuneSubnetGroupResource.class, model.dbSubnetGroup())
        );
        setDbClusterParameterGroup(
            findById(NeptuneClusterParameterGroupResource.class, model.dbClusterParameterGroup())
        );
        setAvailabilityZones(model.availabilityZones());
        setBackupRetentionPeriod(model.backupRetentionPeriod());
        setDatabaseName(model.databaseName());
        setDeletionProtection(model.deletionProtection());
        setEnableIamDatabaseAuthentication(model.iamDatabaseAuthenticationEnabled());
        setMasterUsername(model.masterUsername());
        setPort(model.port());
        setPreferredBackupWindow(model.preferredBackupWindow());
        setPreferredMaintenanceWindow(model.preferredMaintenanceWindow());
        setStorageEncrypted(model.storageEncrypted());
        setMasterUsername(model.masterUsername());
        setKmsKey(findById(KmsKeyResource.class, model.kmsKeyId()));
        setReplicationSourceIdentifier(model.replicationSourceIdentifier());
        setEnableCloudwatchLogsExports(model.hasEnabledCloudwatchLogsExports()
            ? model.enabledCloudwatchLogsExports()
            : Collections.emptyList());
        setAssociatedRoles(model.associatedRoles().stream().map(DBClusterRole::toString).collect(Collectors.toList()));
        setArn(model.dbClusterArn());
    }

    @Override
    protected boolean doRefresh() {
        NeptuneClient client = createClient(NeptuneClient.class);

        try {
            DescribeDbClustersResponse response = client.describeDBClusters(
                r -> r.dbClusterIdentifier(getDbClusterIdentifier())
            );

            copyFrom(response.dbClusters().get(0));

        } catch (DbClusterNotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        NeptuneClient client = createClient(NeptuneClient.class);

        CreateDbClusterResponse response = client.createDBCluster(
            r -> r.dbClusterIdentifier(getDbClusterIdentifier())
                .engine(getEngine())
                .engineVersion(getEngineVersion())
                .dbSubnetGroupName(getDbSubnetGroup() != null ? getDbSubnetGroup().getName() : null)
                .vpcSecurityGroupIds(
                    getVpcSecurityGroups().stream()
                        .map(SecurityGroupResource::getId)
                        .collect(Collectors.toList())
                )
                .dbClusterParameterGroupName(
                    getDbClusterParameterGroup() != null ? getDbClusterParameterGroup().getName() : null
                )
                .availabilityZones(getAvailabilityZones())
                .backupRetentionPeriod(getBackupRetentionPeriod())
                .databaseName(getDatabaseName())
                .deletionProtection(getDeletionProtection())
                .enableIAMDatabaseAuthentication(getEnableIamDatabaseAuthentication())
                .port(getPort())
                .preferredBackupWindow(getPreferredBackupWindow())
                .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
                .storageEncrypted(getStorageEncrypted())
                .kmsKeyId(getKmsKey() != null ? getKmsKey().getArn() : null)
                .enableCloudwatchLogsExports(getEnableCloudwatchLogsExports())
                .replicationSourceIdentifier(getReplicationSourceIdentifier())
        );

        setArn(response.dbCluster().dbClusterArn());

        Wait.atMost(10, TimeUnit.MINUTES)
            .checkEvery(30, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> isAvailable(client));
    }

    @Override
    protected void doUpdate(Resource current, Set<String> changedProperties) {
        NeptuneClient client = createClient(NeptuneClient.class);

        DescribeDbClustersResponse describeResponse = client.describeDBClusters(
            r -> r.dbClusterIdentifier(getDbClusterIdentifier())
        );

        List<String> currentExports = describeResponse.hasDbClusters()
            ? describeResponse.dbClusters().get(0).enabledCloudwatchLogsExports() : new ArrayList<>();

        List<String> disableLogs = currentExports.stream()
            .filter(s -> !getEnableCloudwatchLogsExports().contains(s))
            .collect(Collectors.toList());

        client.modifyDBCluster(
            r -> r.dbClusterIdentifier(getDbClusterIdentifier())
                .vpcSecurityGroupIds(
                    getVpcSecurityGroups().stream()
                        .map(SecurityGroupResource::getId)
                        .collect(Collectors.toList())
                )
                .dbClusterParameterGroupName(getDbClusterParameterGroup().getName())
                .deletionProtection(getDeletionProtection())
                .backupRetentionPeriod(getBackupRetentionPeriod())
                .enableIAMDatabaseAuthentication(getEnableIamDatabaseAuthentication())
                .port(getPort())
                .preferredBackupWindow(getPreferredBackupWindow())
                .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
                .cloudwatchLogsExportConfiguration(o -> o.enableLogTypes(getEnableCloudwatchLogsExports())
                    .disableLogTypes(disableLogs))
                .applyImmediately(true)
        );

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> isAvailable(client));
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        NeptuneClient client = createClient(NeptuneClient.class);

        client.deleteDBCluster(r -> r.dbClusterIdentifier(getDbClusterIdentifier()).skipFinalSnapshot(true));

        Wait.atMost(5, TimeUnit.MINUTES)
            .checkEvery(15, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> isDeleted(client));
    }

    private boolean isAvailable(NeptuneClient client) {
        DescribeDbClustersResponse response = client.describeDBClusters(
            r -> r.dbClusterIdentifier(getDbClusterIdentifier())
        );

        return response.dbClusters().get(0).status().equals("available");
    }

    private boolean isDeleted(NeptuneClient client) {
        try {
            client.describeDBClusters(
                r -> r.dbClusterIdentifier(getDbClusterIdentifier())
            );

        } catch (DbClusterNotFoundException ex) {
            return true;
        }

        return false;
    }

}
