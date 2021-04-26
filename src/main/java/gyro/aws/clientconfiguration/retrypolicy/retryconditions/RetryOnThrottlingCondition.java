package gyro.aws.clientconfiguration.retrypolicy.retryconditions;

import gyro.aws.clientconfiguration.ClientConfigurationInterface;
import software.amazon.awssdk.core.retry.conditions.RetryCondition;

public class RetryOnThrottlingCondition implements RetryConditionInterface, ClientConfigurationInterface {

    @Override
    public void validate() {

    }

    @Override
    public RetryCondition toRetryCondition() {
        return software.amazon.awssdk.core.retry.conditions.RetryOnThrottlingCondition.create();
    }
}
