package gyro.aws.elb;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.elasticloadbalancing.model.ConnectionDraining;

public class LoadBalancerConnectionDraining extends Diffable implements Copyable<ConnectionDraining> {
    private Boolean enabled;
    private Integer timeout;

    @Updatable
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Updatable
    public Integer getTimeout() {
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
