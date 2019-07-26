package gyro.aws.docdb;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.aws.kms.KmsKeyResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.diff.Context;
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
 *         db-cluster-identifier: "db-cluster-example"
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
    private String dbClusterIdentifier;
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

    private String dbClusterResourceId;
    private String status;
    private String arn;

    /**
     * Set backup retention period. Minimum 1. (Required)
     */
    @Updatable
    public Integer getBackupRetentionPeriod() {
        return backupRetentionPeriod;
    }

    public void setBackupRetentionPeriod(Integer backupRetentionPeriod) {
        this.backupRetentionPeriod = backupRetentionPeriod;
    }

    /**
     * Name of the cluster. (Required)
     */
    @Id
    public String getDbClusterIdentifier() {
        return dbClusterIdentifier;
    }

    public void setDbClusterIdentifier(String dbClusterIdentifier) {
        this.dbClusterIdentifier = dbClusterIdentifier;
    }

    /**
     * Associated db subnet group. (Required)
     */
    public DbSubnetGroupResource getDbSubnetGroup() {
        return dbSubnetGroup;
    }

    public void setDbSubnetGroup(DbSubnetGroupResource dbSubnetGroup) {
        this.dbSubnetGroup = dbSubnetGroup;
    }

    /**
     * Engine for the cluster. (Required)
     */
    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    /**
     * Engine version for the cluster. (Required)
     */
    @Updatable
    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    /**
     * Associated db cluster parameter group. (Required)
     */
    @Updatable
    public DbClusterParameterGroupResource getDbClusterParamGroup() {
        return dbClusterParamGroup;
    }

    public void setDbClusterParamGroup(DbClusterParameterGroupResource dbClusterParamGroup) {
        this.dbClusterParamGroup = dbClusterParamGroup;
    }

    /**
     * Associated kms key. (Optional)
     */
    public KmsKeyResource getKmsKey() {
        return kmsKey;
    }

    public void setKmsKey(KmsKeyResource kmsKey) {
        this.kmsKey = kmsKey;
    }

    /**
     * Master username. (Required)
     */
    public String getMasterUsername() {
        return masterUsername;
    }

    public void setMasterUsername(String masterUsername) {
        this.masterUsername = masterUsername;
    }

    /**
     * Master user password. (Required)
     */
    public String getMasterUserPassword() {
        return masterUserPassword;
    }

    public void setMasterUserPassword(String masterUserPassword) {
        this.masterUserPassword = masterUserPassword;
    }

    /**
     * Set the access port. (Required)
     */
    @Updatable
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * Set preferred backup window. (Required)
     */
    @Updatable
    public String getPreferredBackupWindow() {
        return preferredBackupWindow;
    }

    public void setPreferredBackupWindow(String preferredBackupWindow) {
        this.preferredBackupWindow = preferredBackupWindow;
    }

    /**
     * Set preferred maintenance window. (Required)
     */
    @Updatable
    public String getPreferredMaintenanceWindow() {
        return preferredMaintenanceWindow;
    }

    public void setPreferredMaintenanceWindow(String preferredMaintenanceWindow) {
        this.preferredMaintenanceWindow = preferredMaintenanceWindow;
    }

    /**
     * Associated vpc security groups. (Required)
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
     * Encrypt storage. (Optional)
     */
    public Boolean getStorageEncrypted() {
        return storageEncrypted;
    }

    public void setStorageEncrypted(Boolean storageEncrypted) {
        this.storageEncrypted = storageEncrypted;
    }

    /**
     * Enabled cloud watch log exports. (Optional)
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
    public String getDbClusterResourceId() {
        return dbClusterResourceId;
    }

    public void setDbClusterResourceId(String dbClusterResourceId) {
        this.dbClusterResourceId = dbClusterResourceId;
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
    protected String getId() {
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
    protected void doCreate(GyroUI ui, Context context) {
        DocDbClient client = createClient(DocDbClient.class);

        CreateDbClusterResponse response = client.createDBCluster(
            o -> o.backupRetentionPeriod(getBackupRetentionPeriod())
                .dbClusterIdentifier(getDbClusterIdentifier())
                .dbSubnetGroupName(getDbSubnetGroup().getDbSubnetGroupName())
                .engine(getEngine())
                .engineVersion(getEngineVersion())
                .dbClusterParameterGroupName(getDbClusterParamGroup().getDbClusterParamGroupName())
                .kmsKeyId(getKmsKey() != null ? getKmsKey().getKeyId() : null)
                .masterUsername(getMasterUsername())
                .masterUserPassword(getMasterUserPassword())
                .port(getPort())
                .preferredBackupWindow(getPreferredBackupWindow())
                .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
                .vpcSecurityGroupIds(getVpcSecurityGroups().stream().map(SecurityGroupResource::getGroupId).collect(Collectors.toList()))
                .storageEncrypted(getStorageEncrypted())
                .enableCloudwatchLogsExports(getEnableCloudwatchLogsExports())
        );

        setDbClusterResourceId(response.dbCluster().dbClusterResourceId());
        setArn(response.dbCluster().dbClusterArn());

        context.save();

        boolean waitResult = Wait.atMost(3, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> isAvailable(client));

        if (!waitResult) {
            throw new GyroException("Unable to reach 'available' state for docdb cluster - " + getDbClusterIdentifier());
        }
    }

    @Override
    protected void doUpdate(Resource current, Set changedProperties) {
        DocDbClient client = createClient(DocDbClient.class);

        DbClusterResource resource = (DbClusterResource) current;

        ModifyDbClusterRequest.Builder builder = ModifyDbClusterRequest.builder()
            .backupRetentionPeriod(getBackupRetentionPeriod())
            .dbClusterIdentifier(getDbClusterIdentifier())
            .dbClusterParameterGroupName(getDbClusterParamGroup().getDbClusterParamGroupName())
            .masterUserPassword(getMasterUserPassword())
            .port(getPort())
            .preferredBackupWindow(getPreferredBackupWindow())
            .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
            .vpcSecurityGroupIds(getVpcSecurityGroups().stream().map(SecurityGroupResource::getGroupId).collect(Collectors.toList()));

        if (!resource.getEngineVersion().equals(getEngineVersion())) {
            builder.engineVersion(getEngineVersion());
        }

        client.modifyDBCluster(builder.build());

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> isAvailable(client));
    }

    @Override
    public void delete(GyroUI ui, Context context) {
        DocDbClient client = createClient(DocDbClient.class);

        DeleteDbClusterRequest.Builder builder = DeleteDbClusterRequest
            .builder()
            .dbClusterIdentifier(getDbClusterIdentifier());

        if (ObjectUtils.isBlank(getPostDeleteSnapshotIdentifier())) {
            builder.skipFinalSnapshot(true);
        } else {
            builder.finalDBSnapshotIdentifier(getPostDeleteSnapshotIdentifier())
                .skipFinalSnapshot(false);
        }

        client.deleteDBCluster(builder.build());

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> getDbCluster(client) == null);
    }

    @Override
    public void copyFrom(DBCluster dbCluster) {

        setBackupRetentionPeriod(dbCluster.backupRetentionPeriod());
        setDbClusterIdentifier(dbCluster.dbClusterIdentifier());
        setDbSubnetGroup(findById(DbSubnetGroupResource.class, dbCluster.dbSubnetGroup()));
        setEngine(dbCluster.engine());
        setEngineVersion(dbCluster.engineVersion());
        setDbClusterParamGroup(findById(DbClusterParameterGroupResource.class, dbCluster.dbClusterParameterGroup()));
        setDbClusterIdentifier(dbCluster.dbClusterIdentifier());
        setKmsKey(findById(KmsKeyResource.class, dbCluster.kmsKeyId()));
        setMasterUsername(dbCluster.masterUsername());
        setPort(dbCluster.port());
        setPreferredBackupWindow(dbCluster.preferredBackupWindow());
        setPreferredMaintenanceWindow(dbCluster.preferredMaintenanceWindow());
        setVpcSecurityGroups(dbCluster.vpcSecurityGroups().stream().map(v -> findById(SecurityGroupResource.class, v.vpcSecurityGroupId())).collect(Collectors.toSet()));
        setStorageEncrypted(dbCluster.storageEncrypted());
        setEnableCloudwatchLogsExports(dbCluster.enabledCloudwatchLogsExports().isEmpty() ? new ArrayList<>() : dbCluster.enabledCloudwatchLogsExports());
        setStatus(dbCluster.status());
        setDbClusterResourceId(dbCluster.dbClusterResourceId());
        setArn(dbCluster.dbClusterArn());
    }

    private boolean isAvailable(DocDbClient client) {
        DBCluster dbCluster = getDbCluster(client);

        return dbCluster != null && dbCluster.status().equals("available");
    }

    private DBCluster getDbCluster(DocDbClient client) {
        DBCluster dbCluster = null;

        if (ObjectUtils.isBlank(getDbClusterIdentifier())) {
            throw new GyroException("db-cluster-identifier is missing, unable to load db cluster.");
        }

        try {
            DescribeDbClustersResponse response = client.describeDBClusters(
                r -> r.dbClusterIdentifier(getDbClusterIdentifier())
            );

            if (!response.dbClusters().isEmpty()) {
                dbCluster = response.dbClusters().get(0);
            }

        } catch (DbClusterNotFoundException ex) {

        }

        return dbCluster;
    }

}
