package gyro.aws.docdb;

import gyro.core.Type;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.DBClusterParameterGroup;
import software.amazon.awssdk.services.docdb.model.DbParameterGroupNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Type("docdb-cluster-param-group")
public class DbClusterParameterGroupFinder extends DocDbFinder<DocDbClient, DBClusterParameterGroup, DbClusterParameterGroupResource> {

    private String name;

    /**
     * The name of a Document DB cluster parameter group.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<DBClusterParameterGroup> findAllAws(DocDbClient client) {
        return client.describeDBClusterParameterGroups().dbClusterParameterGroups();
    }

    @Override
    protected List<DBClusterParameterGroup> findAws(DocDbClient client, Map<String, String> filters) {
        try {
            return client.describeDBClusterParameterGroups(r -> r.dbClusterParameterGroupName(filters.get("name"))).dbClusterParameterGroups();
        } catch (DbParameterGroupNotFoundException ex) {
            return Collections.emptyList();
        }
    }
}
