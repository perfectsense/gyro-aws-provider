package gyro.aws.globalaccelerator;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.globalaccelerator.model.PortRange;

public class ListenerPortRange extends Diffable implements Copyable<PortRange> {

    private Integer fromPort;
    private Integer toPort;

    @Override
    public void copyFrom(PortRange portRange) {
        setFromPort(portRange.fromPort());
        setToPort(portRange.toPort());
    }

    @Override
    public String primaryKey() {
        return String.format("%s:%s", fromPort, toPort);
    }

    public Integer getFromPort() {
        return fromPort;
    }

    public void setFromPort(Integer fromPort) {
        this.fromPort = fromPort;
    }

    public Integer getToPort() {
        return toPort;
    }

    public void setToPort(Integer toPort) {
        this.toPort = toPort;
    }

    PortRange portRange() {
        return PortRange.builder()
            .fromPort(getFromPort())
            .toPort(getToPort())
            .build();
    }
}
