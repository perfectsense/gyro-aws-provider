package gyro.aws.autoscaling;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.autoscalingplans.model.PredefinedScalingMetricSpecification;
import software.amazon.awssdk.services.autoscalingplans.model.ScalingMetricType;

public class AutoScalingPredefinedScalingMetricSpecification extends Diffable
    implements Copyable<PredefinedScalingMetricSpecification> {

    private ScalingMetricType predefinedScalingMetricType;
    private String resourceLabel;

    /**
     * The metric type.
     */
    @Updatable
    @Required
    public ScalingMetricType getPredefinedScalingMetricType() {
        return predefinedScalingMetricType;
    }

    public void setPredefinedScalingMetricType(ScalingMetricType predefinedScalingMetricType) {
        this.predefinedScalingMetricType = predefinedScalingMetricType;
    }

    /**
     * The resource associated with the metric type.
     */
    @Updatable
    public String getResourceLabel() {
        return resourceLabel;
    }

    public void setResourceLabel(String resourceLabel) {
        this.resourceLabel = resourceLabel;
    }

    @Override
    public void copyFrom(PredefinedScalingMetricSpecification model) {
        setPredefinedScalingMetricType(model.predefinedScalingMetricType());
        setResourceLabel(model.resourceLabel());
    }

    @Override
    public String primaryKey() {
        return null;
    }

    public PredefinedScalingMetricSpecification toPredefinedScalingMetricSpecification() {
        return PredefinedScalingMetricSpecification.builder()
            .predefinedScalingMetricType(getPredefinedScalingMetricType())
            .resourceLabel(getResourceLabel())
            .build();
    }
}
