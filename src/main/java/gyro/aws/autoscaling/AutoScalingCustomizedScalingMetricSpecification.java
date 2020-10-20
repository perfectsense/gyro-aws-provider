package gyro.aws.autoscaling;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.autoscalingplans.model.CustomizedScalingMetricSpecification;
import software.amazon.awssdk.services.autoscalingplans.model.MetricStatistic;

public class AutoScalingCustomizedScalingMetricSpecification extends Diffable
    implements Copyable<CustomizedScalingMetricSpecification> {

    List<AutoScalingMetricDimension> dimensions;
    private String metricName;
    private String namespace;
    private MetricStatistic statistic;
    private String unit;

    public List<AutoScalingMetricDimension> getDimensions() {
        if (dimensions == null) {
            dimensions = new ArrayList<>();
        }
        return dimensions;
    }

    public void setDimensions(List<AutoScalingMetricDimension> dimensions) {
        this.dimensions = dimensions;
    }

    @Required
    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    @Required
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Required
    public MetricStatistic getStatistic() {
        return statistic;
    }

    public void setStatistic(MetricStatistic statistic) {
        this.statistic = statistic;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public void copyFrom(CustomizedScalingMetricSpecification model) {
        setMetricName(model.metricName());
        setNamespace(model.namespace());
        setStatistic(model.statistic());
        setUnit(model.unit());

        if (model.dimensions() != null) {
            List<AutoScalingMetricDimension> dimensions = new ArrayList<>();
            model.dimensions().forEach(metricDimension -> {
                AutoScalingMetricDimension autoScalingMetricDimension = newSubresource(AutoScalingMetricDimension.class);
                autoScalingMetricDimension.copyFrom(metricDimension);
                dimensions.add(autoScalingMetricDimension);
            });
            setDimensions(dimensions);
        }
    }

    @Override
    public String primaryKey() {
        return null;
    }

    public CustomizedScalingMetricSpecification toCustomizedScalingMetricSpecification() {
        return CustomizedScalingMetricSpecification.builder()
            .dimensions(getDimensions().stream()
                .map(AutoScalingMetricDimension::toMetricDimension).collect(Collectors.toList()))
            .metricName(getMetricName())
            .namespace(getNamespace())
            .statistic(getStatistic())
            .unit(getUnit())
            .build();
    }
}
