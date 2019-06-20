package gyro.aws.docdb;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.aws.ec2.SubnetResource;
import gyro.core.GyroException;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.CreateDbSubnetGroupResponse;
import software.amazon.awssdk.services.docdb.model.DBSubnetGroup;
import software.amazon.awssdk.services.docdb.model.DbSubnetGroupNotFoundException;
import software.amazon.awssdk.services.docdb.model.DescribeDbSubnetGroupsResponse;

import java.util.HashSet;
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
@Type("docdb-subnet-group")
public class DbSubnetGroupResource extends DocDbTaggableResource implements Copyable<DBSubnetGroup> {

    private String dbSubnetGroupDescription;
    private String dbSubnetGroupName;
    private Set<SubnetResource> subnets;

    //-- Read-only Attributes

    private String arn;
    private String status;

    /**
     * Description of the db subnet group.
     */
    @Updatable
    public String getDbSubnetGroupDescription() {
        return dbSubnetGroupDescription;
    }

    public void setDbSubnetGroupDescription(String dbSubnetGroupDescription) {
        this.dbSubnetGroupDescription = dbSubnetGroupDescription;
    }

    /**
     * Name of the db subnet group. (Required)
     */
    @Id
    public String getDbSubnetGroupName() {
        return dbSubnetGroupName;
    }

    public void setDbSubnetGroupName(String dbSubnetGroupName) {
        this.dbSubnetGroupName = dbSubnetGroupName;
    }

    /**
     * A list of associated subnet id's. (Required)
     */
    @Updatable
    public Set<SubnetResource> getSubnets() {
        if (subnets == null) {
            subnets = new HashSet<>();
        }

        return subnets;
    }

    public void setSubnets(Set<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    /**
     * The arn of the db subnet group.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The status of this subnet group.
     */
    @Output
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

        copyFrom(dbSubnetGroup);

        return true;
    }

    @Override
    protected void doCreate() {
        DocDbClient client = createClient(DocDbClient.class);

        CreateDbSubnetGroupResponse response = client.createDBSubnetGroup(
            r -> r.dbSubnetGroupDescription(getDbSubnetGroupDescription())
                .dbSubnetGroupName(getDbSubnetGroupName())
                .subnetIds(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList()))
        );

        setArn(response.dbSubnetGroup().dbSubnetGroupArn());
    }

    @Override
    protected void doUpdate(Resource current, Set changedProperties) {
        DocDbClient client = createClient(DocDbClient.class);

        client.modifyDBSubnetGroup(
            r -> r.dbSubnetGroupName(getDbSubnetGroupName())
                .dbSubnetGroupDescription(getDbSubnetGroupDescription())
                .subnetIds(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList()))
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

    @Override
    public void copyFrom(DBSubnetGroup dbSubnetGroup) {
        setArn(dbSubnetGroup.dbSubnetGroupArn());
        setDbSubnetGroupDescription(dbSubnetGroup.dbSubnetGroupDescription());
        setDbSubnetGroupName(dbSubnetGroup.dbSubnetGroupName());
        setStatus(dbSubnetGroup.subnetGroupStatus());
        setSubnets(dbSubnetGroup.subnets().stream().map(s -> findById(SubnetResource.class, s.subnetIdentifier())).collect(Collectors.toSet()));

        loadTags();
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
