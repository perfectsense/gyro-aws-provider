package gyro.aws.rds;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBClusterParameterGroup;
import software.amazon.awssdk.services.rds.model.DbParameterGroupNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query db cluster parameter group.
 *
 * .. code-block:: gyro
 *
 *    cluster-parameter-groups: $(external-query aws::db-cluster-parameter-group { name: 'cluster-parameter-group-example'})
 */
@Type("db-cluster-parameter-group")
public class DbClusterParameterGroupFinder extends AwsFinder<RdsClient, DBClusterParameterGroup, DbClusterParameterGroupResource> {

    private String name;

    /**
     * The name of the cluster parameter group.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<DBClusterParameterGroup> findAws(RdsClient client, Map<String, String> filters) {
        try {
            return client.describeDBClusterParameterGroups(r -> r.dbClusterParameterGroupName(filters.get("name"))).dbClusterParameterGroups();
        } catch (DbParameterGroupNotFoundException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    protected List<DBClusterParameterGroup> findAllAws(RdsClient client) {
        return client.describeDBClusterParameterGroups().dbClusterParameterGroups();
    }

}
