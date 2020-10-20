package gyro.aws.autoscaling;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
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
    private ServiceNamespace serviceNamspace;
    private List<AutoScalingTargetTrackingConfiguration> targetTrackingConfiguration;

    public AutoScalingCustomizedLoadMetricSpecification getCustomizedLoadMetricSpecification() {
        return customizedLoadMetricSpecification;
    }

    public void setCustomizedLoadMetricSpecification(AutoScalingCustomizedLoadMetricSpecification customizedLoadMetricSpecification) {
        this.customizedLoadMetricSpecification = customizedLoadMetricSpecification;
    }

    public Boolean getDisableDynamicScaling() {
        return disableDynamicScaling;
    }

    public void setDisableDynamicScaling(Boolean disableDynamicScaling) {
        this.disableDynamicScaling = disableDynamicScaling;
    }

    @Required
    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    @Required
    public Integer getMinCapacity() {
        return minCapacity;
    }

    public void setMinCapacity(Integer minCapacity) {
        this.minCapacity = minCapacity;
    }

    public AutoScalingPredefinedLoadMetricSpecification getPredefinedLoadMetricSpecification() {
        return predefinedLoadMetricSpecification;
    }

    public void setPredefinedLoadMetricSpecification(AutoScalingPredefinedLoadMetricSpecification predefinedLoadMetricSpecification) {
        this.predefinedLoadMetricSpecification = predefinedLoadMetricSpecification;
    }

    public PredictiveScalingMaxCapacityBehavior getPredictiveScalingMaxCapacityBehavior() {
        return predictiveScalingMaxCapacityBehavior;
    }

    public void setPredictiveScalingMaxCapacityBehavior(PredictiveScalingMaxCapacityBehavior predictiveScalingMaxCapacityBehavior) {
        this.predictiveScalingMaxCapacityBehavior = predictiveScalingMaxCapacityBehavior;
    }

    public Integer getPredictiveScalingMaxCapacityBuffer() {
        return predictiveScalingMaxCapacityBuffer;
    }

    public void setPredictiveScalingMaxCapacityBuffer(Integer predictiveScalingMaxCapacityBuffer) {
        this.predictiveScalingMaxCapacityBuffer = predictiveScalingMaxCapacityBuffer;
    }

    public PredictiveScalingMode getPredictiveScalingMode() {
        return predictiveScalingMode;
    }

    public void setPredictiveScalingMode(PredictiveScalingMode predictiveScalingMode) {
        this.predictiveScalingMode = predictiveScalingMode;
    }

    @Required
    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public ScalableDimension getScalableDimension() {
        return scalableDimension;
    }

    public void setScalableDimension(ScalableDimension scalableDimension) {
        this.scalableDimension = scalableDimension;
    }

    public ScalingPolicyUpdateBehavior getScalingPolicyUpdateBehavior() {
        return scalingPolicyUpdateBehavior;
    }

    public void setScalingPolicyUpdateBehavior(ScalingPolicyUpdateBehavior scalingPolicyUpdateBehavior) {
        this.scalingPolicyUpdateBehavior = scalingPolicyUpdateBehavior;
    }

    public Integer getScheduledActionBufferTime() {
        return scheduledActionBufferTime;
    }

    public void setScheduledActionBufferTime(Integer scheduledActionBufferTime) {
        this.scheduledActionBufferTime = scheduledActionBufferTime;
    }

    public ServiceNamespace getServiceNamspace() {
        return serviceNamspace;
    }

    public void setServiceNamspace(ServiceNamespace serviceNamspace) {
        this.serviceNamspace = serviceNamspace;
    }

    public List<AutoScalingTargetTrackingConfiguration> getTargetTrackingConfiguration() {
        if (targetTrackingConfiguration == null) {
            targetTrackingConfiguration = new ArrayList<>();
        }
        return targetTrackingConfiguration;
    }

    public void setTargetTrackingConfiguration(List<AutoScalingTargetTrackingConfiguration> targetTrackingConfiguration) {
        this.targetTrackingConfiguration = targetTrackingConfiguration;
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
        setServiceNamspace(model.serviceNamespace());

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

        if (model.targetTrackingConfigurations() != null) {
            List<AutoScalingTargetTrackingConfiguration> configurations = new ArrayList<>();
            model.targetTrackingConfigurations().forEach(config -> {
                AutoScalingTargetTrackingConfiguration configuration = newSubresource(
                    AutoScalingTargetTrackingConfiguration.class);
                configuration.copyFrom(config);
                configurations.add(configuration);
            });
            setTargetTrackingConfiguration(configurations);
        }
    }

    @Override
    public String primaryKey() {
        return null;
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
            .serviceNamespace(getServiceNamspace())
            .targetTrackingConfigurations(getTargetTrackingConfiguration().stream()
                .map(AutoScalingTargetTrackingConfiguration::toTargetTrackingConfiguration)
                .collect(Collectors.toList()))
            .build();
    }

}
