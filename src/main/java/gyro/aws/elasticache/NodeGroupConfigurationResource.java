package gyro.aws.elasticache;

import gyro.core.diff.Diffable;
import software.amazon.awssdk.services.elasticache.model.NodeGroup;
import software.amazon.awssdk.services.elasticache.model.NodeGroupConfiguration;

import java.util.List;

public class NodeGroupConfigurationResource extends Diffable {

    private String nodeGroupId;
    private String slots;
    private List<String> replicaAvailabilityZones;
    private String primaryAvailabilityZones;
    private Integer replicaCount;

    public String getNodeGroupId() {
        return nodeGroupId;
    }

    public void setNodeGroupId(String nodeGroupId) {
        this.nodeGroupId = nodeGroupId;
    }

    public String getSlots() {
        return slots;
    }

    public void setSlots(String slots) {
        this.slots = slots;
    }

    public List<String> getReplicaAvailabilityZones() {
        return replicaAvailabilityZones;
    }

    public void setReplicaAvailabilityZones(List<String> replicaAvailabilityZones) {
        this.replicaAvailabilityZones = replicaAvailabilityZones;
    }

    public String getPrimaryAvailabilityZones() {
        return primaryAvailabilityZones;
    }

    public void setPrimaryAvailabilityZones(String primaryAvailabilityZones) {
        this.primaryAvailabilityZones = primaryAvailabilityZones;
    }

    public Integer getReplicaCount() {
        return replicaCount;
    }

    public void setReplicaCount(Integer replicaCount) {
        this.replicaCount = replicaCount;
    }

    public NodeGroupConfigurationResource() {

    }

    public NodeGroupConfigurationResource(NodeGroupConfiguration nodeGroupConfiguration) {
        nodeGroupConfiguration.nodeGroupId();
        nodeGroupConfiguration.replicaAvailabilityZones();
        nodeGroupConfiguration.primaryAvailabilityZone();
        nodeGroupConfiguration.replicaCount();
        nodeGroupConfiguration.slots();
    }

    public NodeGroupConfigurationResource(NodeGroup nodeGroup) {
        setNodeGroupId(nodeGroup.nodeGroupId());
        setSlots(nodeGroup.slots());
        setReplicaCount(nodeGroup.nodeGroupMembers().size());
    }

    @Override
    public String primaryKey() {
        return getNodeGroupId();
    }

    @Override
    public String toDisplayString() {
        return "Node group - " + getNodeGroupId();
    }

    NodeGroupConfiguration getNodeGroupConfiguration() {
        return NodeGroupConfiguration.builder()
            .nodeGroupId(getNodeGroupId())
            .slots(getSlots())
            .replicaCount(getReplicaCount())
            .primaryAvailabilityZone(getPrimaryAvailabilityZones())
            .replicaAvailabilityZones(getReplicaAvailabilityZones())
            .build();
    }
}
