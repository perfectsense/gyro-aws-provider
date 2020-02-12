package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.scope.State;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Creates a Placement Group with the specified name and placement strategy.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::placement-group example-placement-group
 *         group-name: "TestGroup"
 *         placement-strategy: "partition"
 *         partition-count: 3
 *     end
 */

@Type("placement-group")
public class PlacementGroupResource extends Ec2TaggableResource<PlacementGroup> implements Copyable<PlacementGroup> {

    private String groupName;
    private PlacementStrategy placementStrategy = PlacementStrategy.CLUSTER;
    private Integer partitionCount;
    private String id;

    /**
     * A name to identify the Placement Group. (Required)
     */
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * One of three approaches towards managing the placement of instances on the underlying hardware (cluster | spread | partition).
     * Defaults to the "cluster" strategy.
     */
    public PlacementStrategy getPlacementStrategy() {
        return placementStrategy;
    }

    public void setPlacementStrategy(PlacementStrategy placementStrategy) {
        this.placementStrategy = placementStrategy;
    }

    /**
     * The number of partitions comprising the Placement Group.
     * Ranges from 1 to 7. This value must be excluded unless using the "partition" strategy.
     */
    public Integer getPartitionCount() {
        return partitionCount;
    }

    public void setPartitionCount(Integer partitionCount) {
        this.partitionCount = partitionCount;
    }

    /**
     * The ID of the Placement Group.
     */
    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected String getResourceId() {
        return getId();
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);
        CreatePlacementGroupRequest createRequest = CreatePlacementGroupRequest.builder().groupName(getGroupName()).strategy(getPlacementStrategy()).partitionCount(getPartitionCount()).build();
        CreatePlacementGroupResponse createResponse = client.createPlacementGroup(createRequest);

        DescribePlacementGroupsRequest describeRequest = DescribePlacementGroupsRequest.builder().groupNames(getGroupName()).build();
        PlacementGroup group = null;
        try {
            DescribePlacementGroupsResponse describeResponse = client.describePlacementGroups(describeRequest);
            if (!describeResponse.placementGroups().isEmpty()) {
                group = describeResponse.placementGroups().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("unknown")) {
                throw ex;
            }
        }

        if (group != null) {
            setId(group.groupId());
        }

    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {

    }

    @Override
    public boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);
        if (ObjectUtils.isBlank(getGroupName())) {
            throw new GyroException("group name is missing, unable to load placement group.");
        }

        DescribePlacementGroupsRequest request = DescribePlacementGroupsRequest.builder().groupNames(getGroupName()).build();
        PlacementGroup group = null;
        try {
            DescribePlacementGroupsResponse response = client.describePlacementGroups(request);
            if (!response.placementGroups().isEmpty()) {
                group = response.placementGroups().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("unknown")) {
                throw ex;
            }
        }

        if (group == null) {
            return false;
        }

        copyFrom(group);
        return true;
    }

    @Override
    public void copyFrom(PlacementGroup model) {
        setId(model.groupId());
        setGroupName(model.groupName());
        setPlacementStrategy(model.strategy());
        setPartitionCount(model.partitionCount());
        refreshTags();
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);

        DeletePlacementGroupRequest request = DeletePlacementGroupRequest.builder().groupName(getGroupName()).build();

        client.deletePlacementGroup(request);
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getPlacementStrategy() == PlacementStrategy.PARTITION) {
            if (getPartitionCount() == null || getPartitionCount() < 1 || getPartitionCount() > 7) {
                ValidationError error = new ValidationError(this, "partitionCount", "Partition count must range from 1 to 7.");
                errors.add(error);
            }
        } else if (getPartitionCount() != null) {
            ValidationError error = new ValidationError(this, "partitionCount", "Partition count must be excluded unless placement strategy is 'partition'.");
            errors.add(error);
        }

        return errors;
    }
}
