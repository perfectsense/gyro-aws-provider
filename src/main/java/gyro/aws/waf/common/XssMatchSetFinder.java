package gyro.aws.waf.common;

import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.waf.model.XssMatchSet;

abstract public class XssMatchSetFinder<T extends SdkClient, U extends AwsResource> extends AwsFinder<T, XssMatchSet, U> {
    private String XssMatchSetId;

    /**
     * The ID of xss match set.
     */
    public String getXssMatchSetId() {
        return XssMatchSetId;
    }

    public void setXssMatchSetId(String xssMatchSetId) {
        XssMatchSetId = xssMatchSetId;
    }
}
