package gyro.aws.docdb;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import gyro.core.resource.ResourceOutput;
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
import java.util.stream.Collectors;

/**
 * Creates an Document db cluster.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::db-cluster db-cluster-example
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
@ResourceName("db-cluster")
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
    @ResourceDiffProperty(updatable = true)
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
    @ResourceDiffProperty(updatable = true)
    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    /**
     * Associated db cluster parameter group. (Required)
     */
    @ResourceDiffProperty(updatable = true)
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
    @ResourceDiffProperty(updatable = true)
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * Set preferred backup window. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public String getPreferredBackupWindow() {
        return preferredBackupWindow;
    }

    public void setPreferredBackupWindow(String preferredBackupWindow) {
        this.preferredBackupWindow = preferredBackupWindow;
    }

    /**
     * Set preferred maintenance window. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public String getPreferredMaintenanceWindow() {
        return preferredMaintenanceWindow;
    }

    public void setPreferredMaintenanceWindow(String preferredMaintenanceWindow) {
        this.preferredMaintenanceWindow = preferredMaintenanceWindow;
    }

    /**
     * Associated vpc security group ids. (Required)
     */
    @ResourceDiffProperty(updatable = true)
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
    @ResourceDiffProperty(updatable = true)
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

        DescribeDbClustersResponse response = client.describeDBClusters(
            r -> r.dbClusterIdentifier(getDbClusterIdentifier())
        );

        if (!response.dbClusters().isEmpty()) {
            DBCluster dbCluster = response.dbClusters().get(0);

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
        } else {
            return false;
        }
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
                .tags(toDocDbTags(getTags()))
        );

        setDbClusterResourceId(response.dbCluster().dbClusterResourceId());
        setArn(response.dbCluster().dbClusterArn());

        waitForAvailability(client);
        refresh();
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
        waitForAvailability(client);

        refresh();
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

        waitForDelete(client);
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

    private void waitForAvailability(DocDbClient client) {
        boolean available = false;
        int count = 0;
        while (!available && count < 6) {
            DescribeDbClustersResponse response = waitHelper(count, client, 10000);

            available = response.dbClusters().get(0).status().equals("available");
            count++;
        }
    }

    private void waitForDelete(DocDbClient client) {
        boolean deleted = false;
        int count = 0;
        while (!deleted && count < 6) {
            try {
                DescribeDbClustersResponse response = waitHelper(count, client, 10000);

                deleted = response.dbClusters().isEmpty();
            } catch (DbClusterNotFoundException ex) {
                deleted = true;
            }
            count++;
        }
    }

    private DescribeDbClustersResponse waitHelper(int count, DocDbClient client, long interval) {
        if (count > 0) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return client.describeDBClusters(
            r -> r.dbClusterIdentifier(getDbClusterIdentifier())
        );
    }
}
