package gyro.aws.autoscaling;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.autoscalingplans.model.LoadMetricType;
import software.amazon.awssdk.services.autoscalingplans.model.PredefinedLoadMetricSpecification;

public class AutoScalingPredefinedLoadMetricSpecification extends Diffable
    implements Copyable<PredefinedLoadMetricSpecification> {

    private LoadMetricType predefinedLoadMetricType;
    private String resourceLabel;

    /**
     * The metric type.
     */
    @Updatable
    @Required
    public LoadMetricType getPredefinedLoadMetricType() {
        return predefinedLoadMetricType;
    }

    public void setPredefinedLoadMetricType(LoadMetricType predefinedLoadMetricType) {
        this.predefinedLoadMetricType = predefinedLoadMetricType;
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
    public void copyFrom(PredefinedLoadMetricSpecification model) {
        setPredefinedLoadMetricType(model.predefinedLoadMetricType());
        setResourceLabel(model.resourceLabel());
    }

    @Override
    public String primaryKey() {
        return null;
    }

    public PredefinedLoadMetricSpecification toPredefinedLoadMetricSpecification() {
        return PredefinedLoadMetricSpecification.builder()
            .predefinedLoadMetricType(getPredefinedLoadMetricType())
            .resourceLabel(getResourceLabel())
            .build();
    }
}
