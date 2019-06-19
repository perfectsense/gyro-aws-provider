package gyro.aws.docdb;

import gyro.core.Type;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.DBInstance;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Type("docdb-instance")
public class DbInstanceFinder extends DocDbFinder<DocDbClient, DBInstance, DbInstance> {

    private String dbClusterId;
    private String dbInstanceId;

    /**
     * The DocumentDB cluster identifier or arn associated to an instance.
     */
    public String getDbClusterId() {
        return dbClusterId;
    }

    public void setDbClusterId(String dbClusterId) {
        this.dbClusterId = dbClusterId;
    }

    /**
     * The DocumentDB instance identifier or arn.
     */
    public String getDbInstanceId() {
        return dbInstanceId;
    }

    public void setDbInstanceId(String dbInstanceId) {
        this.dbInstanceId = dbInstanceId;
    }

    @Override
    protected List<DBInstance> findAllAws(DocDbClient client) {
        return client.describeDBInstancesPaginator().dbInstances().stream().collect(Collectors.toList());
    }

    @Override
    protected List<DBInstance> findAws(DocDbClient client, Map<String, String> filters) {
        return client.describeDBInstancesPaginator(r -> r.filters(createDocDbFilters(filters))).dbInstances().stream().collect(Collectors.toList());
    }

}
