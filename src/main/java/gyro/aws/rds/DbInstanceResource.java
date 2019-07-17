package gyro.aws.rds;

import gyro.aws.Copyable;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.aws.kms.KmsKeyResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.scope.State;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.CreateDbInstanceResponse;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DBSecurityGroupMembership;
import software.amazon.awssdk.services.rds.model.DbInstanceNotFoundException;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesResponse;
import software.amazon.awssdk.services.rds.model.DomainMembership;
import software.amazon.awssdk.services.rds.model.InvalidDbInstanceStateException;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Create a db instance.
 *
 * .. code-block:: gyro
 *
 *    aws::db-instance db-instance-example
 *        allocated-storage: 20
 *        db-instance-identifier: "db-instance-example"
 *        storage-type: "gp2"
 *        engine: "mysql"
 *        engine-version: "5.7"
 *        db-instance-class: "db.t2.micro"
 *        master-username: "user"
 *        master-user-password: "password"
 *        delete-automated-backups: true
 *        skip-final-snapshot: true
 *        tags: {
 *            Name: "db-instance-example"
 *        }
 *    end
 */
@Type("db-instance")
public class DbInstanceResource extends RdsTaggableResource implements Copyable<DBInstance> {

    private Integer allocatedStorage;
    private Boolean allowMajorVersionUpgrade;
    private Boolean applyImmediately;
    private Boolean autoMinorVersionUpgrade;
    private String availabilityZone;
    private Integer backupRetentionPeriod;
    private String characterSetName;
    private Boolean copyTagsToSnapshot;
    private DbClusterResource dbCluster;
    private String dbInstanceClass;
    private String dbInstanceIdentifier;
    private String dbName;
    private DbParameterGroupResource dbParameterGroup;
    private List<String> dbSecurityGroups;
    private DbSubnetGroupResource dbSubnetGroup;
    private Boolean deleteAutomatedBackups;
    private Boolean deletionProtection;
    private String domain;
    private String domainIamRoleName;
    private List<String> enableCloudwatchLogsExports;
    private Boolean enableIamDatabaseAuthentication;
    private Boolean enablePerformanceInsights;
    private String engine;
    private String engineVersion;
    private String finalDbSnapshotIdentifier;
    private Integer iops;
    private KmsKeyResource kmsKey;
    private String licenseModel;
    private String masterUserPassword;
    private String masterUsername;
    private Integer monitoringInterval;
    private String monitoringRoleArn;
    private Boolean multiAz;
    private DbOptionGroupResource optionGroup;
    private KmsKeyResource performanceInsightsKmsKey;
    private Integer performanceInsightsRetentionPeriod;
    private Integer port;
    private String preferredBackupWindow;
    private String preferredMaintenanceWindow;
    private Integer promotionTier;
    private Boolean publiclyAccessible;
    private Boolean skipFinalSnapshot;
    private Boolean storageEncrypted;
    private String storageType;
    private String tdeCredentialArn;
    private String tdeCredentialPassword;
    private String timezone;
    private List<SecurityGroupResource> vpcSecurityGroups;
    private String endpointAddress;

    /**
     * The amount of storage to allocate in gibibytes. Not applicable for Aurora.
     */
    @Updatable
    public Integer getAllocatedStorage() {
        return allocatedStorage;
    }

    public void setAllocatedStorage(Integer allocatedStorage) {
        this.allocatedStorage = allocatedStorage;
    }

    /**
     * Allow or disallow major version upgrades.
     */
    public Boolean getAllowMajorVersionUpgrade() {
        return allowMajorVersionUpgrade;
    }

    public void setAllowMajorVersionUpgrade(Boolean allowMajorVersionUpgrade) {
        this.allowMajorVersionUpgrade = allowMajorVersionUpgrade;
    }

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
     * Allow or disallow automatic minor engine version upgrades during the maintenance window. Defaults to true (allow).
     */
    @Updatable
    public Boolean getAutoMinorVersionUpgrade() {
        return autoMinorVersionUpgrade;
    }

    public void setAutoMinorVersionUpgrade(Boolean autoMinorVersionUpgrade) {
        this.autoMinorVersionUpgrade = autoMinorVersionUpgrade;
    }

    /**
     * The availability zone to launch this DB instance in. The default picks a random availability zone in the currently configured region. Leave this value unset if ``multi-az`` is set to true.
     */
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * The number of days to retain backups. Must be a value from ``0`` to ``35`` where ``0`` to disables automated backups. Not applicable for Aurora.
     */
    @Updatable
    public Integer getBackupRetentionPeriod() {
        return backupRetentionPeriod;
    }

    public void setBackupRetentionPeriod(Integer backupRetentionPeriod) {
        this.backupRetentionPeriod = backupRetentionPeriod;
    }

    /**
     * Sets the character set name for this DB instance on supported engines.
     */
    public String getCharacterSetName() {
        return characterSetName;
    }

    public void setCharacterSetName(String characterSetName) {
        this.characterSetName = characterSetName;
    }

    /**
     * Copy the DB instance tags to snapshots. Default is false.
     */
    @Updatable
    public Boolean getCopyTagsToSnapshot() {
        return copyTagsToSnapshot;
    }

    public void setCopyTagsToSnapshot(Boolean copyTagsToSnapshot) {
        this.copyTagsToSnapshot = copyTagsToSnapshot;
    }

    /**
     * The existing DB cluster this DB instance belongs to. Only applies to Aurora engine.
     */
    public DbClusterResource getDbCluster() {
        return dbCluster;
    }

    public void setDbCluster(DbClusterResource dbCluster) {
        this.dbCluster = dbCluster;
    }

    /**
     * The DB instance type. See `DB Instance Class <https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Concepts.DBInstanceClass.html>`_. (Required)
     */
    @Updatable
    public String getDbInstanceClass() {
        return dbInstanceClass;
    }

    public void setDbInstanceClass(String dbInstanceClass) {
        this.dbInstanceClass = dbInstanceClass;
    }

    /**
     * The unique name of the DB instance. (Required)
     */
    @Id
    public String getDbInstanceIdentifier() {
        return dbInstanceIdentifier;
    }

    public void setDbInstanceIdentifier(String dbInstanceIdentifier) {
        this.dbInstanceIdentifier = dbInstanceIdentifier;
    }

    /**
     * The database name (or Oracle System ID for Oracle) when creating the DB instance. Not applicable for SQL Server. See `CreateDBInstance <https://docs.aws.amazon.com/AmazonRDS/latest/APIReference/API_CreateDBInstance.html>`_.
     */
    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    /**
     * The DB parameter group to use for this instance. The default DB Parameter Group is used if this is not set.
     */
    @Updatable
    public DbParameterGroupResource getDbParameterGroup() {
        return dbParameterGroup;
    }

    public void setDbParameterGroup(DbParameterGroupResource dbParameterGroup) {
        this.dbParameterGroup = dbParameterGroup;
    }

    /**
     * A list of security groups to use with this DB instance. This is for EC2 Classic, for VPCs use ``vpc-security-group-ids``.
     */
    @Updatable
    public List<String> getDbSecurityGroups() {
        return dbSecurityGroups;
    }

    public void setDbSecurityGroups(List<String> dbSecurityGroups) {
        this.dbSecurityGroups = dbSecurityGroups;
    }

    /**
     * A DB subnet group to use for this DB instance.
     */
    public DbSubnetGroupResource getDbSubnetGroup() {
        return dbSubnetGroup;
    }

    public void setDbSubnetGroup(DbSubnetGroupResource dbSubnetGroup) {
        this.dbSubnetGroup = dbSubnetGroup;
    }

    /**
     * Delete automated backups after the DB instance is deleted. Default to false (keep automated backups).
     */
    @Updatable
    public Boolean getDeleteAutomatedBackups() {
        return deleteAutomatedBackups;
    }

    public void setDeleteAutomatedBackups(Boolean deleteAutomatedBackups) {
        this.deleteAutomatedBackups = deleteAutomatedBackups;
    }

    /**
     * Enable deletion protection on the DB instance. This prevents the database from accidentally being deleted. The default is false.
     */
    @Updatable
    public Boolean getDeletionProtection() {
        return deletionProtection;
    }

    public void setDeletionProtection(Boolean deletionProtection) {
        this.deletionProtection = deletionProtection;
    }

    /**
     * The Active Directory Domain to create the instance in, only applicable to SQL Server engine.
     */
    @Updatable
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * The name of the IAM role to be used when making API calls to the Directory Service, only applicable to SQL Server engine.
     */
    @Updatable
    public String getDomainIamRoleName() {
        return domainIamRoleName;
    }

    public void setDomainIamRoleName(String domainIamRoleName) {
        this.domainIamRoleName = domainIamRoleName;
    }

    /**
     * The list of log types to export to CloudWatch Logs. Valid values depend on the DB engine being used. See `Publishing Database Logs to Amazon CloudWatch Logs <https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/USER_LogAccess.html#USER_LogAccess.Procedural.UploadtoCloudWatch>`_.
     */
    @Updatable
    public List<String> getEnableCloudwatchLogsExports() {
        return enableCloudwatchLogsExports;
    }

    public void setEnableCloudwatchLogsExports(List<String> enableCloudwatchLogsExports) {
        this.enableCloudwatchLogsExports = enableCloudwatchLogsExports;
    }

    /**
     * Enable mapping IAM accounts to database accounts, default to false (disable). Not applicable to Aurora.
     */
    @Updatable
    public Boolean getEnableIamDatabaseAuthentication() {
        return enableIamDatabaseAuthentication;
    }

    public void setEnableIamDatabaseAuthentication(Boolean enableIamDatabaseAuthentication) {
        this.enableIamDatabaseAuthentication = enableIamDatabaseAuthentication;
    }

    /**
     * Enable Performance Insights for the DB instance. The default to false.
     */
    @Updatable
    public Boolean getEnablePerformanceInsights() {
        return enablePerformanceInsights;
    }

    public void setEnablePerformanceInsights(Boolean enablePerformanceInsights) {
        this.enablePerformanceInsights = enablePerformanceInsights;
    }

    /**
     * The name of the database engine to use for this DB Instance. Valid values are ``aurora``, ``aurora-mysql``, ``aurora-postgresql``, ``mariadb``, ``mysql``, ``oracle-ee``, ``oracle-se2``, ``oracle-se1``, ``oracle-se``, ``postgres``, ``sqlserver-ee``, ``sqlserver-se``, ``sqlserver-ex``, ``sqlserver-we``.
     */
    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    /**
     * The version number of the database engine to use.
     */
    @Updatable
    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    /**
     * The name of the final snap shot when deleting this DB instance.
     */
    public String getFinalDbSnapshotIdentifier() {
        return finalDbSnapshotIdentifier;
    }

    public void setFinalDbSnapshotIdentifier(String finalDbSnapshotIdentifier) {
        this.finalDbSnapshotIdentifier = finalDbSnapshotIdentifier;
    }

    /**
     * The amount of Provisioned IOPS to be allocated. The value must be equal to or greater than 1000. Required if `storage-type` is ``io1``.
     */
    @Updatable
    public Integer getIops() {
        return iops;
    }

    public void setIops(Integer iops) {
        this.iops = iops;
    }

    /**
     * The AWS KMS key to encrypt the DB instance.
     */
    public KmsKeyResource getKmsKey() {
        return kmsKey;
    }

    public void setKmsKey(KmsKeyResource kmsKey) {
        this.kmsKey = kmsKey;
    }

    /**
     * License model for this DB instance. Valid values: ``license-included``, ``bring-your-own-license``, ``general-public-license``.
     */
    @Updatable
    public String getLicenseModel() {
        return licenseModel;
    }

    public void setLicenseModel(String licenseModel) {
        this.licenseModel = licenseModel;
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
     * The name for the master user.
     */
    public String getMasterUsername() {
        return masterUsername;
    }

    public void setMasterUsername(String masterUsername) {
        this.masterUsername = masterUsername;
    }

    /**
     * Enhanced Monitoring metrics collecting interval in seconds. The default is 0 (disable collection). Valid Values: ``0``, ``1``, ``5``, ``10``, ``15``, ``30``, ``60``.
     */
    @Updatable
    public Integer getMonitoringInterval() {
        return monitoringInterval;
    }

    public void setMonitoringInterval(Integer monitoringInterval) {
        this.monitoringInterval = monitoringInterval;
    }

    /**
     * The ARN for the IAM role that permits RDS to send enhanced monitoring metrics to Amazon CloudWatch Logs.
     */
    @Updatable
    public String getMonitoringRoleArn() {
        return monitoringRoleArn;
    }

    public void setMonitoringRoleArn(String monitoringRoleArn) {
        this.monitoringRoleArn = monitoringRoleArn;
    }

    /**
     * Launch this DB instance in multiple availability zones. If true, ``availability-zone`` must not be set.
     */
    @Updatable
    public Boolean getMultiAz() {
        return multiAz;
    }

    public void setMultiAz(Boolean multiAz) {
        this.multiAz = multiAz;
    }

    /**
     * The option group to associate with.
     */
    @Updatable
    public DbOptionGroupResource getOptionGroup() {
        return optionGroup;
    }

    public void setOptionGroup(DbOptionGroupResource optionGroup) {
        this.optionGroup = optionGroup;
    }

    /**
     * The AWS KMS key for encryption of Performance Insights data. Not applicable if `enable-performance-insights` is false.
     */
    @Updatable
    public KmsKeyResource getPerformanceInsightsKmsKey() {
        return performanceInsightsKmsKey;
    }

    public void setPerformanceInsightsKmsKey(KmsKeyResource performanceInsightsKmsKey) {
        this.performanceInsightsKmsKey = performanceInsightsKmsKey;
    }

    /**
     * How many days to retain Performance Insights data. Valid values are ``7`` or ``731`` (2 years).
     */
    @Updatable
    public Integer getPerformanceInsightsRetentionPeriod() {
        return performanceInsightsRetentionPeriod;
    }

    public void setPerformanceInsightsRetentionPeriod(Integer performanceInsightsRetentionPeriod) {
        this.performanceInsightsRetentionPeriod = performanceInsightsRetentionPeriod;
    }

    /**
     * The port number on which the database accepts connections.
     */
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
     * The order of the Aurora Replica is promoted to the primary instance after the existing primary instance fails. Valid Values: 0 - 15.
     */
    @Updatable
    public Integer getPromotionTier() {
        return promotionTier;
    }

    public void setPromotionTier(Integer promotionTier) {
        this.promotionTier = promotionTier;
    }

    /**
     * The public accessibility of the DB instance. If true, this DB instance will have a public DNS name and public IP.
     */
    public Boolean getPubliclyAccessible() {
        if (publiclyAccessible == null) {
            publiclyAccessible = false;
        }

        return publiclyAccessible;
    }

    public void setPubliclyAccessible(Boolean publiclyAccessible) {
        this.publiclyAccessible = publiclyAccessible;
    }

    /**
     * Skip the final DB snapshot when this DB instance is deleted. Default is false.
     */
    public Boolean getSkipFinalSnapshot() {
        return skipFinalSnapshot;
    }

    public void setSkipFinalSnapshot(Boolean skipFinalSnapshot) {
        this.skipFinalSnapshot = skipFinalSnapshot;
    }

    /**
     * Enable DB instance encryption. Default to false.
     */
    public Boolean getStorageEncrypted() {
        return storageEncrypted;
    }

    public void setStorageEncrypted(Boolean storageEncrypted) {
        this.storageEncrypted = storageEncrypted;
    }

    /**
     * The storage type for the DB instance. Valid values are ``standard``, ``gp2``, ``io1``.
     */
    @Updatable
    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    /**
     * The ARN from the key store for Transparent data encryption.
     */
    public String getTdeCredentialArn() {
        return tdeCredentialArn;
    }

    public void setTdeCredentialArn(String tdeCredentialArn) {
        this.tdeCredentialArn = tdeCredentialArn;
    }

    /**
     * The password for the given ARN from the key store.
     */
    public String getTdeCredentialPassword() {
        return tdeCredentialPassword;
    }

    public void setTdeCredentialPassword(String tdeCredentialPassword) {
        this.tdeCredentialPassword = tdeCredentialPassword;
    }

    /**
     * The time zone of the DB instance. The time zone parameter is currently supported only by Microsoft SQL Server.
     */
    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
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
     * DNS hostname to access this database at.
     */
    @Output
    public String getEndpointAddress() {
        return endpointAddress;
    }

    public void setEndpointAddress(String endpointAddress) {
        this.endpointAddress = endpointAddress;
    }

    @Override
    public void copyFrom(DBInstance instance) {
        setAllocatedStorage(instance.allocatedStorage());
        setAutoMinorVersionUpgrade(instance.autoMinorVersionUpgrade());
        setAvailabilityZone(instance.availabilityZone());
        setBackupRetentionPeriod(instance.backupRetentionPeriod());
        setCharacterSetName(instance.characterSetName());
        setCopyTagsToSnapshot(instance.copyTagsToSnapshot());
        setDbCluster(instance.dbClusterIdentifier() != null ? findById(DbClusterResource.class, instance.dbClusterIdentifier()) : null);
        setDbInstanceClass(instance.dbInstanceClass());
        setDbInstanceIdentifier(instance.dbInstanceIdentifier());
        setDbName(instance.dbName());

        setDbParameterGroup(instance.dbParameterGroups().stream()
            .findFirst().map(s -> findById(DbParameterGroupResource.class, s.dbParameterGroupName()))
            .orElse(null));

        setDbSecurityGroups(instance.dbSecurityGroups().stream()
            .map(DBSecurityGroupMembership::dbSecurityGroupName)
            .collect(Collectors.toList()));

        setDbSubnetGroup(instance.dbSubnetGroup() != null ? findById(DbSubnetGroupResource.class, instance.dbSubnetGroup().dbSubnetGroupName()) : null);
        setDeletionProtection(instance.deletionProtection());

        setDomain(instance.domainMemberships().stream()
            .findFirst().map(DomainMembership::domain)
            .orElse(null));

        setDomainIamRoleName(instance.domainMemberships().stream()
            .findFirst().map(DomainMembership::iamRoleName)
            .orElse(null));

        List<String> cwLogsExports = instance.enabledCloudwatchLogsExports();
        setEnableCloudwatchLogsExports(cwLogsExports.isEmpty() ? null : cwLogsExports);
        setEnableIamDatabaseAuthentication(instance.iamDatabaseAuthenticationEnabled());
        setEnablePerformanceInsights(instance.performanceInsightsEnabled());
        setEngine(instance.engine());

        String version = instance.engineVersion();
        if (getEngineVersion() != null) {
            version = version.substring(0, getEngineVersion().length());
        }

        setEngineVersion(version);
        setIops(instance.iops());
        setKmsKey(instance.kmsKeyId() != null ? findById(KmsKeyResource.class, instance.kmsKeyId()) : null);
        setLicenseModel(instance.licenseModel());
        setMasterUsername(instance.masterUsername());
        setMonitoringInterval(instance.monitoringInterval());
        setMonitoringRoleArn(instance.monitoringRoleArn());
        setMultiAz(instance.multiAZ());

        setOptionGroup(instance.optionGroupMemberships().stream()
            .findFirst().map(s -> findById(DbOptionGroupResource.class, s.optionGroupName()))
            .orElse(null));

        setPerformanceInsightsKmsKey(instance.performanceInsightsKMSKeyId() != null ? findById(KmsKeyResource.class, instance.performanceInsightsKMSKeyId()) : null);
        setPerformanceInsightsRetentionPeriod(instance.performanceInsightsRetentionPeriod());
        setPort(instance.dbInstancePort());
        setPreferredBackupWindow(instance.preferredBackupWindow());
        setPreferredMaintenanceWindow(instance.preferredMaintenanceWindow());
        setPromotionTier(instance.promotionTier());
        setPubliclyAccessible(instance.publiclyAccessible());
        setStorageEncrypted(instance.storageEncrypted());
        setStorageType(instance.storageType());
        setTdeCredentialArn(instance.tdeCredentialArn());
        setTimezone(instance.timezone());
        setVpcSecurityGroups(instance.vpcSecurityGroups().stream()
            .map(s -> findById(SecurityGroupResource.class, s.vpcSecurityGroupId()))
            .collect(Collectors.toList()));
        setArn(instance.dbInstanceArn());

        if (instance.endpoint() != null) {
            setEndpointAddress(instance.endpoint().address());
        }
    }

    @Override
    public boolean doRefresh() {
        RdsClient client = createClient(RdsClient.class);

        if (ObjectUtils.isBlank(getDbInstanceIdentifier())) {
            throw new GyroException("db-instance-identifier is missing, unable to load db instance.");
        }

        try {
            DescribeDbInstancesResponse response = client.describeDBInstances(
                r -> r.dbInstanceIdentifier(getDbInstanceIdentifier())
            );

            response.dbInstances().forEach(this::copyFrom);

        } catch (DbInstanceNotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    public void doCreate() {
        RdsClient client = createClient(RdsClient.class);
        CreateDbInstanceResponse response = client.createDBInstance(
            r -> r.allocatedStorage(getAllocatedStorage())
                    .autoMinorVersionUpgrade(getAutoMinorVersionUpgrade())
                    .availabilityZone(getAvailabilityZone())
                    .backupRetentionPeriod(getBackupRetentionPeriod())
                    .characterSetName(getCharacterSetName())
                    .copyTagsToSnapshot(getCopyTagsToSnapshot())
                    .dbClusterIdentifier(getDbCluster() != null ? getDbCluster().getDbClusterIdentifier() : null)
                    .dbInstanceClass(getDbInstanceClass())
                    .dbInstanceIdentifier(getDbInstanceIdentifier())
                    .dbName(getDbName())
                    .dbParameterGroupName(getDbParameterGroup() != null ? getDbParameterGroup().getName() : null)
                    .dbSecurityGroups(getDbSecurityGroups())
                    .dbSubnetGroupName(getDbSubnetGroup() != null ? getDbSubnetGroup().getGroupName() : null)
                    .deletionProtection(getDeletionProtection())
                    .domain(getDomain())
                    .domainIAMRoleName(getDomainIamRoleName())
                    .enableCloudwatchLogsExports(getEnableCloudwatchLogsExports())
                    .enableIAMDatabaseAuthentication(getEnableIamDatabaseAuthentication())
                    .enablePerformanceInsights(getEnablePerformanceInsights())
                    .engine(getEngine())
                    .engineVersion(getEngineVersion())
                    .iops(getIops())
                    .kmsKeyId(getKmsKey() != null ? getKmsKey().getKeyArn() : null)
                    .licenseModel(getLicenseModel())
                    .masterUsername(getMasterUsername())
                    .masterUserPassword(getMasterUserPassword())
                    .monitoringInterval(getMonitoringInterval())
                    .monitoringRoleArn(getMonitoringRoleArn())
                    .multiAZ(getMultiAz())
                    .optionGroupName(getOptionGroup() != null ? getOptionGroup().getName() : null)
                    .performanceInsightsKMSKeyId(getPerformanceInsightsKmsKey() != null ? getPerformanceInsightsKmsKey().getKeyArn() : null)
                    .performanceInsightsRetentionPeriod(getPerformanceInsightsRetentionPeriod())
                    .port(getPort())
                    .preferredBackupWindow(getPreferredBackupWindow())
                    .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
                    .promotionTier(getPromotionTier())
                    .publiclyAccessible(getPubliclyAccessible())
                    .storageEncrypted(getStorageEncrypted())
                    .storageType(getStorageType())
                    .tdeCredentialArn(getTdeCredentialArn())
                    .tdeCredentialPassword(getTdeCredentialPassword())
                    .timezone(getTimezone())
                    .vpcSecurityGroupIds(getVpcSecurityGroups() != null ? getVpcSecurityGroups()
                        .stream()
                        .map(SecurityGroupResource::getGroupId)
                        .collect(Collectors.toList()) : null)
        );

        setArn(response.dbInstance().dbInstanceArn());

        Wait.atMost(10, TimeUnit.MINUTES)
            .checkEvery(15, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> isAvailable(client));

        DescribeDbInstancesResponse describeResponse = client.describeDBInstances(
            r -> r.dbInstanceIdentifier(getDbInstanceIdentifier())
        );

        setEndpointAddress(describeResponse.dbInstances().get(0).endpoint().address());
    }

    private boolean isAvailable(RdsClient client) {
        DescribeDbInstancesResponse describeResponse = client.describeDBInstances(
            r -> r.dbInstanceIdentifier(getDbInstanceIdentifier())
        );

        return describeResponse.dbInstances().get(0).dbInstanceStatus().equals("available");
    }

    @Override
    public void doUpdate(Resource config, Set<String> changedProperties) {
        RdsClient client = createClient(RdsClient.class);
        DbInstanceResource current = (DbInstanceResource) config;

        String parameterGroupName = getDbParameterGroup() != null ? getDbParameterGroup().getName() : null;
        String subnetGroupName = getDbSubnetGroup() != null ? getDbSubnetGroup().getGroupName() : null;
        String optionGroupName = getOptionGroup() != null ? getOptionGroup().getName() : null;
        String performanceInsightsKmsKeyId = getPerformanceInsightsKmsKey() != null ? getPerformanceInsightsKmsKey().getKeyArn() : null;
        List<String> vpcSecurityGroupIds = getVpcSecurityGroups() != null ? getVpcSecurityGroups()
            .stream()
            .map(SecurityGroupResource::getGroupId)
            .collect(Collectors.toList()) : null;

        try {
            client.modifyDBInstance(
                r -> r.allocatedStorage(Objects.equals(getAllocatedStorage(), current.getAllocatedStorage()) ? null : getAllocatedStorage())
                    .applyImmediately(Objects.equals(getApplyImmediately(), current.getApplyImmediately()) ? null : getApplyImmediately())
                    .allowMajorVersionUpgrade(Objects.equals(getAllowMajorVersionUpgrade(), current.getAllowMajorVersionUpgrade())
                        ? null : getAllowMajorVersionUpgrade())
                    .autoMinorVersionUpgrade(Objects.equals(getAutoMinorVersionUpgrade(), current.getAutoMinorVersionUpgrade())
                        ? null : getAutoMinorVersionUpgrade())
                    .backupRetentionPeriod(Objects.equals(getBackupRetentionPeriod(), current.getBackupRetentionPeriod())
                        ? null : getBackupRetentionPeriod())
                    .cloudwatchLogsExportConfiguration(c -> c.enableLogTypes(getEnableCloudwatchLogsExports()))
                    .copyTagsToSnapshot(Objects.equals(getCopyTagsToSnapshot(), current.getCopyTagsToSnapshot()) ? null : getCopyTagsToSnapshot())
                    .dbInstanceClass(Objects.equals(getDbInstanceClass(), current.getDbInstanceClass()) ? null : getDbInstanceClass())
                    .dbInstanceIdentifier(getDbInstanceIdentifier())
                    .dbParameterGroupName(Objects.equals(getDbParameterGroup(), current.getDbParameterGroup())
                        ? null : parameterGroupName)
                    .dbSecurityGroups(Objects.equals(getDbSecurityGroups(), current.getDbSecurityGroups()) ? null : getDbSecurityGroups())
                    .dbSubnetGroupName(Objects.equals(getDbSubnetGroup(), current.getDbSubnetGroup()) ? null : subnetGroupName)
                    .deletionProtection(Objects.equals(getDeletionProtection(), current.getDeletionProtection()) ? null : getDeletionProtection())
                    .domain(Objects.equals(getDomain(), current.getDomain()) ? null : getDomain())
                    .domainIAMRoleName(Objects.equals(getDomainIamRoleName(), current.getDomainIamRoleName()) ? null : getDomainIamRoleName())
                    .enableIAMDatabaseAuthentication(Objects.equals(
                        getEnableIamDatabaseAuthentication(), current.getEnableIamDatabaseAuthentication())
                        ? null : getEnableIamDatabaseAuthentication())
                    .enablePerformanceInsights(Objects.equals(getEnablePerformanceInsights(), current.getEnablePerformanceInsights())
                        ? null : getEnablePerformanceInsights())
                    .engineVersion(Objects.equals(getEngineVersion(), current.getEngineVersion()) ? null : getEngineVersion())
                    .iops(Objects.equals(getIops(), current.getIops()) ? null : getIops())
                    .licenseModel(Objects.equals(getLicenseModel(), current.getLicenseModel()) ? null : getLicenseModel())
                    .masterUserPassword(Objects.equals(getMasterUserPassword(), current.getMasterUserPassword()) ? null : getMasterUserPassword())
                    .monitoringInterval(Objects.equals(getMonitoringInterval(), current.getMonitoringInterval()) ? null : getMonitoringInterval())
                    .monitoringRoleArn(Objects.equals(getMonitoringRoleArn(), current.getMonitoringRoleArn()) ? null : getMonitoringRoleArn())
                    .multiAZ(Objects.equals(getMultiAz(), current.getMultiAz()) ? null : getMultiAz())
                    .optionGroupName(Objects.equals(getOptionGroup(), current.getOptionGroup()) ? null : optionGroupName)
                    .performanceInsightsKMSKeyId(Objects.equals(getPerformanceInsightsKmsKey(), current.getPerformanceInsightsKmsKey())
                        ? null : performanceInsightsKmsKeyId)
                    .performanceInsightsRetentionPeriod(Objects.equals(
                        getPerformanceInsightsRetentionPeriod(), current.getPerformanceInsightsRetentionPeriod())
                        ? null : getPerformanceInsightsRetentionPeriod())
                    .preferredBackupWindow(Objects.equals(getPreferredBackupWindow(), current.getPreferredBackupWindow())
                        ? null : getPreferredBackupWindow())
                    .preferredMaintenanceWindow(Objects.equals(getPreferredMaintenanceWindow(), current.getPreferredMaintenanceWindow())
                        ? null : getPreferredMaintenanceWindow())
                    .promotionTier(Objects.equals(getPromotionTier(), current.getPromotionTier()) ? null : getPromotionTier())
                    .publiclyAccessible(Objects.equals(getPubliclyAccessible(), current.getPubliclyAccessible()) ? null : getPubliclyAccessible())
                    .storageType(Objects.equals(getStorageType(), current.getStorageType()) ? null : getStorageType())
                    .tdeCredentialArn(Objects.equals(getTdeCredentialArn(), current.getTdeCredentialArn()) ? null : getTdeCredentialArn())
                    .tdeCredentialPassword(Objects.equals(getTdeCredentialPassword(), current.getTdeCredentialPassword())
                        ? null : getTdeCredentialPassword())
                    .vpcSecurityGroupIds(Objects.equals(getVpcSecurityGroups(), current.getVpcSecurityGroups())
                        ? null : vpcSecurityGroupIds)
            );
        } catch (InvalidDbInstanceStateException ex) {
            throw new GyroException(ex.getLocalizedMessage());
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        RdsClient client = createClient(RdsClient.class);
        client.deleteDBInstance(
            r -> r.dbInstanceIdentifier(getDbInstanceIdentifier())
                    .finalDBSnapshotIdentifier(getFinalDbSnapshotIdentifier())
                    .skipFinalSnapshot(getSkipFinalSnapshot())
                    .deleteAutomatedBackups(getDeleteAutomatedBackups())
        );

        Wait.atMost(5, TimeUnit.MINUTES)
            .checkEvery(15, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> isDeleted(client));
    }

    private boolean isDeleted(RdsClient client) {
        try {
            client.describeDBInstances(
                r -> r.dbInstanceIdentifier(getDbInstanceIdentifier())
            );

        } catch (DbInstanceNotFoundException ex) {
            return true;
        }

        return false;
    }

}
