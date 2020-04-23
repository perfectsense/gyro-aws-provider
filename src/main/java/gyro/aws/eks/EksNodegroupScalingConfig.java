package gyro.aws.eks;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.eks.model.NodegroupScalingConfig;

public class EksNodegroupScalingConfig extends Diffable implements Copyable<NodegroupScalingConfig> {

    private Integer desiredSize;
    private Integer maxSize;
    private Integer minSize;

    public Integer getDesiredSize() {
        return desiredSize;
    }

    public void setDesiredSize(Integer desiredSize) {
        this.desiredSize = desiredSize;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    public Integer getMinSize() {
        return minSize;
    }

    public void setMinSize(Integer minSize) {
        this.minSize = minSize;
    }

    @Override
    public void copyFrom(NodegroupScalingConfig model) {
        setDesiredSize(model.desiredSize());
        setMaxSize(model.maxSize());
        setMinSize(model.minSize());
    }

    @Override
    public String primaryKey() {
        return null;
    }

    NodegroupScalingConfig toNodegroupScalingConfig() {
        return NodegroupScalingConfig.builder()
            .desiredSize(getDesiredSize())
            .maxSize(getMaxSize())
            .minSize(getMinSize())
            .build();
    }
}
