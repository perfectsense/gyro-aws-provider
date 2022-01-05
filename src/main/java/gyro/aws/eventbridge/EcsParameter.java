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

    public List<CapacityProviderStrategyItem> getCapacityProviderStrategy() {
        if (capacityProviderStrategy == null) {
            capacityProviderStrategy = new ArrayList<>();
        }

        return capacityProviderStrategy;
    }

    public void setCapacityProviderStrategy(List<CapacityProviderStrategyItem> capacityProviderStrategy) {
        this.capacityProviderStrategy = capacityProviderStrategy;
    }

    public Boolean getEnableEcsManagedTags() {
        if (enableEcsManagedTags == null) {
            enableEcsManagedTags = false;
        }

        return enableEcsManagedTags;
    }

    public void setEnableEcsManagedTags(Boolean enableEcsManagedTags) {
        this.enableEcsManagedTags = enableEcsManagedTags;
    }

    public Boolean getEnableExecuteCommand() {
        if (enableExecuteCommand == null) {
            enableExecuteCommand = false;
        }

        return enableExecuteCommand;
    }

    public void setEnableExecuteCommand(Boolean enableExecuteCommand) {
        this.enableExecuteCommand = enableExecuteCommand;
    }

    public LaunchType getLaunchType() {
        return launchType;
    }

    public void setLaunchType(LaunchType launchType) {
        this.launchType = launchType;
    }

    public NetworkConfiguration getNetworkConfiguration() {
        return networkConfiguration;
    }

    public void setNetworkConfiguration(NetworkConfiguration networkConfiguration) {
        this.networkConfiguration = networkConfiguration;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public List<PlacementConstraint> getPlacementConstraint() {
        if (placementConstraint == null) {
            placementConstraint = new ArrayList<>();
        }

        return placementConstraint;
    }

    public void setPlacementConstraint(List<PlacementConstraint> placementConstraint) {
        this.placementConstraint = placementConstraint;
    }

    public List<PlacementStrategy> getPlacementStrategy() {
        if (placementStrategy == null) {
            placementStrategy = new ArrayList<>();
        }

        return placementStrategy;
    }

    public void setPlacementStrategy(List<PlacementStrategy> placementStrategy) {
        this.placementStrategy = placementStrategy;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public PropagateTags getPropagateTags() {
        return propagateTags;
    }

    public void setPropagateTags(PropagateTags propagateTags) {
        this.propagateTags = propagateTags;
    }

    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public Integer getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Integer taskCount) {
        this.taskCount = taskCount;
    }

    public EcsTaskDefinitionResource getTaskDefinition() {
        return taskDefinition;
    }

    public void setTaskDefinition(EcsTaskDefinitionResource taskDefinition) {
        this.taskDefinition = taskDefinition;
    }

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

        return builder.build();
    }
}
