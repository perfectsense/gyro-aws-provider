package gyro.aws.waf.common;

import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.waf.model.SizeConstraintSet;

abstract public class SizeConstraintSetFinder<T extends SdkClient, U extends AwsResource> extends AwsFinder<T, SizeConstraintSet, U> {
    private String id;

    /**
     * The ID of size constraint set.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}