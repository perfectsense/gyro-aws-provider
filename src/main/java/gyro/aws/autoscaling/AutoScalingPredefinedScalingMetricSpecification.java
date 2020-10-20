package gyro.aws.autoscaling;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.autoscalingplans.model.PredefinedScalingMetricSpecification;
import software.amazon.awssdk.services.autoscalingplans.model.ScalingMetricType;

public class AutoScalingPredefinedScalingMetricSpecification extends Diffable
    implements Copyable<PredefinedScalingMetricSpecification> {

    private ScalingMetricType predefinedScalingMetricType;
    private String resourceLabel;

    @Required
    public ScalingMetricType getPredefinedScalingMetricType() {
        return predefinedScalingMetricType;
    }

    public void setPredefinedScalingMetricType(ScalingMetricType predefinedScalingMetricType) {
        this.predefinedScalingMetricType = predefinedScalingMetricType;
    }

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
