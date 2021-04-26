package gyro.aws.clientconfiguration.retrypolicy.retryconditions;

import java.util.HashSet;
import java.util.List;

import gyro.aws.clientconfiguration.ClientConfigurationInterface;
import gyro.core.GyroException;
import software.amazon.awssdk.core.retry.conditions.RetryCondition;
import software.amazon.awssdk.core.retry.conditions.RetryOnStatusCodeCondition;

public class RetryOnStatusCodesCondition implements RetryConditionInterface, ClientConfigurationInterface {

    private List<Integer> retryableStatusCodes;

    public List<Integer> getRetryableStatusCodes() {
        return retryableStatusCodes;
    }

    public void setRetryableStatusCodes(List<Integer> retryableStatusCodes) {
        this.retryableStatusCodes = retryableStatusCodes;
    }

    @Override
    public void validate() {
        if (getRetryableStatusCodes().isEmpty()) {
            throw new GyroException("'retryable-status-codes' cannot be empty.");
        }

    }

    @Override
    public RetryCondition toRetryCondition() {
        return RetryOnStatusCodeCondition.create(new HashSet<>(getRetryableStatusCodes()));
    }
}
