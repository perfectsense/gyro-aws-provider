package gyro.aws.rds;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBSubnetGroup;

import java.util.List;
import java.util.Map;

@Type("db-subnet-group")
public class DbSubnetGroupFinder extends AwsFinder<RdsClient, DBSubnetGroup, DbSubnetGroupResource> {

    private String groupName;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    protected List<DBSubnetGroup> findAws(RdsClient client, Map<String, String> filters) {
        return client.describeDBSubnetGroups(r -> r.dbSubnetGroupName(filters.get("group-name"))).dbSubnetGroups();
    }

    @Override
    protected List<DBSubnetGroup> findAllAws(RdsClient client) {
        return client.describeDBSubnetGroups().dbSubnetGroups();
    }

}
