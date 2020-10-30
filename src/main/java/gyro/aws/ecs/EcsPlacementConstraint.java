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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ecs.model.PlacementConstraint;
import software.amazon.awssdk.services.ecs.model.PlacementConstraintType;

public class EcsPlacementConstraint extends Diffable implements Copyable<PlacementConstraint> {

    private String expression;
    private PlacementConstraintType type;

    /**
     * A cluster query language expression to apply to the constraint.
     */
    @Required
    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
     * The type of constraint.
     */
    @Required
    public PlacementConstraintType getType() {
        return type;
    }

    public void setType(PlacementConstraintType type) {
        this.type = type;
    }

    @Override
    public String primaryKey() {
        return String.format("Expression: %s, Type: %s", getExpression(), getType());
    }

    @Override
    public void copyFrom(PlacementConstraint model) {
        setExpression(model.expression());
        setType(model.type());
    }

    public PlacementConstraint toPlacementConstraint() {
        return PlacementConstraint.builder().expression(getExpression()).type(getType()).build();
    }
}
