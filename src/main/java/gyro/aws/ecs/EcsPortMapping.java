package gyro.aws.ecs;

import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
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
}
