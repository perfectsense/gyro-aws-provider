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
import software.amazon.awssdk.core.retry.conditions.RetryCondition;
import software.amazon.awssdk.core.retry.conditions.TokenBucketExceptionCostFunction;

public class TokenBucketRetryCondition implements RetryConditionInterface, ClientConfigurationInterface {

    private Integer bucketSize;
    private Integer exceptionCost;
    private Integer throttlingExceptionCost;

    public Integer getBucketSize() {
        return bucketSize;
    }

    public void setBucketSize(Integer bucketSize) {
        this.bucketSize = bucketSize;
    }

    public Integer getExceptionCost() {
        return exceptionCost;
    }

    public void setExceptionCost(Integer exceptionCost) {
        this.exceptionCost = exceptionCost;
    }

    public Integer getThrottlingExceptionCost() {
        return throttlingExceptionCost;
    }

    public void setThrottlingExceptionCost(Integer throttlingExceptionCost) {
        this.throttlingExceptionCost = throttlingExceptionCost;
    }

    @Override
    public void validate() {
        if (getBucketSize() == null || getBucketSize() < 1) {
            throw new ClientConfigurationException("bucket-size", "Is required and cannot be less that 1.");
        }

        long count = Stream.of(getExceptionCost(), getThrottlingExceptionCost()).filter(Objects::nonNull).count();
        if (count != 1) {
            throw new ClientConfigurationException(
                null,
                "Either both of 'exception-cost' or 'throttling-exception-cost' is allowed or none.");
        } else if (getExceptionCost() != null && getExceptionCost() < 1) {
            throw new ClientConfigurationException("exception-cost", "Cannot be less that 1.");
        } else if (getThrottlingExceptionCost() != null && getExceptionCost() < 1) {
            throw new ClientConfigurationException("throttling-exception-cost", "Cannot be less that 1.");
        }
    }

    @Override
    public RetryCondition toRetryCondition() {
        software.amazon.awssdk.core.retry.conditions.TokenBucketRetryCondition.Builder builder = software.amazon.awssdk.core.retry.conditions.TokenBucketRetryCondition
            .builder();

        builder.tokenBucketSize(getBucketSize());
        if (getExceptionCost() != null) {
            builder.exceptionCostFunction(TokenBucketExceptionCostFunction.builder()
                .defaultExceptionCost(getExceptionCost())
                .throttlingExceptionCost(getThrottlingExceptionCost())
                .build());

        } else {
            builder.exceptionCostFunction(TokenBucketExceptionCostFunction.builder().build());
        }

        return builder.build();
    }
}
