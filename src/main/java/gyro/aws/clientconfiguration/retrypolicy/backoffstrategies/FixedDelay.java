package gyro.aws.clientconfiguration.retrypolicy.backoffstrategies;

import gyro.aws.clientconfiguration.ClientConfigurationInterface;
import gyro.aws.clientconfiguration.ClientConfigurationUtils;
import software.amazon.awssdk.core.retry.backoff.BackoffStrategy;
import software.amazon.awssdk.core.retry.backoff.FixedDelayBackoffStrategy;

public class FixedDelay implements BackoffStrategyInterface, ClientConfigurationInterface {

    private String delay;

    public String getDelay() {
        return delay;
    }

    public void setDelay(String delay) {
        this.delay = delay;
    }

    @Override
    public void validate() {
        ClientConfigurationUtils.validate(getDelay(), "delay", "fixed-delay");
    }

    @Override
    public BackoffStrategy toBackoffStrategy() {
        return FixedDelayBackoffStrategy.create(ClientConfigurationUtils.getDuration(getDelay()));
    }
}
