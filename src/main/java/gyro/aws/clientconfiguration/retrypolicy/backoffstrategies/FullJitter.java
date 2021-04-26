package gyro.aws.clientconfiguration.retrypolicy.backoffstrategies;

import gyro.aws.clientconfiguration.ClientConfigurationInterface;
import gyro.aws.clientconfiguration.ClientConfigurationUtils;
import software.amazon.awssdk.core.retry.backoff.BackoffStrategy;
import software.amazon.awssdk.core.retry.backoff.FullJitterBackoffStrategy;

public class FullJitter implements BackoffStrategyInterface, ClientConfigurationInterface {

    private String baseDelay;
    private String maxBackoffTime;

    public String getBaseDelay() {
        return baseDelay;
    }

    public void setBaseDelay(String baseDelay) {
        this.baseDelay = baseDelay;
    }

    public String getMaxBackoffTime() {
        return maxBackoffTime;
    }

    public void setMaxBackoffTime(String maxBackoffTime) {
        this.maxBackoffTime = maxBackoffTime;
    }

    @Override
    public void validate() {
        ClientConfigurationUtils.validate(getBaseDelay(), "base-delay", "full-jitter");
        ClientConfigurationUtils.validate(getMaxBackoffTime(), "max-backoff-time", "full-jitter");
    }

    @Override
    public BackoffStrategy toBackoffStrategy() {
        return FullJitterBackoffStrategy.builder()
            .maxBackoffTime(ClientConfigurationUtils.getDuration(getMaxBackoffTime()))
            .baseDelay(ClientConfigurationUtils.getDuration(getBaseDelay()))
            .build();
    }
}
