package gyro.aws.ecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ecs.model.NetworkMode;
import software.amazon.awssdk.services.ecs.model.PortMapping;
import software.amazon.awssdk.services.ecs.model.TransportProtocol;

public class EcsPortMapping extends Diffable {

    private Integer containerPort;
    private Integer hostPort;
    private TransportProtocol protocol;

    @Required
    public Integer getContainerPort() {
        return containerPort;
    }

    public void setContainerPort(Integer containerPort) {
        this.containerPort = containerPort;
    }

    public Integer getHostPort() {
        return hostPort;
    }

    public void setHostPort(Integer hostPort) {
        this.hostPort = hostPort;
    }

    public TransportProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(TransportProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public String primaryKey() {
        return null;
    }

    public void copyFrom(PortMapping model) {
        setContainerPort(model.containerPort());
        setHostPort(model.hostPort());
        setProtocol(model.protocol());
    }

    public PortMapping copyTo() {
        return PortMapping.builder()
            .containerPort(getContainerPort())
            .hostPort(getHostPort())
            .protocol(getProtocol())
            .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        EcsContainerDefinition containerDefinition = (EcsContainerDefinition) parent();
        EcsTaskDefinitionResource taskDefinition = containerDefinition.getParentTaskDefinition();

        if (taskDefinition.getNetworkMode() == NetworkMode.AWSVPC && configuredFields.contains("host-port") && !getHostPort().equals(getContainerPort())) {
            errors.add(new ValidationError(
                this,
                "host-port",
                "When the task definition's 'network-mode' is 'awsvpc', the 'host-port' must either be blank or hold the same value as the 'container-port'."
            ));
        }

        return errors;
    }
}
