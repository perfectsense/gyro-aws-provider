package gyro.aws.ecs;

import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ecs.model.Ulimit;
import software.amazon.awssdk.services.ecs.model.UlimitName;

public class EcsUlimit extends Diffable {

    private UlimitName name;
    private Integer softLimit;
    private Integer hardLimit;

    /**
     * The type of the ulimit. (Required)
     * Valid values are ``core``, ``cpu``, ``data``, ``fsize``, ``locks``, ``memlock``, ``msgqueue``, ``nice``, ``nofile``, ``nproc``, ``rss``, ``rtprio``, ``rttime``, ``sigpending``, and ``stack``.
     */
    @Required
    public UlimitName getName() {
        return name;
    }

    public void setName(UlimitName name) {
        this.name = name;
    }

    /**
     * The soft limit for the ulimit type. (Required)
     */
    @Required
    public Integer getSoftLimit() {
        return softLimit;
    }

    public void setSoftLimit(Integer softLimit) {
        this.softLimit = softLimit;
    }

    /**
     * The hard limit for the ulimit type. (Required)
     */
    @Required
    public Integer getHardLimit() {
        return hardLimit;
    }

    public void setHardLimit(Integer hardLimit) {
        this.hardLimit = hardLimit;
    }

    @Override
    public String primaryKey() {
        return getName().toString();
    }

    public void copyFrom(Ulimit model) {
        setName(model.name());
        setSoftLimit(model.softLimit());
        setHardLimit(model.hardLimit());
    }

    public Ulimit copyTo() {
        return Ulimit.builder()
            .name(getName())
            .softLimit(getSoftLimit())
            .hardLimit(getHardLimit())
            .build();
    }
}
