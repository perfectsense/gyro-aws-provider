/*
 * Copyright 2022, Brightspot.
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

package gyro.aws.eventbridge;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;

public class RetryPolicy extends Diffable implements Copyable<software.amazon.awssdk.services.eventbridge.model.RetryPolicy> {

    private Integer maximumEventAgeInSeconds;
    private Integer maximumRetryAttempts;

    @Required
    public Integer getMaximumEventAgeInSeconds() {
        return maximumEventAgeInSeconds;
    }

    public void setMaximumEventAgeInSeconds(Integer maximumEventAgeInSeconds) {
        this.maximumEventAgeInSeconds = maximumEventAgeInSeconds;
    }

    @Required
    public Integer getMaximumRetryAttempts() {
        return maximumRetryAttempts;
    }

    public void setMaximumRetryAttempts(Integer maximumRetryAttempts) {
        this.maximumRetryAttempts = maximumRetryAttempts;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.eventbridge.model.RetryPolicy model) {
        setMaximumRetryAttempts(model.maximumEventAgeInSeconds());
        setMaximumEventAgeInSeconds(model.maximumRetryAttempts());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    protected software.amazon.awssdk.services.eventbridge.model.RetryPolicy toRetryPolicy() {
        return software.amazon.awssdk.services.eventbridge.model.RetryPolicy.builder()
            .maximumEventAgeInSeconds(getMaximumEventAgeInSeconds())
            .maximumRetryAttempts(getMaximumRetryAttempts()).build();
    }
}
