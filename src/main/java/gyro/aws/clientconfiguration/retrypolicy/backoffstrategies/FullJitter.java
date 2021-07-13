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
        ClientConfigurationUtils.validate(getBaseDelay(), "base-delay");
        ClientConfigurationUtils.validate(getMaxBackoffTime(), "max-backoff-time");
    }

    @Override
    public BackoffStrategy toBackoffStrategy() {
        return FullJitterBackoffStrategy.builder()
            .maxBackoffTime(ClientConfigurationUtils.getDuration(getMaxBackoffTime()))
            .baseDelay(ClientConfigurationUtils.getDuration(getBaseDelay()))
            .build();
    }
}
