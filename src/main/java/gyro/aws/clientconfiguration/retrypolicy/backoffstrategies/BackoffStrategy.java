package gyro.aws.clientconfiguration.retrypolicy.backoffstrategies;

import java.util.Objects;
import java.util.stream.Stream;

import gyro.aws.clientconfiguration.ClientConfigurationInterface;
import gyro.core.GyroException;

public class BackoffStrategy implements BackoffStrategyInterface, ClientConfigurationInterface {

    private FixedDelay fixedDelay;
    private FullJitter fullJitter;
    private EqualJitter equalJitter;

    public FixedDelay getFixedDelay() {
        return fixedDelay;
    }

    public void setFixedDelay(FixedDelay fixedDelay) {
        this.fixedDelay = fixedDelay;
    }

    public FullJitter getFullJitter() {
        return fullJitter;
    }

    public void setFullJitter(FullJitter fullJitter) {
        this.fullJitter = fullJitter;
    }

    public EqualJitter getEqualJitter() {
        return equalJitter;
    }

    public void setEqualJitter(EqualJitter equalJitter) {
        this.equalJitter = equalJitter;
    }

    @Override
    public void validate() {
        long count = Stream.of(getEqualJitter(), getFullJitter(), getFixedDelay()).filter(Objects::nonNull).count();

        if (count > 1) {
            throw new GyroException("Only one of 'fixed-delay', 'full-jitter' or 'equal-jitter' is allowed.");
        } else if (count == 0) {
            throw new GyroException("One of 'fixed-delay', 'full-jitter' or 'equal-jitter' is required.");
        }
    }

    @Override
    public software.amazon.awssdk.core.retry.backoff.BackoffStrategy toBackoffStrategy() {
        return Stream.of(getEqualJitter(), getFullJitter(), getFixedDelay())
            .filter(Objects::nonNull)
            .findFirst()
            .get()
            .toBackoffStrategy();
    }
}
