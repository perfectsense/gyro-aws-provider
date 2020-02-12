package gyro.aws.ec2;

import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.PlacementGroup;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query Placement Groups.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    pg: $(external-query aws::placement-group {group-name: "abc", state: "available", strategy: "spread" })
 */

@Type("placement-group")
public class PlacementGroupFinder extends Ec2TaggableAwsFinder<Ec2Client, PlacementGroup, PlacementGroupResource> {

    private String groupName;
    private String state;
    private String strategy;

    /**
     * The name identifying the Placement Group.
     */
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * The current state of the Placement Group (pending | available | deleting | deleted).
     */
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * One of three approaches towards managing the placement of instances on the underlying hardware (cluster | spread | partition).
     */
    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    @Override
    protected List<PlacementGroup> findAllAws(Ec2Client client) {
        return client.describePlacementGroups().placementGroups().stream().collect(Collectors.toList());
    }

    @Override
    protected List<PlacementGroup> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describePlacementGroups(r -> r.filters(createFilters(filters))).placementGroups().stream().collect(Collectors.toList());
    }
}
