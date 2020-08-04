package gyro.aws.ecs;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ecs.model.DeploymentController;
import software.amazon.awssdk.services.ecs.model.DeploymentControllerType;

public class EcsDeploymentController extends Diffable implements Copyable<DeploymentController> {

    private DeploymentControllerType type;

    /**
     * The deployment controller to use for the service. (Required)
     */
    @Required
    public DeploymentControllerType getType() {
        return type;
    }

    public void setType(DeploymentControllerType type) {
        this.type = type;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(DeploymentController model) {
        setType(model.type());
    }

    public DeploymentController toDeploymentController() {
        return DeploymentController.builder().type(getType()).build();
    }
}
