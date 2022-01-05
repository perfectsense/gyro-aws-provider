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
import software.amazon.awssdk.services.eventbridge.model.PlacementConstraintType;

public class PlacementConstraint extends Diffable implements Copyable<software.amazon.awssdk.services.eventbridge.model.PlacementConstraint> {

    private PlacementConstraintType type;
    private String expression;

    @Required
    public PlacementConstraintType getType() {
        return type;
    }

    public void setType(PlacementConstraintType type) {
        this.type = type;
    }

    @Required
    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.eventbridge.model.PlacementConstraint model) {
        setType(model.type());
        setExpression(model.expression());
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s", getType(), getExpression());
    }

    protected software.amazon.awssdk.services.eventbridge.model.PlacementConstraint toPlacementConstraint() {
        return software.amazon.awssdk.services.eventbridge.model.PlacementConstraint.builder()
            .type(getType())
            .expression(getExpression())
            .build();
    }
}
