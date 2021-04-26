package gyro.aws.clientconfiguration.retrypolicy.backoffstrategies;

import software.amazon.awssdk.core.retry.backoff.BackoffStrategy;

public interface BackoffStrategyInterface {

    BackoffStrategy toBackoffStrategy();
}
