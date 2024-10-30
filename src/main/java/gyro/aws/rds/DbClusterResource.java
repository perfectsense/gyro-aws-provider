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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.DependsOn;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.CreateDbClusterRequest;
import software.amazon.awssdk.services.rds.model.CreateDbClusterResponse;
import software.amazon.awssdk.services.rds.model.DBCluster;
import software.amazon.awssdk.services.rds.model.DbClusterNotFoundException;
import software.amazon.awssdk.services.rds.model.DescribeDbClustersResponse;
import software.amazon.awssdk.services.rds.model.InvalidDbClusterStateException;
import software.amazon.awssdk.services.rds.model.LocalWriteForwardingStatus;
import software.amazon.awssdk.services.rds.model.ModifyDbClusterRequest;
import software.amazon.awssdk.services.rds.model.RestoreDbClusterFromS3Response;
import software.amazon.awssdk.services.rds.model.RestoreDbClusterFromSnapshotResponse;
import software.amazon.awssdk.services.rds.model.RestoreDbClusterToPointInTimeResponse;

/**
 * Create a Aurora cluster.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::db-cluster db-cluster-example
 *        identifier: "aurora-mysql-cluster"
 *        engine: "aurora-mysql"
 *        availability-zones: ["us-east-2a", "us-east-2b", "us-east-2c"]
 *        db-name: "clusterexample"
 *        master-username: "user"
 *        master-user-password: "password"
 *        backup-retention-period: 5
 *        preferred-backup-window: "07:00-09:00"
 *        delete-automated-backups: true
 *        skip-final-snapshot: true
 *        tags: {
 *            Name: "aurora-mysql-cluster1"
 *        }
 *    end
 *
 * .. code-block:: gyro
 *
 *    aws::db-cluster db-cluster-serverless-example
 *        identifier: "aurora-serverless-cluster"
 *        engine: "aurora"
 *        engine-mode: "serverless"
 *
 *        scaling-configuration
 *            auto-pause: true
 *            max-capacity: 128
 *            min-capacity: 2
 *            seconds-until-auto-pause: 300
 *        end
 *
 *        availability-zones: ["us-east-2a", "us-east-2b", "us-east-2c"]
 *        db-name: "clusterexample"
 *        master-username: "user"
 *        master-user-password: "password"
 *        backup-retention-period: 5
 *        preferred-backup-window: "07:00-09:00"
 *        delete-automated-backups: true
 *        skip-final-snapshot: true
 *        tags: {
 *            Name: "aurora-serverless-cluster"
 *        }
 *    end
 */
@Type("db-cluster")
public class DbClusterResource extends RdsTaggableResource implements Copyable<DBCluster> {

    private Boolean applyImmediately;
    private List<String> availabilityZones;
    private Long backTrackWindow;
    private Integer backupRetentionPeriod;
    private String characterSetName;
    private String identifier;
    private String dbName;
    private DbClusterParameterGroupResource dbClusterParameterGroup;
    private DbSubnetGroupResource dbSubnetGroup;
    private Boolean deletionProtection;
    private List<String> enableCloudwatchLogsExports;
    private Boolean enableIamDatabaseAuthentication;
    private String engine;
    private String engineMode;
    private String engineVersion;
    private String finalDbSnapshotIdentifier;
    private DbGlobalClusterResource globalCluster;
    private KmsKeyResource kmsKey;
    private String masterUserPassword;
    private String masterUsername;
    private DbOptionGroupResource optionGroup;
    private Integer port;
    private String preferredBackupWindow;
    private String preferredMaintenanceWindow;
    private String preSignedUrl;
    private String replicationSourceIdentifier;
    private String snapshotIdentifier;
    private DbClusterS3Import s3Import;
    private ScalingConfiguration scalingConfiguration;
    private Boolean skipFinalSnapshot;
    private Boolean storageEncrypted;
    private List<SecurityGroupResource> vpcSecurityGroups;
    private String dbClusterInstanceClass;
    private Boolean enableGlobalWriteForwarding;
    private Integer iops;
    private Boolean manageMasterUserPassword;
    private KmsKeyResource masterUserSecretKmsKey;
    private ServerlessV2ScalingConfig serverlessV2ScalingConfiguration;
    private String storageType;
    private Integer allocatedStorage;
    private Boolean autoMinorVersionUpgrade;
    private Boolean copyTagsToSnapshot;
    private Boolean enableLocalWriteForwarding;
    private DbClusterResource sourceDbCluster;
    private Date restoreToTime;
    private String restoreType;
    private Boolean useLatestRestorableTime;

    // Read-only
    private String endpointAddress;
    private String readerEndpointAddress;
    private Date earliestRestorableTime;
    private Date latestRestorableTime;

    /**
     * Apply modifications in this request and any pending modifications asynchronously as soon as possible, regardless of the `preferred-maintenance-window`. Default is false.
     */
    @Updatable
    public Boolean getApplyImmediately() {
        if (applyImmediately == null) {
            applyImmediately = false;
        }

        return applyImmediately;
    }

    public void setApplyImmediately(Boolean applyImmediately) {
        this.applyImmediately = applyImmediately;
    }

    /**
     * A list of availability zones that instances in the DB cluster can be created in.
     */
    public List<String> getAvailabilityZones() {
        if (availabilityZones == null) {
            availabilityZones = new ArrayList<>();
        } else {
            Collections.sort(availabilityZones);
        }
        return availabilityZones;
    }

    public void setAvailabilityZones(List<String> availabilityZones) {
        this.availabilityZones = availabilityZones;
    }

    /**
     * The target backtrack window specified in seconds. Must be a number from ``0`` to ``259,200`` (72 hours) with ``0`` to disable backtracking.
     */
    @Updatable
    public Long getBackTrackWindow() {
        if (backTrackWindow == null) {
            backTrackWindow = 0L;
        }

        return backTrackWindow;
    }

    public void setBackTrackWindow(Long backTrackWindow) {
        this.backTrackWindow = backTrackWindow;
    }

    /**
     * The number of days to retain backups. Must be a value from ``1`` to ``35``.
     */
    @Updatable
    public Integer getBackupRetentionPeriod() {
        return backupRetentionPeriod;
    }

    public void setBackupRetentionPeriod(Integer backupRetentionPeriod) {
        this.backupRetentionPeriod = backupRetentionPeriod;
    }

    /**
     * The CharacterSet name for the DB cluster.
     */
    public String getCharacterSetName() {
        return characterSetName;
    }

    public void setCharacterSetName(String characterSetName) {
        this.characterSetName = characterSetName;
    }

    /**
     * The unique name of the DB Cluster.
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
     * The database name when creating the DB cluster. If omitted, no database will be created.
     */
    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    /**
     * The DB cluster parameter group to associate with. If omitted, ``default.aurora5.6`` is used.
     */
    @Updatable
    public DbClusterParameterGroupResource getDbClusterParameterGroup() {
        return dbClusterParameterGroup;
    }

    public void setDbClusterParameterGroup(DbClusterParameterGroupResource dbClusterParameterGroup) {
        this.dbClusterParameterGroup = dbClusterParameterGroup;
    }

    /**
     * A DB subnet group to use for this DB cluster.
     */
    public DbSubnetGroupResource getDbSubnetGroup() {
        return dbSubnetGroup;
    }

    public void setDbSubnetGroup(DbSubnetGroupResource dbSubnetGroup) {
        this.dbSubnetGroup = dbSubnetGroup;
    }

    /**
     * Enable deletion protection on the DB cluster. The default is false.
     */
    @Updatable
    public Boolean getDeletionProtection() {
        return deletionProtection;
    }

    public void setDeletionProtection(Boolean deletionProtection) {
        this.deletionProtection = deletionProtection;
    }

    /**
     * The list of log types to export to CloudWatch Logs. The values in the list depend on the DB engine being used. See `Publishing Database Logs to Amazon CloudWatch Logs <https://docs.aws.amazon.com/AmazonRDS/latest/AuroraUserGuide/USER_LogAccess.html#USER_LogAccess.Procedural.UploadtoCloudWatch>`_.
     */
    @Updatable
    public List<String> getEnableCloudwatchLogsExports() {
        return enableCloudwatchLogsExports;
    }

    public void setEnableCloudwatchLogsExports(List<String> enableCloudwatchLogsExports) {
        this.enableCloudwatchLogsExports = enableCloudwatchLogsExports;
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
     * The name of the database engine.
     */
    @Required
    @ValidStrings({"mysql", "postgres", "aurora-mysql", "aurora-postgresql"})
    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    /**
     * The DB engine mode of the DB cluster.
     */
    @ValidStrings({"provisioned", "serverless", "parallelquery", "global"})
    public String getEngineMode() {
        return engineMode;
    }

    public void setEngineMode(String engineMode) {
        this.engineMode = engineMode;
    }

    /**
     * The version number of the database engine to use.
     */
    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    /**
     * The name of the final snap shot when this DB cluster is deleted.
     */
    @Updatable
    public String getFinalDbSnapshotIdentifier() {
        return finalDbSnapshotIdentifier;
    }

    public void setFinalDbSnapshotIdentifier(String finalDbSnapshotIdentifier) {
        this.finalDbSnapshotIdentifier = finalDbSnapshotIdentifier;
    }

    /**
     * The global cluster that becomes the primary cluster in the new global database cluster.
     */
    public DbGlobalClusterResource getGlobalCluster() {
        return globalCluster;
    }

    public void setGlobalCluster(DbGlobalClusterResource globalCluster) {
        this.globalCluster = globalCluster;
    }

    /**
     * The AWS KMS key to encrypt the DB cluster.
     */
    public KmsKeyResource getKmsKey() {
        return kmsKey;
    }

    public void setKmsKey(KmsKeyResource kmsKey) {
        this.kmsKey = kmsKey;
    }

    /**
     * The password for the master database user.
     */
    @Updatable
    public String getMasterUserPassword() {
        return masterUserPassword;
    }

    public void setMasterUserPassword(String masterUserPassword) {
        this.masterUserPassword = masterUserPassword;
    }

    /**
     * The name of the master user for the DB cluster.
     */
    @Required
    public String getMasterUsername() {
        return masterUsername;
    }

    public void setMasterUsername(String masterUsername) {
        this.masterUsername = masterUsername;
    }

    /**
     * The name of the option group to associate with.
     */
    @Updatable
    public DbOptionGroupResource getOptionGroup() {
        return optionGroup;
    }

    public void setOptionGroup(DbOptionGroupResource optionGroup) {
        this.optionGroup = optionGroup;
    }

    /**
     * The port number on which the instances in the DB cluster accept connections. If omitted, default to aurora: ``3306`` or aurora-postgresql: ``5432``.
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
     */
    @Updatable
    public String getPreferredMaintenanceWindow() {
        return preferredMaintenanceWindow;
    }

    public void setPreferredMaintenanceWindow(String preferredMaintenanceWindow) {
        this.preferredMaintenanceWindow = preferredMaintenanceWindow;
    }

    /**
     * A URL that contains a Signature Version 4 signed request when performing cross-region replication from an encrypted DB cluster. Only applicable when ``replication-source-identifier`` is set and the replication source is encrypted.
     */
    public String getPreSignedUrl() {
        return preSignedUrl;
    }

    public void setPreSignedUrl(String preSignedUrl) {
        this.preSignedUrl = preSignedUrl;
    }

    /**
     * The Amazon Resource Name (ARN) of the source DB instance or DB cluster if this DB cluster is created as a Read Replica.
     */
    @Updatable
    public String getReplicationSourceIdentifier() {
        return replicationSourceIdentifier;
    }

    public void setReplicationSourceIdentifier(String replicationSourceIdentifier) {
        this.replicationSourceIdentifier = replicationSourceIdentifier;
    }

    /**
     * The identifier of the snapshot.
     */
    @ConflictsWith({"s3-import", "source-db-cluster"})
    public String getSnapshotIdentifier() {
        return snapshotIdentifier;
    }

    public void setSnapshotIdentifier(String snapshotIdentifier) {
        this.snapshotIdentifier = snapshotIdentifier;
    }

    /**
     * The s3 import to restore the database from.
     */
    @ConflictsWith({"snapshot-identifier", "source-db-cluster"})
    public DbClusterS3Import getS3Import() {
        return s3Import;
    }

    public void setS3Import(DbClusterS3Import s3Import) {
        this.s3Import = s3Import;
    }

    /**
     *  The scaling properties of the DB cluster. Only applicable for DB clusters in `serverless` DB engine mode.
     *
     * @subresource gyro.ws.rds.ScalingConfiguration
     */
    @Updatable
    public ScalingConfiguration getScalingConfiguration() {
        return scalingConfiguration;
    }

    public void setScalingConfiguration(ScalingConfiguration scalingConfiguration) {
        this.scalingConfiguration = scalingConfiguration;
    }

    /**
     * Skip the final DB snapshot when this DB cluster is deleted. The default is false.
     */
    @Updatable
    public Boolean getSkipFinalSnapshot() {
        return skipFinalSnapshot;
    }

    public void setSkipFinalSnapshot(Boolean skipFinalSnapshot) {
        this.skipFinalSnapshot = skipFinalSnapshot;
    }

    /**
     * Enable DB cluster encryption. The default is false.
     */
    public Boolean getStorageEncrypted() {
        return storageEncrypted;
    }

    public void setStorageEncrypted(Boolean storageEncrypted) {
        this.storageEncrypted = storageEncrypted;
    }

    /**
     * A list of Amazon VPC security groups to associate with.
     */
    @Updatable
    public List<SecurityGroupResource> getVpcSecurityGroups() {
        return vpcSecurityGroups;
    }

    public void setVpcSecurityGroups(List<SecurityGroupResource> vpcSecurityGroups) {
        this.vpcSecurityGroups = vpcSecurityGroups;
    }

    /**
     * The compute and memory capacity of each DB instance in the Multi-AZ DB cluster.
     * See `DB instance classes <https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Concepts.DBInstanceClass.html>`_.
     */
    @Updatable
    public String getDbClusterInstanceClass() {
        return dbClusterInstanceClass;
    }

    public void setDbClusterInstanceClass(String dbClusterInstanceClass) {
        this.dbClusterInstanceClass = dbClusterInstanceClass;
    }

    /**
     * When set to ``true``, enable this DB cluster to forward write operations to the primary cluster of a global cluster (Aurora global database).
     */
    @Updatable
    public Boolean getEnableGlobalWriteForwarding() {
        return enableGlobalWriteForwarding;
    }

    public void setEnableGlobalWriteForwarding(Boolean enableGlobalWriteForwarding) {
        this.enableGlobalWriteForwarding = enableGlobalWriteForwarding;
    }

    /**
     * The amount of Provisioned IOPS (input/output operations per second) to be initially allocated for each DB instance in the Multi-AZ DB cluster.
     */
    @Updatable
    public Integer getIops() {
        return iops;
    }

    public void setIops(Integer iops) {
        this.iops = iops;
    }

    /**
     * When set to ``true``, manage the master user password with Amazon Web Services Secrets Manager.
     */
    @Updatable
    public Boolean getManageMasterUserPassword() {
        return manageMasterUserPassword;
    }

    public void setManageMasterUserPassword(Boolean manageMasterUserPassword) {
        this.manageMasterUserPassword = manageMasterUserPassword;
    }

    /**
     * The Amazon Web Services KMS key to encrypt a secret that is automatically generated and managed in Amazon Web Services Secrets Manager.
     */
    @Updatable
    public KmsKeyResource getMasterUserSecretKmsKey() {
        return masterUserSecretKmsKey;
    }

    public void setMasterUserSecretKmsKey(KmsKeyResource masterUserSecretKmsKey) {
        this.masterUserSecretKmsKey = masterUserSecretKmsKey;
    }

    /**
     * The serverless scaling configuration.
     */
    @Updatable
    public ServerlessV2ScalingConfig getServerlessV2ScalingConfiguration() {
        return serverlessV2ScalingConfiguration;
    }

    public void setServerlessV2ScalingConfiguration(ServerlessV2ScalingConfig serverlessV2ScalingConfiguration) {
        this.serverlessV2ScalingConfiguration = serverlessV2ScalingConfiguration;
    }

    /**
     * The storage type to associate with the DB cluster.
     */
    @Updatable
    @ValidStrings({"gp3", "io1", "io2", "aurora", "aurora-iopt1"})
    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    /**
     * The amount of storage in gibibytes (GiB) to allocate to each DB instance in the Multi-AZ DB cluster.
     * Valid for Cluster Type: Multi-AZ DB clusters only
     */
    @Updatable
    public Integer getAllocatedStorage() {
        return allocatedStorage;
    }

    public void setAllocatedStorage(Integer allocatedStorage) {
        this.allocatedStorage = allocatedStorage;
    }

    /**
     * When set to ``true``, minor engine upgrades are applied automatically to the DB cluster during the maintenance window. Defaults to ``true``.
     */
    @Updatable
    public Boolean getAutoMinorVersionUpgrade() {
        return autoMinorVersionUpgrade;
    }

    public void setAutoMinorVersionUpgrade(Boolean autoMinorVersionUpgrade) {
        this.autoMinorVersionUpgrade = autoMinorVersionUpgrade;
    }

    /**
     * When set to ``true``, copies all tags from the DB cluster to snapshots of the DB cluster. The defaults to ``false``.
     */
    @Updatable
    public Boolean getCopyTagsToSnapshot() {
        return copyTagsToSnapshot;
    }

    public void setCopyTagsToSnapshot(Boolean copyTagsToSnapshot) {
        this.copyTagsToSnapshot = copyTagsToSnapshot;
    }

    /**
     * When set to ``true``, read replicas can forward write operations to the writer DB instance in the DB cluster.
     * By default, write operations aren't allowed on reader DB instances.
     */
    @Updatable
    public Boolean getEnableLocalWriteForwarding() {
        if (enableGlobalWriteForwarding == null) {
            enableGlobalWriteForwarding = false;
        }

        return enableLocalWriteForwarding;
    }

    public void setEnableLocalWriteForwarding(Boolean enableLocalWriteForwarding) {
        this.enableLocalWriteForwarding = enableLocalWriteForwarding;
    }

    /**
     * The source DB cluster to restore from.
     */
    @ConflictsWith({"snapshot-identifier", "s3-import"})
    public DbClusterResource getSourceDbCluster() {
        return sourceDbCluster;
    }

    public void setSourceDbCluster(DbClusterResource sourceDbCluster) {
        this.sourceDbCluster = sourceDbCluster;
    }

    /**
     * The date and time to restore the DB cluster to.
     */
    @ConflictsWith({"use-latest-restorable-time"})
    @DependsOn("source-db-cluster")
    public Date getRestoreToTime() {
        return restoreToTime;
    }

    public void setRestoreToTime(Date restoreToTime) {
        this.restoreToTime = restoreToTime;
    }

    /**
     * The type of restore to perform.
     */
    @DependsOn("source-db-cluster")
    @ValidStrings({"copy-on-write", "full-copy"})
    public String getRestoreType() {
        return restoreType;
    }

    public void setRestoreType(String restoreType) {
        this.restoreType = restoreType;
    }

    /**
     * When set to ``true``, restores the DB cluster to the latest restorable backup time. Defaults to ``false``.
     */
    @ConflictsWith({"restore-to-time"})
    @DependsOn("source-db-cluster")
    public Boolean getUseLatestRestorableTime() {
        return useLatestRestorableTime;
    }

    public void setUseLatestRestorableTime(Boolean useLatestRestorableTime) {
        this.useLatestRestorableTime = useLatestRestorableTime;
    }

    /**
     * DNS hostname to access the primary instance of the cluster.
     */
    @Output
    public String getEndpointAddress() {
        return endpointAddress;
    }

    public void setEndpointAddress(String endpointAddress) {
        this.endpointAddress = endpointAddress;
    }

    /**
     * DNS hostname to access the readers of the cluster.
     */
    @Output
    public String getReaderEndpointAddress() {
        return readerEndpointAddress;
    }

    public void setReaderEndpointAddress(String readerEndpointAddress) {
        this.readerEndpointAddress = readerEndpointAddress;
    }

    /**
     * The earliest restorable time for the DB cluster.
     */
    @Output
    public Date getEarliestRestorableTime() {
        return earliestRestorableTime;
    }

    public void setEarliestRestorableTime(Date earliestRestorableTime) {
        this.earliestRestorableTime = earliestRestorableTime;
    }

    /**
     * The latest restorable time for the DB cluster.
     */
    @Output
    public Date getLatestRestorableTime() {
        return latestRestorableTime;
    }

    public void setLatestRestorableTime(Date latestRestorableTime) {
        this.latestRestorableTime = latestRestorableTime;
    }

    @Override
    public void copyFrom(DBCluster cluster) {
        setAvailabilityZones(new ArrayList<>(cluster.availabilityZones()));

        setBackTrackWindow(cluster.backtrackWindow());
        setBackupRetentionPeriod(cluster.backupRetentionPeriod());
        setCharacterSetName(cluster.characterSetName());
        setDbClusterParameterGroup(findById(DbClusterParameterGroupResource.class, cluster.dbClusterParameterGroup()));
        setDbName(cluster.databaseName());
        setDbSubnetGroup(findById(DbSubnetGroupResource.class, cluster.dbSubnetGroup()));
        setDeletionProtection(cluster.deletionProtection());

        List<String> cwLogsExports = cluster.enabledCloudwatchLogsExports();
        setEnableCloudwatchLogsExports(cwLogsExports.isEmpty() ? null : cwLogsExports);
        setEnableIamDatabaseAuthentication(cluster.iamDatabaseAuthenticationEnabled());
        setEngine(cluster.engine());

        String version = cluster.engineVersion();
        if (getEngineVersion() != null) {
            version = version.substring(0, getEngineVersion().length());
        }

        setEngineVersion(version);
        setEngineMode(cluster.engineMode());
        setKmsKey(cluster.kmsKeyId() != null ? findById(KmsKeyResource.class, cluster.kmsKeyId()) : null);
        setMasterUsername(cluster.masterUsername());

        setOptionGroup(cluster.dbClusterOptionGroupMemberships().stream()
            .findFirst().map(s -> findById(DbOptionGroupResource.class, s.dbClusterOptionGroupName()))
            .orElse(null));

        setPort(cluster.port());
        setPreferredBackupWindow(cluster.preferredBackupWindow());
        setPreferredMaintenanceWindow(cluster.preferredMaintenanceWindow());
        setReplicationSourceIdentifier(cluster.replicationSourceIdentifier());

        if (cluster.scalingConfigurationInfo() != null) {
            ScalingConfiguration scalingConfiguration = new ScalingConfiguration();
            scalingConfiguration.setAutoPause(cluster.scalingConfigurationInfo().autoPause());
            scalingConfiguration.setMaxCapacity(cluster.scalingConfigurationInfo().maxCapacity());
            scalingConfiguration.setMinCapacity(cluster.scalingConfigurationInfo().minCapacity());
            scalingConfiguration.setSecondsUntilAutoPause(cluster.scalingConfigurationInfo().secondsUntilAutoPause());
            setScalingConfiguration(scalingConfiguration);
        }

        setStorageEncrypted(cluster.storageEncrypted());

        setVpcSecurityGroups(cluster.vpcSecurityGroups().stream()
            .map(g -> findById(SecurityGroupResource.class, g.vpcSecurityGroupId()))
            .collect(Collectors.toList()));

        setEndpointAddress(cluster.endpoint());
        setReaderEndpointAddress(cluster.readerEndpoint());
        setDbClusterInstanceClass(cluster.dbClusterInstanceClass());
        setEnableGlobalWriteForwarding(cluster.globalWriteForwardingRequested());
        setIops(cluster.iops());
        setStorageType(cluster.storageType() == null ? "aurora" : cluster.storageType());
        setAllocatedStorage(cluster.allocatedStorage());
        setAutoMinorVersionUpgrade(cluster.autoMinorVersionUpgrade());
        setCopyTagsToSnapshot(cluster.copyTagsToSnapshot());

        if (cluster.masterUserSecret() != null && cluster.masterUserSecret().kmsKeyId() != null) {
            setManageMasterUserPassword(true);
            setMasterUserSecretKmsKey(findById(KmsKeyResource.class, cluster.masterUserSecret().kmsKeyId()));
        }

        if (cluster.serverlessV2ScalingConfiguration() != null) {
            ServerlessV2ScalingConfig config = new ServerlessV2ScalingConfig();
            config.copyFrom(cluster.serverlessV2ScalingConfiguration());
            setServerlessV2ScalingConfiguration(config);
        }

        setEnableLocalWriteForwarding(
            LocalWriteForwardingStatus.REQUESTED.equals(cluster.localWriteForwardingStatus()) ||
                LocalWriteForwardingStatus.ENABLING.equals(cluster.localWriteForwardingStatus()) ||
                LocalWriteForwardingStatus.ENABLED.equals(cluster.localWriteForwardingStatus())
        );

        setEarliestRestorableTime(
            cluster.earliestRestorableTime() == null ? null : Date.from(cluster.earliestRestorableTime()));
        setLatestRestorableTime(
            cluster.latestRestorableTime() == null ? null : Date.from(cluster.latestRestorableTime()));
    }

    @Override
    protected boolean doRefresh() {
        RdsClient client = createClient(RdsClient.class);

        if (ObjectUtils.isBlank(getIdentifier())) {
            throw new GyroException("identifier is missing, unable to load db cluster.");
        }

        try {
            DescribeDbClustersResponse response = client.describeDBClusters(
                r -> r.dbClusterIdentifier(getIdentifier())
            );

            response.dbClusters().forEach(this::copyFrom);

        } catch (DbClusterNotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        RdsClient client = createClient(RdsClient.class);
        software.amazon.awssdk.services.rds.model.ScalingConfiguration scalingConfiguration =
            getScalingConfiguration() != null
                ? software.amazon.awssdk.services.rds.model.ScalingConfiguration.builder()
                .autoPause(getScalingConfiguration().getAutoPause())
                .maxCapacity(getScalingConfiguration().getMaxCapacity())
                .minCapacity(getScalingConfiguration().getMinCapacity())
                .secondsUntilAutoPause(getScalingConfiguration().getSecondsUntilAutoPause())
                .build()
                : null;

        if (getSourceDbCluster() != null) {
            RestoreDbClusterToPointInTimeResponse response =
                client.restoreDBClusterToPointInTime(
                    r -> r.backtrackWindow(getBackTrackWindow())
                        .copyTagsToSnapshot(getCopyTagsToSnapshot())
                        .dbClusterIdentifier(getIdentifier())
                        .dbClusterInstanceClass(getDbClusterInstanceClass())
                        .dbClusterParameterGroupName(
                            getDbClusterParameterGroup() != null ? getDbClusterParameterGroup().getName() : null)
                        .dbSubnetGroupName(getDbSubnetGroup() != null ? getDbSubnetGroup().getName() : null)
                        .deletionProtection(getDeletionProtection())
                        .enableCloudwatchLogsExports(getEnableCloudwatchLogsExports())
                        .enableIAMDatabaseAuthentication(getEnableIamDatabaseAuthentication())
                        .engineMode(getEngineMode())
                        .iops(getIops())
                        .kmsKeyId(getKmsKey() != null ? getKmsKey().getArn() : null)
                        .optionGroupName(getOptionGroup() != null ? getOptionGroup().getName() : null)
                        .port(getPort())
                        .restoreToTime(getRestoreToTime() == null ? null : getRestoreToTime().toInstant())
                        .restoreType(getRestoreType())
                        .scalingConfiguration(scalingConfiguration)
                        .serverlessV2ScalingConfiguration(getServerlessV2ScalingConfiguration() != null ?
                            getServerlessV2ScalingConfiguration().toServerlessV2ScalingConfiguration() : null)
                        .sourceDBClusterIdentifier(getSourceDbCluster().getIdentifier())
                        .storageType(getStorageType())
                        .useLatestRestorableTime(getUseLatestRestorableTime())
                        .vpcSecurityGroupIds(getVpcSecurityGroups() != null ? getVpcSecurityGroups()
                            .stream()
                            .map(SecurityGroupResource::getId)
                            .collect(Collectors.toList()) : null)
                );

            setArn(response.dbCluster().dbClusterArn());
            state.save();
            waitForActiveStatus(client, TimeoutSettings.Action.CREATE);

            ModifyDbClusterRequest.Builder request =
                ModifyDbClusterRequest.builder().applyImmediately(true).dbClusterIdentifier(getIdentifier());

            boolean modify = false;

            if (getBackupRetentionPeriod() != null) {
                request = request.backupRetentionPeriod(getBackupRetentionPeriod());
                modify = true;
            }

            if (getPreferredBackupWindow() != null) {
                request = request.preferredBackupWindow(getPreferredBackupWindow());
                modify = true;
            }

            if (getPreferredMaintenanceWindow() != null) {
                request = request.preferredMaintenanceWindow(getPreferredMaintenanceWindow());
                modify = true;
            }

            if (getManageMasterUserPassword() != null) {
                request = request.manageMasterUserPassword(getManageMasterUserPassword());
                modify = true;
            }

            if (getMasterUserSecretKmsKey() != null) {
                request = request.masterUserSecretKmsKeyId(getMasterUserSecretKmsKey().getId());
                modify = true;
            }

            if (getAutoMinorVersionUpgrade() != null) {
                request = request.autoMinorVersionUpgrade(getAutoMinorVersionUpgrade());
                modify = true;
            }

            if (modify) {
                client.modifyDBCluster(request.build());
                waitForActiveStatus(client, TimeoutSettings.Action.CREATE);
            }

        } else if (getSnapshotIdentifier() != null) {
            RestoreDbClusterFromSnapshotResponse response = client.restoreDBClusterFromSnapshot(
                r -> r.availabilityZones(getAvailabilityZones())
                    .snapshotIdentifier(getSnapshotIdentifier())
                    .backtrackWindow(getBackTrackWindow())
                    .databaseName(getDbName())
                    .dbClusterIdentifier(getIdentifier())
                    .dbClusterParameterGroupName(
                        getDbClusterParameterGroup() != null ? getDbClusterParameterGroup().getName() : null)
                    .dbSubnetGroupName(getDbSubnetGroup() != null ? getDbSubnetGroup().getName() : null)
                    .deletionProtection(getDeletionProtection())
                    .enableCloudwatchLogsExports(getEnableCloudwatchLogsExports())
                    .enableIAMDatabaseAuthentication(getEnableIamDatabaseAuthentication())
                    .engine(getEngine())
                    .engineVersion(getEngineVersion())
                    .engineMode(getEngineMode())
                    .kmsKeyId(getKmsKey() != null ? getKmsKey().getArn() : null)
                    .optionGroupName(getOptionGroup() != null ? getOptionGroup().getName() : null)
                    .port(getPort())
                    .scalingConfiguration(scalingConfiguration)
                    .vpcSecurityGroupIds(getVpcSecurityGroups() != null ? getVpcSecurityGroups()
                        .stream()
                        .map(SecurityGroupResource::getId)
                        .collect(Collectors.toList()) : null)
                    .dbClusterInstanceClass(getDbClusterInstanceClass())
                    .iops(getIops())
                    .serverlessV2ScalingConfiguration(getServerlessV2ScalingConfiguration() != null ?
                        getServerlessV2ScalingConfiguration().toServerlessV2ScalingConfiguration() : null)
                    .storageType(getStorageType())
                    .copyTagsToSnapshot(getCopyTagsToSnapshot())
            );

            setArn(response.dbCluster().dbClusterArn());
            state.save();
            waitForActiveStatus(client, TimeoutSettings.Action.CREATE);

            ModifyDbClusterRequest.Builder request =
                ModifyDbClusterRequest.builder().applyImmediately(true).dbClusterIdentifier(getIdentifier());

            boolean modify = false;

            if (getBackupRetentionPeriod() != null) {
                request = request.backupRetentionPeriod(getBackupRetentionPeriod());
                modify = true;
            }

            if (getPreferredBackupWindow() != null) {
                request = request.preferredBackupWindow(getPreferredBackupWindow());
                modify = true;
            }

            if (getPreferredMaintenanceWindow() != null) {
                request = request.preferredMaintenanceWindow(getPreferredMaintenanceWindow());
                modify = true;
            }

            if (getManageMasterUserPassword() != null) {
                request = request.manageMasterUserPassword(getManageMasterUserPassword());
                modify = true;
            }

            if (getMasterUserSecretKmsKey() != null) {
                request = request.masterUserSecretKmsKeyId(getMasterUserSecretKmsKey().getId());
                modify = true;
            }

            if (getAutoMinorVersionUpgrade() != null) {
                request = request.autoMinorVersionUpgrade(getAutoMinorVersionUpgrade());
                modify = true;
            }

            if (modify) {
                client.modifyDBCluster(request.build());
                waitForActiveStatus(client, TimeoutSettings.Action.CREATE);
            }

        } else if (getS3Import() != null) {
            RestoreDbClusterFromS3Response response = client.restoreDBClusterFromS3(r ->
                r.availabilityZones(getAvailabilityZones())
                    .backupRetentionPeriod(getBackupRetentionPeriod())
                    .characterSetName(getCharacterSetName())
                    .databaseName(getDbName())
                    .dbClusterIdentifier(getIdentifier())
                    .dbClusterParameterGroupName(
                        getDbClusterParameterGroup() != null ? getDbClusterParameterGroup().getName() : null)
                    .dbSubnetGroupName(getDbSubnetGroup() != null ? getDbSubnetGroup().getName() : null)
                    .engine(getEngine())
                    .engineVersion(getEngineVersion())
                    .port(getPort())
                    .masterUsername(getMasterUsername())
                    .masterUserPassword(getMasterUserPassword())
                    .optionGroupName(getOptionGroup() != null ? getOptionGroup().getName() : null)
                    .preferredBackupWindow(getPreferredBackupWindow())
                    .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
                    .storageEncrypted(getStorageEncrypted())
                    .kmsKeyId(getKmsKey() != null ? getKmsKey().getArn() : null)
                    .enableIAMDatabaseAuthentication(getEnableIamDatabaseAuthentication())
                    .sourceEngine(getS3Import().getSourceEngine())
                    .sourceEngineVersion(getS3Import().getSourceEngineVersion())
                    .s3BucketName(getS3Import().getS3Bucket().getName())
                    .s3Prefix(getS3Import().getS3Prefix())
                    .s3IngestionRoleArn(getS3Import().getS3IngestionRole().getArn())
                    .backtrackWindow(getBackTrackWindow())
                    .enableCloudwatchLogsExports(getEnableCloudwatchLogsExports())
                    .deletionProtection(getDeletionProtection())
                    .vpcSecurityGroupIds(getVpcSecurityGroups() != null ? getVpcSecurityGroups()
                        .stream()
                        .map(SecurityGroupResource::getId)
                        .collect(Collectors.toList()) : null)
                    .manageMasterUserPassword(getManageMasterUserPassword())
                    .masterUserSecretKmsKeyId(
                        getMasterUserSecretKmsKey() == null ? null : getMasterUserSecretKmsKey().getId())
                    .serverlessV2ScalingConfiguration(getServerlessV2ScalingConfiguration() != null ?
                        getServerlessV2ScalingConfiguration().toServerlessV2ScalingConfiguration() : null)
                    .storageType(getStorageType())
                    .copyTagsToSnapshot(getCopyTagsToSnapshot())
            );

            setArn(response.dbCluster().dbClusterArn());
            state.save();
            waitForActiveStatus(client, TimeoutSettings.Action.CREATE);

            ModifyDbClusterRequest.Builder request =
                ModifyDbClusterRequest.builder().applyImmediately(true).dbClusterIdentifier(getIdentifier());

            boolean modify = false;

            if (getBackupRetentionPeriod() != null) {
                request = request.backupRetentionPeriod(getBackupRetentionPeriod());
                modify = true;
            }

            if (getPreferredBackupWindow() != null) {
                request = request.preferredBackupWindow(getPreferredBackupWindow());
                modify = true;
            }

            if (getPreferredMaintenanceWindow() != null) {
                request = request.preferredMaintenanceWindow(getPreferredMaintenanceWindow());
                modify = true;
            }

            if (getManageMasterUserPassword() != null) {
                request = request.manageMasterUserPassword(getManageMasterUserPassword());
                modify = true;
            }

            if (getMasterUserSecretKmsKey() != null) {
                request = request.masterUserSecretKmsKeyId(getMasterUserSecretKmsKey().getId());
                modify = true;
            }

            if (getScalingConfiguration() != null) {
                request = request.scalingConfiguration(scalingConfiguration);
                modify = true;
            }

            if (getAutoMinorVersionUpgrade() != null) {
                request = request.autoMinorVersionUpgrade(getAutoMinorVersionUpgrade());
                modify = true;
            }

            if (modify) {
                client.modifyDBCluster(request.build());
                waitForActiveStatus(client, TimeoutSettings.Action.CREATE);
            }

        } else {
            CreateDbClusterRequest.Builder builder = CreateDbClusterRequest.builder();
            if (Boolean.TRUE.equals(getEnableGlobalWriteForwarding())) {
                builder.enableGlobalWriteForwarding(getEnableGlobalWriteForwarding());
            }

            CreateDbClusterResponse response = client.createDBCluster(
                builder.availabilityZones(getAvailabilityZones())
                    .backtrackWindow(getBackTrackWindow())
                    .backupRetentionPeriod(getBackupRetentionPeriod())
                    .characterSetName(getCharacterSetName())
                    .databaseName(getDbName())
                    .dbClusterIdentifier(getIdentifier())
                    .dbClusterParameterGroupName(
                        getDbClusterParameterGroup() != null ? getDbClusterParameterGroup().getName() : null)
                    .dbSubnetGroupName(getDbSubnetGroup() != null ? getDbSubnetGroup().getName() : null)
                    .deletionProtection(getDeletionProtection())
                    .enableCloudwatchLogsExports(getEnableCloudwatchLogsExports())
                    .enableIAMDatabaseAuthentication(getEnableIamDatabaseAuthentication())
                    .engine(getEngine())
                    .engineVersion(getEngineVersion())
                    .engineMode(getEngineMode())
                    .globalClusterIdentifier(getGlobalCluster() != null ? getGlobalCluster().getIdentifier() : null)
                    .kmsKeyId(getKmsKey() != null ? getKmsKey().getArn() : null)
                    .masterUsername(getMasterUsername())
                    .masterUserPassword(getMasterUserPassword())
                    .optionGroupName(getOptionGroup() != null ? getOptionGroup().getName() : null)
                    .port(getPort())
                    .preferredBackupWindow(getPreferredBackupWindow())
                    .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
                    .preSignedUrl(getPreSignedUrl())
                    .replicationSourceIdentifier(getReplicationSourceIdentifier())
                    .scalingConfiguration(scalingConfiguration)
                    .storageEncrypted(getStorageEncrypted())
                    .vpcSecurityGroupIds(getVpcSecurityGroups() != null ? getVpcSecurityGroups()
                        .stream()
                        .map(SecurityGroupResource::getId)
                        .collect(Collectors.toList()) : null)
                    .dbClusterInstanceClass(getDbClusterInstanceClass())
                    .iops(getIops())
                    .manageMasterUserPassword(getManageMasterUserPassword())
                    .masterUserSecretKmsKeyId(
                        getMasterUserSecretKmsKey() == null ? null : getMasterUserSecretKmsKey().getId())
                    .serverlessV2ScalingConfiguration(getServerlessV2ScalingConfiguration() != null ?
                        getServerlessV2ScalingConfiguration().toServerlessV2ScalingConfiguration() : null)
                    .storageType(getStorageType())
                    .allocatedStorage(getAllocatedStorage())
                    .autoMinorVersionUpgrade(getAutoMinorVersionUpgrade())
                    .copyTagsToSnapshot(getCopyTagsToSnapshot())
                    .enableLocalWriteForwarding(getEnableLocalWriteForwarding())
                    .build()
            );

            setArn(response.dbCluster().dbClusterArn());
            state.save();
            waitForActiveStatus(client, TimeoutSettings.Action.CREATE);
        }

        DescribeDbClustersResponse describeResponse = client.describeDBClusters(
            r -> r.dbClusterIdentifier(getIdentifier())
        );

        setEndpointAddress(describeResponse.dbClusters().get(0).endpoint());
        setReaderEndpointAddress(describeResponse.dbClusters().get(0).readerEndpoint());
    }

    private boolean isAvailable(RdsClient client) {
        DescribeDbClustersResponse describeResponse = client.describeDBClusters(
            r -> r.dbClusterIdentifier(getIdentifier())
        );

        return describeResponse.dbClusters().get(0).status().equals("available");
    }

    @Override
    protected void doUpdate(Resource config, Set<String> changedProperties) {
        RdsClient client = createClient(RdsClient.class);
        DbClusterResource current = (DbClusterResource) config;
        software.amazon.awssdk.services.rds.model.ScalingConfiguration scalingConfiguration =
            getScalingConfiguration() != null
                ? software.amazon.awssdk.services.rds.model.ScalingConfiguration.builder()
                .autoPause(getScalingConfiguration().getAutoPause())
                .maxCapacity(getScalingConfiguration().getMaxCapacity())
                .minCapacity(getScalingConfiguration().getMinCapacity())
                .secondsUntilAutoPause(getScalingConfiguration().getSecondsUntilAutoPause())
                .build()
                : null;

        String clusterParameterGroupName =
            getDbClusterParameterGroup() != null ? getDbClusterParameterGroup().getName() : null;
        String optionGroupName = getOptionGroup() != null ? getOptionGroup().getName() : null;
        List<String> vpcSecurityGroupIds = getVpcSecurityGroups() != null ? getVpcSecurityGroups()
            .stream()
            .map(SecurityGroupResource::getId)
            .collect(Collectors.toList()) : null;

        try {
            ModifyDbClusterRequest.Builder builder = ModifyDbClusterRequest.builder();
            if (!Objects.equals(getEnableGlobalWriteForwarding(), current.getEnableGlobalWriteForwarding())) {
                builder.enableGlobalWriteForwarding(getEnableGlobalWriteForwarding());
            }

            client.modifyDBCluster(
                builder.applyImmediately(getApplyImmediately())
                    .backtrackWindow(Objects.equals(getBackTrackWindow(), current.getBackTrackWindow())
                        ? null : getBackTrackWindow())
                    .backupRetentionPeriod(Objects.equals(
                        getBackupRetentionPeriod(),
                        current.getBackupRetentionPeriod()) ? null : getBackupRetentionPeriod())
                    .cloudwatchLogsExportConfiguration(c -> c.enableLogTypes(getEnableCloudwatchLogsExports()))
                    .dbClusterIdentifier(current.getIdentifier())
                    .dbClusterParameterGroupName(Objects.equals(
                        getDbClusterParameterGroup(),
                        current.getDbClusterParameterGroup()) ? null : clusterParameterGroupName)
                    .deletionProtection(Objects.equals(getDeletionProtection(), current.getDeletionProtection())
                        ? null : getDeletionProtection())
                    .enableIAMDatabaseAuthentication(Objects.equals(
                        getEnableIamDatabaseAuthentication(),
                        current.getEnableIamDatabaseAuthentication()) ? null : getEnableIamDatabaseAuthentication())
                    .engineVersion(Objects.equals(getEngineVersion(), current.getEngineVersion())
                        ? null : getEngineVersion())
                    .masterUserPassword(Objects.equals(getMasterUserPassword(), current.getMasterUserPassword())
                        ? null : getMasterUserPassword())
                    .optionGroupName(Objects.equals(getOptionGroup(), current.getOptionGroup())
                        ? null : optionGroupName)
                    .port(Objects.equals(getPort(), current.getPort()) ? null : getPort())
                    .preferredBackupWindow(Objects.equals(
                        getPreferredBackupWindow(),
                        current.getPreferredBackupWindow()) ? null : getPreferredBackupWindow())
                    .preferredMaintenanceWindow(Objects.equals(
                        getPreferredMaintenanceWindow(),
                        current.getPreferredMaintenanceWindow()) ? null : getPreferredMaintenanceWindow())
                    .scalingConfiguration(scalingConfiguration)
                    .vpcSecurityGroupIds(Objects.equals(getVpcSecurityGroups(), current.getVpcSecurityGroups())
                        ? null : vpcSecurityGroupIds)
                    .dbClusterInstanceClass(getDbClusterInstanceClass())
                    .iops(getIops())
                    .manageMasterUserPassword(getManageMasterUserPassword())
                    .masterUserSecretKmsKeyId(
                        getMasterUserSecretKmsKey() == null ? null : getMasterUserSecretKmsKey().getId())
                    .serverlessV2ScalingConfiguration(getServerlessV2ScalingConfiguration() != null ?
                        getServerlessV2ScalingConfiguration().toServerlessV2ScalingConfiguration() : null)
                    .storageType(getStorageType())
                    .allocatedStorage(getAllocatedStorage())
                    .autoMinorVersionUpgrade(getAutoMinorVersionUpgrade())
                    .copyTagsToSnapshot(getCopyTagsToSnapshot())
                    .enableLocalWriteForwarding(Objects.equals(getEnableLocalWriteForwarding(),
                        current.getEnableLocalWriteForwarding()) ? null : getEnableLocalWriteForwarding())
                    .build()
            );
        } catch (InvalidDbClusterStateException ex) {
            throw new GyroException(ex.getLocalizedMessage());
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        RdsClient client = createClient(RdsClient.class);
        if (getGlobalCluster() != null) {
            client.removeFromGlobalCluster(
                r -> r.dbClusterIdentifier(getArn())
                    .globalClusterIdentifier(getGlobalCluster().getIdentifier())
            );
        }

        client.deleteDBCluster(
            r -> r.dbClusterIdentifier(getIdentifier())
                .finalDBSnapshotIdentifier(
                    Boolean.TRUE.equals(getSkipFinalSnapshot()) ? null : getFinalDbSnapshotIdentifier())
                .skipFinalSnapshot(getSkipFinalSnapshot())
        );

        Wait.atMost(5, TimeUnit.MINUTES)
            .checkEvery(15, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.DELETE)
            .prompt(true)
            .until(() -> isDeleted(client));
    }

    private boolean isDeleted(RdsClient client) {
        try {
            client.describeDBClusters(
                r -> r.dbClusterIdentifier(getIdentifier())
            );

        } catch (DbClusterNotFoundException ex) {
            return true;
        }

        return false;
    }

    private void waitForActiveStatus(RdsClient client, TimeoutSettings.Action action) {
        boolean waitResult = Wait.atMost(10, TimeUnit.MINUTES)
            .checkEvery(30, TimeUnit.SECONDS)
            .resourceOverrides(this, action)
            .prompt(false)
            .until(() -> isAvailable(client));

        if (!waitResult) {
            throw new GyroException("Unable to reach 'available' state for rds db cluster - " + getIdentifier());
        }
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        ArrayList<ValidationError> errors = new ArrayList<>();

        if ((getSkipFinalSnapshot() == null || Boolean.FALSE.equals(getSkipFinalSnapshot())) &&
            getFinalDbSnapshotIdentifier() == null) {
            errors.add(new ValidationError(
                this,
                "final-db-snapshot-identifier",
                "'final-db-snapshot-identifier' is required when 'skip-final-snapshot' is unspecified or set to 'false'."
            ));
        }

        if (getSourceDbCluster() != null) {
            if (getRestoreToTime() == null &&
                (getUseLatestRestorableTime() == null || Boolean.FALSE.equals(getUseLatestRestorableTime()))) {
                errors.add(new ValidationError(
                    this,
                    "restore-to-time",
                    "Either 'restore-to-time' or 'use-latest-restorable-time' is required when restoring from a 'source-db-cluster'."
                ));
            }

            if (getRestoreType() != null && getRestoreType().equals("copy-on-write") &&
                (getRestoreToTime() != null || !Boolean.TRUE.equals(getUseLatestRestorableTime()))) {
                errors.add(new ValidationError(
                    this,
                    "restore-to-time",
                    "'restore-to-time' cannot be set when 'restore-type' is set to 'copy-on-write'. Use 'use-latest-restorable-time' instead and set it to `true` instead."
                ));
            }
        }

        return errors;
    }
}
