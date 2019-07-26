package gyro.aws.waf.common;

import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.waf.model.IPSet;

abstract public class IpSetFinder<T extends SdkClient, U extends AwsResource> extends AwsFinder<T, IPSet, U> {
    private String id;

    /**
     * The ID of the ip set.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}