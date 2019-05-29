package gyro.aws.rds;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBInstance;

import java.util.List;
import java.util.Map;

@Type("db-instance")
public class DbInstanceFinder extends AwsFinder<RdsClient, DBInstance, DbInstanceResource> {

    private String dbClusterId;
    private String dbInstanceId;

    public String getDbClusterId() {
        return dbClusterId;
    }

    public void setDbClusterId(String dbClusterId) {
        this.dbClusterId = dbClusterId;
    }

    public String getDbInstanceId() {
        return dbInstanceId;
    }

    public void setDbInstanceId(String dbInstanceId) {
        this.dbInstanceId = dbInstanceId;
    }

    @Override
    protected List<DBInstance> findAws(RdsClient client, Map<String, String> filters) {
        return client.describeDBInstances(r -> r.filters(createRdsFilters(filters))).dbInstances();
    }

    @Override
    protected List<DBInstance> findAllAws(RdsClient client) {
        return client.describeDBInstances().dbInstances();
    }

}
