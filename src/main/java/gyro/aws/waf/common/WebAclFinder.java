package gyro.aws.waf.common;

import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.waf.model.WebACL;

abstract public class WebAclFinder<T extends SdkClient, U extends AwsResource> extends AwsFinder<T, WebACL, U> {
    private String id;

    /**
     * The ID of web acl.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
