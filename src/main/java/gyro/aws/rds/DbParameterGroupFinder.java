package gyro.aws.rds;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBParameterGroup;

import java.util.List;
import java.util.Map;

@Type("db-parameter-group")
public class DbParameterGroupFinder extends AwsFinder<RdsClient, DBParameterGroup, DbParameterGroupResource> {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<DBParameterGroup> findAws(RdsClient client, Map<String, String> filters) {
        return client.describeDBParameterGroups(r -> r.dbParameterGroupName(filters.get("name"))).dbParameterGroups();
    }

    @Override
    protected List<DBParameterGroup> findAllAws(RdsClient client) {
        return client.describeDBParameterGroups().dbParameterGroups();
    }

}
