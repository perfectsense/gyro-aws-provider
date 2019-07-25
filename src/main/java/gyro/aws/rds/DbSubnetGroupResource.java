package gyro.aws.rds;

import gyro.aws.Copyable;
import gyro.aws.ec2.SubnetResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.scope.State;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.CreateDbSubnetGroupResponse;
import software.amazon.awssdk.services.rds.model.DBSubnetGroup;
import software.amazon.awssdk.services.rds.model.DbSubnetGroupNotFoundException;
import software.amazon.awssdk.services.rds.model.DescribeDbSubnetGroupsResponse;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Create a db subnet group.
 *
 * .. code-block:: gyro
 *
 *    aws::db-subnet-group db-subnet-group
 *        group-name: "db-subnet-group-example"
 *        description: "db subnet group description"
 *        subnets: [
 *            $(aws::subnet subnet-us-east-2a),
 *            $(aws::subnet subnet-us-east-2b)
 *        ]
 *
 *        tags: {
 *            Name: "db-subnet-group-example"
 *        }
 *    end
 */
@Type("db-subnet-group")
public class DbSubnetGroupResource extends RdsTaggableResource implements Copyable<DBSubnetGroup> {

    private String description;
    private String groupName;
    private Set<SubnetResource> subnets;

    /**
     * The description for the DB subnet group. (Required)
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The name for the DB subnet group. (Required)
     */
    @Id
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * The list of Subnets for the DB subnet group. (Required)
     */
    @Updatable
    public Set<SubnetResource> getSubnets() {
        if (subnets == null) {
            return new HashSet<>();
        }

        return subnets;
    }

    public void setSubnets(Set<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    @Override
    public void copyFrom(DBSubnetGroup group) {
        setDescription(group.dbSubnetGroupDescription());
        setGroupName(group.dbSubnetGroupName());
        setSubnets(group.subnets().stream().map(s -> findById(SubnetResource.class, s.subnetIdentifier())).collect(Collectors.toSet()));
        setArn(group.dbSubnetGroupArn());
    }

    @Override
    public boolean doRefresh() {
        RdsClient client = createClient(RdsClient.class);

        if (ObjectUtils.isBlank(getGroupName())) {
            throw new GyroException("group-name is missing, unable to load db subnet group.");
        }

        try {
            DescribeDbSubnetGroupsResponse response = client.describeDBSubnetGroups(
                r -> r.dbSubnetGroupName(getGroupName())
            );

            response.dbSubnetGroups().forEach(this::copyFrom);

        } catch (DbSubnetGroupNotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    public void doCreate(GyroUI ui, State state) {
        RdsClient client = createClient(RdsClient.class);
        CreateDbSubnetGroupResponse response = client.createDBSubnetGroup(
            r -> r.dbSubnetGroupDescription(getDescription())
                    .dbSubnetGroupName(getGroupName())
                    .subnetIds(getSubnets().stream().map(SubnetResource::getSubnetId).collect(Collectors.toSet()))
        );

        setArn(response.dbSubnetGroup().dbSubnetGroupArn());
    }

    @Override
    public void doUpdate(Resource current, Set<String> changedProperties) {
        RdsClient client = createClient(RdsClient.class);
        client.modifyDBSubnetGroup(
            r -> r.dbSubnetGroupName(getGroupName())
                    .dbSubnetGroupDescription(getDescription())
                    .subnetIds(getSubnets().stream().map(SubnetResource::getSubnetId).collect(Collectors.toSet()))
        );
    }

    @Override
    public void delete(GyroUI ui, State state) {
        RdsClient client = createClient(RdsClient.class);
        client.deleteDBSubnetGroup(
            r -> r.dbSubnetGroupName(getGroupName())
        );
    }

}
