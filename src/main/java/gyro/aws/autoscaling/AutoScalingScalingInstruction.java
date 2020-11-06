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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.CollectionMax;
import gyro.core.validation.Range;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.autoscalingplans.model.PredictiveScalingMaxCapacityBehavior;
import software.amazon.awssdk.services.autoscalingplans.model.PredictiveScalingMode;
import software.amazon.awssdk.services.autoscalingplans.model.ScalableDimension;
import software.amazon.awssdk.services.autoscalingplans.model.ScalingInstruction;
import software.amazon.awssdk.services.autoscalingplans.model.ScalingPolicyUpdateBehavior;
import software.amazon.awssdk.services.autoscalingplans.model.ServiceNamespace;

public class AutoScalingScalingInstruction extends Diffable implements Copyable<ScalingInstruction> {

    private AutoScalingCustomizedLoadMetricSpecification customizedLoadMetricSpecification;
    private Boolean disableDynamicScaling;
    private Integer maxCapacity;
    private Integer minCapacity;
    private AutoScalingPredefinedLoadMetricSpecification predefinedLoadMetricSpecification;
    private PredictiveScalingMaxCapacityBehavior predictiveScalingMaxCapacityBehavior;
    private Integer predictiveScalingMaxCapacityBuffer;
    private PredictiveScalingMode predictiveScalingMode;
    private String resourceId;
    private ScalableDimension scalableDimension;
    private ScalingPolicyUpdateBehavior scalingPolicyUpdateBehavior;
    private Integer scheduledActionBufferTime;
    private ServiceNamespace serviceNamespace;
    private List<AutoScalingTargetTrackingConfiguration> targetTrackingConfigurations;

    /**
     * The customized load metric for predictive scaling.
     *
     * @subresource gyro.aws.autoscaling.AutoScalingCustomizedLoadMetricSpecification
     */
    @Updatable
    public AutoScalingCustomizedLoadMetricSpecification getCustomizedLoadMetricSpecification() {
        return customizedLoadMetricSpecification;
    }

    public void setCustomizedLoadMetricSpecification(AutoScalingCustomizedLoadMetricSpecification customizedLoadMetricSpecification) {
        this.customizedLoadMetricSpecification = customizedLoadMetricSpecification;
    }

    /**
     * When set to ``true`` the dynamic scaling by AWS Auto Scaling is disabled.
     */
    @Updatable
    public Boolean getDisableDynamicScaling() {
        return disableDynamicScaling;
    }

    public void setDisableDynamicScaling(Boolean disableDynamicScaling) {
        this.disableDynamicScaling = disableDynamicScaling;
    }

    /**
     * The maximum capacity of the resource.
     */
    @Updatable
    @Required
    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    /**
     * The minimum capacity of the resource.
     */
    @Updatable
    @Required
    public Integer getMinCapacity() {
        return minCapacity;
    }

    public void setMinCapacity(Integer minCapacity) {
        this.minCapacity = minCapacity;
    }

    /**
     * The predefined load metric to use for predictive scaling.
     *
     * @subresource gyro.aws.autoscaling.AutoScalingPredefinedLoadMetricSpecification
     */
    @Updatable
    public AutoScalingPredefinedLoadMetricSpecification getPredefinedLoadMetricSpecification() {
        return predefinedLoadMetricSpecification;
    }

    public void setPredefinedLoadMetricSpecification(AutoScalingPredefinedLoadMetricSpecification predefinedLoadMetricSpecification) {
        this.predefinedLoadMetricSpecification = predefinedLoadMetricSpecification;
    }

    /**
     * The forecast max capacity behavior that should be applied to the resource.
     */
    @Updatable
    @ValidStrings({
        "SetForecastCapacityToMaxCapacity",
        "SetMaxCapacityToForecastCapacity",
        "SetMaxCapacityAboveForecastCapacity" })
    public PredictiveScalingMaxCapacityBehavior getPredictiveScalingMaxCapacityBehavior() {
        return predictiveScalingMaxCapacityBehavior;
    }

    public void setPredictiveScalingMaxCapacityBehavior(PredictiveScalingMaxCapacityBehavior predictiveScalingMaxCapacityBehavior) {
        this.predictiveScalingMaxCapacityBehavior = predictiveScalingMaxCapacityBehavior;
    }

    /**
     * The size of the capacity buffer.
     */
    @Updatable
    @Range(min = 1, max = 100)
    public Integer getPredictiveScalingMaxCapacityBuffer() {
        return predictiveScalingMaxCapacityBuffer;
    }

    public void setPredictiveScalingMaxCapacityBuffer(Integer predictiveScalingMaxCapacityBuffer) {
        this.predictiveScalingMaxCapacityBuffer = predictiveScalingMaxCapacityBuffer;
    }

    /**
     * The predictive scaling mode.
     */
    @Updatable
    @ValidStrings({ "ForecastAndScale", "ForecastOnly" })
    public PredictiveScalingMode getPredictiveScalingMode() {
        return predictiveScalingMode;
    }

    public void setPredictiveScalingMode(PredictiveScalingMode predictiveScalingMode) {
        this.predictiveScalingMode = predictiveScalingMode;
    }

    /**
     * The ID of the resource that also specifies the resource type and unique identifier.
     */
    @Required
    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    /**
     * The scalable dimension associated with the resource.
     */
    @Updatable
    @ValidStrings({
        "autoscaling:autoScalingGroup:DesiredCapacity",
        "ecs:service:DesiredCount",
        "ec2:spot-fleet-request:TargetCapacity",
        "rds:cluster:ReadReplicaCount",
        "dynamodb:table:ReadCapacityUnits",
        "dynamodb:table:WriteCapacityUnits",
        "dynamodb:index:ReadCapacityUnits",
        "dynamodb:index:WriteCapacityUnits" })
    public ScalableDimension getScalableDimension() {
        return scalableDimension;
    }

    public void setScalableDimension(ScalableDimension scalableDimension) {
        this.scalableDimension = scalableDimension;
    }

    /**
     * The policy update behavior for the resource.
     */
    @Updatable
    @ValidStrings({ "KeepExternalPolicies", "ReplaceExternalPolicies" })
    public ScalingPolicyUpdateBehavior getScalingPolicyUpdateBehavior() {
        return scalingPolicyUpdateBehavior;
    }

    public void setScalingPolicyUpdateBehavior(ScalingPolicyUpdateBehavior scalingPolicyUpdateBehavior) {
        this.scalingPolicyUpdateBehavior = scalingPolicyUpdateBehavior;
    }

    /**
     * The amount of time, in seconds, to buffer the run time of scheduled scaling actions.
     */
    @Updatable
    public Integer getScheduledActionBufferTime() {
        return scheduledActionBufferTime;
    }

    public void setScheduledActionBufferTime(Integer scheduledActionBufferTime) {
        this.scheduledActionBufferTime = scheduledActionBufferTime;
    }

    /**
     * The namespace of the service.
     */
    @Updatable
    @ValidStrings({ "autoscaling", "ecs", "ec2", "rds", "dynamodb" })
    public ServiceNamespace getServiceNamespace() {
        return serviceNamespace;
    }

    public void setServiceNamespace(ServiceNamespace serviceNamespace) {
        this.serviceNamespace = serviceNamespace;
    }

    /**
     * The target tracking configurations.
     *
     * @subresource gyro.aws.autoscaling.AutoScalingTargetTrackingConfiguration
     */
    @CollectionMax(10)
    @Updatable
    public List<AutoScalingTargetTrackingConfiguration> getTargetTrackingConfigurations() {
        if (targetTrackingConfigurations == null) {
            targetTrackingConfigurations = new ArrayList<>();
        }
        return targetTrackingConfigurations;
    }

    public void setTargetTrackingConfigurations(List<AutoScalingTargetTrackingConfiguration> targetTrackingConfigurations) {
        this.targetTrackingConfigurations = targetTrackingConfigurations;
    }

    @Override
    public void copyFrom(ScalingInstruction model) {
        setDisableDynamicScaling(model.disableDynamicScaling());
        setMaxCapacity(model.maxCapacity());
        setMinCapacity(model.minCapacity());
        setPredictiveScalingMaxCapacityBehavior(model.predictiveScalingMaxCapacityBehavior());
        setPredictiveScalingMaxCapacityBuffer(model.predictiveScalingMaxCapacityBuffer());
        setPredictiveScalingMode(model.predictiveScalingMode());
        setResourceId(model.resourceId());
        setScalableDimension(model.scalableDimension());
        setScalingPolicyUpdateBehavior(model.scalingPolicyUpdateBehavior());
        setScheduledActionBufferTime(model.scheduledActionBufferTime());
        setServiceNamespace(model.serviceNamespace());

        if (model.customizedLoadMetricSpecification() != null) {
            AutoScalingCustomizedLoadMetricSpecification specification = newSubresource(
                AutoScalingCustomizedLoadMetricSpecification.class);
            specification.copyFrom(model.customizedLoadMetricSpecification());
            setCustomizedLoadMetricSpecification(specification);
        } else {
            setCustomizedLoadMetricSpecification(null);
        }

        if (model.predefinedLoadMetricSpecification() != null) {
            AutoScalingPredefinedLoadMetricSpecification specification = newSubresource(
                AutoScalingPredefinedLoadMetricSpecification.class);
            specification.copyFrom(model.predefinedLoadMetricSpecification());
            setPredefinedLoadMetricSpecification(specification);
        } else {
            setPredefinedLoadMetricSpecification(null);
        }

        getTargetTrackingConfigurations().clear();
        if (model.targetTrackingConfigurations() != null) {
            model.targetTrackingConfigurations().forEach(config -> {
                AutoScalingTargetTrackingConfiguration configuration = newSubresource(
                    AutoScalingTargetTrackingConfiguration.class);
                configuration.copyFrom(config);
                getTargetTrackingConfigurations().add(configuration);
            });
        }
    }

    @Override
    public String primaryKey() {
        return String.format("%s", getCustomizedLoadMetricSpecification().getName());
    }

    public ScalingInstruction toScalingInstruction() {
        return ScalingInstruction.builder()
            .customizedLoadMetricSpecification(getCustomizedLoadMetricSpecification() != null
                ? getCustomizedLoadMetricSpecification().toCustomizedLoadMetricSpecification()
                : null)
            .disableDynamicScaling(getDisableDynamicScaling())
            .maxCapacity(getMaxCapacity())
            .minCapacity(getMinCapacity())
            .predefinedLoadMetricSpecification(getPredefinedLoadMetricSpecification() != null
                ? getPredefinedLoadMetricSpecification().toPredefinedLoadMetricSpecification()
                : null)
            .predictiveScalingMaxCapacityBehavior(getPredictiveScalingMaxCapacityBehavior())
            .predictiveScalingMaxCapacityBuffer(getPredictiveScalingMaxCapacityBuffer())
            .predictiveScalingMode(getPredictiveScalingMode())
            .resourceId(getResourceId())
            .scalableDimension(getScalableDimension())
            .scalingPolicyUpdateBehavior(getScalingPolicyUpdateBehavior())
            .scheduledActionBufferTime(getScheduledActionBufferTime())
            .serviceNamespace(getServiceNamespace())
            .targetTrackingConfigurations(getTargetTrackingConfigurations().stream()
                .map(AutoScalingTargetTrackingConfiguration::toTargetTrackingConfiguration)
                .collect(Collectors.toList()))
            .build();
    }

}
