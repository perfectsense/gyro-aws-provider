package gyro.aws.rds;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.GlobalCluster;
import software.amazon.awssdk.services.rds.model.GlobalClusterNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query global cluster.
 *
 * .. code-block:: gyro
 *
 *    global-clusters: $(aws::db-global-cluster EXTERNAL/* | db-cluster-id = 'aurora-global-cluster')
 */
@Type("db-global-cluster")
public class DbGlobalClusterFinder extends AwsFinder<RdsClient, GlobalCluster, DbGlobalClusterResource> {

    private String dbClusterId;

    /**
     * The identifier or arn of the global cluster.
     */
    public String getDbClusterId() {
        return dbClusterId;
    }

    public void setDbClusterId(String dbClusterId) {
        this.dbClusterId = dbClusterId;
    }

    @Override
    protected List<GlobalCluster> findAws(RdsClient client, Map<String, String> filters) {
        try {
            return client.describeGlobalClusters(r -> r.filters(createRdsFilters(filters))).globalClusters();
        } catch (GlobalClusterNotFoundException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    protected List<GlobalCluster> findAllAws(RdsClient client) {
        return client.describeGlobalClustersPaginator().globalClusters().stream().collect(Collectors.toList());
    }

}
