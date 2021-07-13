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

import java.util.HashSet;
import java.util.List;

import gyro.aws.clientconfiguration.ClientConfigurationException;
import gyro.aws.clientconfiguration.ClientConfigurationInterface;
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
            throw new ClientConfigurationException("retryable-error-codes", "Cannot be empty.");
        }
    }

    @Override
    public RetryCondition toRetryCondition() {
        return RetryOnErrorCodeCondition.create(new HashSet<>(getRetryableErrorCodes()));
    }
}
