/*
 * Copyright 2021, Brightspot, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.clientconfiguration.retrypolicy.retryconditions;

import java.util.Objects;
import java.util.stream.Stream;

import gyro.aws.clientconfiguration.ClientConfigurationException;
import gyro.aws.clientconfiguration.ClientConfigurationInterface;

public abstract class AbstractRetryCondition implements RetryConditionInterface, ClientConfigurationInterface {

    private AndRetryCondition andRetryCondition;
    private OrRetryCondition orRetryCondition;
    private MaxNumberOfRetryCondition maxNumberOfRetryCondition;
    private RetryOnErrorCodesCondition retryOnErrorCodesCondition;
    private RetryOnStatusCodesCondition retryOnStatusCodesCondition;
    private RetryOnThrottlingCondition retryOnThrottlingCondition;
    private RetryOnClockSkewCondition retryOnClockSkewCondition;
    private TokenBucketRetryCondition tokenBucketRetryCondition;

    abstract String getRetryConditionType();

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

    public RetryOnClockSkewCondition getRetryOnClockSkewCondition() {
        return retryOnClockSkewCondition;
    }

    public void setRetryOnClockSkewCondition(RetryOnClockSkewCondition retryOnClockSkewCondition) {
        this.retryOnClockSkewCondition = retryOnClockSkewCondition;
    }

    public TokenBucketRetryCondition getTokenBucketRetryCondition() {
        return tokenBucketRetryCondition;
    }

    public void setTokenBucketRetryCondition(TokenBucketRetryCondition tokenBucketRetryCondition) {
        this.tokenBucketRetryCondition = tokenBucketRetryCondition;
    }

    @Override
    public void validate() {
        long count = Stream.of(
            getAndRetryCondition(),
            getOrRetryCondition(),
            getMaxNumberOfRetryCondition(),
            getRetryOnErrorCodesCondition(),
            getRetryOnStatusCodesCondition(),
            getRetryOnThrottlingCondition(),
            getRetryOnClockSkewCondition(),
            getTokenBucketRetryCondition())
            .filter(Objects::nonNull).count();

        if (count > 1) {
            throw new ClientConfigurationException(
                null,
                String.format("Only one of"
                    + " 'and-retry-condition',"
                    + " 'or-retry-condition',"
                    + " 'max-number-of-retry-condition',"
                    + " 'retry-on-error-codes-condition',"
                    + " 'retry-on-status-codes-condition'"
                    + " 'retry-on-throttling-condition'"
                    + " 'retry-on-clock-screw-condition'"
                    + " or 'token-bucket-retry-condition'"
                    + " is allowed for '%s'.", getRetryConditionType()));
        } else if (count == 0) {
            throw new ClientConfigurationException(
                null,
                String.format("One of"
                    + " 'and-retry-condition',"
                    + " 'or-retry-condition',"
                    + " 'max-number-of-retry-condition',"
                    + " 'retry-on-error-codes-condition',"
                    + " 'retry-on-status-codes-condition'"
                    + " 'retry-on-throttling-condition'"
                    + " 'retry-on-clock-screw-condition'"
                    + " or 'token-bucket-retry-condition'"
                    + " is required for '%s'.", getRetryConditionType()));
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
            getRetryOnThrottlingCondition(),
            getRetryOnClockSkewCondition(),
            getTokenBucketRetryCondition()).filter(Objects::nonNull).findFirst().get().toRetryCondition();
    }
}
