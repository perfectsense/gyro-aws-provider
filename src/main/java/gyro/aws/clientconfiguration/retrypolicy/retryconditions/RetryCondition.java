package gyro.aws.clientconfiguration.retrypolicy.retryconditions;

import java.util.Objects;
import java.util.stream.Stream;

import gyro.aws.clientconfiguration.ClientConfigurationInterface;
import gyro.core.GyroException;

public class RetryCondition implements RetryConditionInterface, ClientConfigurationInterface {

    private AndRetryCondition andRetryCondition;
    private OrRetryCondition orRetryCondition;
    private MaxNumberOfRetryCondition maxNumberOfRetryCondition;
    private RetryOnErrorCodesCondition retryOnErrorCodesCondition;
    private RetryOnStatusCodesCondition retryOnStatusCodesCondition;
    private RetryOnThrottlingCondition retryOnThrottlingCondition;

    public AndRetryCondition getAndRetryCondition() {
        return andRetryCondition;
    }

    public void setAndRetryCondition(AndRetryCondition andRetryCondition) {
        this.andRetryCondition = andRetryCondition;
    }

    public OrRetryCondition getOrRetryCondition() {
        return orRetryCondition;
    }

    public void setOrRetryCondition(OrRetryCondition orRetryCondition) {
        this.orRetryCondition = orRetryCondition;
    }

    public MaxNumberOfRetryCondition getMaxNumberOfRetryCondition() {
        return maxNumberOfRetryCondition;
    }

    public void setMaxNumberOfRetryCondition(MaxNumberOfRetryCondition maxNumberOfRetryCondition) {
        this.maxNumberOfRetryCondition = maxNumberOfRetryCondition;
    }

    public RetryOnErrorCodesCondition getRetryOnErrorCodesCondition() {
        return retryOnErrorCodesCondition;
    }

    public void setRetryOnErrorCodesCondition(RetryOnErrorCodesCondition retryOnErrorCodesCondition) {
        this.retryOnErrorCodesCondition = retryOnErrorCodesCondition;
    }

    public RetryOnStatusCodesCondition getRetryOnStatusCodesCondition() {
        return retryOnStatusCodesCondition;
    }

    public void setRetryOnStatusCodesCondition(RetryOnStatusCodesCondition retryOnStatusCodesCondition) {
        this.retryOnStatusCodesCondition = retryOnStatusCodesCondition;
    }

    public RetryOnThrottlingCondition getRetryOnThrottlingCondition() {
        return retryOnThrottlingCondition;
    }

    public void setRetryOnThrottlingCondition(RetryOnThrottlingCondition retryOnThrottlingCondition) {
        this.retryOnThrottlingCondition = retryOnThrottlingCondition;
    }

    @Override
    public void validate() {
        long count = Stream.of(
            getAndRetryCondition(),
            getOrRetryCondition(),
            getMaxNumberOfRetryCondition(),
            getRetryOnErrorCodesCondition(),
            getRetryOnStatusCodesCondition(),
            getRetryOnThrottlingCondition())
            .filter(Objects::nonNull).count();

        if (count > 1) {
            throw new GyroException(
                "Only one of 'and-retry-condition',"
                    + " 'or-retry-condition',"
                    + " 'max-number-of-retry-condition',"
                    + " 'retry-on-error-codes-condition',"
                    + " 'retry-on-status-codes-condition'"
                    + " or 'retry-on-throttling-condition' is allowed.");
        } else if (count == 0) {
            throw new GyroException(
                "One of 'and-retry-condition',"
                    + " 'or-retry-condition',"
                    + " 'max-number-of-retry-condition',"
                    + " 'retry-on-error-codes-condition',"
                    + " 'retry-on-status-codes-condition'"
                    + " or 'retry-on-throttling-condition' is required.");
        }
    }

    @Override
    public software.amazon.awssdk.core.retry.conditions.RetryCondition toRetryCondition() {
        return Stream.of(
            getAndRetryCondition(),
            getOrRetryCondition(),
            getMaxNumberOfRetryCondition(),
            getRetryOnErrorCodesCondition(),
            getRetryOnStatusCodesCondition(),
            getRetryOnThrottlingCondition()).filter(Objects::nonNull).findFirst().get().toRetryCondition();
    }
}
