/*
 * Copyright 2020, Perfect Sense, Inc.
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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ecs.model.PlacementStrategy;
import software.amazon.awssdk.services.ecs.model.PlacementStrategyType;

public class EcsPlacementStrategy extends Diffable implements Copyable<PlacementStrategy> {

    private String field;
    private PlacementStrategyType type;

    /**
     * The field to apply the placement strategy against.
     */
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    /**
     * The type of placement strategy. (Required)
     */
    @Required
    public PlacementStrategyType getType() {
        return type;
    }

    public void setType(PlacementStrategyType type) {
        this.type = type;
    }

    @Override
    public String primaryKey() {
        StringBuilder sb = new StringBuilder("Ecs Placement Strategy - ");

        if (getField() != null) {
            sb.append("Field: ").append(getField()).append(" ");
        }

        sb.append("Type: ").append(getType());

        return sb.toString();
    }

    @Override
    public void copyFrom(PlacementStrategy model) {
        setField(model.field());
        setType(model.type());
    }

    public PlacementStrategy toPlacementStrategy() {
        return PlacementStrategy.builder().field(getField()).type(getType()).build();
    }
}
