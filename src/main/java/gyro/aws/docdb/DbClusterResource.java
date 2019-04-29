package gyro.aws.docdb;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.CreateDbClusterResponse;
import software.amazon.awssdk.services.docdb.model.DBCluster;
import software.amazon.awssdk.services.docdb.model.DeleteDbClusterRequest;
import software.amazon.awssdk.services.docdb.model.DescribeDbClustersResponse;
import software.amazon.awssdk.services.docdb.model.ModifyDbClusterRequest;
import software.amazon.awssdk.services.docdb.model.VpcSecurityGroupMembership;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @ResourceDiffProperty(updatable = true)
    public Integer getBackupRetentionPeriod() {
        return backupRetentionPeriod;
    }

    public void setBackupRetentionPeriod(Integer backupRetentionPeriod) {
        this.backupRetentionPeriod = backupRetentionPeriod;
    }

    @ResourceDiffProperty(updatable = true)
    public String getDbClusterIdentifier() {
        return dbClusterIdentifier;
    }

    public void setDbClusterIdentifier(String dbClusterIdentifier) {
        this.dbClusterIdentifier = dbClusterIdentifier;
    }

    public String getDbSubnetGroupName() {
        return dbSubnetGroupName;
    }

    public void setDbSubnetGroupName(String dbSubnetGroupName) {
        this.dbSubnetGroupName = dbSubnetGroupName;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    @ResourceDiffProperty(updatable = true)
    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    @ResourceDiffProperty(updatable = true)
    public String getDbClusterParamGroupName() {
        return dbClusterParamGroupName;
    }

    public void setDbClusterParamGroupName(String dbClusterParamGroupName) {
        this.dbClusterParamGroupName = dbClusterParamGroupName;
    }

    public String getKmsKeyId() {
        return kmsKeyId;
    }

    public void setKmsKeyId(String kmsKeyId) {
        this.kmsKeyId = kmsKeyId;
    }

    public String getMasterUsername() {
        return masterUsername;
    }

    public void setMasterUsername(String masterUsername) {
        this.masterUsername = masterUsername;
    }

    public String getMasterUserPassword() {
        return masterUserPassword;
    }

    public void setMasterUserPassword(String masterUserPassword) {
        this.masterUserPassword = masterUserPassword;
    }

    @ResourceDiffProperty(updatable = true)
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @ResourceDiffProperty(updatable = true)
    public String getPreferredBackupWindow() {
        return preferredBackupWindow;
    }

    public void setPreferredBackupWindow(String preferredBackupWindow) {
        this.preferredBackupWindow = preferredBackupWindow;
    }

    @ResourceDiffProperty(updatable = true)
    public String getPreferredMaintenanceWindow() {
        return preferredMaintenanceWindow;
    }

    public void setPreferredMaintenanceWindow(String preferredMaintenanceWindow) {
        this.preferredMaintenanceWindow = preferredMaintenanceWindow;
    }

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

    public Boolean getStorageEncrypted() {
        return storageEncrypted;
    }

    public void setStorageEncrypted(Boolean storageEncrypted) {
        this.storageEncrypted = storageEncrypted;
    }

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

    @ResourceDiffProperty(updatable = true)
    public String getPostDeleteSnapshotIdentifier() {
        return postDeleteSnapshotIdentifier;
    }

    public void setPostDeleteSnapshotIdentifier(String postDeleteSnapshotIdentifier) {
        this.postDeleteSnapshotIdentifier = postDeleteSnapshotIdentifier;
    }

    public String getDbClusterResourceId() {
        return DbClusterResourceId;
    }

    public void setDbClusterResourceId(String dbClusterResourceId) {
        DbClusterResourceId = dbClusterResourceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

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
        );

        setDbClusterResourceId(response.dbCluster().dbClusterResourceId());
        setArn(response.dbCluster().dbClusterArn());
    }

    @Override
    protected void doUpdate(Resource current, Set changedProperties) {
        DocDbClient client = createClient(DocDbClient.class);

        DbClusterResource resource = (DbClusterResource) current;

        ModifyDbClusterRequest.Builder builder = ModifyDbClusterRequest.builder()
            .backupRetentionPeriod(getBackupRetentionPeriod())
            .dbClusterIdentifier(resource.getDbClusterIdentifier())
            .dbClusterParameterGroupName(getDbClusterParamGroupName())
            .masterUserPassword(getMasterUserPassword())
            .port(getPort())
            .preferredBackupWindow(getPreferredBackupWindow())
            .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
            .vpcSecurityGroupIds(getVpcSecurityGroupIds())
            .newDBClusterIdentifier(getDbClusterIdentifier());

        if (!resource.getEngineVersion().equals(getEngineVersion())) {
            builder.engineVersion(getEngineVersion());
        }

        client.modifyDBCluster(builder.build());

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
}
