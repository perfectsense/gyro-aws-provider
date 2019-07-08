package gyro.aws.waf.common;

import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.waf.model.IPSet;

abstract public class IpSetFinder<T extends SdkClient, U extends AwsResource> extends AwsFinder<T, IPSet, U> {
    private String ipSetId;

    /**
     * The ID of the ip set.
     */
    public String getIpSetId() {
        return ipSetId;
    }

    public void setIpSetId(String ipSetId) {
        this.ipSetId = ipSetId;
    }
}