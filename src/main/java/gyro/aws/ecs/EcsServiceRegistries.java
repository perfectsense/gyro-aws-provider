package gyro.aws.ecs;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.ecs.model.ServiceRegistry;

public class EcsServiceRegistries extends Diffable implements Copyable<ServiceRegistry> {

    private String containerName;
    private Integer containerPort;
    private Integer port;
    private String registryArn;

    /**
     * The container to be used for your service discovery service.
     */
    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    /**
     * The container port to be used for your service discovery service.
     */
    public Integer getContainerPort() {
        return containerPort;
    }

    public void setContainerPort(Integer containerPort) {
        this.containerPort = containerPort;
    }

    /**
     * The port value used if your service discovery service specified an SRV record.
     */
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * The Amazon Resource Name (ARN) of the service registry.
     */
    public String getRegistryArn() {
        return registryArn;
    }

    public void setRegistryArn(String registryArn) {
        this.registryArn = registryArn;
    }

    @Override
    public String primaryKey() {
        return String.format(
            "Container Name: %s, Container Port: %s, Port: %s, Registry Arn: %s",
            getContainerName(),
            getContainerPort(),
            getPort(),
            getRegistryArn());
    }

    @Override
    public void copyFrom(ServiceRegistry model) {
        setContainerName(model.containerName());
        setContainerPort(model.containerPort());
        setPort(model.port());
        setRegistryArn(model.registryArn());
    }

    public ServiceRegistry toServiceRegistry() {
        return ServiceRegistry.builder()
            .containerName(getContainerName())
            .containerPort(getContainerPort())
            .port(getPort())
            .registryArn(getRegistryArn())
            .build();
    }
}
