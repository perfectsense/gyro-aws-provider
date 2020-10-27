package gyro.aws.autoscaling;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.autoscalingplans.model.CustomizedLoadMetricSpecification;

public class AutoScalingCustomizedLoadMetricSpecification extends Diffable
    implements Copyable<CustomizedLoadMetricSpecification> {

    List<AutoScalingMetricDimension> dimensions;
    private String metricName;
    private String namespace;
    private String statistic;
    private String unit;

    /**
     * The dimensions of the metric.
     */
    @Updatable
    public List<AutoScalingMetricDimension> getDimensions() {
        if (dimensions == null) {
            dimensions = new ArrayList<>();
        }
        return dimensions;
    }

    public void setDimensions(List<AutoScalingMetricDimension> dimensions) {
        this.dimensions = dimensions;
    }

    /**
     * The name of the metric.
     */
    @Updatable
    @Required
    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    /**
     * The namespace of the metric.
     */
    @Updatable
    @Required
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * The statistic of the metric.
     */
    @Updatable
    @Required
    @ValidStrings("Sum")
    public String getStatistic() {
        return statistic;
    }

    public void setStatistic(String statistic) {
        this.statistic = statistic;
    }

    /**
     * The unit of the metric.
     */
    @Updatable
    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public void copyFrom(CustomizedLoadMetricSpecification model) {
        setMetricName(model.metricName());
        setNamespace(model.namespace());
        setStatistic(model.statisticAsString());
        setUnit(model.unit());

        if (model.dimensions() != null) {
            List<AutoScalingMetricDimension> dimensions = new ArrayList<>();
            model.dimensions().forEach(metricDimension -> {
                AutoScalingMetricDimension autoScalingMetricDimension = newSubresource(AutoScalingMetricDimension.class);
                autoScalingMetricDimension.copyFrom(metricDimension);
                dimensions.add(autoScalingMetricDimension);
            });
            setDimensions(dimensions);
        } else {
            setDimensions(null);
        }
    }

    @Override
    public String primaryKey() {
        return null;
    }

    public CustomizedLoadMetricSpecification toCustomizedLoadMetricSpecification() {
        return CustomizedLoadMetricSpecification.builder()
            .dimensions(getDimensions().stream()
                .map(AutoScalingMetricDimension::toMetricDimension)
                .collect(Collectors.toList()))
            .metricName(getMetricName())
            .namespace(getNamespace())
            .statistic(getStatistic())
            .unit(getUnit())
            .build();
    }
}
