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
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.eventbridge.model.PlacementStrategyType;

public class PlacementStrategy extends Diffable implements Copyable<software.amazon.awssdk.services.eventbridge.model.PlacementStrategy> {

    private String field;
    private PlacementStrategyType type;

    /**
     * The field to apply the placement strategy against.
     */
    @Required
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    /**
     * The type of placement strategy.
     */
    @Required
    @ValidStrings({"random", "spread", "binpack"})
    public PlacementStrategyType getType() {
        return type;
    }

    public void setType(PlacementStrategyType type) {
        this.type = type;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.eventbridge.model.PlacementStrategy model) {
        setField(model.field());
        setType(model.type());
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s", getType(), getField());
    }

    protected software.amazon.awssdk.services.eventbridge.model.PlacementStrategy toPlacementStrategy() {
        return software.amazon.awssdk.services.eventbridge.model.PlacementStrategy.builder()
            .field(getField())
            .type(getType())
            .build();
    }
}
