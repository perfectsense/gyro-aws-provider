package gyro.aws.docdb;

import gyro.core.Type;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.DBSubnetGroup;

import java.util.ArrayList;
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
        List<DBSubnetGroup> groups = new ArrayList<>();

        if (filters.containsKey("name")) {
            groups = client.describeDBSubnetGroups(r -> r.dbSubnetGroupName(filters.get("name"))).dbSubnetGroups();
        }

        return groups;
    }

}
