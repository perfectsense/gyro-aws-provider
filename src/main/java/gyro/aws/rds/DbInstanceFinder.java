package gyro.aws.rds;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBInstance;

import java.util.List;
import java.util.Map;

/**
 * Query db instance.
 *
 * .. code-block:: gyro
 *
 *    db-instances: $(aws::db-instance EXTERNAL/* | db-instance-id = 'db-instance-example')
 */
@Type("db-instance")
public class DbInstanceFinder extends AwsFinder<RdsClient, DBInstance, DbInstanceResource> {

    private String dbClusterId;
    private String dbInstanceId;

    /**
     * The identifier or arn of the cluster.
     */
    public String getDbClusterId() {
        return dbClusterId;
    }

    public void setDbClusterId(String dbClusterId) {
        this.dbClusterId = dbClusterId;
    }

    /**
     * The identifier or arn of the db instance.
     */
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
