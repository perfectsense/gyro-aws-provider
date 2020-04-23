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
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.neptune.NeptuneClient;
import software.amazon.awssdk.services.neptune.model.CreateDbClusterResponse;
import software.amazon.awssdk.services.neptune.model.DBCluster;
import software.amazon.awssdk.services.neptune.model.DBClusterMember;
import software.amazon.awssdk.services.neptune.model.DBClusterRole;
import software.amazon.awssdk.services.neptune.model.DbClusterNotFoundException;
import software.amazon.awssdk.services.neptune.model.DeleteDbClusterRequest;
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
 *
 *     vpc-security-groups: [$(aws::security-group security-group-neptune-cluster-example)]
 *     db-subnet-group: $(aws::neptune-subnet-group neptune-subnet-group-cluster-example)
 *     db-cluster-parameter-group: $(aws::neptune-cluster-parameter-group neptune-cluster-parameter-group-cluster-example)
 *     kms-key: $(aws::kms-key kms-key-neptune-cluster-example)
 *
 *     backup-retention-period: 7
 *     deletion-protection: false
 *     port: 8182
 *     preferred-backup-window: "07:39-08:09"
 *     preferred-maintenance-window: "sun:05:12-sun:05:42"
 *     storage-encrypted: true
 *     enable-cloudwatch-logs-exports: ["audit"]
 *     enable-iam-database-authentication: false
 *     skip-final-snapshot: false
 *     final-db-snapshot-identifier: "neptune-cluster-example-final-snapshot-test"
 *     apply-immediately: true
 *
 *     tags: {
 *         Name: "neptune cluster example"
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
    private List<String> dbClusterMembers;
    private Boolean skipFinalSnapshot;
    private String finalDbSnapshotIdentifier;
    private Boolean applyImmediately;

    /**
     * The name of the database engine. The only valid value is ``neptune``. (Required)
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
     * The version number of the database engine to use. Valid values are ``1.0.2.1``, ``1.0.2.0`` or ``1.0.1.0``.
     * Defaults to ``1.0.2.1``.
     */
    @ValidStrings({ "1.0.2.1", "1.0.2.0", "1.0.1.0" })
    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    /**
     * The unique name of the Neptune cluster. (Required)
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
     * A list of security groups to associate the cluster with.
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
     * A DB subnet group to use for this Neptune cluster. If omitted, the ``default`` group is used.
     */
    public NeptuneSubnetGroupResource getDbSubnetGroup() {
        return dbSubnetGroup;
    }

    public void setDbSubnetGroup(NeptuneSubnetGroupResource dbSubnetGroup) {
        this.dbSubnetGroup = dbSubnetGroup;
    }

    /**
     * The Neptune cluster parameter group to associate with. If omitted, the ``default.neptune1`` group is used.
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
    @Output
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
     * The number of days to retain backups. Valid values are from ``1`` to ``35``.
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
     * Enable deletion protection on the Neptune cluster. Defaults to ``false``.
     */
    @Updatable
    public Boolean getDeletionProtection() {
        return deletionProtection;
    }

    public void setDeletionProtection(Boolean deletionProtection) {
        this.deletionProtection = deletionProtection;
    }

    /**
     * Enable mapping IAM accounts to database accounts. Defaults to ``false``.
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
     * The window must be at least 30 minutes long.
     */
    @Updatable
    public String getPreferredMaintenanceWindow() {
        return preferredMaintenanceWindow;
    }

    public void setPreferredMaintenanceWindow(String preferredMaintenanceWindow) {
        this.preferredMaintenanceWindow = preferredMaintenanceWindow;
    }

    /**
     * Enable Neptune cluster encryption. Defaults to ``false``.
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
     * The list of log types to export to CloudWatch Logs. Currently, the only supported value is ``audit``.
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

    /**
     * The list of instances that make up the Neptune cluster.
     */
    @Output
    public List<String> getDbClusterMembers() {
        return dbClusterMembers;
    }

    public void setDbClusterMembers(List<String> dbClusterMembers) {
        this.dbClusterMembers = dbClusterMembers;
    }

    /**
     * Determines whether a final DB cluster snapshot is created before the Neptune cluster is deleted.
     * Defaults to ``true`` where no snapshot is created. If set to ``false``, a snapshot is created before the cluster is deleted.
     */
    @Updatable
    public Boolean getSkipFinalSnapshot() {
        if (skipFinalSnapshot == null) {
            skipFinalSnapshot = true;
        }

        return skipFinalSnapshot;
    }

    public void setSkipFinalSnapshot(Boolean skipFinalSnapshot) {
        this.skipFinalSnapshot = skipFinalSnapshot;
    }

    /**
     * Specifies whether the modifications in update requests are asynchronously applied as soon as possible.
     * When set to ``false``, changes to the Neptune cluster are applied during the next preferred-maintenance-window.
     */
    @Updatable
    public Boolean getApplyImmediately() {
        return applyImmediately;
    }

    public void setApplyImmediately(Boolean applyImmediately) {
        this.applyImmediately = applyImmediately;
    }

    /**
     * The DB cluster snapshot identifier of the new DB cluster snapshot created when the Neptune cluster is deleted.
     * Can only be set if ``skip-final-snapshot`` is set to ``false``.
     */
    @Updatable
    @Regex(value = "^[a-zA-Z]((?!.*--)[-a-zA-Z0-9]{0,253}[a-z0-9]$)?", message = "1-255 letters, numbers, or hyphens. May not contain two consecutive hyphens. The first character must be a letter, and the last may not be a hyphen.")
    public String getFinalDbSnapshotIdentifier() {
        return finalDbSnapshotIdentifier;
    }

    public void setFinalDbSnapshotIdentifier(String finalDbSnapshotIdentifier) {
        this.finalDbSnapshotIdentifier = finalDbSnapshotIdentifier;
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
        setDbClusterMembers(model.dbClusterMembers().stream()
            .map(DBClusterMember::dbInstanceIdentifier)
            .collect(Collectors.toList()));
        setArn(model.dbClusterArn());
    }

    @Override
    protected boolean doRefresh() {
        NeptuneClient client = createClient(NeptuneClient.class);

        DBCluster cluster = getDbCluster(client);

        if (cluster != null) {
            copyFrom(cluster);
            return true;
        }

        return false;
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
                .backupRetentionPeriod(getBackupRetentionPeriod())
                .deletionProtection(getDeletionProtection())
                .enableIAMDatabaseAuthentication(getEnableIamDatabaseAuthentication())
                .masterUsername(getMasterUsername())
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

        DBCluster cluster = getDbCluster(client);

        List<String> currentExports = cluster != null
            ? cluster.enabledCloudwatchLogsExports() : new ArrayList<>();

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
                .applyImmediately(getApplyImmediately())
        );

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // ignore exception
        }

        Wait.atMost(5, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> isAvailable(client));
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        NeptuneClient client = createClient(NeptuneClient.class);

        DeleteDbClusterRequest.Builder request = DeleteDbClusterRequest.builder()
            .dbClusterIdentifier(getDbClusterIdentifier())
            .skipFinalSnapshot(getSkipFinalSnapshot());

        if (!getSkipFinalSnapshot()) {
            request.finalDBSnapshotIdentifier(getFinalDbSnapshotIdentifier());
        }

        client.deleteDBCluster(request.build());

        Wait.atMost(5, TimeUnit.MINUTES)
            .checkEvery(15, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> isDeleted(client));
    }

    private DBCluster getDbCluster(NeptuneClient client) {
        DBCluster cluster = null;

        try {
            DescribeDbClustersResponse response = client.describeDBClusters(
                r -> r.dbClusterIdentifier(getDbClusterIdentifier())
            );

            if (response.hasDbClusters()) {
                cluster = response.dbClusters().get(0);
            }

        } catch (DbClusterNotFoundException ex) {
            // cluster not found - ignore exception and return null
        }

        return cluster;
    }

    private boolean isAvailable(NeptuneClient client) {
        DBCluster cluster = getDbCluster(client);

        return cluster != null && cluster.status().equals("available");
    }

    private boolean isDeleted(NeptuneClient client) {
        DBCluster cluster = getDbCluster(client);

        return cluster == null;
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (configuredFields.contains("final-db-snapshot-identifier")) {
            if (getSkipFinalSnapshot()) {
                errors.add(new ValidationError(
                    this,
                    "final-db-snapshot-identifier",
                    "If skip-final-snapshot is set to true, then final-db-snapshot-identifier must not be specified."));
            }
        } else {
            if (!getSkipFinalSnapshot()) {
                errors.add(new ValidationError(
                    this,
                    "final-db-snapshot-identifier",
                    "If skip-final-snapshot is set to false, then final-db-snapshot-identifier must be specified."));
            }
        }

        return errors;
    }
}
