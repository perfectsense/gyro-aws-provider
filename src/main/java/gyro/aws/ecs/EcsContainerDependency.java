package gyro.aws.ecs;

import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ecs.model.ContainerCondition;
import software.amazon.awssdk.services.ecs.model.ContainerDependency;

public class EcsContainerDependency extends Diffable {

    private String containerName;
    private ContainerCondition condition;

    /**
     * The name of a container. (Required)
     */
    @Required
    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    /**
     * The dependency condition of the container. (Required)
     * Valid values are ``START``, ``COMPLETE``, ``SUCCESS``, and ``HEALTHY``.
     */
    @Required
    public ContainerCondition getCondition() {
        return condition;
    }

    public void setCondition(ContainerCondition condition) {
        this.condition = condition;
    }

    @Override
    public String primaryKey() {
        return null;
    }

    public void copyFrom(ContainerDependency model) {
        setContainerName(model.containerName());
        setCondition(model.condition());
    }

    public ContainerDependency copyTo() {
        return ContainerDependency.builder()
            .containerName(getContainerName())
            .condition(getCondition())
            .build();
    }
}
