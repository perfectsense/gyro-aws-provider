package gyro.aws.rds;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBCluster;

import java.util.List;
import java.util.Map;

/**
 * Query db cluster.
 *
 * .. code-block:: gyro
 *
 *    clusters: $(aws::db-cluster EXTERNAL/* | db-cluster-id = 'aurora-mysql-cluster')
 */
@Type("db-cluster")
public class DbClusterFinder extends AwsFinder<RdsClient, DBCluster, DbClusterResource> {

    private String dbClusterId;

    /**
     * The identifier or arn of the cluster.
     */
    public String getDbClusterId() {
        return dbClusterId;
    }

    public void setDbClusterId(String dbClusterId) {
        this.dbClusterId = dbClusterId;
    }

    @Override
    protected List<DBCluster> findAws(RdsClient client, Map<String, String> filters) {
        return client.describeDBClusters(r -> r.filters(createRdsFilters(filters))).dbClusters();
    }

    @Override
    protected List<DBCluster> findAllAws(RdsClient client) {
        return client.describeDBClusters().dbClusters();
    }

}
