package gyro.aws.rds;

import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.CreateDbSubnetGroupResponse;
import software.amazon.awssdk.services.rds.model.DBSubnetGroup;
import software.amazon.awssdk.services.rds.model.DbSubnetGroupNotFoundException;
import software.amazon.awssdk.services.rds.model.DescribeDbSubnetGroupsResponse;
import software.amazon.awssdk.services.rds.model.Subnet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
 *        subnet-ids: [
 *            $(aws::subnet subnet-us-east-2a | subnet-id),
 *            $(aws::subnet subnet-us-east-2b | subnet-id)
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
    private List<String> subnetIds;

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
     * The list of Subnet IDs for the DB subnet group. (Required)
     */
    @Updatable
    public List<String> getSubnetIds() {
        if (subnetIds == null || subnetIds.isEmpty()) {
            return new ArrayList<>();
        }

        subnetIds = subnetIds.stream().filter(Objects::nonNull).collect(Collectors.toList());
        List<String> sorted = new ArrayList<>(subnetIds);
        Collections.sort(sorted);

        return sorted;
    }

    public void setSubnetIds(List<String> subnetIds) {
        this.subnetIds = subnetIds;
    }

    @Override
    public void copyFrom(DBSubnetGroup group) {
        setDescription(group.dbSubnetGroupDescription());
        setGroupName(group.dbSubnetGroupName());
        setSubnetIds(group.subnets().stream().map(Subnet::subnetIdentifier).collect(Collectors.toList()));
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
    public void doCreate() {
        RdsClient client = createClient(RdsClient.class);
        CreateDbSubnetGroupResponse response = client.createDBSubnetGroup(
            r -> r.dbSubnetGroupDescription(getDescription())
                    .dbSubnetGroupName(getGroupName())
                    .subnetIds(getSubnetIds())
        );

        setArn(response.dbSubnetGroup().dbSubnetGroupArn());
    }

    @Override
    public void doUpdate(Resource current, Set<String> changedProperties) {
        RdsClient client = createClient(RdsClient.class);
        client.modifyDBSubnetGroup(
            r -> r.dbSubnetGroupName(getGroupName())
                    .dbSubnetGroupDescription(getDescription())
                    .subnetIds(getSubnetIds())
        );
    }

    @Override
    public void delete() {
        RdsClient client = createClient(RdsClient.class);
        client.deleteDBSubnetGroup(
            r -> r.dbSubnetGroupName(getGroupName())
        );
    }

    @Override
    public String toDisplayString() {
        return "db subnet group " + getGroupName();
    }
}
