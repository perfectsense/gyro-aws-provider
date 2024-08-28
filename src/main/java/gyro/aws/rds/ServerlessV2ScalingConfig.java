package gyro.aws.rds;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.rds.model.ServerlessV2ScalingConfiguration;
import software.amazon.awssdk.services.rds.model.ServerlessV2ScalingConfigurationInfo;

public class ServerlessV2ScalingConfig extends Diffable implements Copyable<ServerlessV2ScalingConfiguration> {

    private Double maxCapacity;
    private Double minCapacity;

    /**
     * The maximum number of Aurora capacity units (ACUs) for a DB instance in an Aurora Serverless v2 cluster.
     */
    public Double getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Double maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    /**
     * The minimum number of Aurora capacity units (ACUs) for a DB instance in an Aurora Serverless v2 cluster.
     */
    public Double getMinCapacity() {
        return minCapacity;
    }

    public void setMinCapacity(Double minCapacity) {
        this.minCapacity = minCapacity;
    }

    @Override
    public void copyFrom(ServerlessV2ScalingConfiguration model) {
        setMaxCapacity(model.maxCapacity());
        setMinCapacity(model.minCapacity());
    }

    public void copyFrom(ServerlessV2ScalingConfigurationInfo model) {
        setMaxCapacity(model.maxCapacity());
        setMinCapacity(model.minCapacity());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public ServerlessV2ScalingConfiguration toServerlessV2ScalingConfiguration() {
        ServerlessV2ScalingConfiguration.Builder builder = ServerlessV2ScalingConfiguration.builder();

        if (getMaxCapacity() != null) {
            builder.maxCapacity(getMaxCapacity());
        }

        if (getMinCapacity() != null) {
            builder.minCapacity(getMinCapacity());
        }

        return builder.build();
    }
}
