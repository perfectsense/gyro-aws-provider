package gyro.aws.globalaccelerator;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.globalaccelerator.model.PortOverride;

public class EndpointGroupPortOverride extends Diffable implements Copyable<PortOverride> {

    private Integer endpointPort;
    private Integer listenerPort;

    @Override
    public void copyFrom(PortOverride override) {
        setEndpointPort(override.endpointPort());
        setListenerPort(override.listenerPort());
    }

    @Override
    public String primaryKey() {
        return String.format("%s -> %s", getListenerPort(), getEndpointPort());
    }

    /**
     * The port to connect to on the endpoint.
     */
    public Integer getEndpointPort() {
        return endpointPort;
    }

    public void setEndpointPort(Integer endpointPort) {
        this.endpointPort = endpointPort;
    }

    /**
     * The listener port to override.
     */
    public Integer getListenerPort() {
        return listenerPort;
    }

    public void setListenerPort(Integer listenerPort) {
        this.listenerPort = listenerPort;
    }

    PortOverride portOverride() {
        return PortOverride.builder()
            .endpointPort(getEndpointPort())
            .listenerPort(getListenerPort())
            .build();
    }
}
