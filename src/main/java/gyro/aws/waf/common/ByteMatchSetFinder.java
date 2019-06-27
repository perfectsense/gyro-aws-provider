package gyro.aws.waf.common;

import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.waf.model.ByteMatchSet;

abstract public class ByteMatchSetFinder<T extends SdkClient, U extends AwsResource> extends AwsFinder<T, ByteMatchSet, U> {
    private String byteMatchSetId;

    public String getByteMatchSetId() {
        return byteMatchSetId;
    }

    public void setByteMatchSetId(String byteMatchSetId) {
        this.byteMatchSetId = byteMatchSetId;
    }
}