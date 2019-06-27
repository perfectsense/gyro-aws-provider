package gyro.aws.waf.common;

import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.waf.model.RegexPatternSet;

abstract public class RegexPatternSetFinder<T extends SdkClient, U extends AwsResource> extends AwsFinder<T, RegexPatternSet, U> {
    private String regexPatternSetId;

    /**
     * The ID of regex pattern set.
     */
    public String getRegexPatternSetId() {
        return regexPatternSetId;
    }

    public void setRegexPatternSetId(String regexPatternSetId) {
        this.regexPatternSetId = regexPatternSetId;
    }
}