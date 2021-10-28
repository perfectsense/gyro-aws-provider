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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.clientconfiguration.ClientConfigurationException;
import gyro.aws.clientconfiguration.ClientConfigurationInterface;
import software.amazon.awssdk.core.retry.conditions.RetryCondition;

public class OrRetryCondition implements RetryConditionInterface, ClientConfigurationInterface {

    private List<RetryConditionInterface> retryCondition;

    public List<RetryConditionInterface> getRetryCondition() {
        if (retryCondition == null) {
            retryCondition = new ArrayList<>();
        }

        return retryCondition;
    }

    // Pseudo setters.
    // Configs point to one or more of the below variables based on the setter names
    //
    // Ex:
    //  ..
    //  or-retry-condition
    //      retry-on-throttling-condition # <-- triggers setRetryOnThrottlingCondition()
    //      end
    //  end
    //

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
            throw new ClientConfigurationException("or-retry-condition", "Cannot be empty.");
        }
    }

    @Override
    public RetryCondition toRetryCondition() {
        return software.amazon.awssdk.core.retry.conditions.OrRetryCondition.create(getRetryCondition().stream()
            .map(o -> toRetryCondition())
            .collect(
                Collectors.toList())
            .toArray(new RetryCondition[getRetryCondition().size()]));
    }
}
