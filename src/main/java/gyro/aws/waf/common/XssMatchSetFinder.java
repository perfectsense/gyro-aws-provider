package gyro.aws.waf.common;

import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.waf.model.XssMatchSet;

abstract public class XssMatchSetFinder<T extends SdkClient, U extends AwsResource> extends AwsFinder<T, XssMatchSet, U> {
    private String id;

    /**
     * The ID of xss match set.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
