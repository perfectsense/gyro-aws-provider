package gyro.aws.rds;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBClusterParameterGroup;

import java.util.List;
import java.util.Map;

@Type("db-cluster-parameter-group")
public class DbClusterParameterGroupFinder extends AwsFinder<RdsClient, DBClusterParameterGroup, DbClusterParameterGroupResource> {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<DBClusterParameterGroup> findAws(RdsClient client, Map<String, String> filters) {
        return client.describeDBClusterParameterGroups(r -> r.dbClusterParameterGroupName(filters.get("name"))).dbClusterParameterGroups();
    }

    @Override
    protected List<DBClusterParameterGroup> findAllAws(RdsClient client) {
        return client.describeDBClusterParameterGroups().dbClusterParameterGroups();
    }

}
