package gyro.aws.elbv2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroupStickinessConfig;

public class TargetGroupStickiness extends Diffable implements Copyable<TargetGroupStickinessConfig> {

    private Boolean enabled;
    private Integer duration;

    /**
     * Determines if requests should be directed to the same target group
     */
    @Updatable
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * How long requests should be directed to the same target group
     */
    @Updatable
    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    @Override
    public void copyFrom(TargetGroupStickinessConfig targetGroupStickinessConfig) {
        setDuration(targetGroupStickinessConfig.durationSeconds());
        setEnabled(targetGroupStickinessConfig.enabled());
    }

    @Override
    public String primaryKey() {
        return String.format("%s:%s", getDuration(), getEnabled());
    }
}
