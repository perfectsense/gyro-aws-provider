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

package gyro.aws.ecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.CollectionMax;
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.ClusterNotFoundException;
import software.amazon.awssdk.services.ecs.model.LaunchType;
import software.amazon.awssdk.services.ecs.model.PropagateTags;
import software.amazon.awssdk.services.ecs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.ecs.model.RunTaskRequest;
import software.amazon.awssdk.services.ecs.model.RunTaskResponse;
import software.amazon.awssdk.services.ecs.model.Tag;
import software.amazon.awssdk.services.ecs.model.Task;

/**
 * Create an ECS Task.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::ecs-task task
 *        cluster: $(external-query aws::ecs-cluster {name: "example-ecs-task"})
 *        count: 1
 *        group: "example-group"
 *        launch-type: ECS
 *        reference-id: "example-ecs-task-reference"
 *        started-by: "example-user"
 *        task-definition: $(external-query aws::ecs-task-definition { family: 'test-ec2-task-definition', revision: 4 })
 *    end
 */
 @Type("ecs-task")
public class EcsTaskResource extends AwsResource implements Copyable<Task> {

    private List<EcsCapacityProviderStrategyItem> capacityProviderStrategyItem;
    private EcsClusterResource cluster;
    private Integer count;
    private Boolean enableEcsManagedTags;
    private String group;
    private LaunchType launchType;
    private EcsNetworkConfiguration networkConfiguration;
    private List<EcsPlacementConstraint> placementConstraints;
    private List<EcsPlacementStrategy> placementStrategy;
    private PropagateTags propagateTags;
    private String referenceId;
    private String startedBy;
    private Map<String, String> tags;
    private EcsTaskDefinitionResource taskDefinition;

    // Output
    private String arn;

    /**
     * The capacity provider strategy to use for the task.
     */
    @ConflictsWith("launch-type")
    public List<EcsCapacityProviderStrategyItem> getCapacityProviderStrategyItem() {
        if (capacityProviderStrategyItem == null) {
            capacityProviderStrategyItem = new ArrayList<>();
        }

        return capacityProviderStrategyItem;
    }

    public void setCapacityProviderStrategyItem(List<EcsCapacityProviderStrategyItem> capacityProviderStrategyItem) {
        this.capacityProviderStrategyItem = capacityProviderStrategyItem;
    }

    /**
     * The cluster on which to run your task. (Required)
     */
    @Required
    public EcsClusterResource getCluster() {
        return cluster;
    }

    public void setCluster(EcsClusterResource cluster) {
        this.cluster = cluster;
    }

    /**
     * The cluster on which to run your task. (Required)
     */
    @Required
    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * Enable or disable Amazon ECS managed tags for the tasks within the task.
     */
    @ConflictsWith("capacity-provider-strategy-item")
    public Boolean getEnableEcsManagedTags() {
        return enableEcsManagedTags;
    }

    public void setEnableEcsManagedTags(Boolean enableEcsManagedTags) {
        this.enableEcsManagedTags = enableEcsManagedTags;
    }

    /**
     * The name of the task group to associate with the task.
     */
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * The launch type on which to run your task.
     */
    @ConflictsWith("capacity-provider-strategy-item")
    public LaunchType getLaunchType() {
        return launchType;
    }

    public void setLaunchType(LaunchType launchType) {
        this.launchType = launchType;
    }

    /**
     * The network configuration for the task.
     */
    public EcsNetworkConfiguration getNetworkConfiguration() {
        return networkConfiguration;
    }

    public void setNetworkConfiguration(EcsNetworkConfiguration networkConfiguration) {
        this.networkConfiguration = networkConfiguration;
    }

    /**
     * The placement constraints to use for task. Maximum of ``10`` constraints.
     */
    @CollectionMax(10)
    public List<EcsPlacementConstraint> getPlacementConstraints() {
        if (placementConstraints == null) {
            placementConstraints = new ArrayList<>();
        }

        return placementConstraints;
    }

    public void setPlacementConstraints(List<EcsPlacementConstraint> placementConstraints) {
        this.placementConstraints = placementConstraints;
    }

    /**
     * The placement strategies to use for your task. Maximum of ``5`` strategies.
     */
    @CollectionMax(5)
    public List<EcsPlacementStrategy> getPlacementStrategy() {
        if (placementStrategy == null) {
            placementStrategy = new ArrayList<>();
        }

        return placementStrategy;
    }

    public void setPlacementStrategy(List<EcsPlacementStrategy> placementStrategy) {
        this.placementStrategy = placementStrategy;
    }

    /**
     * The option to propagate the tags from the task definition or the service to the tasks in the service. Valid values are ``TASK_DEFINITION`` or ``SERVICE``.
     */
    public PropagateTags getPropagateTags() {
        return propagateTags;
    }

    public void setPropagateTags(PropagateTags propagateTags) {
        this.propagateTags = propagateTags;
    }

    /**
     * The reference ID to use for the task.
     */
    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    /**
     * An optional tag specified when a task is started.
     */
    public String getStartedBy() {
        return startedBy;
    }

    public void setStartedBy(String startedBy) {
        this.startedBy = startedBy;
    }

    /**
     * The tags to apply to the task.
     */
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
     * The task definition to run in your task.
     */
    public EcsTaskDefinitionResource getTaskDefinition() {
        return taskDefinition;
    }

    public void setTaskDefinition(EcsTaskDefinitionResource taskDefinition) {
        this.taskDefinition = taskDefinition;
    }

    /**
     * The Amazon Resource Number (ARN) of the task.
     */
    @Output
    @Id
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public void copyFrom(Task model) {
        setCluster(findById(EcsClusterResource.class, model.clusterArn()));
        setGroup(model.group());
        setLaunchType(model.launchType());
        setStartedBy(model.startedBy());
        setTags(model.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value)));
        setTaskDefinition(findById(EcsTaskDefinitionResource.class, model.taskDefinitionArn()));
        setArn(model.taskArn());
    }

    @Override
    public boolean refresh() {
        EcsClient client = createClient(EcsClient.class);

        Task task = getTask(client);

        if (task == null) {
            return false;
        }

        copyFrom(task);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        EcsClient client = createClient(EcsClient.class);

        RunTaskRequest.Builder builder = RunTaskRequest.builder()
            .cluster(getCluster().getClusterName())
            .taskDefinition(getTaskDefinition().getArn());

        if (getEnableEcsManagedTags() != null) {
            builder = builder.enableECSManagedTags(getEnableEcsManagedTags());
        }

        if (getCapacityProviderStrategyItem() != null) {
            builder = builder.capacityProviderStrategy(getCapacityProviderStrategyItem().stream()
                .map(EcsCapacityProviderStrategyItem::copyTo).collect(Collectors.toList()));
        }

        if (getCount() != null) {
            builder = builder.count(getCount());
        }

        if (getGroup() != null) {
            builder = builder.group(getGroup());
        }

        if (getLaunchType() != null) {
            builder = builder.launchType(getLaunchType());
        }

        if (getNetworkConfiguration() != null) {
            builder = builder.networkConfiguration(getNetworkConfiguration().toNetworkConfiguration());
        }

        if (getPlacementConstraints() != null) {
            builder = builder.placementConstraints(getPlacementConstraints().stream()
                .map(EcsPlacementConstraint::toPlacementConstraint).collect(Collectors.toList()));
        }

        if (getPlacementStrategy() != null) {
            builder = builder.placementStrategy(getPlacementStrategy().stream()
                .map(EcsPlacementStrategy::toPlacementStrategy).collect(Collectors.toList()));
        }

        if (getPropagateTags() != null) {
            builder = builder.propagateTags(getPropagateTags());
        }

        if (getReferenceId() != null) {
            builder = builder.referenceId(getReferenceId());
        }

        if (getStartedBy() != null) {
            builder = builder.startedBy(getStartedBy());
        }

        if (getTags() != null) {
            builder = builder.tags(getTags().entrySet()
                .stream()
                .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
                .collect(Collectors.toList()));
        }

        RunTaskResponse runTaskResponse = client.runTask(builder.build());

        setArn(runTaskResponse.tasks().get(0).taskArn());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        EcsClient client = createClient(EcsClient.class);

        try {
            client.stopTask(r -> r.cluster(getCluster().getClusterName()).task(getArn()));

        } catch (ResourceNotFoundException ex) {
            // ignore
        }
    }

    public Task getTask(EcsClient client) {
        Task task = null;

        try {
            List<Task> tasks = client.describeTasks(r -> r.tasks(getArn()).cluster(getCluster().getClusterName()))
                .tasks();

        } catch (ClusterNotFoundException ex) {
            // ignore
        }

        return task;
    }
}
