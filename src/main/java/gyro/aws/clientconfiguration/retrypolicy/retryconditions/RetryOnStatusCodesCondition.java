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
