package gyro.aws.waf.common;

import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.waf.model.ByteMatchSet;

abstract public class ByteMatchSetFinder<T extends SdkClient, U extends AwsResource> extends AwsFinder<T, ByteMatchSet, U> {
    private String ByteMatchSetId;

    public String getByteMatchSetId() {
        return ByteMatchSetId;
    }

    public void setByteMatchSetId(String byteMatchSetId) {
        ByteMatchSetId = byteMatchSetId;
    }
}