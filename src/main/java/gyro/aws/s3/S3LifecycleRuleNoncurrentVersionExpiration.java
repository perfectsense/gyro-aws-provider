package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.s3.model.NoncurrentVersionExpiration;

public class S3LifecycleRuleNoncurrentVersionExpiration extends Diffable implements Copyable<NoncurrentVersionExpiration> {
    private Integer days;

    /**
     * Non current version expiration days. Depends on the values set in non current version transition.
     */
    @Updatable
    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    @Override
    public void copyFrom(NoncurrentVersionExpiration noncurrentVersionExpiration) {
        setDays(noncurrentVersionExpiration.noncurrentDays());
    }

    @Override
    public String primaryKey() {
        return "non current version expiration";
    }

    NoncurrentVersionExpiration toNoncurrentVersionExpiration() {
        return NoncurrentVersionExpiration.builder().noncurrentDays(getDays()).build();
    }
}
