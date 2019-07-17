package gyro.aws.waf.common;

import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.waf.model.RegexMatchSet;

abstract public class RegexMatchSetFinder<T extends SdkClient, U extends AwsResource> extends AwsFinder<T, RegexMatchSet, U> {
    private String id;

    /**
     * The ID of regex match set.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}