package gyro.aws.rds;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.scope.State;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DescribeGlobalClustersResponse;
import software.amazon.awssdk.services.rds.model.GlobalCluster;
import software.amazon.awssdk.services.rds.model.GlobalClusterNotFoundException;

import java.util.Set;

/**
 * Create a global cluster.
 *
 * .. code-block:: gyro
 *
 *    aws::db-global-cluster db-global-cluster-example
 *        identifier: "aurora-global-cluster"
 *        engine: "aurora"
 *    end
 */
@Type("db-global-cluster")
public class DbGlobalClusterResource extends AwsResource implements Copyable<GlobalCluster> {

    private String databaseName;
    private Boolean deletionProtection;
    private String engine;
    private String engineVersion;
    private String identifier;
    private DbClusterResource sourceDbCluster;
    private Boolean storageEncrypted;

    /**
     * The name for your database of up to 64 alpha-numeric characters. If omitted, no database will be created in the global database cluster.
     */
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * Enable or disable deletion protection on the global cluster. The default is false.
     */
    @Updatable
    public Boolean getDeletionProtection() {
        return deletionProtection;
    }

    public void setDeletionProtection(Boolean deletionProtection) {
        this.deletionProtection = deletionProtection;
    }

    /**
     * The name of the database engine.
     */
    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    /**
     * The engine version of the Aurora global database.
     */
    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    /**
     * The unique identifier of the global database cluster. (Required)
     */
    @Id
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * The primary cluster of the global database.
     */
    public DbClusterResource getSourceDbCluster() {
        return sourceDbCluster;
    }

    public void setSourceDbCluster(DbClusterResource sourceDbCluster) {
        this.sourceDbCluster = sourceDbCluster;
    }

    /**
     * Enable or disable global cluster encryption. Default to false (no encryption).
     */
    public Boolean getStorageEncrypted() {
        return storageEncrypted;
    }

    public void setStorageEncrypted(Boolean storageEncrypted) {
        this.storageEncrypted = storageEncrypted;
    }

    @Override
    public void copyFrom(GlobalCluster cluster) {
        setDatabaseName(cluster.databaseName());
        setDeletionProtection(cluster.deletionProtection());
        setEngine(cluster.engine());

        String version = cluster.engineVersion();
        if (getEngineVersion() != null) {
            version = version.substring(0, getEngineVersion().length());
        }

        setEngineVersion(version);
        setStorageEncrypted(cluster.storageEncrypted());
    }

    @Override
    public boolean refresh() {
        RdsClient client = createClient(RdsClient.class);

        if (ObjectUtils.isBlank(getIdentifier())) {
            throw new GyroException("identifier is missing, unable to load db global cluster.");
        }

        try {
            DescribeGlobalClustersResponse response = client.describeGlobalClusters(
                r -> r.globalClusterIdentifier(getIdentifier())
            );

            response.globalClusters().forEach(this::copyFrom);

        } catch (GlobalClusterNotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        RdsClient client = createClient(RdsClient.class);
        client.createGlobalCluster(
            r -> r.databaseName(getDatabaseName())
                    .deletionProtection(getDeletionProtection())
                    .engine(getEngine())
                    .engineVersion(getEngineVersion())
                    .globalClusterIdentifier(getIdentifier())
                    .sourceDBClusterIdentifier(getSourceDbCluster() != null ? getSourceDbCluster().getArn() : null)
                    .storageEncrypted(getStorageEncrypted())
        );
    }

    @Override
    public void update(GyroUI ui, State state, Resource config, Set<String> changedFieldNames) {
        RdsClient client = createClient(RdsClient.class);
        DbGlobalClusterResource current = (DbGlobalClusterResource) config;
        // The modify global cluster api currently return a 500
        client.modifyGlobalCluster(
            r -> r.deletionProtection(getDeletionProtection())
                    .globalClusterIdentifier(current.getIdentifier())
                    .newGlobalClusterIdentifier(getIdentifier())
        );
    }

    @Override
    public void delete(GyroUI ui, State state) {
        RdsClient client = createClient(RdsClient.class);
        client.deleteGlobalCluster(
            r -> r.globalClusterIdentifier(getIdentifier())
        );
    }
}
