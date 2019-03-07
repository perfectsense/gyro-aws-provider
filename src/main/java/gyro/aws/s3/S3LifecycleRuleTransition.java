package gyro.aws.s3;

import gyro.core.diff.Diffable;
import gyro.core.diff.ResourceDiffProperty;
import software.amazon.awssdk.services.s3.model.Transition;

public class S3LifecycleRuleTransition extends Diffable {
    private Integer days;
    private String storageClass;

    public S3LifecycleRuleTransition() {

    }

    public S3LifecycleRuleTransition(Transition transition) {
        setDays(transition.days());
        setStorageClass(transition.storageClassAsString());
    }

    /**
     * Days after creation that versioning would start. Min value 30. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    /**
     * Type of transition. Valid values are ``GLACIER`` or ``STANDARD_IA`` or ``ONEZONE_IA`` or ``INTELLIGENT_TIERING``. (Required)
     */
    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    @Override
    public String primaryKey() {
        return String.format("transition - %s ", getStorageClass());
    }

    @Override
    public String toDisplayString() {
        return String.format("transition - days [%s], storage-class [%s]", getDays(), getStorageClass());
    }

    Transition toTransition() {
        return Transition.builder()
            .date(null)
            .days(getDays())
            .storageClass(getStorageClass())
            .build();
    }
}
