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
import gyro.aws.ecs.EcsCapacityProviderResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

public class CapacityProviderStrategyItem extends Diffable implements Copyable<software.amazon.awssdk.services.eventbridge.model.CapacityProviderStrategyItem> {

    private EcsCapacityProviderResource capacityProvider;
    private Integer base;
    private Integer weight;

    /**
     * The source capacity provider.
     */
    @Required
    public EcsCapacityProviderResource getCapacityProvider() {
        return capacityProvider;
    }

    public void setCapacityProvider(EcsCapacityProviderResource capacityProvider) {
        this.capacityProvider = capacityProvider;
    }

    /**
     * The base value designates how many tasks, at a minimum, to run on the specified capacity provider. Only one capacity provider in a capacity provider strategy can have a base defined. If no value is specified, the default value of 0 is used.
     */
    @Updatable
    public Integer getBase() {
        return base;
    }

    public void setBase(Integer base) {
        this.base = base;
    }

    /**
     * The weight value designates the relative percentage of the total number of tasks launched that should use the specified capacity provider. The weight value is taken into consideration after the base value, if defined, is satisfied.
     */
    @Updatable
    public Integer getWeight() {
        if (weight == null) {
            weight = 0;
        }

        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.eventbridge.model.CapacityProviderStrategyItem model) {
        setCapacityProvider(findById(EcsCapacityProviderResource.class, model.capacityProvider()));
        setBase(model.base());
        setWeight(model.weight());
    }

    @Override
    public String primaryKey() {
        return getCapacityProvider() != null ? getCapacityProvider().getName() : "";
    }

    protected software.amazon.awssdk.services.eventbridge.model.CapacityProviderStrategyItem toCapacityProviderStrategyItem() {
        return software.amazon.awssdk.services.eventbridge.model.CapacityProviderStrategyItem.builder()
            .capacityProvider(getCapacityProvider().getName())
            .base(getBase())
            .weight(getWeight())
            .build();
    }
}
