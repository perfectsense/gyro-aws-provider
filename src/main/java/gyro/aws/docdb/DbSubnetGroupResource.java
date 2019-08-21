package gyro.aws.docdb;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.aws.ec2.SubnetResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import org.apache.commons.lang.NotImplementedException;
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
 *         name: "db-subnet-group-example"
 *         description: "db-subnet-group-example-description"
 *         subnets: [
 *             $(aws::subnet subnet-db-subnet-group-example-1),
 *             $(aws::subnet subnet-db-subnet-group-example-2)
 *         ]
 *
 *         tags: {
 *             Name: "db-subnet-group-example"
 *         }
 *     end
 */
@Type("docdb-subnet-group")
public class DbSubnetGroupResource extends DocDbTaggableResource implements Copyable<DBSubnetGroup> {

    private String description;
    private String name;
    private Set<SubnetResource> subnets;

    //-- Read-only Attributes

    private String arn;
    private String status;

    /**
     * Description of the db subnet group.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Name of the db subnet group. (Required)
     */
    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * A list of associated subnets. (Required)
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
    protected String getResourceId() {
        return getArn();
    }

    @Override
    protected boolean doRefresh() {
        throw new NotImplementedException();
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        throw new NotImplementedException();
    }

    @Override
    protected void doUpdate(Resource current, Set changedProperties) {
        throw new NotImplementedException();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        throw new NotImplementedException();
    }

    @Override
    public void copyFrom(DBSubnetGroup dbSubnetGroup) {
        throw new NotImplementedException();
    }

    private DBSubnetGroup getDbSubnetGroup(DocDbClient client) {
        DBSubnetGroup dbSubnetGroup = null;

        if (ObjectUtils.isBlank(getName())) {
            throw new GyroException("name is missing, unable to load db subnet group.");
        }

        try {
            DescribeDbSubnetGroupsResponse response = client.describeDBSubnetGroups(
                r -> r.dbSubnetGroupName(getName())
            );

            if (!response.dbSubnetGroups().isEmpty()) {
                dbSubnetGroup = response.dbSubnetGroups().get(0);
            }
        } catch (DbSubnetGroupNotFoundException ex) {

        }

        return dbSubnetGroup;
    }

}
