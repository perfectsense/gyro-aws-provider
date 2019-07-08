package gyro.aws.elb;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.elasticloadbalancing.model.ConnectionSettings;

public class LoadBalancerConnectionSettings extends Diffable implements Copyable<ConnectionSettings> {
    private Integer idleTimeout;

    @Updatable
    public Integer getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(Integer idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    @Override
    public void copyFrom(ConnectionSettings connectionSettings) {
        setIdleTimeout(connectionSettings.idleTimeout());
    }

    @Override
    public String toDisplayString() {
        return "connection settings";
    }

    @Override
    public String primaryKey() {
        return "connection settings";
    }

    ConnectionSettings toConnectionSettings() {
        return ConnectionSettings.builder().idleTimeout(getIdleTimeout()).build();
    }
}
