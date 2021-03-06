/*
 * Copyright 2019, Perfect Sense, Inc.
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Min;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.MetricType;
import software.amazon.awssdk.services.autoscaling.model.PutScalingPolicyResponse;
import software.amazon.awssdk.services.autoscaling.model.ScalingPolicy;
import software.amazon.awssdk.services.autoscaling.model.StepAdjustment;

public class AutoScalingPolicyResource extends AwsResource implements Copyable<ScalingPolicy> {

    private String policyName;
    private String adjustmentType;
    private Integer cooldown;
    private Boolean enabled;
    private Integer estimatedInstanceWarmup;
    private String metricAggregationType;
    private Integer minAdjustmentMagnitude;
    private String policyType;
    private Integer scalingAdjustment;
    private Boolean disableScaleIn;
    private Double targetValue;
    private MetricType predefinedMetricType;
    private String predefinedMetricResourceLabel;
    private String policyArn;
    private Set<AutoScalingPolicyStepAdjustment> stepAdjustment;
    private AutoScalingTargetTrackingConfiguration targetTrackingConfiguration;

    /**
     * The name of the policy.
     */
    @Required
    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    /**
     * The adjustment type.
     */
    @Updatable
    @ValidStrings({"ChangeInCapacity", "ExactCapacity", "PercentChangeInCapacity"})
    public String getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(String adjustmentType) {
        this.adjustmentType = adjustmentType;
    }

    /**
     * The amount of time between two scaling events.
     */
    @Updatable
    @Min(0)
    public Integer getCooldown() {
        return cooldown;
    }

    public void setCooldown(Integer cooldown) {
        this.cooldown = cooldown;
    }

    /**
     * When set to ``true`` the scaling policy is enabled.
     */
    @Updatable
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * The estimated time, in seconds, until a newly launched instance can contribute to the CloudWatch metrics.
     */
    @Updatable
    @Min(0)
    public Integer getEstimatedInstanceWarmup() {
        return estimatedInstanceWarmup;
    }

    public void setEstimatedInstanceWarmup(Integer estimatedInstanceWarmup) {
        this.estimatedInstanceWarmup = estimatedInstanceWarmup;
    }

    /**
     * The aggregation type for cloud watch metrics. Defaults to ``Average``.
     */
    @Updatable
    @ValidStrings({"Minimum", "Maximum", "Average"})
    public String getMetricAggregationType() {
        if (metricAggregationType == null) {
            metricAggregationType = "Average";
        }

        return metricAggregationType;
    }

    public void setMetricAggregationType(String metricAggregationType) {
        this.metricAggregationType = metricAggregationType;
    }

    /**
     * The minimum number of instances to scale.
     */
    @Updatable
    public Integer getMinAdjustmentMagnitude() {
        return minAdjustmentMagnitude;
    }

    public void setMinAdjustmentMagnitude(Integer minAdjustmentMagnitude) {
        this.minAdjustmentMagnitude = minAdjustmentMagnitude;
    }

    /**
     * The type of policy. Defaults to ``SimpleScaling``.
     */
    @ValidStrings({"SimpleScaling", "StepScaling", "TargetTrackingScaling"})
    public String getPolicyType() {
        if (policyType == null) {
            policyType = "SimpleScaling";
        }
        return policyType;
    }

    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }

    /**
     * The amount by which the scaling would happen.
     */
    @Updatable
    public Integer getScalingAdjustment() {
        return scalingAdjustment;
    }

    public void setScalingAdjustment(Integer scalingAdjustment) {
        this.scalingAdjustment = scalingAdjustment;
    }

    /**
     * When set to ``true``, scaling in by the target tracking policy is enabled. Defaulted to ``false``.
     */
    @Updatable
    public Boolean getDisableScaleIn() {
        if (disableScaleIn == null) {
            disableScaleIn = false;
        }

        return disableScaleIn;
    }

    public void setDisableScaleIn(Boolean disableScaleIn) {
        this.disableScaleIn = disableScaleIn;
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

    /**
     * The predefined metric type.
     */
    @Updatable
    public MetricType getPredefinedMetricType() {
        return predefinedMetricType;
    }

    public void setPredefinedMetricType(MetricType predefinedMetricType) {
        this.predefinedMetricType = predefinedMetricType;
    }

    /**
     * The predefined defines metric resource label.
     */
    @Updatable
    @ValidStrings({"ASGAverageCPUUtilization", "ASGAverageNetworkIn", "ASGAverageNetworkOut", "ALBRequestCountPerTarget"})
    public String getPredefinedMetricResourceLabel() {
        return predefinedMetricResourceLabel;
    }

    public void setPredefinedMetricResourceLabel(String predefinedMetricResourceLabel) {
        this.predefinedMetricResourceLabel = predefinedMetricResourceLabel;
    }

    /**
     * The set of adjustments that enable you to scale based on the size of the alarm breach.
     *
     * @subresource gyro.aws.autoscaling.AutoScalingPolicyStepAdjustment
     */
    @Updatable
    public Set<AutoScalingPolicyStepAdjustment> getStepAdjustment() {
        if (stepAdjustment == null) {
            stepAdjustment = new HashSet<>();
        }
        return stepAdjustment;
    }

    public void setStepAdjustment(Set<AutoScalingPolicyStepAdjustment> stepAdjustment) {
        this.stepAdjustment = stepAdjustment;
    }

    /**
     * The target tracking configurations for the scaling policy.
     *
     * @subresource gyro.aws.autoscaling.AutoScalingTargetTrackingConfiguration
     */
    @Updatable
    public AutoScalingTargetTrackingConfiguration getTargetTrackingConfiguration() {
        return targetTrackingConfiguration;
    }

    public void setTargetTrackingConfiguration(AutoScalingTargetTrackingConfiguration targetTrackingConfiguration) {
        this.targetTrackingConfiguration = targetTrackingConfiguration;
    }

    /**
     * The arn of the policy.
     */
    @Output
    public String getPolicyArn() {
        return policyArn;
    }

    public void setPolicyArn(String policyArn) {
        this.policyArn = policyArn;
    }

    @Override
    public void copyFrom(ScalingPolicy scalingPolicy) {
        setAdjustmentType(scalingPolicy.adjustmentType());
        setPolicyName(scalingPolicy.policyName());
        setCooldown(scalingPolicy.cooldown());
        setEstimatedInstanceWarmup(scalingPolicy.estimatedInstanceWarmup());
        setMetricAggregationType(scalingPolicy.metricAggregationType());
        setMinAdjustmentMagnitude(scalingPolicy.minAdjustmentMagnitude());
        setPolicyType(scalingPolicy.policyType());
        setScalingAdjustment(scalingPolicy.scalingAdjustment());
        setPolicyArn(scalingPolicy.policyARN());

        getStepAdjustment().clear();
        if (scalingPolicy.stepAdjustments() != null && !scalingPolicy.stepAdjustments().isEmpty()) {
            for (StepAdjustment stepAdjustment : scalingPolicy.stepAdjustments()) {
                AutoScalingPolicyStepAdjustment policyStepAdjustment = newSubresource(AutoScalingPolicyStepAdjustment.class);
                policyStepAdjustment.copyFrom(stepAdjustment);
                getStepAdjustment().add(policyStepAdjustment);
            }
        }

        if (scalingPolicy.targetTrackingConfiguration() != null) {
            setDisableScaleIn(scalingPolicy.targetTrackingConfiguration().disableScaleIn());
            setTargetValue(scalingPolicy.targetTrackingConfiguration().targetValue());

            if (scalingPolicy.targetTrackingConfiguration().predefinedMetricSpecification() != null) {
                setPredefinedMetricType(scalingPolicy.targetTrackingConfiguration()
                    .predefinedMetricSpecification().predefinedMetricType());
                setPredefinedMetricResourceLabel(scalingPolicy.targetTrackingConfiguration()
                    .predefinedMetricSpecification().resourceLabel());
            }
        }
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, State state) {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        savePolicy(client);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        savePolicy(client);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        client.deletePolicy(
            r -> r.autoScalingGroupName(getParentId())
                .policyName(getPolicyName())
        );
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s", getPolicyName(), getPolicyType());
    }

    private void savePolicy(AutoScalingClient client) {
        PutScalingPolicyResponse response = null;

        if (getPolicyType().equalsIgnoreCase("SimpleScaling")) {
            response = client.putScalingPolicy(
                r -> r.policyName(getPolicyName())
                    .autoScalingGroupName(getParentId())
                    .adjustmentType(getAdjustmentType())
                    .policyType(getPolicyType())
                    .cooldown(getCooldown())
                    .scalingAdjustment(getScalingAdjustment())
                    .minAdjustmentMagnitude(getMinAdjustmentMagnitude())
            );
        } else if (getPolicyType().equalsIgnoreCase("StepScaling")) {
            response = client.putScalingPolicy(
                r -> r.policyName(getPolicyName())
                    .autoScalingGroupName(getParentId())
                    .adjustmentType(getAdjustmentType())
                    .policyType(getPolicyType())
                    .estimatedInstanceWarmup(getEstimatedInstanceWarmup())
                    .metricAggregationType(getMetricAggregationType())
                    .stepAdjustments(stepAdjustment.stream().map(AutoScalingPolicyStepAdjustment::getStepPolicyStep).collect(Collectors.toList()))
            );
        } else if (getPolicyType().equalsIgnoreCase("TargetTrackingScaling")) {
            response = client.putScalingPolicy(
                r -> r.policyName(getPolicyName())
                    .autoScalingGroupName(getParentId())
                    .policyType(getPolicyType())
                    .estimatedInstanceWarmup(getEstimatedInstanceWarmup())
                    .targetTrackingConfiguration(
                        t -> t.targetValue(getTargetValue())
                            .predefinedMetricSpecification(
                                p -> p.predefinedMetricType(getPredefinedMetricType())
                                    .resourceLabel(getPredefinedMetricResourceLabel())
                            )
                            .disableScaleIn(getDisableScaleIn())
                    )
            );
        }

        if (response != null) {
            setPolicyArn(response.policyARN());
        }
    }

    private String getParentId() {
        AutoScalingGroupResource parent = (AutoScalingGroupResource) parentResource();
        if (parent == null) {
            throw new GyroException("Parent Auto Scale Group resource not found.");
        }
        return parent.getName();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        // Attribute validation when not SimpleScaling
        if (!getPolicyType().equals("SimpleScaling")) {
            if (getCooldown() != null) {
                errors.add(new ValidationError(this, null, "The param 'cooldown' is only allowed when"
                    + " 'policy-type' is 'SimpleScaling'."));
            }

            if (getScalingAdjustment() != null) {
                errors.add(new ValidationError(this, null, "The param 'scaling-adjustment' is only allowed when"
                    + " 'policy-type' is 'SimpleScaling'."));
            }
        }

        // Attribute validation when not StepScaling
        if (!getPolicyType().equals("StepScaling")) {
            if (getMetricAggregationType() != null && !getMetricAggregationType().equalsIgnoreCase("average")) {
                errors.add(new ValidationError(this, null, "The param 'metric-aggregation-type' is only allowed when"
                    + " 'policy-type' is 'StepScaling'."));
            }

            if (!getStepAdjustment().isEmpty()) {
                errors.add(new ValidationError(this, null, "The param 'step-adjustment' is only allowed when"
                    + " 'policy-type' is 'StepScaling'."));
            }
        }

        // Attribute validation when not TargetTrackingScaling
        if (!getPolicyType().equals("TargetTrackingScaling")) {
            if (getTargetValue() != null) {
                errors.add(new ValidationError(this, null, "The param 'target-value' is only allowed when"
                    + " 'policy-type' is 'TargetTrackingScaling'."));
            }

            if (getPredefinedMetricType() != null) {
                errors.add(new ValidationError(this, null, "The param 'predefined-metric-type' is only allowed when"
                    + " 'policy-type' is 'TargetTrackingScaling'."));
            }

            if (getPredefinedMetricResourceLabel() != null) {
                errors.add(new ValidationError(
                    this,
                    null,
                    "The param 'predefined-metric-resource-label' is only allowed when"
                        + " 'policy-type' is 'TargetTrackingScaling'."));
            }

            if (getDisableScaleIn()) {
                errors.add(new ValidationError(this, null, "The param 'disable-scale-in' is only allowed when"
                    + " 'policy-type' is 'TargetTrackingScaling'."));
            }

            if (getAdjustmentType() != null && !getAdjustmentType().equals("PercentChangeInCapacity")
                && getMinAdjustmentMagnitude() != null) {
                errors.add(new ValidationError(
                    this,
                    null,
                    "The param 'min-adjustment-magnitude' is only allowed when 'adjustment-type' is 'PercentChangeInCapacity'."));
            }
        }

        // Attribute validation when SimpleScaling
        if (getPolicyType().equals("SimpleScaling")) {
            if (getEstimatedInstanceWarmup() != null) {
                errors.add(new ValidationError(this, null, "The param 'estimated-instance-warmup' is only allowed when"
                    + " 'policy-type' is either 'StepScaling' or 'TargetTrackingScaling'."));
            }
        }

        // Attribute validation when StepScaling
        if (getPolicyType().equals("StepScaling") && getStepAdjustment().isEmpty()) {
            errors.add(new ValidationError(this, null, "the param 'step-adjustment' needs to have at least one step."));
        }

        // Attribute validation when TargetTrackingScaling
        if (getPolicyType().equals("TargetTrackingScaling")) {
            if (getMinAdjustmentMagnitude() != null) {
                errors.add(new ValidationError(this, null, "The param 'min-adjustment-magnitude' is only allowed when"
                    + " 'policy-type' is either 'StepScaling' or 'SimpleScaling'."));
            }

            if (getAdjustmentType() != null) {
                errors.add(new ValidationError(this, null, "The param 'adjustment-type' is only allowed when"
                    + " 'policy-type' is either 'StepScaling' or 'SimpleScaling'."));
            }
        }

        return errors;
    }
}
