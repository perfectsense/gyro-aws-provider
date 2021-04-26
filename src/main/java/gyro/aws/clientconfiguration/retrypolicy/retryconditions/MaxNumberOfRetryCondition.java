package gyro.aws.clientconfiguration.retrypolicy.retryconditions;

import gyro.aws.clientconfiguration.ClientConfigurationInterface;
import gyro.core.GyroException;
import software.amazon.awssdk.core.retry.conditions.MaxNumberOfRetriesCondition;
import software.amazon.awssdk.core.retry.conditions.RetryCondition;

public class MaxNumberOfRetryCondition implements RetryConditionInterface, ClientConfigurationInterface {

    private Integer retryCount;

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    @Override
    public void validate() {
        if (getRetryCount() == null || getRetryCount() < 0) {
            throw new GyroException("retry-count cannot be empty or less than 1.");
        }
    }

    @Override
    public RetryCondition toRetryCondition() {
        return MaxNumberOfRetriesCondition.create(getRetryCount());
    }
}
