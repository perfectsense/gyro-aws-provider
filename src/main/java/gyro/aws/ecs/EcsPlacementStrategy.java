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
