package gyro.aws.waf.common;

import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.waf.model.RegexPatternSet;

abstract public class RegexPatternSetFinder<T extends SdkClient, U extends AwsResource> extends AwsFinder<T, RegexPatternSet, U> {
    private String id;

    /**
     * The ID of regex pattern set.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}