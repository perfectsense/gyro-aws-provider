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
     * A cluster query language expression to apply to the constraint. (Required)
     */
    @Required
    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
     * The type of constraint. (Required)
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
