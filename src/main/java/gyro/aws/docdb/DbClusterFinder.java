package gyro.aws.docdb;

import gyro.core.Type;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.DBCluster;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Type("docdb-cluster")
public class DbClusterFinder extends DocDbFinder<DocDbClient, DBCluster, DbClusterResource> {

    private String dbClusterId;

    /**
     * The DocumentDB cluster identifier.
     */
    public String getDbClusterId() {
        return dbClusterId;
    }

    public void setDbClusterId(String dbClusterId) {
        this.dbClusterId = dbClusterId;
    }

    @Override
    protected List<DBCluster> findAllAws(DocDbClient client) {
        return client.describeDBClustersPaginator().dbClusters().stream().collect(Collectors.toList());
    }

    @Override
    protected List<DBCluster> findAws(DocDbClient client, Map<String, String> filters) {
        return client.describeDBClustersPaginator(r -> r.filters(createDocDbFilters(filters))).dbClusters().stream().collect(Collectors.toList());
    }

}
