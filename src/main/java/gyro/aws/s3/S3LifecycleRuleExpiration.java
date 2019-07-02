package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.s3.model.LifecycleExpiration;

public class S3LifecycleRuleExpiration extends Diffable implements Copyable<LifecycleExpiration> {
    private Integer days;
    private Boolean expiredObjectDeleteMarker;

    /**
     * The lifetime, in days, of the objects that are subject to the rule. Valid values integers ``1`` and above.
     */
    @Updatable
    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;

        validate();
    }

    /**
     * Indicates whether Amazon S3 will remove a delete marker with no noncurrent versions. If set to true, the delete marker will be expired. Cannot be set with 'days' or lifecyclerule 'tags'.
     */
    @Updatable
    public Boolean getExpiredObjectDeleteMarker() {
        return expiredObjectDeleteMarker;
    }

    public void setExpiredObjectDeleteMarker(Boolean expiredObjectDeleteMarker) {
        this.expiredObjectDeleteMarker = expiredObjectDeleteMarker;

        validate();
    }

    @Override
    public void copyFrom(LifecycleExpiration lifecycleExpiration) {
        setDays(lifecycleExpiration.days());
        setExpiredObjectDeleteMarker(lifecycleExpiration.expiredObjectDeleteMarker());
    }

    @Override
    public String toDisplayString() {
        return "lifecycle expiration";
    }

    @Override
    public String primaryKey() {
        return "lifecycle expiration";
    }

    LifecycleExpiration toLifecycleExpiration() {
        return LifecycleExpiration.builder().date(null)
            .days(getDays())
            .expiredObjectDeleteMarker(getExpiredObjectDeleteMarker())
            .build();
    }

    private void validate() {
        if (getDays() != null && getExpiredObjectDeleteMarker() != null) {
            throw new GyroException("Param 'days' and 'expired-object-delete-marker' cannot be both set together.");
        }
    }
}
