package gyro.aws.waf.common;

import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.waf.model.RateBasedRule;

abstract public class RateRuleFinder<T extends SdkClient, U extends AwsResource> extends AwsFinder<T, RateBasedRule, U> {
    private String id;

    /**
     * The ID of rate rule.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
