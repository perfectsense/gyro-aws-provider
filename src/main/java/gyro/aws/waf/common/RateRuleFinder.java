package gyro.aws.waf.common;

import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.waf.model.RateBasedRule;

abstract public class RateRuleFinder<T extends SdkClient, U extends AwsResource> extends AwsFinder<T, RateBasedRule, U> {
    private String ruleId;

    /**
     * The id of rate rule.
     */
    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }
}
