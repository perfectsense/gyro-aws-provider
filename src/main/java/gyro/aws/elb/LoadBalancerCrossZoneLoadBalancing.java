package gyro.aws.elb;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.elasticloadbalancing.model.CrossZoneLoadBalancing;

public class LoadBalancerCrossZoneLoadBalancing extends Diffable implements Copyable<CrossZoneLoadBalancing> {
    private Boolean enabled;

    /**
     * If enabled, the load balancer routes the request traffic evenly across all instances regardless of the Availability Zones. Defaults to false.
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

    @Override
    public void copyFrom(CrossZoneLoadBalancing crossZoneLoadBalancing) {
        setEnabled(crossZoneLoadBalancing.enabled());
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
