package gyro.aws.rds;

import gyro.core.resource.Diffable;
import gyro.core.resource.ResourceUpdatable;

public class ScalingConfiguration extends Diffable {

    private Boolean autoPause;
    private Integer maxCapacity;
    private Integer minCapacity;
    private Integer secondsUntilAutoPause;

    /**
     * Allow automatic pause for an Aurora DB cluster in serverless DB engine mode. A DB cluster can be paused only when it's idle (i.e. it has no connections).
     */
    @ResourceUpdatable
    public Boolean getAutoPause() {
        return autoPause;
    }

    public void setAutoPause(Boolean autoPause) {
        this.autoPause = autoPause;
    }

    /**
     * The maximum capacity for an Aurora DB cluster in serverless DB engine mode. Valid values are ``2``, ``4``, ``8``, ``16``, ``32``, ``64``, ``128``, and ``256``.
     */
    @ResourceUpdatable
    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    /**
     * The minimum capacity for an Aurora DB cluster in serverless DB engine mode. Valid values are ``2``, ``4``, ``8``, ``16``, ``32``, ``64``, ``128``, and ``256``.
     */
    @ResourceUpdatable
    public Integer getMinCapacity() {
        return minCapacity;
    }

    public void setMinCapacity(Integer minCapacity) {
        this.minCapacity = minCapacity;
    }

    /**
     * The time before an Aurora DB cluster in serverless mode is paused in seconds.
     */
    @ResourceUpdatable
    public Integer getSecondsUntilAutoPause() {
        return secondsUntilAutoPause;
    }

    public void setSecondsUntilAutoPause(Integer secondsUntilAutoPause) {
        this.secondsUntilAutoPause = secondsUntilAutoPause;
    }

    @Override
    public String primaryKey() {
        return "scaling configuration";
    }

    @Override
    public String toDisplayString() {
        return "scaling configuration";
    }
}
