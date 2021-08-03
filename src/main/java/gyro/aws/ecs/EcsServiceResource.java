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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.RoleResource;
import gyro.core.GyroUI;
import gyro.core.TimeoutSettings;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.CollectionMax;
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.DependsOn;
import gyro.core.validation.Range;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.CreateServiceRequest;
import software.amazon.awssdk.services.ecs.model.CreateServiceResponse;
import software.amazon.awssdk.services.ecs.model.DeploymentControllerType;
import software.amazon.awssdk.services.ecs.model.LaunchType;
import software.amazon.awssdk.services.ecs.model.PropagateTags;
import software.amazon.awssdk.services.ecs.model.SchedulingStrategy;
import software.amazon.awssdk.services.ecs.model.Tag;
import software.amazon.awssdk.services.ecs.model.UpdateServiceRequest;

/**
 * Create an ECS service.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::ecs-service example-ecs-service
 *         name: "example-ecs-service"
 *         task-definition: $(external-query aws::ecs-task-definition { family: 'test-fargate-task-definition', revision: 1 })
 *         cluster: $(aws::ecs-cluster ecs-cluster-example)
 *         desired-count: 1
 *         enable-ecs-managed-tags: false
 *         launch-type: EC2
 *         scheduling-strategy: REPLICA
 *
 *         deployment-configuration
 *             maximum-percent: 200
 *             minimum-healthy-percent: 100
 *         end
 *
 *         deployment-controller
 *             type: ECS
 *         end
 *
 *         network-configuration
 *             aws-vpc-configuration
 *                 assign-public-ip: DISABLED
 *                 security-groups: [
 *                     $(aws::security-group security-group)
 *                 ]
 *                 subnets: [
 *                     $(aws::subnet "subnet-us-east-1a"),
 *                     $(aws::subnet "subnet-us-east-1b")
 *                 ]
 *             end
 *         end
 *
 *         placement-constraint
 *             expression: "agentVersion > 0"
 *             type: "memberOf"
 *         end
 *
 *         placement-strategy
 *             type: binpack
 *             field: "MEMORY"
 *         end
 *     end
 */
@Type("ecs-service")
public class EcsServiceResource extends AwsResource
    implements Copyable<software.amazon.awssdk.services.ecs.model.Service> {

    private List<EcsCapacityProviderStrategyItem> capacityProviderStrategyItem;
    private EcsDeploymentConfiguration deploymentConfiguration;
    private EcsDeploymentController deploymentController;
    private Integer desiredCount;
    private Boolean enableEcsManagedTags;
    private Integer healthCheckGracePeriodSeconds;
    private LaunchType launchType;
    private List<EcsLoadBalancer> loadBalancer;
    private EcsNetworkConfiguration networkConfiguration;
    private List<EcsPlacementConstraint> placementConstraint;
    private List<EcsPlacementStrategy> placementStrategy;
    private String platformVersion;
    private PropagateTags propagateTags;
    private RoleResource role;
    private SchedulingStrategy schedulingStrategy;
    private String name;
    private List<EcsServiceRegistries> serviceRegistries;
    private EcsTaskDefinitionResource taskDefinition;
    private EcsClusterResource cluster;
    private Map<String, String> tags;

    // Output
    private String arn;
    private Integer runningCount;
    private Integer pendingCount;

    /**
     * The capacity provider strategy to use for the service.
     */
    @ConflictsWith("launch-type")
    @Updatable
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
     * The deployment parameters that control how many tasks run during the deployment.
     */
    @Updatable
    public EcsDeploymentConfiguration getDeploymentConfiguration() {
        return deploymentConfiguration;
    }

    public void setDeploymentConfiguration(EcsDeploymentConfiguration deploymentConfiguration) {
        this.deploymentConfiguration = deploymentConfiguration;
    }

    /**
     * The deployment controller to use for the service. Defaults to ``ECS``.
     */
    public EcsDeploymentController getDeploymentController() {
        return deploymentController;
    }

    public void setDeploymentController(EcsDeploymentController deploymentController) {
        this.deploymentController = deploymentController;
    }

    /**
     * The number of instantiations of the specified task definition to keep running on your cluster.
     */
    @Updatable
    public Integer getDesiredCount() {
        return desiredCount;
    }

    public void setDesiredCount(Integer desiredCount) {
        this.desiredCount = desiredCount;
    }

    /**
     * Enable or disable Amazon ECS managed tags for the tasks within the service.
     */
    public Boolean getEnableEcsManagedTags() {
        return enableEcsManagedTags;
    }

    public void setEnableEcsManagedTags(Boolean enableEcsManagedTags) {
        this.enableEcsManagedTags = enableEcsManagedTags;
    }

    /**
     * The period of time, in seconds, that the Amazon ECS service scheduler should ignore unhealthy Elastic Load Balancing target health checks after a task first starts.
     */
    @Range(min = 0, max = 2147483647L)
    @DependsOn("load-balancer")
    @Updatable
    public Integer getHealthCheckGracePeriodSeconds() {
        return healthCheckGracePeriodSeconds;
    }

    public void setHealthCheckGracePeriodSeconds(Integer healthCheckGracePeriodSeconds) {
        this.healthCheckGracePeriodSeconds = healthCheckGracePeriodSeconds;
    }

    /**
     * The launch type on which to run your service. Defaults to ``REPLICA``.
     */
    @ConflictsWith("capacity-provider-strategy-item")
    public LaunchType getLaunchType() {
        return launchType;
    }

    public void setLaunchType(LaunchType launchType) {
        this.launchType = launchType;
    }

    /**
     * The load balancers to use with your service.
     */
    public List<EcsLoadBalancer> getLoadBalancer() {
        if (loadBalancer == null) {
            loadBalancer = new ArrayList<>();
        }

        return loadBalancer;
    }

    public void setLoadBalancer(List<EcsLoadBalancer> loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    /**
     * The network configuration for the service.
     */
    @Updatable
    public EcsNetworkConfiguration getNetworkConfiguration() {
        return networkConfiguration;
    }

    public void setNetworkConfiguration(EcsNetworkConfiguration networkConfiguration) {
        this.networkConfiguration = networkConfiguration;
    }

    /**
     * The placement constraints to use for tasks in your service.
     */
    @CollectionMax(10)
    @Updatable
    public List<EcsPlacementConstraint> getPlacementConstraint() {
        if (placementConstraint == null) {
            placementConstraint = new ArrayList<>();
        }

        return placementConstraint;
    }

    public void setPlacementConstraint(List<EcsPlacementConstraint> placementConstraint) {
        this.placementConstraint = placementConstraint;
    }

    /**
     * The placement strategies to use for tasks in your service.
     */
    @CollectionMax(5)
    @Updatable
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
     * The platform version that your tasks in the service should run on.
     */
    @Updatable
    public String getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    /**
     * The option to propagate the tags from the task definition or the service to the tasks in the service.
     */
    public PropagateTags getPropagateTags() {
        return propagateTags;
    }

    public void setPropagateTags(PropagateTags propagateTags) {
        this.propagateTags = propagateTags;
    }

    /**
     * The role that allows Amazon ECS to make calls to your load balancer
     */
    @DependsOn("load-balancer")
    public RoleResource getRole() {
        return role;
    }

    public void setRole(RoleResource role) {
        this.role = role;
    }

    /**
     * The scheduling strategy to use for the service.
     */
    public SchedulingStrategy getSchedulingStrategy() {
        return schedulingStrategy;
    }

    public void setSchedulingStrategy(SchedulingStrategy schedulingStrategy) {
        this.schedulingStrategy = schedulingStrategy;
    }

    /**
     * The name of the service.
     */
    @Regex(value = "^[a-zA-Z]([-a-zA-Z0-9]{0,254})?", message = "a string 1 to 255 characters long containing letters, numbers, and hyphens. Must begin with a letter")
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The details of the service discovery registries to assign to this service.
     */
    public List<EcsServiceRegistries> getServiceRegistries() {
        if (serviceRegistries == null) {
            serviceRegistries = new ArrayList<>();
        }

        return serviceRegistries;
    }

    public void setServiceRegistries(List<EcsServiceRegistries> serviceRegistries) {
        this.serviceRegistries = serviceRegistries;
    }

    /**
     * The task definition to run in your service.
     */
    @Updatable
    public EcsTaskDefinitionResource getTaskDefinition() {
        return taskDefinition;
    }

    public void setTaskDefinition(EcsTaskDefinitionResource taskDefinition) {
        this.taskDefinition = taskDefinition;
    }

    /**
     * The tags to apply to the service.
     */
    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The cluster on which to run your service.
     */
    @Required
    public EcsClusterResource getCluster() {
        return cluster;
    }

    public void setCluster(EcsClusterResource cluster) {
        this.cluster = cluster;
    }

    /**
     * The Amazon Resource Name (ARN) of the service.
     */
    @Output
    @Id
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The number of tasks in the cluster that are currently running.
     */
    @Output
    public Integer getRunningCount() {
        return runningCount;
    }

    public void setRunningCount(Integer runningCount) {
        this.runningCount = runningCount;
    }

    /**
     * The number of tasks in the cluster that are currently pending.
     */
    @Output
    public Integer getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Integer pendingCount) {
        this.pendingCount = pendingCount;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.ecs.model.Service model) {
        setCapacityProviderStrategyItem(model.capacityProviderStrategy().stream().map(i -> {
            EcsCapacityProviderStrategyItem item = newSubresource(EcsCapacityProviderStrategyItem.class);
            item.copyFrom(i);

            return item;
        }).collect(Collectors.toList()));

        if (model.deploymentConfiguration() != null) {
            EcsDeploymentConfiguration deploymentConfig = newSubresource(EcsDeploymentConfiguration.class);
            deploymentConfig.copyFrom(model.deploymentConfiguration());
            setDeploymentConfiguration(deploymentConfig);
        }

        if (model.deploymentController() != null) {
            EcsDeploymentController deploymentController = newSubresource(EcsDeploymentController.class);
            deploymentController.copyFrom(model.deploymentController());
            setDeploymentController(deploymentController);
        }

        setLoadBalancer(model.loadBalancers().stream().map(i -> {
            EcsLoadBalancer lb = newSubresource(EcsLoadBalancer.class);
            lb.copyFrom(i);

            return lb;
        }).collect(Collectors.toList()));

        if (model.networkConfiguration() != null) {
            EcsNetworkConfiguration networkConfig = newSubresource(EcsNetworkConfiguration.class);
            networkConfig.copyFrom(model.networkConfiguration());
            setNetworkConfiguration(networkConfig);
        }

        setPlacementConstraint(model.placementConstraints().stream().map(i -> {
            EcsPlacementConstraint pc = newSubresource(EcsPlacementConstraint.class);
            pc.copyFrom(i);

            return pc;
        }).collect(Collectors.toList()));

        setPlacementStrategy(model.placementStrategy().stream().map(i -> {
            EcsPlacementStrategy ps = newSubresource(EcsPlacementStrategy.class);
            ps.copyFrom(i);

            return ps;
        }).collect(Collectors.toList()));

        setServiceRegistries(model.serviceRegistries().stream().map(i -> {
            EcsServiceRegistries sr = newSubresource(EcsServiceRegistries.class);
            sr.copyFrom(i);

            return sr;
        }).collect(Collectors.toList()));

        setDesiredCount(model.desiredCount());
        setEnableEcsManagedTags(model.enableECSManagedTags());
        setHealthCheckGracePeriodSeconds(model.healthCheckGracePeriodSeconds());
        setLaunchType(model.launchType());
        setPlatformVersion(model.platformVersion());
        setPropagateTags(model.propagateTags());
        setRole(findById(RoleResource.class, model.roleArn()));
        setSchedulingStrategy(model.schedulingStrategy());
        setName(model.serviceName());
        setTaskDefinition(findById(EcsTaskDefinitionResource.class, model.taskDefinition()));
        setTags(model.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value)));
        setArn(model.serviceArn());
        setRunningCount(model.runningCount());
        setPendingCount(model.pendingCount());
    }

    @Override
    public boolean refresh() {
        EcsClient client = createClient(EcsClient.class);

        software.amazon.awssdk.services.ecs.model.Service service = getService(client);

        if (service == null) {
            return false;
        }

        copyFrom(service);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        EcsClient client = createClient(EcsClient.class);
        CreateServiceRequest.Builder builder = CreateServiceRequest.builder()
            .cluster(getCluster().getClusterName())
            .serviceName(getName());

        if (getCapacityProviderStrategyItem() != null) {
            builder.capacityProviderStrategy(getCapacityProviderStrategyItem().stream()
                .map(EcsCapacityProviderStrategyItem::copyTo)
                .collect(Collectors.toList()));
        }

        if (getDeploymentConfiguration() != null) {
            builder.deploymentConfiguration(getDeploymentConfiguration().toDeploymentConfiguration());
        }

        if (getDeploymentController() != null) {
            builder.deploymentController(getDeploymentController().toDeploymentController());
        }

        if (getLoadBalancer() != null) {
            builder.loadBalancers(getLoadBalancer().stream()
                .map(EcsLoadBalancer::toLoadBalancer)
                .collect(Collectors.toList()));
        }

        if (getNetworkConfiguration() != null) {
            builder.networkConfiguration(getNetworkConfiguration().toNetworkConfiguration());
        }

        if (getPlacementConstraint() != null) {
            builder.placementConstraints(getPlacementConstraint().stream()
                .map(EcsPlacementConstraint::toPlacementConstraint)
                .collect(Collectors.toList()));
        }

        if (getPlacementStrategy() != null) {
            builder.placementStrategy(getPlacementStrategy().stream()
                .map(EcsPlacementStrategy::toPlacementStrategy)
                .collect(Collectors.toList()));
        }

        if (getServiceRegistries() != null) {
            builder.serviceRegistries(getServiceRegistries().stream()
                .map(EcsServiceRegistries::toServiceRegistry)
                .collect(Collectors.toList()));
        }

        if (getDesiredCount() != null) {
            builder.desiredCount(getDesiredCount());
        }

        if (getEnableEcsManagedTags() != null) {
            builder.enableECSManagedTags(getEnableEcsManagedTags());
        }

        if (getHealthCheckGracePeriodSeconds() != null) {
            builder.healthCheckGracePeriodSeconds(getHealthCheckGracePeriodSeconds());
        }

        if (getRole() != null) {
            builder.role(getRole().getArn());
        }

        if (getLaunchType() != null) {
            builder.launchType(getLaunchType());
        }

        if (getPlatformVersion() != null) {
            builder.platformVersion(getPlatformVersion());
        }

        if (getPropagateTags() != null) {
            builder.propagateTags(getPropagateTags());
        }

        if (getSchedulingStrategy() != null) {
            builder.schedulingStrategy(getSchedulingStrategy());

        }

        if (getTaskDefinition() != null) {
            builder.taskDefinition(getTaskDefinition().getArn());
        }

        if (getTags() != null) {
            builder.tags(getTags().entrySet()
                .stream()
                .map(o -> Tag.builder().key(o.getKey()).value(o.getValue()).build())
                .collect(Collectors.toList()));
        }

        CreateServiceResponse response = client.createService(builder.build());

        copyFrom(response.service());

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.CREATE)
            .prompt(false)
            .until(() -> isActive(client));
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        EcsClient client = createClient(EcsClient.class);

        UpdateServiceRequest.Builder builder = UpdateServiceRequest.builder()
            .cluster(getCluster().getClusterName())
            .service(getName());

        if (changedFieldNames.contains("capacity-provider-strategy-item")) {
            builder.capacityProviderStrategy(getCapacityProviderStrategyItem().stream()
                .map(EcsCapacityProviderStrategyItem::copyTo)
                .collect(Collectors.toList()));
        }

        if (changedFieldNames.contains("deployment-configuration")) {
            builder.deploymentConfiguration(getDeploymentConfiguration().toDeploymentConfiguration());
        }

        if (changedFieldNames.contains("network-configuration")) {
            builder.networkConfiguration(getNetworkConfiguration().toNetworkConfiguration());
        }

        if (changedFieldNames.contains("placement-constraint")) {
            builder.placementConstraints(getPlacementConstraint().stream()
                .map(EcsPlacementConstraint::toPlacementConstraint)
                .collect(Collectors.toList()));
        }

        if (changedFieldNames.contains("placement-strategy")) {
            builder.placementStrategy(getPlacementStrategy().stream()
                .map(EcsPlacementStrategy::toPlacementStrategy)
                .collect(Collectors.toList()));
        }

        if (changedFieldNames.contains("desired-count")) {
            builder.desiredCount(getDesiredCount());
        }

        if (changedFieldNames.contains("health-check-grace-period-seconds")) {
            builder.healthCheckGracePeriodSeconds(getHealthCheckGracePeriodSeconds());
        }

        if (changedFieldNames.contains("platform-version")) {
            builder.platformVersion(getPlatformVersion());
        }

        if (changedFieldNames.contains("task-definition")) {
            builder.taskDefinition(getTaskDefinition().getArn());
        }

        client.updateService(builder.build());

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.UPDATE)
            .prompt(false)
            .until(() -> isActive(client));
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        EcsClient client = createClient(EcsClient.class);

        client.deleteService(s -> s.cluster(getCluster().getClusterName()).service(getName()));

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.DELETE)
            .prompt(false)
            .until(() -> isDeleted(client));
    }

    private software.amazon.awssdk.services.ecs.model.Service getService(EcsClient client) {
        software.amazon.awssdk.services.ecs.model.Service service = null;

        List<software.amazon.awssdk.services.ecs.model.Service> services = client.describeServices(r -> r.cluster(
            getCluster().getClusterName()).services(getArn())).services();

        if (!services.isEmpty()) {
            service = services.get(0);
        }

        return service;
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getDesiredCount() == null && (getSchedulingStrategy() == null || getSchedulingStrategy().equals(
            SchedulingStrategy.REPLICA))) {
            errors.add(new ValidationError(
                this,
                "desired-count",
                "'desired-count' is required if 'scheduling-strategy' is set to 'REPLICA'"));
        }

        if (!configuredFields.contains("task-definition") && (getDeploymentController() == null
            || getDeploymentController().getType().equals(DeploymentControllerType.ECS))) {
            errors.add(new ValidationError(
                this,
                "task-definition",
                "'task-definition' is required if 'deployment-controller' is set to 'ECS'"));
        }

        return errors;
    }

    private boolean isActive(EcsClient client) {
        software.amazon.awssdk.services.ecs.model.Service service = getService(client);

        return (service != null && service.status().equals("ACTIVE"));
    }

    private boolean isDeleted(EcsClient client) {
        software.amazon.awssdk.services.ecs.model.Service service = getService(client);

        return (service == null || service.status().equals("INACTIVE"));
    }
}
