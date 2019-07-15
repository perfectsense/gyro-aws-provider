package gyro.aws.elb;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.elasticloadbalancing.model.ConnectionSettings;

public class LoadBalancerConnectionSettings extends Diffable implements Copyable<ConnectionSettings> {
    private Integer idleTimeout;

    /**
     * The amount in seconds the load balancer allows the connections to remain idle (no data is sent over the connection) for the specified duration. Defaults to ``600``.
     */
    @Updatable
    public Integer getIdleTimeout() {
        if (idleTimeout == null) {
            idleTimeout = 600;
        }

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
