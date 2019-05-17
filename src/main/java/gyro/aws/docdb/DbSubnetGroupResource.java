package gyro.aws.docdb;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.GyroException;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceOutput;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceUpdatable;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.CreateDbSubnetGroupResponse;
import software.amazon.awssdk.services.docdb.model.DBSubnetGroup;
import software.amazon.awssdk.services.docdb.model.DbSubnetGroupNotFoundException;
import software.amazon.awssdk.services.docdb.model.DescribeDbSubnetGroupsResponse;
import software.amazon.awssdk.services.docdb.model.Subnet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates an Document db subnet group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::db-subnet-group db-subnet-group-example
 *         db-subnet-group-name: "db-subnet-group-example"
 *         db-subnet-group-description: "db-subnet-group-example-description"
 *         subnet-ids: [
 *             $(aws::subnet subnet-db-subnet-group-example-1 | subnet-id),
 *             $(aws::subnet subnet-db-subnet-group-example-2 | subnet-id)
 *         ]
 *
 *         tags: {
 *             Name: "db-subnet-group-example"
 *         }
 *     end
 */
@ResourceType("docdb-subnet-group")
public class DbSubnetGroupResource extends DocDbTaggableResource {
    private String dbSubnetGroupDescription;
    private String dbSubnetGroupName;
    private List<String> subnetIds;

    private String arn;

    /**
     * Description of the db subnet group.
     */
    @ResourceUpdatable
    public String getDbSubnetGroupDescription() {
        return dbSubnetGroupDescription;
    }

    public void setDbSubnetGroupDescription(String dbSubnetGroupDescription) {
        this.dbSubnetGroupDescription = dbSubnetGroupDescription;
    }

    /**
     * Name of the db subnet group. (Required)
     */
    public String getDbSubnetGroupName() {
        return dbSubnetGroupName;
    }

    public void setDbSubnetGroupName(String dbSubnetGroupName) {
        this.dbSubnetGroupName = dbSubnetGroupName;
    }

    /**
     * A list of associated subnet id's. (Required)
     */
    @ResourceUpdatable
    public List<String> getSubnetIds() {
        if (subnetIds == null) {
            subnetIds = new ArrayList<>();
        }

        if (!subnetIds.isEmpty() && !subnetIds.contains(null)) {
            Collections.sort(subnetIds);
        }

        return subnetIds;
    }

    public void setSubnetIds(List<String> subnetIds) {
        this.subnetIds = subnetIds;
    }

    /**
     * The arn of the db subnet group.
     */
    @ResourceOutput
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    protected String getId() {
        return getArn();
    }

    @Override
    protected boolean doRefresh() {
        DocDbClient client = createClient(DocDbClient.class);

        DBSubnetGroup dbSubnetGroup = getDbSubnetGroup(client);

        if (dbSubnetGroup == null) {
            return false;
        }

        setDbSubnetGroupDescription(dbSubnetGroup.dbSubnetGroupDescription());
        setArn(dbSubnetGroup.dbSubnetGroupArn());
        setSubnetIds(dbSubnetGroup.subnets().stream().map(Subnet::subnetIdentifier).collect(Collectors.toList()));

        return true;
    }

    @Override
    protected void doCreate() {
        DocDbClient client = createClient(DocDbClient.class);

        CreateDbSubnetGroupResponse response = client.createDBSubnetGroup(
            r -> r.dbSubnetGroupDescription(getDbSubnetGroupDescription())
                .dbSubnetGroupName(getDbSubnetGroupName())
                .subnetIds(getSubnetIds())
        );

        setArn(response.dbSubnetGroup().dbSubnetGroupArn());
    }

    @Override
    protected void doUpdate(Resource current, Set changedProperties) {
        DocDbClient client = createClient(DocDbClient.class);

        client.modifyDBSubnetGroup(
            r -> r.dbSubnetGroupName(getDbSubnetGroupName())
                .dbSubnetGroupDescription(getDbSubnetGroupDescription())
                .subnetIds(getSubnetIds())
        );
    }

    @Override
    public void delete() {
        DocDbClient client = createClient(DocDbClient.class);

        client.deleteDBSubnetGroup(
            r -> r.dbSubnetGroupName(getDbSubnetGroupName())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("db subnet group");

        if (!ObjectUtils.isBlank(getDbSubnetGroupName())) {
            sb.append(" - ").append(getDbSubnetGroupName());
        }

        return sb.toString();
    }

    private DBSubnetGroup getDbSubnetGroup(DocDbClient client) {
        DBSubnetGroup dbSubnetGroup = null;

        if (ObjectUtils.isBlank(getDbSubnetGroupName())) {
            throw new GyroException("db-subnet-group-name is missing, unable to load db subnet group.");
        }

        try {
            DescribeDbSubnetGroupsResponse response = client.describeDBSubnetGroups(
                r -> r.dbSubnetGroupName(getDbSubnetGroupName())
            );

            if (!response.dbSubnetGroups().isEmpty()) {
                dbSubnetGroup = response.dbSubnetGroups().get(0);
            }
        } catch (DbSubnetGroupNotFoundException ex) {

        }

        return dbSubnetGroup;
    }
}
