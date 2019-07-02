package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.s3.model.AbortIncompleteMultipartUpload;

public class S3LifecycleRuleAbortIncompleteMultipartUpload extends Diffable implements Copyable<AbortIncompleteMultipartUpload> {
    private Integer daysAfterInitiation;

    /**
     * Number of days after which incomplete multipart upload data be deleted.
     */
    @Updatable
    public Integer getDaysAfterInitiation() {
        return daysAfterInitiation;
    }

    public void setDaysAfterInitiation(Integer daysAfterInitiation) {
        this.daysAfterInitiation = daysAfterInitiation;
    }

    @Override
    public void copyFrom(AbortIncompleteMultipartUpload abortIncompleteMultipartUpload) {
        setDaysAfterInitiation(abortIncompleteMultipartUpload.daysAfterInitiation());
    }

    @Override
    public String toDisplayString() {
        return "abort incomplete multipart abort";
    }

    @Override
    public String primaryKey() {
        return "abort incomplete multipart abort";
    }

    AbortIncompleteMultipartUpload toAbortIncompleteMultipartUpload() {
        return AbortIncompleteMultipartUpload.builder().daysAfterInitiation(getDaysAfterInitiation()).build();
    }
}
