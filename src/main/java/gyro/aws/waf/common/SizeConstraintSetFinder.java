package gyro.aws.waf.common;

import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.waf.model.SizeConstraintSet;

abstract public class SizeConstraintSetFinder<T extends SdkClient, U extends AwsResource> extends AwsFinder<T, SizeConstraintSet, U> {
    private String SizeConstraintSetId;

    /**
     * The id of size constraint set.
     */
    public String getSizeConstraintSetId() {
        return SizeConstraintSetId;
    }

    public void setSizeConstraintSetId(String sizeConstraintSetId) {
        SizeConstraintSetId = sizeConstraintSetId;
    }
}