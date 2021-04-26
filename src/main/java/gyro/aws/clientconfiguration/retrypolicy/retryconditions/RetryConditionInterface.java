package gyro.aws.clientconfiguration.retrypolicy.retryconditions;

import software.amazon.awssdk.core.retry.conditions.RetryCondition;

public interface RetryConditionInterface {

    RetryCondition toRetryCondition();
}
