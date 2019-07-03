package gyro.aws.docdb;

import gyro.core.Type;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.DBSubnetGroup;
import software.amazon.awssdk.services.docdb.model.DbSubnetGroupNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Type("docdb-subnet-group")
public class DbSubnetGroupFinder extends DocDbFinder<DocDbClient, DBSubnetGroup, DbSubnetGroupResource> {

    private String name;

    /**
     * The DocumentDB subnet group name.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<DBSubnetGroup> findAllAws(DocDbClient client) {
        return client.describeDBSubnetGroupsPaginator().dbSubnetGroups().stream().collect(Collectors.toList());
    }

    @Override
    protected List<DBSubnetGroup> findAws(DocDbClient client, Map<String, String> filters) {
        if (!filters.containsKey("name")) {
            throw new IllegalArgumentException("'name' is required.");
        }

        try {
            return client.describeDBSubnetGroups(r -> r.dbSubnetGroupName(filters.get("name"))).dbSubnetGroups();
        } catch (DbSubnetGroupNotFoundException ex) {
            return Collections.emptyList();
        }
    }

}
