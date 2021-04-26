package gyro.aws.clientconfiguration.retrypolicy.retryconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.clientconfiguration.ClientConfigurationInterface;
import gyro.core.GyroException;
import software.amazon.awssdk.core.retry.conditions.RetryCondition;

public class AndRetryCondition implements RetryConditionInterface, ClientConfigurationInterface {

    private List<RetryConditionInterface> retryCondition;

    public List<RetryConditionInterface> getRetryCondition() {
        if (retryCondition == null) {
            retryCondition = new ArrayList<>();
        }

        return retryCondition;
    }

    public void setAndRetryCondition(List<AndRetryCondition> andRetryConditions) {
        getRetryCondition().addAll(andRetryConditions);
    }

    public void setOrRetryCondition(List<OrRetryCondition> orRetryConditions) {
        getRetryCondition().addAll(orRetryConditions);
    }

    public void setMaxNumberOfRetryCondition(List<MaxNumberOfRetryCondition> maxNumberOfRetryConditions) {
        getRetryCondition().addAll(maxNumberOfRetryConditions);
    }

    public void setRetryOnErrorCodesCondition(List<RetryOnErrorCodesCondition> retryOnErrorCodesConditions) {
        getRetryCondition().addAll(retryOnErrorCodesConditions);
    }

    public void setRetryOnStatusCodesCondition(List<RetryOnStatusCodesCondition> retryOnStatusCodesConditions) {
        getRetryCondition().addAll(retryOnStatusCodesConditions);
    }

    public void setRetryOnThrottlingCondition(List<RetryOnThrottlingCondition> retryOnThrottlingConditions) {
        getRetryCondition().addAll(retryOnThrottlingConditions);
    }

    @Override
    public void validate() {
        if (getRetryCondition().isEmpty()) {
            throw new GyroException("'and-retry-condition' cannot be empty.");
        }
    }

    @Override
    public RetryCondition toRetryCondition() {
        return software.amazon.awssdk.core.retry.conditions.AndRetryCondition.create(getRetryCondition().stream()
            .map(o -> toRetryCondition())
            .collect(
                Collectors.toList())
            .toArray(new RetryCondition[getRetryCondition().size()]));
    }
}
