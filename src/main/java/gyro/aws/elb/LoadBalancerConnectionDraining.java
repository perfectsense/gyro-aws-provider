package gyro.aws.elb;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.elasticloadbalancing.model.ConnectionDraining;

public class LoadBalancerConnectionDraining extends Diffable implements Copyable<ConnectionDraining> {
    private Boolean enabled;
    private Integer timeout;

    /**
     * If set to ``true``, the load balancer allows existing requests to complete before the load balancer shifts traffic away from a deregistered or unhealthy instance. Defaults to ``false``.
     */
    @Updatable
    public Boolean getEnabled() {
        if (enabled == null) {
            enabled = false;
        }

        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * The maximum time, in seconds, to keep the existing connections open before deregistering the instances. Defaults to ``600``.
     */
    @Updatable
    public Integer getTimeout() {
        if (timeout == null) {
            timeout = 600;
        }

        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    @Override
    public void copyFrom(ConnectionDraining connectionDraining) {
        setEnabled(connectionDraining.enabled());
        setTimeout(connectionDraining.timeout());
    }

    @Override
    public String toDisplayString() {
        return "connection draining";
}

    @Override
    public String primaryKey() {
        return "connection draining";
    }

    ConnectionDraining toConnectionDraining() {
        return ConnectionDraining.builder().enabled(getEnabled()).timeout(getTimeout()).build();
    }
}
