package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.s3.model.NoncurrentVersionTransition;

public class S3LifecycleRuleNoncurrentVersionTransition extends Diffable implements Copyable<NoncurrentVersionTransition> {
    private Integer days;
    private String storageClass;

    /**
     * Days after creation that versioning would start. Min value 30. (Required)
     */
    @Updatable
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
        return String.format("%s %s", getDays(), getStorageClass());
    }

    @Override
    public void copyFrom(NoncurrentVersionTransition noncurrentVersionTransition) {
        setDays(noncurrentVersionTransition.noncurrentDays());
        setStorageClass(noncurrentVersionTransition.storageClassAsString());
    }

    NoncurrentVersionTransition toNoncurrentVersionTransition() {
        return NoncurrentVersionTransition.builder()
            .noncurrentDays(getDays())
            .storageClass(getStorageClass())
            .build();
    }
}
