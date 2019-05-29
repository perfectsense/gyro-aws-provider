package gyro.aws.rds;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBClusterEndpoint;

import java.util.List;
import java.util.Map;

@Type("db-cluster-endpoint")
public class DbClusterEndpointFinder extends AwsFinder<RdsClient, DBClusterEndpoint, DbClusterEndpointResource> {

    private String dbClusterEndpointType;
    private String dbClusterEndpointCustomType;
    private String dbClusterEndpointId;
    private String dbClusterEndpointStatus;

    public String getDbClusterEndpointType() {
        return dbClusterEndpointType;
    }

    public void setDbClusterEndpointType(String dbClusterEndpointType) {
        this.dbClusterEndpointType = dbClusterEndpointType;
    }

    public String getDbClusterEndpointCustomType() {
        return dbClusterEndpointCustomType;
    }

    public void setDbClusterEndpointCustomType(String dbClusterEndpointCustomType) {
        this.dbClusterEndpointCustomType = dbClusterEndpointCustomType;
    }

    public String getDbClusterEndpointId() {
        return dbClusterEndpointId;
    }

    public void setDbClusterEndpointId(String dbClusterEndpointId) {
        this.dbClusterEndpointId = dbClusterEndpointId;
    }

    public String getDbClusterEndpointStatus() {
        return dbClusterEndpointStatus;
    }

    public void setDbClusterEndpointStatus(String dbClusterEndpointStatus) {
        this.dbClusterEndpointStatus = dbClusterEndpointStatus;
    }

    @Override
    protected List<DBClusterEndpoint> findAws(RdsClient client, Map<String, String> filters) {
        return client.describeDBClusterEndpoints(r -> r.filters(createRdsFilters(filters))).dbClusterEndpoints();
    }

    @Override
    protected List<DBClusterEndpoint> findAllAws(RdsClient client) {
        return client.describeDBClusterEndpoints().dbClusterEndpoints();
    }

}
