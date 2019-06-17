package gyro.aws.rds;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBSubnetGroup;
import software.amazon.awssdk.services.rds.model.DbSubnetGroupNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query db subnet group.
 *
 * .. code-block:: gyro
 *
 *    db-subnet-group: $(aws::db-subnet-group EXTERNAL/* | group-name = 'db-subnet-group-db-cluster-example')
 */
@Type("db-subnet-group")
public class DbSubnetGroupFinder extends AwsFinder<RdsClient, DBSubnetGroup, DbSubnetGroupResource> {

    private String groupName;

    /**
     * The name of the subnet group.
     */
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    protected List<DBSubnetGroup> findAws(RdsClient client, Map<String, String> filters) {
        try {
            return client.describeDBSubnetGroups(r -> r.dbSubnetGroupName(filters.get("group-name"))).dbSubnetGroups();
        } catch (DbSubnetGroupNotFoundException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    protected List<DBSubnetGroup> findAllAws(RdsClient client) {
        return client.describeDBSubnetGroups().dbSubnetGroups();
    }

}