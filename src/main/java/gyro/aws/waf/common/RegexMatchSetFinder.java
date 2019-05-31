package gyro.aws.waf.common;

import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.waf.model.RegexMatchSet;

abstract public class RegexMatchSetFinder<T extends SdkClient, U extends AwsResource> extends AwsFinder<T, RegexMatchSet, U> {
    private String RegexMatchSetId;

    /**
     * The id of regex match set.
     */
    public String getRegexMatchSetId() {
        return RegexMatchSetId;
    }

    public void setRegexMatchSetId(String regexMatchSetId) {
        RegexMatchSetId = regexMatchSetId;
    }
}