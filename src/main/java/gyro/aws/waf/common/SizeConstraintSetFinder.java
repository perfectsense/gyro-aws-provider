package gyro.aws.waf.common;

import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.waf.model.SizeConstraintSet;

abstract public class SizeConstraintSetFinder<T extends SdkClient, U extends AwsResource> extends AwsFinder<T, SizeConstraintSet, U> {
    private String sizeConstraintSetId;

    /**
     * The ID of size constraint set.
     */
    public String getSizeConstraintSetId() {
        return sizeConstraintSetId;
    }

    public void setSizeConstraintSetId(String sizeConstraintSetId) {
        this.sizeConstraintSetId = sizeConstraintSetId;
    }
}