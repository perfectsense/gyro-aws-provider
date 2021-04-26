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

package gyro.aws.clientconfiguration.retrypolicy.backoffstrategies;

import java.util.Objects;
import java.util.stream.Stream;

import gyro.aws.clientconfiguration.ClientConfigurationInterface;
import gyro.core.GyroException;

public class ThrottlingBackoffStrategy implements BackoffStrategyInterface, ClientConfigurationInterface {

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
