/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.ecs;

import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ecs.model.CapacityProviderStrategyItem;

public class EcsCapacityProviderStrategyItem extends Diffable {

    private EcsCapacityProviderResource capacityProvider;
    private Integer base;
    private Integer weight;

    /**
     * The capacity provider to include in the strategy. (Required)
     */
    @Required
    @Updatable
    public EcsCapacityProviderResource getCapacityProvider() {
        return capacityProvider;
    }

    public void setCapacityProvider(EcsCapacityProviderResource capacityProvider) {
        this.capacityProvider = capacityProvider;
    }

    /**
     * Designates how many tasks, at a minimum, to run on the specified ``capacity-provider``.
     * Only one capacity provider in a capacity provider strategy can have a ``base`` defined at a nonzero value.
     * Defaults to ``0``.
     */
    @Updatable
    public Integer getBase() {
        if (base == null) {
            base = 0;
        }

        return base;
    }

    public void setBase(Integer base) {
        this.base = base;
    }

    /**
     * Designates the relative percentage of the total number of tasks launched that should use the specified ``capacity-provider``.
     * At least one capacity provider in a capacity provider strategy must have ``weight`` greater than ``0``.
     * Defaults to ``1``.
     */
    @Updatable
    public Integer getWeight() {
        if (weight == null) {
            weight = 1;
        }

        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Override
    public String primaryKey() {
        return getCapacityProvider().getName();
    }

    public void copyFrom(CapacityProviderStrategyItem model) {
        setCapacityProvider(findById(EcsCapacityProviderResource.class, model.capacityProvider()));
        setBase(model.base());
        setWeight(model.weight());
    }

    public CapacityProviderStrategyItem copyTo() {
        return CapacityProviderStrategyItem.builder()
            .capacityProvider(getCapacityProvider().getName())
            .base(getBase())
            .weight(getWeight())
            .build();
    }
}
