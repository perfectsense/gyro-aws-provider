package gyro.aws.docdb;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.GyroException;
import gyro.core.Wait;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceOutput;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceUpdatable;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.CreateDbClusterResponse;
import software.amazon.awssdk.services.docdb.model.DBCluster;
import software.amazon.awssdk.services.docdb.model.DbClusterNotFoundException;
import software.amazon.awssdk.services.docdb.model.DeleteDbClusterRequest;
import software.amazon.awssdk.services.docdb.model.DescribeDbClustersResponse;
import software.amazon.awssdk.services.docdb.model.ModifyDbClusterRequest;
import software.amazon.awssdk.services.docdb.model.VpcSecurityGroupMembership;

import java.util.ArrayList;
import java.util.Collections;
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
 *         db-subnet-group-name: $(aws::db-subnet-group db-subnet-group-db-cluster-example | db-subnet-group-name)
 *         engine: "docdb"
 *         engine-version: "3.6.0"
 *         db-cluster-param-group-name: $(aws::db-cluster-param-group db-cluster-param-group-db-cluster-example | db-cluster-param-group-name)
 *         master-username: "master"
 *         master-user-password: "masterpassword"
 *         port: 27017
 *         preferred-backup-window: "00:00-00:30"
 *         preferred-maintenance-window: "wed:03:28-wed:03:58"
 *         vpc-security-group-ids: [
 *             $(aws::security-group security-group-db-cluster-example-1 | group-id),
 *             $(aws::security-group security-group-db-cluster-example-2 | group-id)
 *         ]
 *         storage-encrypted: false
 *         backup-retention-period: 1
 *         tags: {
 *             Name: "db-cluster-example"
 *         }
 *         post-delete-snapshot-identifier: "db-cluster-example-backup-snapshot"
 *     end
 */
@ResourceType("docdb-cluster")
public class DbClusterResource extends DocDbTaggableResource {
    private Integer backupRetentionPeriod;
    private String dbClusterIdentifier;
    private String dbSubnetGroupName;
    private String engine;
    private String engineVersion;
    private String dbClusterParamGroupName;
    private String kmsKeyId;
    private String masterUsername;
    private String masterUserPassword;
    private Integer port;
    private String preferredBackupWindow;
    private String preferredMaintenanceWindow;
    private List<String> vpcSecurityGroupIds;
    private Boolean storageEncrypted;
    private List<String> enableCloudwatchLogsExports;
    private String postDeleteSnapshotIdentifier;

    private String DbClusterResourceId;
    private String status;
    private String arn;

    /**
     * Set backup retention period. Minimum 1. (Required)
     */
    @ResourceUpdatable
    public Integer getBackupRetentionPeriod() {
        return backupRetentionPeriod;
    }

    public void setBackupRetentionPeriod(Integer backupRetentionPeriod) {
        this.backupRetentionPeriod = backupRetentionPeriod;
    }

    /**
     * Name of the cluster. (Required)
     */
    public String getDbClusterIdentifier() {
        return dbClusterIdentifier;
    }

    public void setDbClusterIdentifier(String dbClusterIdentifier) {
        this.dbClusterIdentifier = dbClusterIdentifier;
    }

    /**
     * Associated db subnet group. (Required)
     */
    public String getDbSubnetGroupName() {
        return dbSubnetGroupName;
    }

    public void setDbSubnetGroupName(String dbSubnetGroupName) {
        this.dbSubnetGroupName = dbSubnetGroupName;
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
    @ResourceUpdatable
    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    /**
     * Associated db cluster parameter group. (Required)
     */
    @ResourceUpdatable
    public String getDbClusterParamGroupName() {
        return dbClusterParamGroupName;
    }

    public void setDbClusterParamGroupName(String dbClusterParamGroupName) {
        this.dbClusterParamGroupName = dbClusterParamGroupName;
    }

    /**
     * Associated kms key id. (Optional)
     */
    public String getKmsKeyId() {
        return kmsKeyId;
    }

    public void setKmsKeyId(String kmsKeyId) {
        this.kmsKeyId = kmsKeyId;
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
    @ResourceUpdatable
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * Set preferred backup window. (Required)
     */
    @ResourceUpdatable
    public String getPreferredBackupWindow() {
        return preferredBackupWindow;
    }

    public void setPreferredBackupWindow(String preferredBackupWindow) {
        this.preferredBackupWindow = preferredBackupWindow;
    }

    /**
     * Set preferred maintenance window. (Required)
     */
    @ResourceUpdatable
    public String getPreferredMaintenanceWindow() {
        return preferredMaintenanceWindow;
    }

    public void setPreferredMaintenanceWindow(String preferredMaintenanceWindow) {
        this.preferredMaintenanceWindow = preferredMaintenanceWindow;
    }

    /**
     * Associated vpc security group ids. (Required)
     */
    @ResourceUpdatable
    public List<String> getVpcSecurityGroupIds() {
        if (vpcSecurityGroupIds == null) {
            vpcSecurityGroupIds = new ArrayList<>();
        }

        if (!vpcSecurityGroupIds.isEmpty() && !vpcSecurityGroupIds.contains(null)) {
            Collections.sort(vpcSecurityGroupIds);
        }

        return vpcSecurityGroupIds;
    }

    public void setVpcSecurityGroupIds(List<String> vpcSecurityGroupIds) {
        this.vpcSecurityGroupIds = vpcSecurityGroupIds;
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
    @ResourceUpdatable
    public String getPostDeleteSnapshotIdentifier() {
        return postDeleteSnapshotIdentifier;
    }

    public void setPostDeleteSnapshotIdentifier(String postDeleteSnapshotIdentifier) {
        this.postDeleteSnapshotIdentifier = postDeleteSnapshotIdentifier;
    }

    /**
     * The id for the db cluster.
     */
    @ResourceOutput
    public String getDbClusterResourceId() {
        return DbClusterResourceId;
    }

    public void setDbClusterResourceId(String dbClusterResourceId) {
        DbClusterResourceId = dbClusterResourceId;
    }

    /**
     * The status for the db cluster.
     */
    @ResourceOutput
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * The arn for the db cluster.
     */
    @ResourceOutput
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

        setBackupRetentionPeriod(dbCluster.backupRetentionPeriod());
        setDbClusterIdentifier(dbCluster.dbClusterIdentifier());
        setDbSubnetGroupName(dbCluster.dbSubnetGroup());
        setEngine(dbCluster.engine());
        setEngineVersion(dbCluster.engineVersion());
        setDbClusterParamGroupName(dbCluster.dbClusterParameterGroup());
        setDbClusterIdentifier(dbCluster.dbClusterIdentifier());
        setKmsKeyId(dbCluster.kmsKeyId());
        setMasterUsername(dbCluster.masterUsername());
        setPort(dbCluster.port());
        setPreferredBackupWindow(dbCluster.preferredBackupWindow());
        setPreferredMaintenanceWindow(dbCluster.preferredMaintenanceWindow());
        setVpcSecurityGroupIds(dbCluster.vpcSecurityGroups().stream().map(VpcSecurityGroupMembership::vpcSecurityGroupId).collect(Collectors.toList()));
        setStorageEncrypted(dbCluster.storageEncrypted());
        setEnableCloudwatchLogsExports(dbCluster.enabledCloudwatchLogsExports().isEmpty() ? new ArrayList<>() : dbCluster.enabledCloudwatchLogsExports());
        setStatus(dbCluster.status());
        setDbClusterResourceId(dbCluster.dbClusterResourceId());
        setArn(dbCluster.dbClusterArn());

        return true;
    }

    @Override
    protected void doCreate() {
        DocDbClient client = createClient(DocDbClient.class);

        CreateDbClusterResponse response = client.createDBCluster(
            o -> o.backupRetentionPeriod(getBackupRetentionPeriod())
                .dbClusterIdentifier(getDbClusterIdentifier())
                .dbSubnetGroupName(getDbSubnetGroupName())
                .engine(getEngine())
                .engineVersion(getEngineVersion())
                .dbClusterParameterGroupName(getDbClusterParamGroupName())
                .kmsKeyId(getKmsKeyId())
                .masterUsername(getMasterUsername())
                .masterUserPassword(getMasterUserPassword())
                .port(getPort())
                .preferredBackupWindow(getPreferredBackupWindow())
                .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
                .vpcSecurityGroupIds(getVpcSecurityGroupIds())
                .storageEncrypted(getStorageEncrypted())
                .enableCloudwatchLogsExports(getEnableCloudwatchLogsExports())
                //.tags(toDocDbTags(getTags()))
        );

        setDbClusterResourceId(response.dbCluster().dbClusterResourceId());
        setArn(response.dbCluster().dbClusterArn());



        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> isAvailable(client));
    }

    @Override
    protected void doUpdate(Resource current, Set changedProperties) {
        DocDbClient client = createClient(DocDbClient.class);

        DbClusterResource resource = (DbClusterResource) current;

        ModifyDbClusterRequest.Builder builder = ModifyDbClusterRequest.builder()
            .backupRetentionPeriod(getBackupRetentionPeriod())
            .dbClusterIdentifier(getDbClusterIdentifier())
            .dbClusterParameterGroupName(getDbClusterParamGroupName())
            .masterUserPassword(getMasterUserPassword())
            .port(getPort())
            .preferredBackupWindow(getPreferredBackupWindow())
            .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
            .vpcSecurityGroupIds(getVpcSecurityGroupIds());

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
    public void delete() {
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
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("db cluster");

        if (!ObjectUtils.isBlank(getDbClusterIdentifier())) {
            sb.append(" - ").append(getDbClusterIdentifier());
        }

        return sb.toString();
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
