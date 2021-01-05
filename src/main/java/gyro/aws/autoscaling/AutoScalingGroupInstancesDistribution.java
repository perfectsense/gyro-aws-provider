package gyro.aws.autoscaling;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Range;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.autoscaling.model.InstancesDistribution;

public class AutoScalingGroupInstancesDistribution extends Diffable implements Copyable<InstancesDistribution> {

    private String onDemandAllocationStrategy;
    private Integer onDemandBaseCapacity;
    private Integer onDemandPercentageAboveBaseCapacity;
    private String spotAllocationStrategy;
    private Integer spotInstancePools;
    private String spotMaxPrice;

    /**
     * The strategy on how to allocate instance types to fulfill On-Demand capacity.
     */
    @ValidStrings({ "prioritized" })
    public String getOnDemandAllocationStrategy() {
        return onDemandAllocationStrategy;
    }

    public void setOnDemandAllocationStrategy(String onDemandAllocationStrategy) {
        this.onDemandAllocationStrategy = onDemandAllocationStrategy;
    }

    /**
     * The minimum amount of the Auto Scaling group's capacity that must be fulfilled by On-Demand Instances.
     */
    public Integer getOnDemandBaseCapacity() {
        return onDemandBaseCapacity;
    }

    public void setOnDemandBaseCapacity(Integer onDemandBaseCapacity) {
        this.onDemandBaseCapacity = onDemandBaseCapacity;
    }

    /**
     * The percentages of On-Demand Instances and Spot Instances for additional capacity above onDemandBaseCapacity.
     */
    public Integer getOnDemandPercentageAboveBaseCapacity() {
        return onDemandPercentageAboveBaseCapacity;
    }

    public void setOnDemandPercentageAboveBaseCapacity(Integer onDemandPercentageAboveBaseCapacity) {
        this.onDemandPercentageAboveBaseCapacity = onDemandPercentageAboveBaseCapacity;
    }

    /**
     * The strategy on how to allocate instances across Spot Instance pools.
     */
    @ValidStrings({ "lowest-price", "capacity-optimized" })
    public String getSpotAllocationStrategy() {
        return spotAllocationStrategy;
    }

    public void setSpotAllocationStrategy(String spotAllocationStrategy) {
        this.spotAllocationStrategy = spotAllocationStrategy;
    }

    /**
     * The number of Spot Instance pools.
     */
    @Range(min = 1, max = 20)
    public Integer getSpotInstancePools() {
        return spotInstancePools;
    }

    public void setSpotInstancePools(Integer spotInstancePools) {
        this.spotInstancePools = spotInstancePools;
    }

    /**
     * The maximum price for a Spot Instance.
     */
    public String getSpotMaxPrice() {
        return spotMaxPrice;
    }

    public void setSpotMaxPrice(String spotMaxPrice) {
        this.spotMaxPrice = spotMaxPrice;
    }

    @Override
    public void copyFrom(InstancesDistribution model) {
        setOnDemandAllocationStrategy(model.onDemandAllocationStrategy());
        setOnDemandBaseCapacity(model.onDemandBaseCapacity());
        setOnDemandPercentageAboveBaseCapacity(model.onDemandPercentageAboveBaseCapacity());
        setSpotAllocationStrategy(model.spotAllocationStrategy());
        setSpotInstancePools(model.spotInstancePools());
        setSpotMaxPrice(model.spotMaxPrice());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public InstancesDistribution toInstancesDistribution() {
        return InstancesDistribution.builder()
            .onDemandAllocationStrategy(getOnDemandAllocationStrategy())
            .onDemandBaseCapacity(getOnDemandBaseCapacity())
            .onDemandPercentageAboveBaseCapacity(getOnDemandPercentageAboveBaseCapacity())
            .spotAllocationStrategy(getSpotAllocationStrategy())
            .spotInstancePools(getSpotInstancePools())
            .spotMaxPrice(getSpotMaxPrice())
            .build();
    }
}
