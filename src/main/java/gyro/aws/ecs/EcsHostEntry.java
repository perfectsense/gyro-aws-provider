package gyro.aws.ecs;

import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ecs.model.HostEntry;

public class EcsHostEntry extends Diffable {

    private String hostname;
    private String ipAddress;

    /**
     * The hostname to use in the /etc/hosts entry. (Required)
     */
    @Required
    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * The IP address to use in the /etc/hosts entry. (Required)
     */
    @Required
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public String primaryKey() {
        return getHostname();
    }

    public void copyFrom(HostEntry model) {
        setHostname(model.hostname());
        setIpAddress(model.ipAddress());
    }

    public HostEntry copyTo() {
        return HostEntry.builder()
            .hostname(getHostname())
            .ipAddress(getIpAddress())
            .build();
    }
}
