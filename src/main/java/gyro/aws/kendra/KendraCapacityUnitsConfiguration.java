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

package gyro.aws.kendra;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.kendra.model.CapacityUnitsConfiguration;

public class KendraCapacityUnitsConfiguration extends Diffable implements Copyable<CapacityUnitsConfiguration> {

    private Integer queryCapacityUnits;
    private Integer storageCapacityUnits;

    /**
     * The amount of extra query capacity for an index.
     */
    @Required
    public Integer getQueryCapacityUnits() {
        return queryCapacityUnits;
    }

    public void setQueryCapacityUnits(Integer queryCapacityUnits) {
        this.queryCapacityUnits = queryCapacityUnits;
    }

    /**
     * The amount of extra storage capacity for an index.
     */
    public Integer getStorageCapacityUnits() {
        return storageCapacityUnits;
    }

    public void setStorageCapacityUnits(Integer storageCapacityUnits) {
        this.storageCapacityUnits = storageCapacityUnits;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(CapacityUnitsConfiguration model) {
        setQueryCapacityUnits(model.queryCapacityUnits());
        setStorageCapacityUnits(model.storageCapacityUnits());
    }

    public CapacityUnitsConfiguration toCapacityUnitsConfiguration() {
        return CapacityUnitsConfiguration.builder()
            .queryCapacityUnits(getQueryCapacityUnits())
            .storageCapacityUnits(getStorageCapacityUnits())
            .build();
    }
}
