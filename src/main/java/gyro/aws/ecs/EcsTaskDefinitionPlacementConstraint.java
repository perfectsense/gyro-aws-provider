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

import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ecs.model.TaskDefinitionPlacementConstraint;
import software.amazon.awssdk.services.ecs.model.TaskDefinitionPlacementConstraintType;

public class EcsTaskDefinitionPlacementConstraint extends Diffable {

    private TaskDefinitionPlacementConstraintType type;
    private String expression;

    /**
     * The type of constraint. (Required)
     * The only valid value is ``memberOf``. This constraint restricts selection to be from a group of valid candidates.
     */
    @Required
    public TaskDefinitionPlacementConstraintType getType() {
        return type;
    }

    public void setType(TaskDefinitionPlacementConstraintType type) {
        this.type = type;
    }

    /**
     * A cluster query language expression to apply to the constraint. (Required)
     */
    @Required
    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public void copyFrom(TaskDefinitionPlacementConstraint model) {
        setType(model.type());
        setExpression(model.expression());
    }

    public TaskDefinitionPlacementConstraint copyTo() {
        return TaskDefinitionPlacementConstraint.builder()
            .type(getType())
            .expression(getExpression())
            .build();
    }
}
