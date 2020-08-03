package gyro.aws.ecs;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ecs.model.NetworkConfiguration;

public class EcsNetworkConfiguration extends Diffable implements Copyable<NetworkConfiguration> {

    private EcsAwsVpcConfiguration awsVpcConfiguration;

    /**
     * The VPC configuration for the service. (Required)
     */
    @Required
    @Updatable
    public EcsAwsVpcConfiguration getAwsVpcConfiguration() {
        return awsVpcConfiguration;
    }

    public void setAwsVpcConfiguration(EcsAwsVpcConfiguration awsVpcConfiguration) {
        this.awsVpcConfiguration = awsVpcConfiguration;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(NetworkConfiguration model) {
        EcsAwsVpcConfiguration config = newSubresource(EcsAwsVpcConfiguration.class);
        config.copyFrom(model.awsvpcConfiguration());
        setAwsVpcConfiguration(config);
    }

    public NetworkConfiguration toNetworkConfiguration() {
        return NetworkConfiguration.builder()
            .awsvpcConfiguration(getAwsVpcConfiguration().toAwsVpcConfiguration())
            .build();
    }
}
