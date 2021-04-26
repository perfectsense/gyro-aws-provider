/*
 * Copyright 2020, Brightspot, Inc.
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

import gyro.aws.clientconfiguration.ClientConfigurationInterface;
import gyro.core.GyroException;
import software.amazon.awssdk.core.retry.conditions.MaxNumberOfRetriesCondition;
import software.amazon.awssdk.core.retry.conditions.RetryCondition;

public class MaxNumberOfRetryCondition implements RetryConditionInterface, ClientConfigurationInterface {

    private Integer retryCount;

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    @Override
    public void validate() {
        if (getRetryCount() == null || getRetryCount() < 0) {
            throw new GyroException("retry-count cannot be empty or less than 1.");
        }
    }

    @Override
    public RetryCondition toRetryCondition() {
        return MaxNumberOfRetriesCondition.create(getRetryCount());
    }
}
