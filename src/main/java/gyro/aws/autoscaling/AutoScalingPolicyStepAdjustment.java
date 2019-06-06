package gyro.aws.autoscaling;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.autoscaling.model.StepAdjustment;

public class AutoScalingPolicyStepAdjustment extends Diffable implements Copyable<StepAdjustment> {
    private Integer scalingAdjustment;
    private Double metricIntervalLowerBound;
    private Double metricIntervalUpperBound;

    public Integer getScalingAdjustment() {
        return scalingAdjustment;
    }

    public void setScalingAdjustment(Integer scalingAdjustment) {
        this.scalingAdjustment = scalingAdjustment;
    }

    public Double getMetricIntervalLowerBound() {
        return metricIntervalLowerBound;
    }

    public void setMetricIntervalLowerBound(Double metricIntervalLowerBound) {
        this.metricIntervalLowerBound = metricIntervalLowerBound;
    }

    public Double getMetricIntervalUpperBound() {
        return metricIntervalUpperBound;
    }

    public void setMetricIntervalUpperBound(Double metricIntervalUpperBound) {
        this.metricIntervalUpperBound = metricIntervalUpperBound;
    }

    @Override
    public void copyFrom(StepAdjustment stepAdjustment) {
        setScalingAdjustment(stepAdjustment.scalingAdjustment());
        setMetricIntervalLowerBound(stepAdjustment.metricIntervalLowerBound());
        setMetricIntervalUpperBound(stepAdjustment.metricIntervalUpperBound());
    }

    @Override
    public String toDisplayString() {
        return "step adjustment policy";
    }

    public StepAdjustment getStepPolicyStep() {
        return StepAdjustment.builder()
            .scalingAdjustment(getScalingAdjustment())
            .metricIntervalLowerBound(getMetricIntervalLowerBound())
            .metricIntervalUpperBound(getMetricIntervalUpperBound())
            .build();
    }
}
