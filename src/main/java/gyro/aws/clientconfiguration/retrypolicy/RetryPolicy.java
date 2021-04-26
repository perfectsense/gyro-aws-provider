package gyro.aws.clientconfiguration.retrypolicy;

import gyro.aws.clientconfiguration.ClientConfigurationInterface;
import gyro.aws.clientconfiguration.retrypolicy.backoffstrategies.BackoffStrategy;
import gyro.aws.clientconfiguration.retrypolicy.backoffstrategies.ThrottlingBackoffStrategy;
import gyro.aws.clientconfiguration.retrypolicy.retryconditions.CapacityRetryCondition;
import gyro.aws.clientconfiguration.retrypolicy.retryconditions.RetryCondition;
import gyro.core.GyroException;

public class RetryPolicy implements ClientConfigurationInterface {

    private Integer retryCount;
    private Boolean additionalRetryConditionsAllowed;
    private BackoffStrategy backoffStrategy;
    private ThrottlingBackoffStrategy throttlingBackoffStrategy;
    private RetryCondition retryCondition;
    private CapacityRetryCondition capacityRetryCondition;

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Boolean getAdditionalRetryConditionsAllowed() {
        return additionalRetryConditionsAllowed;
    }

    public void setAdditionalRetryConditionsAllowed(Boolean additionalRetryConditionsAllowed) {
        this.additionalRetryConditionsAllowed = additionalRetryConditionsAllowed;
    }

    public BackoffStrategy getBackoffStrategy() {
        return backoffStrategy;
    }

    public void setBackoffStrategy(BackoffStrategy backoffStrategy) {
        this.backoffStrategy = backoffStrategy;
    }

    public ThrottlingBackoffStrategy getThrottlingBackoffStrategy() {
        return throttlingBackoffStrategy;
    }

    public void setThrottlingBackoffStrategy(ThrottlingBackoffStrategy throttlingBackoffStrategy) {
        this.throttlingBackoffStrategy = throttlingBackoffStrategy;
    }

    public RetryCondition getRetryCondition() {
        return retryCondition;
    }

    public void setRetryCondition(RetryCondition retryCondition) {
        this.retryCondition = retryCondition;
    }

    public CapacityRetryCondition getCapacityRetryCondition() {
        return capacityRetryCondition;
    }

    public void setCapacityRetryCondition(CapacityRetryCondition capacityRetryCondition) {
        this.capacityRetryCondition = capacityRetryCondition;
    }

    @Override
    public void validate() {
        if (getRetryCount() != null && getRetryCount() < 0) {
            throw new GyroException("'retry-count' cannot be less than 1.");
        }
    }

    public software.amazon.awssdk.core.retry.RetryPolicy toRetryPolicy() {
        software.amazon.awssdk.core.retry.RetryPolicy.Builder builder = software.amazon.awssdk.core.retry.RetryPolicy.builder();

        if (getRetryCount() != null) {
            builder = builder.numRetries(getRetryCount());
        }

        if (getAdditionalRetryConditionsAllowed() != null) {
            builder = builder.additionalRetryConditionsAllowed(getAdditionalRetryConditionsAllowed());
        }

        if (getBackoffStrategy() != null) {
            builder = builder.backoffStrategy(getBackoffStrategy().toBackoffStrategy());
        }

        if (getThrottlingBackoffStrategy() != null) {
            builder = builder.throttlingBackoffStrategy(getThrottlingBackoffStrategy().toBackoffStrategy());
        }

        if (getRetryCondition() != null) {
            builder = builder.retryCondition(getRetryCondition().toRetryCondition());
        }

        if (getCapacityRetryCondition() != null) {
            builder = builder.retryCapacityCondition(getCapacityRetryCondition().toRetryCondition());
        }

        return builder.build();
    }
}
