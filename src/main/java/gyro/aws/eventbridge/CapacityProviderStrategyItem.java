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

public class CapacityProviderStrategyItem extends Diffable implements Copyable<software.amazon.awssdk.services.eventbridge.model.CapacityProviderStrategyItem> {

    private String capacityProvider;
    private Integer base;
    private Integer weight;

    @Required
    public String getCapacityProvider() {
        return capacityProvider;
    }

    public void setCapacityProvider(String capacityProvider) {
        this.capacityProvider = capacityProvider;
    }

    @Required
    public Integer getBase() {
        return base;
    }

    public void setBase(Integer base) {
        this.base = base;
    }

    @Required
    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.eventbridge.model.CapacityProviderStrategyItem model) {
        setCapacityProvider(model.capacityProvider());
        setBase(model.base());
        setWeight(model.weight());
    }

    @Override
    public String primaryKey() {
        return getCapacityProvider();
    }

    protected software.amazon.awssdk.services.eventbridge.model.CapacityProviderStrategyItem toCapacityProviderStrategyItem() {
        return software.amazon.awssdk.services.eventbridge.model.CapacityProviderStrategyItem.builder()
            .capacityProvider(getCapacityProvider())
            .base(getBase())
            .weight(getWeight())
            .build();
    }
}
