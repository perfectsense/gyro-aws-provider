package gyro.aws.ecs;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.ecs.model.DeploymentConfiguration;

public class EcsDeploymentConfiguration extends Diffable implements Copyable<DeploymentConfiguration> {

    public Integer maximumPercent;
    public Integer minimumHealthyPercent;

    /**
     * The upper limit on the number of tasks in a service that are allowed in the RUNNING or PENDING state during a deployment. Defaults to ``200``.
     */
    @Updatable
    public Integer getMaximumPercent() {
        return maximumPercent;
    }

    public void setMaximumPercent(Integer maximumPercent) {
        this.maximumPercent = maximumPercent;
    }

    /**
     * The lower limit on the number of tasks in a service that are allowed in the RUNNING state during a deployment. Defaults to ``100``.
     */
    @Updatable
    public Integer getMinimumHealthyPercent() {
        return minimumHealthyPercent;
    }

    public void setMinimumHealthyPercent(Integer minimumHealthyPercent) {
        this.minimumHealthyPercent = minimumHealthyPercent;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(DeploymentConfiguration model) {
        setMaximumPercent(model.maximumPercent());
        setMinimumHealthyPercent(model.minimumHealthyPercent());
    }

    public DeploymentConfiguration toDeploymentConfiguration() {
        return DeploymentConfiguration.builder()
            .maximumPercent(getMaximumPercent())
            .minimumHealthyPercent(getMinimumHealthyPercent())
            .build();
    }
}
