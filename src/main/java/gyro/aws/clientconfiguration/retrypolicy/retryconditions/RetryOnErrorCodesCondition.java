package gyro.aws.clientconfiguration.retrypolicy.retryconditions;

import java.util.HashSet;
import java.util.List;

import gyro.aws.clientconfiguration.ClientConfigurationInterface;
import gyro.core.GyroException;
import software.amazon.awssdk.awscore.retry.conditions.RetryOnErrorCodeCondition;
import software.amazon.awssdk.core.retry.conditions.RetryCondition;

public class RetryOnErrorCodesCondition implements RetryConditionInterface, ClientConfigurationInterface {

    private List<String> retryableErrorCodes;

    public List<String> getRetryableErrorCodes() {
        return retryableErrorCodes;
    }

    public void setRetryableErrorCodes(List<String> retryableErrorCodes) {
        this.retryableErrorCodes = retryableErrorCodes;
    }

    @Override
    public void validate() {
        if (getRetryableErrorCodes().isEmpty()) {
            throw new GyroException("'retryable-error-codes' cannot be empty.");
        }
    }

    @Override
    public RetryCondition toRetryCondition() {
        return RetryOnErrorCodeCondition.create(new HashSet<>(getRetryableErrorCodes()));
    }
}
