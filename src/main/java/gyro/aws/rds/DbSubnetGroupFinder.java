package gyro.aws.rds;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBSubnetGroup;
import software.amazon.awssdk.services.rds.model.DbSubnetGroupNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query db subnet group.
 *
 * .. code-block:: gyro
 *
 *    db-subnet-group: $(external-query aws::db-subnet-group { name: 'db-subnet-group-db-cluster-example'})
 */
@Type("db-subnet-group")
public class DbSubnetGroupFinder extends AwsFinder<RdsClient, DBSubnetGroup, DbSubnetGroupResource> {

    private String name;

    /**
     * The name of the subnet group.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<DBSubnetGroup> findAws(RdsClient client, Map<String, String> filters) {
        if (!filters.containsKey("group-name")) {
            throw new IllegalArgumentException("'group-name' is required.");
        }

        try {
            return client.describeDBSubnetGroups(r -> r.dbSubnetGroupName(filters.get("group-name"))).dbSubnetGroups();
        } catch (DbSubnetGroupNotFoundException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    protected List<DBSubnetGroup> findAllAws(RdsClient client) {
        return client.describeDBSubnetGroupsPaginator().dbSubnetGroups().stream().collect(Collectors.toList());
    }

}
