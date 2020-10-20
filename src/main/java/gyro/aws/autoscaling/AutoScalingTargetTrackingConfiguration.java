package gyro.aws.autoscaling;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.autoscalingplans.model.TargetTrackingConfiguration;

public class AutoScalingTargetTrackingConfiguration extends Diffable implements Copyable<TargetTrackingConfiguration> {

    private AutoScalingCustomizedScalingMetricSpecification customizedScalingMetricSpecification;
    private Boolean disableScaleIn;
    private Integer estimatedInstanceWarmup;
    private AutoScalingPredefinedScalingMetricSpecification predefinedScalingMetricSpecification;
    private Integer scaleInCooldown;
    private Integer scaleOutCooldown;
    private Double targetValue;

    public AutoScalingCustomizedScalingMetricSpecification getCustomizedScalingMetricSpecification() {
        return customizedScalingMetricSpecification;
    }

    public void setCustomizedScalingMetricSpecification(AutoScalingCustomizedScalingMetricSpecification customizedScalingMetricSpecification) {
        this.customizedScalingMetricSpecification = customizedScalingMetricSpecification;
    }

    public Boolean getDisableScaleIn() {
        return disableScaleIn;
    }

    public void setDisableScaleIn(Boolean disableScaleIn) {
        this.disableScaleIn = disableScaleIn;
    }

    public Integer getEstimatedInstanceWarmup() {
        return estimatedInstanceWarmup;
    }

    public void setEstimatedInstanceWarmup(Integer estimatedInstanceWarmup) {
        this.estimatedInstanceWarmup = estimatedInstanceWarmup;
    }

    public AutoScalingPredefinedScalingMetricSpecification getPredefinedScalingMetricSpecification() {
        return predefinedScalingMetricSpecification;
    }

    public void setPredefinedScalingMetricSpecification(AutoScalingPredefinedScalingMetricSpecification predefinedScalingMetricSpecification) {
        this.predefinedScalingMetricSpecification = predefinedScalingMetricSpecification;
    }

    public Integer getScaleInCooldown() {
        return scaleInCooldown;
    }

    public void setScaleInCooldown(Integer scaleInCooldown) {
        this.scaleInCooldown = scaleInCooldown;
    }

    public Integer getScaleOutCooldown() {
        return scaleOutCooldown;
    }

    public void setScaleOutCooldown(Integer scaleOutCooldown) {
        this.scaleOutCooldown = scaleOutCooldown;
    }

    public Double getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(Double targetValue) {
        this.targetValue = targetValue;
    }

    @Override
    public void copyFrom(TargetTrackingConfiguration model) {
        setDisableScaleIn(model.disableScaleIn());
        setEstimatedInstanceWarmup(model.estimatedInstanceWarmup());
        setScaleInCooldown(model.scaleInCooldown());
        setScaleOutCooldown(model.scaleOutCooldown());
        setTargetValue(model.targetValue());

        if (model.customizedScalingMetricSpecification() != null) {
            AutoScalingCustomizedScalingMetricSpecification specification = newSubresource(
                AutoScalingCustomizedScalingMetricSpecification.class);
            specification.copyFrom(model.customizedScalingMetricSpecification());
            setCustomizedScalingMetricSpecification(specification);
        } else {
            setCustomizedScalingMetricSpecification(null);
        }

        if (model.predefinedScalingMetricSpecification() != null) {
            AutoScalingPredefinedScalingMetricSpecification specification = newSubresource(
                AutoScalingPredefinedScalingMetricSpecification.class);
            specification.copyFrom(model.predefinedScalingMetricSpecification());
            setPredefinedScalingMetricSpecification(specification);
        } else {
            setCustomizedScalingMetricSpecification(null);
        }
    }

    @Override
    public String primaryKey() {
        return null;
    }

    public TargetTrackingConfiguration toTargetTrackingConfiguration() {
        return TargetTrackingConfiguration.builder()
            .customizedScalingMetricSpecification(getCustomizedScalingMetricSpecification() != null
                ? getCustomizedScalingMetricSpecification().toCustomizedScalingMetricSpecification()
                : null)
            .disableScaleIn(getDisableScaleIn())
            .estimatedInstanceWarmup(getEstimatedInstanceWarmup())
            .predefinedScalingMetricSpecification(getPredefinedScalingMetricSpecification() != null
                ? getPredefinedScalingMetricSpecification().toPredefinedScalingMetricSpecification()
                : null)
            .scaleInCooldown(getScaleInCooldown())
            .scaleOutCooldown(getScaleOutCooldown())
            .targetValue(getTargetValue())
            .build();
    }
}
