/*
 * Copyright 2020, Brightspot.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.autoscaling;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.autoscalingplans.model.TargetTrackingConfiguration;

public class AutoScalingTargetTrackingConfiguration extends Diffable implements Copyable<TargetTrackingConfiguration> {

    private AutoScalingCustomizedScalingMetricSpecification customizedScalingMetricSpecification;
    private Boolean disableScaleIn;
    private Integer estimatedInstanceWarmup;
    private AutoScalingPredefinedScalingMetricSpecification predefinedScalingMetricSpecification;
    private Integer scaleInCooldown;
    private Integer scaleOutCooldown;
    private Double targetValue;

    /**
     * The customized metric.
     *
     * @subresource gyro.aws.autoscaling.AutoScalingCustomizedScalingMetricSpecification
     */
    @Updatable
    public AutoScalingCustomizedScalingMetricSpecification getCustomizedScalingMetricSpecification() {
        return customizedScalingMetricSpecification;
    }

    public void setCustomizedScalingMetricSpecification(AutoScalingCustomizedScalingMetricSpecification customizedScalingMetricSpecification) {
        this.customizedScalingMetricSpecification = customizedScalingMetricSpecification;
    }

    /**
     * When set to ``true``, the scale in by the target tracking scaling policy is disabled.
     */
    @Updatable
    public Boolean getDisableScaleIn() {
        return disableScaleIn;
    }

    public void setDisableScaleIn(Boolean disableScaleIn) {
        this.disableScaleIn = disableScaleIn;
    }

    /**
     * The estimated time (in seconds) until a new instance can contribute to the CloudWatch metrics.
     */
    @Updatable
    public Integer getEstimatedInstanceWarmup() {
        return estimatedInstanceWarmup;
    }

    public void setEstimatedInstanceWarmup(Integer estimatedInstanceWarmup) {
        this.estimatedInstanceWarmup = estimatedInstanceWarmup;
    }

    /**
     * The predefined metric sepcification.
     *
     * @subresource gyro.aws.autoscaling.AutoScalingPredefinedScalingMetricSpecification
     */
    @Updatable
    public AutoScalingPredefinedScalingMetricSpecification getPredefinedScalingMetricSpecification() {
        return predefinedScalingMetricSpecification;
    }

    public void setPredefinedScalingMetricSpecification(AutoScalingPredefinedScalingMetricSpecification predefinedScalingMetricSpecification) {
        this.predefinedScalingMetricSpecification = predefinedScalingMetricSpecification;
    }

    /**
     * The amount of time, in seconds, after a scale in activity completes before another can start.
     */
    @Updatable
    public Integer getScaleInCooldown() {
        return scaleInCooldown;
    }

    public void setScaleInCooldown(Integer scaleInCooldown) {
        this.scaleInCooldown = scaleInCooldown;
    }

    /**
     * The amount of time in seconds, after a scale out activity completes before another can start.
     **/
    @Updatable
    public Integer getScaleOutCooldown() {
        return scaleOutCooldown;
    }

    public void setScaleOutCooldown(Integer scaleOutCooldown) {
        this.scaleOutCooldown = scaleOutCooldown;
    }

    /**
     * The target value for the metric.
     */
    @Updatable
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

        setCustomizedScalingMetricSpecification(null);
        if (model.customizedScalingMetricSpecification() != null) {
            AutoScalingCustomizedScalingMetricSpecification specification = newSubresource(
                AutoScalingCustomizedScalingMetricSpecification.class);
            specification.copyFrom(model.customizedScalingMetricSpecification());
            setCustomizedScalingMetricSpecification(specification);
        }

        setCustomizedScalingMetricSpecification(null);
        if (model.predefinedScalingMetricSpecification() != null) {
            AutoScalingPredefinedScalingMetricSpecification specification = newSubresource(
                AutoScalingPredefinedScalingMetricSpecification.class);
            specification.copyFrom(model.predefinedScalingMetricSpecification());
            setPredefinedScalingMetricSpecification(specification);
        }
    }

    @Override
    public String primaryKey() {
        return String.format("%s", getCustomizedScalingMetricSpecification().getName());
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
