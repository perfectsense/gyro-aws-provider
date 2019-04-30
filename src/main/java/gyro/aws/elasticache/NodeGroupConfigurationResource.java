package gyro.aws.elasticache;

import gyro.core.diff.Diffable;
import software.amazon.awssdk.services.elasticache.model.NodeGroupConfiguration;

public class NodeGroupConfigurationResource extends Diffable {

    public NodeGroupConfigurationResource() {

    }

    public NodeGroupConfigurationResource(NodeGroupConfiguration nodeGroupConfiguration) {
        nodeGroupConfiguration.nodeGroupId();
        nodeGroupConfiguration.replicaAvailabilityZones();
        nodeGroupConfiguration.primaryAvailabilityZone();
        nodeGroupConfiguration.replicaCount();
        nodeGroupConfiguration.slots();
    }

    @Override
    public String primaryKey() {
        return null;
    }

    @Override
    public String toDisplayString() {
        return null;
    }

    NodeGroupConfiguration getNodeGroupConfiguration() {
        return NodeGroupConfiguration.builder()
            .build();
    }
}
