package gyro.aws.rds;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBCluster;

import java.util.List;
import java.util.Map;

@Type("db-cluster")
public class DbClusterFinder extends AwsFinder<RdsClient, DBCluster, DbInstanceResource> {

    private String dbClusterId;

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
