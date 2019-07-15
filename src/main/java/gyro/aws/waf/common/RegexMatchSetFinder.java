package gyro.aws.waf.common;

import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.waf.model.RegexMatchSet;

abstract public class RegexMatchSetFinder<T extends SdkClient, U extends AwsResource> extends AwsFinder<T, RegexMatchSet, U> {
    private String regexMatchSetId;

    /**
     * The ID of regex match set.
     */
    public String getRegexMatchSetId() {
        return regexMatchSetId;
    }

    public void setRegexMatchSetId(String regexMatchSetId) {
        this.regexMatchSetId = regexMatchSetId;
    }
}