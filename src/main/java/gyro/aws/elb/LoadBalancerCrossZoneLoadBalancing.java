package gyro.aws.elb;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.elasticloadbalancing.model.CrossZoneLoadBalancing;

public class LoadBalancerCrossZoneLoadBalancing extends Diffable implements Copyable<CrossZoneLoadBalancing> {
    private Boolean enabled;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void copyFrom(CrossZoneLoadBalancing crossZoneLoadBalancing) {
        setEnabled(crossZoneLoadBalancing.enabled());
    }

    @Override
    public String toDisplayString() {
        return "cross zone load balancing";
    }

    @Override
    public String primaryKey() {
        return "cross zone load balancing";
    }

    CrossZoneLoadBalancing toCrossZoneLoadBalancing() {
        return CrossZoneLoadBalancing.builder()
            .enabled(getEnabled())
            .build();
    }
}
