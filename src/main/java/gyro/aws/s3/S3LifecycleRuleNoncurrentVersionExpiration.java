package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.s3.model.NoncurrentVersionExpiration;

public class S3LifecycleRuleNoncurrentVersionExpiration extends Diffable implements Copyable<NoncurrentVersionExpiration> {
    private Integer noncurrentDays;

    /**
     * Non current version expiration days. Depends on the values set in non current version transition.
     */
    @Updatable
    public Integer getNoncurrentDays() {
        return noncurrentDays;
    }

    public void setNoncurrentDays(Integer noncurrentDays) {
        this.noncurrentDays = noncurrentDays;
    }

    @Override
    public void copyFrom(NoncurrentVersionExpiration noncurrentVersionExpiration) {
        setNoncurrentDays(noncurrentVersionExpiration.noncurrentDays());
    }

    @Override
    public String toDisplayString() {
        return "non current version expiration";
    }

    @Override
    public String primaryKey() {
        return "non current version expiration";
    }

    NoncurrentVersionExpiration toNoncurrentVersionExpiration() {
        return NoncurrentVersionExpiration.builder().noncurrentDays(getNoncurrentDays()).build();
    }
}
