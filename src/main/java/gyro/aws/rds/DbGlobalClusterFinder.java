package gyro.aws.rds;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.GlobalCluster;

import java.util.List;
import java.util.Map;

@Type("db-global-cluster")
public class DbGlobalClusterFinder extends AwsFinder<RdsClient, GlobalCluster, DbGlobalClusterResource> {

    private String dbClusterId;

    public String getDbClusterId() {
        return dbClusterId;
    }

    public void setDbClusterId(String dbClusterId) {
        this.dbClusterId = dbClusterId;
    }

    @Override
    protected List<GlobalCluster> findAws(RdsClient client, Map<String, String> filters) {
        return client.describeGlobalClusters(r -> r.filters(createRdsFilters(filters))).globalClusters();
    }

    @Override
    protected List<GlobalCluster> findAllAws(RdsClient client) {
        return client.describeGlobalClusters().globalClusters();
    }

}
