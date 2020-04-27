package gyro.aws.ecs;

import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ecs.model.CapacityProviderStrategyItem;

public class EcsCapacityProviderStrategyItem extends Diffable {

    private EcsCapacityProviderResource capacityProvider;
    private Integer base;
    private Integer weight;

    @Required
    @Updatable
    public EcsCapacityProviderResource getCapacityProvider() {
        return capacityProvider;
    }

    public void setCapacityProvider(EcsCapacityProviderResource capacityProvider) {
        this.capacityProvider = capacityProvider;
    }

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
