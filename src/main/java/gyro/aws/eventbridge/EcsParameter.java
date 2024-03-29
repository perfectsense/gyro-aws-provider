/*
 * Copyright 2022, Brightspot.
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

package gyro.aws.eventbridge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.aws.ecs.EcsTaskDefinitionResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.eventbridge.model.EcsParameters;
import software.amazon.awssdk.services.eventbridge.model.LaunchType;
import software.amazon.awssdk.services.eventbridge.model.PropagateTags;
import software.amazon.awssdk.services.eventbridge.model.Tag;

public class EcsParameter extends Diffable implements Copyable<EcsParameters> {

    private List<CapacityProviderStrategyItem> capacityProviderStrategy;
    private Boolean enableEcsManagedTags;
    private Boolean enableExecuteCommand;
    private LaunchType launchType;
    private NetworkConfiguration networkConfiguration;
    private String group;
    private List<PlacementConstraint> placementConstraint;
    private List<PlacementStrategy> placementStrategy;
    private String platformVersion;
    private PropagateTags propagateTags;
    private Map<String, String> tags;
    private Integer taskCount;
    private EcsTaskDefinitionResource taskDefinition;
    private String referenceId;

    /**
     * A list of capacity provider strategy item configs.
     *
     * @subresource gyro.aws.eventbridge.CapacityProviderStrategyItem
     */
    @Updatable
    public List<CapacityProviderStrategyItem> getCapacityProviderStrategy() {
        if (capacityProviderStrategy == null) {
            capacityProviderStrategy = new ArrayList<>();
        }

        return capacityProviderStrategy;
    }

    public void setCapacityProviderStrategy(List<CapacityProviderStrategyItem> capacityProviderStrategy) {
        this.capacityProviderStrategy = capacityProviderStrategy;
    }

    /**
     * When set to ``true``, Amazon ECS managed tags are enabled for the task. Defaults to ``false``.
     */
    @Updatable
    public Boolean getEnableEcsManagedTags() {
        if (enableEcsManagedTags == null) {
            enableEcsManagedTags = false;
        }

        return enableEcsManagedTags;
    }

    public void setEnableEcsManagedTags(Boolean enableEcsManagedTags) {
        this.enableEcsManagedTags = enableEcsManagedTags;
    }

    /**
     * When set to ``true``, the execute command functionality for the containers in this task is enabled. Defaults to ```false`.
     */
    @Updatable
    public Boolean getEnableExecuteCommand() {
        if (enableExecuteCommand == null) {
            enableExecuteCommand = false;
        }

        return enableExecuteCommand;
    }

    public void setEnableExecuteCommand(Boolean enableExecuteCommand) {
        this.enableExecuteCommand = enableExecuteCommand;
    }

    /**
     * The launch type on which the task is running.
     */
    @Updatable
    @ValidStrings({"EC2", "FARGATE", "EXTERNAL"})
    public LaunchType getLaunchType() {
        return launchType;
    }

    public void setLaunchType(LaunchType launchType) {
        this.launchType = launchType;
    }

    /**
     * The network configuration for the ECS task.
     *
     * @subresource gyro.aws.eventbridge.NetworkConfiguration
     */
    @Updatable
    public NetworkConfiguration getNetworkConfiguration() {
        return networkConfiguration;
    }

    public void setNetworkConfiguration(NetworkConfiguration networkConfiguration) {
        this.networkConfiguration = networkConfiguration;
    }

    /**
     * The ECS task group for the task.
     */
    @Updatable
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * The list of placement group configs.
     *
     * @subresource gyro.aws.eventbridge.PlacementConstraint
     */
    @Updatable
    public List<PlacementConstraint> getPlacementConstraint() {
        if (placementConstraint == null) {
            placementConstraint = new ArrayList<>();
        }

        return placementConstraint;
    }

    public void setPlacementConstraint(List<PlacementConstraint> placementConstraint) {
        this.placementConstraint = placementConstraint;
    }

    /**
     * The list of placement strategy configs.
     *
     * @subresource gyro.aws.eventbridge.PlacementStrategy
     */
    @Updatable
    public List<PlacementStrategy> getPlacementStrategy() {
        if (placementStrategy == null) {
            placementStrategy = new ArrayList<>();
        }

        return placementStrategy;
    }

    public void setPlacementStrategy(List<PlacementStrategy> placementStrategy) {
        this.placementStrategy = placementStrategy;
    }

    /**
     * The platform version for the task. Specify only the numeric portion of the platform version, such as 1.1.0.
     */
    @Updatable
    public String getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    /**
     * Specifies whether to propagate the tags from the task definition to the task.
     */
    @Updatable
    @ValidStrings("TASK_DEFINITION")
    public PropagateTags getPropagateTags() {
        return propagateTags;
    }

    public void setPropagateTags(PropagateTags propagateTags) {
        this.propagateTags = propagateTags;
    }

    /**
     * The metadata that you apply to the task to help you categorize and organize them.
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The number of tasks to create based on task-definition.
     */
    @Updatable
    public Integer getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Integer taskCount) {
        this.taskCount = taskCount;
    }

    /**
     * The task definition to use if the event target is an Amazon ECS task.
     */
    @Updatable
    public EcsTaskDefinitionResource getTaskDefinition() {
        return taskDefinition;
    }

    public void setTaskDefinition(EcsTaskDefinitionResource taskDefinition) {
        this.taskDefinition = taskDefinition;
    }

    /**
     * The reference ID to use for the task.
     */
    @Updatable
    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    @Override
    public void copyFrom(EcsParameters model) {
        getCapacityProviderStrategy().clear();
        if (model.capacityProviderStrategy() != null) {
            model.capacityProviderStrategy().forEach(o -> {
                CapacityProviderStrategyItem capacityProviderStrategyItem = newSubresource(CapacityProviderStrategyItem.class);
                capacityProviderStrategyItem.copyFrom(o);
                getCapacityProviderStrategy().add(capacityProviderStrategyItem);
            });
        }

        getPlacementConstraint().clear();
        if (model.placementConstraints() != null){
            model.placementConstraints().forEach(o -> {
                PlacementConstraint placementConstraint = newSubresource(PlacementConstraint.class);
                placementConstraint.copyFrom(o);
                getPlacementConstraint().add(placementConstraint);
            });
        }

        getPlacementStrategy().clear();
        if (model.placementStrategy() != null){
            model.placementStrategy().forEach(o -> {
                PlacementStrategy placementStrategy = newSubresource(PlacementStrategy.class);
                placementStrategy.copyFrom(o);
                getPlacementStrategy().add(placementStrategy);
            });
        }

        NetworkConfiguration networkConfiguration = null;
        if (model.networkConfiguration() != null) {
            networkConfiguration = newSubresource(NetworkConfiguration.class);
            networkConfiguration.copyFrom(model.networkConfiguration());
        }
        setNetworkConfiguration(networkConfiguration);

        getTags().clear();
        if (model.tags() != null) {
            model.tags().forEach(o -> getTags().put(o.key(), o.value()));
        }

        setEnableEcsManagedTags(model.enableECSManagedTags());
        setEnableExecuteCommand(model.enableExecuteCommand());
        setLaunchType(model.launchType());
        setGroup(model.group());
        setPlatformVersion(model.platformVersion());
        setPropagateTags(model.propagateTags());
        setReferenceId(model.referenceId());
        setTaskCount(model.taskCount());

        setTaskDefinition(null);
        if (model.taskDefinitionArn() != null) {
            setTaskDefinition(findById(EcsTaskDefinitionResource.class, model.taskDefinitionArn()));
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    protected EcsParameters toEcsParameters() {
        EcsParameters.Builder builder = EcsParameters.builder();

        if (!getCapacityProviderStrategy().isEmpty()) {
            builder = builder.capacityProviderStrategy(getCapacityProviderStrategy().stream()
                .map(CapacityProviderStrategyItem::toCapacityProviderStrategyItem)
                .collect(Collectors.toList()));
        }

        if (!getPlacementStrategy().isEmpty()) {
            builder = builder.placementStrategy(getPlacementStrategy().stream()
                .map(PlacementStrategy::toPlacementStrategy)
                .collect(Collectors.toList()));
        }

        if (!getPlacementConstraint().isEmpty()) {
            builder = builder.placementConstraints(getPlacementConstraint().stream()
                .map(PlacementConstraint::toPlacementConstraint)
                .collect(Collectors.toList()));
        }

        if (getNetworkConfiguration() != null) {
            builder = builder.networkConfiguration(getNetworkConfiguration().toNetworkConfiguration());
        }

        builder = builder.tags(getTags().entrySet().stream()
            .map(o -> Tag.builder().key(o.getKey()).value(o.getValue()).build())
            .collect(Collectors.toList()));

        builder = builder.enableECSManagedTags(getEnableEcsManagedTags());

        builder = builder.enableExecuteCommand(getEnableExecuteCommand());

        if (getLaunchType() != null) {
            builder = builder.launchType(getLaunchType());
        }

        if (getGroup() != null) {
            builder = builder.group(getGroup());
        }

        if (getPlatformVersion() != null) {
            builder = builder.platformVersion(getPlatformVersion());
        }

        if (getPropagateTags() != null) {
            builder = builder.propagateTags(getPropagateTags());
        }

        if (getTaskCount() != null) {
            builder = builder.taskCount(getTaskCount());
        }

        if (getTaskDefinition() != null) {
            builder = builder.taskDefinitionArn(getTaskDefinition().getArn());
        }

        if (!getTags().isEmpty()) {
            builder = builder.tags(getTags()
                .entrySet()
                .stream()
                .map(o -> Tag.builder()
                    .key(o.getKey())
                    .value(o.getValue())
                    .build())
                .collect(Collectors.toList()));
        }

        if (getReferenceId() != null) {
            builder = builder.referenceId(getReferenceId());
        }

        return builder.build();
    }
}
