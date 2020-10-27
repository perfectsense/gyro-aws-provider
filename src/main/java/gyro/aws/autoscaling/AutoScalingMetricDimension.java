package gyro.aws.autoscaling;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.autoscalingplans.model.MetricDimension;

public class AutoScalingMetricDimension extends Diffable implements Copyable<MetricDimension> {

    private String name;
    private String value;

    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Required
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void copyFrom(MetricDimension model) {
        setName(model.name());
        setValue(model.value());
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    public MetricDimension toMetricDimension() {
        return MetricDimension.builder()
            .name(getName())
            .value(getValue())
            .build();
    }
}
