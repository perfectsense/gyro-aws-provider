package gyro.aws.autoscaling;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.ec2.InstanceResource;
import gyro.aws.ec2.LaunchTemplateResource;
import gyro.aws.ec2.SubnetResource;
import gyro.aws.elb.LoadBalancerResource;
import gyro.aws.elbv2.TargetGroupResource;
import gyro.core.GyroException;
import gyro.core.GyroInstance;
import gyro.core.GyroInstances;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StringUtils;
import gyro.core.scope.State;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingException;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsResponse;
import software.amazon.awssdk.services.autoscaling.model.DescribeLifecycleHooksResponse;
import software.amazon.awssdk.services.autoscaling.model.DescribeNotificationConfigurationsResponse;
import software.amazon.awssdk.services.autoscaling.model.DescribePoliciesResponse;
import software.amazon.awssdk.services.autoscaling.model.DescribeScheduledActionsResponse;
import software.amazon.awssdk.services.autoscaling.model.EnabledMetric;
import software.amazon.awssdk.services.autoscaling.model.LaunchTemplateSpecification;
import software.amazon.awssdk.services.autoscaling.model.LifecycleHook;
import software.amazon.awssdk.services.autoscaling.model.NotificationConfiguration;
import software.amazon.awssdk.services.autoscaling.model.ScalingPolicy;
import software.amazon.awssdk.services.autoscaling.model.ScheduledUpdateGroupAction;
import software.amazon.awssdk.services.autoscaling.model.Tag;
import software.amazon.awssdk.services.autoscaling.model.TagDescription;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates an Auto scaling Group from a Launch Configuration or from a Launch Template.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::autoscaling-group auto-scaling-group-example
 *         auto-scaling-group-name: "auto-scaling-group-gyro-1"
 *         launch-configuration: $(aws::launch-configuration launch-configuration-auto-scaling-group-example)
 *         availability-zones: [
 *             $(aws::subnet subnet-auto-scaling-group-example | availability-zone)
 *         ]
 *     end
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::autoscaling-group auto-scaling-group-example
 *         auto-scaling-group-name: "auto-scaling-group-gyro-1"
 *         launch-template: $(aws::launch-template launch-template-auto-scaling-group-example)
 *         availability-zones: [
 *             $(aws::subnet subnet-auto-scaling-group-example | availability-zone),
 *             $(aws::subnet subnet-auto-scaling-group-example-2 | availability-zone)
 *         ]
 *
 *         scaling-policy
 *             policy-name: "Simple-Policy-1"
 *             adjustment-type: "PercentChangeInCapacity"
 *             policy-type: "SimpleScaling"
 *             cooldown: 3000
 *             scaling-adjustment: 5
 *             min-adjustment-magnitude: 3
 *         end
 *
 *         lifecycle-hook
 *             lifecycle-hook-name: "Lifecycle-Hook-1"
 *             default-result: "CONTINUE"
 *             heartbeat-timeout: 300
 *             lifecycle-transition: "autoscaling:EC2_INSTANCE_LAUNCHING"
 *         end
 *
 *         auto-scaling-notification
 *             topic: "arn:aws:sns:us-west-2:242040583208:gyro-instance-state"
 *             notification-type: "autoscaling:EC2_INSTANCE_LAUNCH_ERROR"
 *         end
 *
 *     end
 */
@Type("autoscaling-group")
public class AutoScalingGroupResource extends AwsResource implements GyroInstances, Copyable<AutoScalingGroup> {

    private String autoScalingGroupName;
    private LaunchTemplateResource launchTemplate;
    private Set<String> availabilityZones;
    private Integer maxSize;
    private Integer minSize;
    private Integer desiredCapacity;
    private Integer defaultCooldown;
    private String healthCheckType;
    private Integer healthCheckGracePeriod;
    private LaunchConfigurationResource launchConfiguration;
    private Boolean newInstancesProtectedFromScaleIn;
    private Set<SubnetResource> subnets;
    private String arn;
    private Boolean enableMetricsCollection;
    private Set<String> disabledMetrics;
    private Map<String, String> tags;
    private Set<String> propagateAtLaunchTags;
    private String serviceLinkedRoleArn;
    private String placementGroup;
    private InstanceResource instance;
    private Set<LoadBalancerResource> classicLoadBalancers;
    private Set<TargetGroupResource> targetGroups;
    private Set<String> terminationPolicies;
    private String status;
    private Date createdTime;
    private Set<AutoScalingPolicyResource> scalingPolicy;
    private Set<AutoScalingGroupLifecycleHookResource> lifecycleHook;
    private Set<AutoScalingGroupScheduledActionResource> scheduledAction;
    private Set<AutoScalingGroupNotificationResource> autoScalingNotification;
    private int actualDesiredCapacity;

    private final Set<String> MASTER_METRIC_SET = new HashSet<>(Arrays.asList(
        "GroupMinSize",
        "GroupMaxSize",
        "GroupDesiredCapacity",
        "GroupInServiceInstances",
        "GroupPendingInstances",
        "GroupStandbyInstances",
        "GroupTerminatingInstances",
        "GroupTotalInstances"
        ));

    /**
     * The name of the Auto Scaling Group, also serves as its identifier and thus unique. (Required)
     */
    @Id
    public String getAutoScalingGroupName() {
        return autoScalingGroupName;
    }

    public void setAutoScalingGroupName(String autoScalingGroupName) {
        this.autoScalingGroupName = autoScalingGroupName;
    }

    /**
     * The ID of an launched template that would be used as a skeleton to create the Auto scaling group. Required if launch configuration name not provided.
     */
    @Updatable
    public LaunchTemplateResource getLaunchTemplate() {
        return launchTemplate;
    }

    public void setLaunchTemplate(LaunchTemplateResource launchTemplate) {
        this.launchTemplate = launchTemplate;
    }

    /**
     *  A set of availability zones for the Auto Scaling group to be active in. See `Distributing Instances Across Availability Zones <https://docs.aws.amazon.com/autoscaling/ec2/userguide/auto-scaling-benefits.html#arch-AutoScalingMultiAZ/>`_. (Required)
     */
    @Updatable
    public Set<String> getAvailabilityZones() {
        if (availabilityZones == null) {
            availabilityZones = new HashSet<>();
        }

        return availabilityZones;
    }

    public void setAvailabilityZones(Set<String> availabilityZones) {
        this.availabilityZones = availabilityZones;
    }

    /**
     * The maximum number of instances for the Auto Scaling group. (Required)
     */
    @Updatable
    public Integer getMaxSize() {
        if (maxSize == null) {
            maxSize = 0;
        }

        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * The minimum number of instances for the Auto Scaling group. (Required)
     */
    @Updatable
    public Integer getMinSize() {
        if (minSize == null) {
            minSize = 0;
        }

        return minSize;
    }

    public void setMinSize(Integer minSize) {
        this.minSize = minSize;
    }

    /**
     * The desired number of instances for the Auto Scaling group. (Required)
     */
    @Updatable
    public Integer getDesiredCapacity() {
        return desiredCapacity;
    }

    public void setDesiredCapacity(Integer desiredCapacity) {
        this.desiredCapacity = desiredCapacity;
    }

    /**
     * The default cool down period in sec for the Auto Scaling group. Defaults to 300 sec. See `Default Cool downs <https://docs.aws.amazon.com/autoscaling/ec2/userguide/Cooldown.html#cooldown-default/>`_.
     */
    @Updatable
    public Integer getDefaultCooldown() {
        if (defaultCooldown == null) {
            defaultCooldown = 300;
        }

        return defaultCooldown;
    }

    public void setDefaultCooldown(Integer defaultCooldown) {
        this.defaultCooldown = defaultCooldown;
    }

    /**
     * The type of health check to be performed on the Auto Scaling group. Defaults to EC2. Can be 'EC2' or 'ELB'. See `Health Checks for Auto Scaling Instances <https://docs.aws.amazon.com/autoscaling/ec2/userguide/healthcheck.html/>`_.
     */
    @Updatable
    public String getHealthCheckType() {
        if (healthCheckType == null) {
            healthCheckType = "EC2";
        }

        return healthCheckType.toUpperCase();
    }

    public void setHealthCheckType(String healthCheckType) {
        this.healthCheckType = healthCheckType;
    }

    /**
     * The grace period after which health check is started, to give time for the Instances in the Auto scaling group to start up. Defaults to 0 sec. See `Health Checks for Auto Scaling Instances <https://docs.aws.amazon.com/autoscaling/ec2/userguide/healthcheck.html/>`_.
     */
    @Updatable
    public Integer getHealthCheckGracePeriod() {
        if (healthCheckGracePeriod == null) {
            healthCheckGracePeriod = 0;
        }

        return healthCheckGracePeriod;
    }

    public void setHealthCheckGracePeriod(Integer healthCheckGracePeriod) {
        this.healthCheckGracePeriod = healthCheckGracePeriod;
    }

    /**
     * The name of a launched configuration that would be used as a skeleton to create the Auto scaling group. Required if launch template Id is not provided.
     */
    @Updatable
    public LaunchConfigurationResource getLaunchConfiguration() {
        return launchConfiguration;
    }

    public void setLaunchConfiguration(LaunchConfigurationResource launchConfiguration) {
        this.launchConfiguration = launchConfiguration;
    }

    /**
     * Enable protection of instances from Auto Scale Group scale in. Defaults to false. see `Controlling Which Auto Scaling Instances Terminate During Scale In <https://docs.aws.amazon.com/autoscaling/ec2/userguide/as-instance-termination.html/>`_.
     */
    @Updatable
    public Boolean getNewInstancesProtectedFromScaleIn() {
        if (newInstancesProtectedFromScaleIn == null) {
            newInstancesProtectedFromScaleIn = false;
        }

        return newInstancesProtectedFromScaleIn;
    }

    public void setNewInstancesProtectedFromScaleIn(Boolean newInstancesProtectedFromScaleIn) {
        this.newInstancesProtectedFromScaleIn = newInstancesProtectedFromScaleIn;
    }

    /**
     * A list of subnet's. If Availability Zone is provided, subnet's need to be part of that. See `Launching Auto Scaling Instances in a VPC <https://docs.aws.amazon.com/autoscaling/ec2/userguide/asg-in-vpc.html/>`_.
     */
    @Updatable
    public Set<SubnetResource> getSubnets() {
        if (subnets == null) {
            subnets = new HashSet<>();
        }

        return subnets;
    }

    public void setSubnets(Set<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    /**
     * Enable/Disable cloud watch metrics for your Auto Scaling Group. Defaults to false. See `Monitoring your Auto Scaling Groups <https://docs.aws.amazon.com/autoscaling/ec2/userguide/as-instance-monitoring.html/>`_.
     */
    @Updatable
    public Boolean getEnableMetricsCollection() {
        if (enableMetricsCollection == null) {
            enableMetricsCollection = false;
        }

        return enableMetricsCollection;
    }

    public void setEnableMetricsCollection(Boolean enableMetricsCollection) {
        this.enableMetricsCollection = enableMetricsCollection;
    }

    /**
     * One or more names of cloud watch metrics you want to disable for the Auto Scaling Group. See `Cloud watch metrics <https://docs.aws.amazon.com/autoscaling/ec2/userguide/as-instance-monitoring.html#as-view-group-metrics/>`_.
     */
    @Updatable
    public Set<String> getDisabledMetrics() {
        if (disabledMetrics == null || disabledMetrics.isEmpty()) {
            disabledMetrics = new HashSet<>();
        }

        return disabledMetrics;
    }

    public void setDisabledMetrics(Set<String> disabledMetrics) {
        this.disabledMetrics = disabledMetrics;
    }

    /**
     * Tags for Auto Scaling Groups. See `Tagging Auto Scaling Groups and Instances <https://docs.aws.amazon.com/autoscaling/ec2/userguide/autoscaling-tagging.html/>`_.
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
     * Tags in Auto Scaling Groups that you want instances to have as well. See `Tagging Auto Scaling Groups and Instances <https://docs.aws.amazon.com/autoscaling/ec2/userguide/autoscaling-tagging.html/>`
     */
    @Updatable
    public Set<String> getPropagateAtLaunchTags() {
        if (propagateAtLaunchTags == null) {
            propagateAtLaunchTags = new HashSet<>();
        }
        return propagateAtLaunchTags;
    }

    public void setPropagateAtLaunchTags(Set<String> propagateAtLaunchTags) {
        this.propagateAtLaunchTags = propagateAtLaunchTags;
    }

    /**
     * The Amazon Resource Name (ARN) of the service-linked role that the Auto Scaling Group uses to call other AWS services.
     */
    public String getServiceLinkedRoleArn() {
        return serviceLinkedRoleArn;
    }

    public void setServiceLinkedRoleArn(String serviceLinkedRoleArn) {
        this.serviceLinkedRoleArn = serviceLinkedRoleArn;
    }

    /**
     * The name of the placement group into which to launch the instances for the Auto Scaling Group.
     */
    public String getPlacementGroup() {
        return placementGroup;
    }

    public void setPlacementGroup(String placementGroup) {
        this.placementGroup = placementGroup;
    }

    /**
     * The Instance used to create a launch configuration for the Auto Scaling Group.
     */
    public InstanceResource getInstance() {
        return instance;
    }

    public void setInstance(InstanceResource instance) {
        this.instance = instance;
    }

    /**
     * A set of classic load balancer's to be attached to the Auto Scaling Group.
     */
    @Updatable
    public Set<LoadBalancerResource> getClassicLoadBalancers() {
        if (classicLoadBalancers == null) {
            classicLoadBalancers = new HashSet<>();
        }

        return classicLoadBalancers;
    }

    public void setClassicLoadBalancers(Set<LoadBalancerResource> classicLoadBalancers) {
        this.classicLoadBalancers = classicLoadBalancers;
    }

    /**
     * A set of target groups for the Auto Scaling Group.
     */
    @Updatable
    public Set<TargetGroupResource> getTargetGroups() {
        if (targetGroups == null) {
            targetGroups = new HashSet<>();
        }

        return targetGroups;
    }

    public void setTargetGroups(Set<TargetGroupResource> targetGroups) {
        this.targetGroups = targetGroups;
    }

    /**
     * A set of termination policies for the Auto Scaling Group.
     */
    @Updatable
    public Set<String> getTerminationPolicies() {
        if (terminationPolicies == null || terminationPolicies.isEmpty()) {
            terminationPolicies = new HashSet<>();
            terminationPolicies.add("Default");
        }

        return terminationPolicies;
    }

    public void setTerminationPolicies(Set<String> terminationPolicies) {
        this.terminationPolicies = terminationPolicies;
    }

    /**
     * A set of policies to trigger for the Auto Scaling Group.
     *
     * @subresource gyro.aws.autoscaling.AutoScalingPolicyResource
     */
    public Set<AutoScalingPolicyResource> getScalingPolicy() {
        if (scalingPolicy == null) {
            scalingPolicy = new HashSet<>();
        }

        return scalingPolicy;
    }

    public void setScalingPolicy(Set<AutoScalingPolicyResource> scalingPolicy) {
        this.scalingPolicy = scalingPolicy;
    }

    /**
     * A set of Life cycle hooks for the Auto Scaling Group.
     *
     * @subresource gyro.aws.autoscaling.AutoScalingGroupLifecycleHookResource
     */
    public Set<AutoScalingGroupLifecycleHookResource> getLifecycleHook() {
        if (lifecycleHook == null) {
            lifecycleHook = new HashSet<>();
        }

        return lifecycleHook;
    }

    public void setLifecycleHook(Set<AutoScalingGroupLifecycleHookResource> lifecycleHook) {
        this.lifecycleHook = lifecycleHook;
    }

    /**
     * A set of scheduled actions for the Auto Scaling Group.
     *
     * @subresource gyro.aws.autoscaling.AutoScalingGroupScheduledActionResource
     */
    public Set<AutoScalingGroupScheduledActionResource> getScheduledAction() {
        if (scheduledAction == null) {
            scheduledAction = new HashSet<>();
        }

        return scheduledAction;
    }

    public void setScheduledAction(Set<AutoScalingGroupScheduledActionResource> scheduledAction) {
        this.scheduledAction = scheduledAction;
    }

    /**
     * A set of notifications for the Auto Scaling Group.
     *
     * @subresource gyro.aws.autoscaling.AutoScalingGroupNotificationResource
     */
    public Set<AutoScalingGroupNotificationResource> getAutoScalingNotification() {
        if (autoScalingNotification == null) {
            autoScalingNotification = new HashSet<>();
        }

        return autoScalingNotification;
    }

    public void setAutoScalingNotification(Set<AutoScalingGroupNotificationResource> autoScalingNotification) {
        this.autoScalingNotification = autoScalingNotification;
    }

    /**
     * The ARN of the Auto Scaling Group.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The status of the Auto Scaling Group.
     */
    @Output
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * The create time of the Auto Scaling Group.
     */
    @Output
    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public void copyFrom(AutoScalingGroup autoScalingGroup) {
        setArn(autoScalingGroup.autoScalingGroupARN());
        setMaxSize(autoScalingGroup.maxSize());
        setMinSize(autoScalingGroup.minSize());
        setAvailabilityZones(new HashSet<>(autoScalingGroup.availabilityZones()));
        setDesiredCapacity(autoScalingGroup.desiredCapacity());
        actualDesiredCapacity = autoScalingGroup.desiredCapacity();
        setDefaultCooldown(autoScalingGroup.defaultCooldown());
        setHealthCheckType(autoScalingGroup.healthCheckType());
        setHealthCheckGracePeriod(autoScalingGroup.healthCheckGracePeriod());
        setNewInstancesProtectedFromScaleIn(autoScalingGroup.newInstancesProtectedFromScaleIn());
        setServiceLinkedRoleArn(autoScalingGroup.serviceLinkedRoleARN());
        setPlacementGroup(autoScalingGroup.placementGroup());
        setStatus(autoScalingGroup.status());
        setCreatedTime(Date.from(autoScalingGroup.createdTime()));
        setTerminationPolicies(new HashSet<>(autoScalingGroup.terminationPolicies()));
        setAutoScalingGroupName(autoScalingGroup.autoScalingGroupName());

        setLaunchTemplate(autoScalingGroup.launchTemplate() == null
            ? null
            : findById(LaunchTemplateResource.class, autoScalingGroup.launchTemplate().launchTemplateId()));

        setLaunchConfiguration(ObjectUtils.isBlank(autoScalingGroup.launchConfigurationName())
            ? null
            : findById(LaunchConfigurationResource.class, autoScalingGroup.launchConfigurationName()));

        setSubnets(autoScalingGroup.vpcZoneIdentifier().equals("")
            ? new HashSet<>()
            : new HashSet<>(Arrays.stream(autoScalingGroup.vpcZoneIdentifier()
            .split(","))
            .map(o -> findById(SubnetResource.class, o))
            .collect(Collectors.toSet())));

        setClassicLoadBalancers(
            (autoScalingGroup.loadBalancerNames() != null && !autoScalingGroup.loadBalancerNames().isEmpty())
                ? autoScalingGroup.loadBalancerNames().stream()
                .map(o -> findById(LoadBalancerResource.class, o))
                .collect(Collectors.toSet())
                : null);
        setTargetGroups(
            (autoScalingGroup.targetGroupARNs() != null && !autoScalingGroup.targetGroupARNs().isEmpty())
                ? autoScalingGroup.targetGroupARNs().stream()
                .map(o -> findById(TargetGroupResource.class, o))
                .collect(Collectors.toSet())
                : null);

        loadMetrics(autoScalingGroup.enabledMetrics());

        loadTags(autoScalingGroup.tags());

        AutoScalingClient client = createClient(AutoScalingClient.class);

        loadScalingPolicy(client);

        loadLifecycleHook(client);

        loadScheduledAction(client);

        loadNotification(client);
    }

    @Override
    public boolean refresh() {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        AutoScalingGroup autoScalingGroup = getAutoScalingGroup(client);

        if (autoScalingGroup == null) {
            return false;
        }

        boolean isDesiredCapacitySet = getDesiredCapacity() != null;

        copyFrom(autoScalingGroup);

        if (!isDesiredCapacitySet) {
            setDesiredCapacity(null);
        }

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        validate();

        client.createAutoScalingGroup(
            r -> r.autoScalingGroupName(getAutoScalingGroupName())
                .maxSize(getMaxSize())
                .minSize(getMinSize())
                .availabilityZones(getAvailabilityZones().isEmpty() ? null : getAvailabilityZones())
                .desiredCapacity(getDesiredCapacity())
                .defaultCooldown(getDefaultCooldown())
                .healthCheckType(getHealthCheckType())
                .healthCheckGracePeriod(getHealthCheckGracePeriod())
                .launchConfigurationName(getLaunchConfiguration() != null ? getLaunchConfiguration().getLaunchConfigurationName() : null)
                .newInstancesProtectedFromScaleIn(getNewInstancesProtectedFromScaleIn())
                .vpcZoneIdentifier(getSubnets().isEmpty() ? " " : StringUtils.join(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList()), ","))
                .launchTemplate(
                    LaunchTemplateSpecification.builder()
                        .launchTemplateId(getLaunchTemplate() != null ? getLaunchTemplate().getId() : null)
                        .build()
                )
                .tags(getAutoScaleGroupTags(getTags(), getPropagateAtLaunchTags()))
                .serviceLinkedRoleARN(getServiceLinkedRoleArn())
                .placementGroup(getPlacementGroup())
                .loadBalancerNames(getClassicLoadBalancers().stream().map(LoadBalancerResource::getLoadBalancerName).collect(Collectors.toList()))
                .targetGroupARNs(getTargetGroups().stream().map(TargetGroupResource::getArn).collect(Collectors.toList()))
                .instanceId(getInstance() != null ? getInstance().getId() : null)
                .terminationPolicies(getTerminationPolicies())
        );

        AutoScalingGroup autoScalingGroup = getAutoScalingGroup(client);

        if (autoScalingGroup != null) {
            setArn(autoScalingGroup.autoScalingGroupARN());
        }

        if (getEnableMetricsCollection()) {
            saveMetrics(client);
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        validate();

        client.updateAutoScalingGroup(
            r -> r.autoScalingGroupName(getAutoScalingGroupName())
                .launchTemplate(
                    LaunchTemplateSpecification.builder()
                        .launchTemplateId(getLaunchTemplate() != null ? getLaunchTemplate().getId() : null)
                        .build()
                )
                .maxSize(getMaxSize())
                .minSize(getMinSize())
                .availabilityZones(getAvailabilityZones().isEmpty() ? null : getAvailabilityZones())
                .desiredCapacity(getDesiredCapacity() != null ? getDesiredCapacity() : getCalculatedDesiredCapacity(((AutoScalingGroupResource) current).actualDesiredCapacity))
                .defaultCooldown(getDefaultCooldown())
                .healthCheckType(getHealthCheckType())
                .healthCheckGracePeriod(getHealthCheckGracePeriod())
                .launchConfigurationName(getLaunchConfiguration() != null ? getLaunchConfiguration().getLaunchConfigurationName() : null)
                .newInstancesProtectedFromScaleIn(getNewInstancesProtectedFromScaleIn())
                .vpcZoneIdentifier(getSubnets().isEmpty() ? " " : StringUtils.join(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList()), ","))
                .terminationPolicies(getTerminationPolicies())
        );

        if (changedFieldNames.contains("enable-metrics-collection") || changedFieldNames.contains("disabled-metrics")) {
            if (getEnableMetricsCollection()) {
                saveMetrics(client);
            } else {
                client.disableMetricsCollection(
                    r -> r.autoScalingGroupName(getAutoScalingGroupName())
                );
            }
        }

        AutoScalingGroupResource oldResource = (AutoScalingGroupResource) current;

        if (changedFieldNames.contains("tags") || changedFieldNames.contains("propagate-at-launch-tags")) {
            if (!getTags().isEmpty()) {
                saveTags(client, getTags(), getPropagateAtLaunchTags(), false);

                removeStaleTags(client, oldResource);
            } else {
                saveTags(client, oldResource.getTags(), oldResource.getPropagateAtLaunchTags(), true);
            }
        }

        if (changedFieldNames.contains("classic-load-balancers")) {
            saveLoadBalancerNames(client, oldResource.getClassicLoadBalancers());
        }

        if (changedFieldNames.contains("target-groups")) {
            saveTargetGroupArns(client, oldResource.getTargetGroups());
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        // have option of graceful delete with configurable timeouts.
        client.deleteAutoScalingGroup(r -> r.autoScalingGroupName(getAutoScalingGroupName()).forceDelete(true));
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("auto scaling group");

        if (!ObjectUtils.isBlank(getAutoScalingGroupName())) {
            sb.append(" - ").append(getAutoScalingGroupName());

        }

        return sb.toString();
    }

    @Override
    public List<GyroInstance> getInstances() {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        DescribeAutoScalingGroupsResponse response = client.describeAutoScalingGroups(r -> r.autoScalingGroupNames(getAutoScalingGroupName()));
        AutoScalingGroup group = response.autoScalingGroups().size() == 1 ? response.autoScalingGroups().get(0) : null;
        if (group == null) {
            throw new GyroException("Unable to load autoscaling group: " + getAutoScalingGroupName());
        }

        List<String> instanceIds = group.instances()
            .stream()
            .map(software.amazon.awssdk.services.autoscaling.model.Instance::instanceId)
            .collect(Collectors.toList());

        Ec2Client ec2Client = createClient(Ec2Client.class);
        DescribeInstancesResponse instancesResponse = ec2Client.describeInstances(r -> r.instanceIds(instanceIds));

        List<GyroInstance> instances = new ArrayList<>();
        for (Reservation reservation : instancesResponse.reservations()) {
            instances.addAll(reservation.instances()
                .stream()
                .map(this::getGyroInstance)
                .collect(Collectors.toList()));
        }

        return instances;
    }

    private GyroInstance getGyroInstance(Instance instance) {
        InstanceResource instanceResource = newSubresource(InstanceResource.class);
        instanceResource.copyFrom(instance);
        return instanceResource;
    }

    private List<Tag> getAutoScaleGroupTags(Map<String, String> localTags, Set<String> passToInstanceTags) {
        List<Tag> tags = new ArrayList<>();

        for (String key : localTags.keySet()) {
            tags.add(
                Tag.builder()
                    .key(key)
                    .value(localTags.get(key))
                    .propagateAtLaunch(passToInstanceTags.contains(key))
                    .resourceId(getAutoScalingGroupName())
                    .resourceType("auto-scaling-group")
                    .build()
            );
        }

        return tags;
    }

    private void loadTags(List<TagDescription> tags) {
        getTags().clear();
        getPropagateAtLaunchTags().clear();

        for (TagDescription tag : tags) {
            getTags().put(tag.key(), tag.value());

            if (tag.propagateAtLaunch()) {
                getPropagateAtLaunchTags().add(tag.key());
            }
        }
    }

    private void saveTags(AutoScalingClient client, Map<String, String> localTags, Set<String> passToInstanceTags, boolean isDelete) {
        List<Tag> tags = getAutoScaleGroupTags(localTags, passToInstanceTags);

        if (!tags.isEmpty()) {

            if (!isDelete) {
                client.createOrUpdateTags(
                    r -> r.tags(tags)
                );
            } else {
                client.deleteTags(
                    r -> r.tags(tags)
                );
            }
        }
    }

    private AutoScalingGroup getAutoScalingGroup(AutoScalingClient client) {
        if (ObjectUtils.isBlank(getAutoScalingGroupName())) {
            throw new GyroException("auto-scale-group-name is missing, unable to load auto scale group.");
        }

        try {
            DescribeAutoScalingGroupsResponse response = client.describeAutoScalingGroups(
                r -> r.autoScalingGroupNames(Collections.singleton(getAutoScalingGroupName()))
            );

            if (response.autoScalingGroups().isEmpty()) {
                return null;
            }

            return response.autoScalingGroups().get(0);
        } catch (AutoScalingException ex) {
            if (ex.getLocalizedMessage().contains("does not exist")) {
                return null;
            }

            throw ex;
        }
    }

    private void removeStaleTags(AutoScalingClient client, AutoScalingGroupResource oldResource) {
        Map<String, String> staleTags = new HashMap<>();
        List<String> staleKeys = oldResource.getTags().keySet().stream()
            .filter(o -> !getTags().keySet().contains(o))
            .collect(Collectors.toList());
        for (String key : staleKeys) {
            staleTags.put(key, oldResource.getTags().get(key));
        }
        saveTags(client, staleTags, oldResource.getPropagateAtLaunchTags(), true);
    }

    private void loadMetrics(List<EnabledMetric> enabledMetrics) {
        setEnableMetricsCollection(!enabledMetrics.isEmpty());
        Set<String> allMetrics = new HashSet<>(MASTER_METRIC_SET);
        allMetrics.removeAll(enabledMetrics.stream().map(EnabledMetric::metric).collect(Collectors.toSet()));
        setDisabledMetrics(allMetrics.size() == MASTER_METRIC_SET.size() ? new HashSet<>() : new HashSet<>(allMetrics));
    }

    private void saveMetrics(AutoScalingClient client) {
        Set<String> metrics = new HashSet<>(MASTER_METRIC_SET);
        metrics.removeAll(getDisabledMetrics());

        client.enableMetricsCollection(
            r -> r.autoScalingGroupName(getAutoScalingGroupName())
                .granularity("1Minute")
                .metrics(metrics)
        );

        if (!getDisabledMetrics().isEmpty()) {
            client.disableMetricsCollection(
                r -> r.autoScalingGroupName(getAutoScalingGroupName())
                    .metrics(getDisabledMetrics()));
        }
    }

    private void loadScalingPolicy(AutoScalingClient client) {
        getScalingPolicy().clear();

        DescribePoliciesResponse policyResponse = client.describePolicies(r -> r.autoScalingGroupName(getAutoScalingGroupName()));

        for (ScalingPolicy scalingPolicy : policyResponse.scalingPolicies()) {
            AutoScalingPolicyResource autoScalingPolicyResource = newSubresource(AutoScalingPolicyResource.class);
            autoScalingPolicyResource.copyFrom(scalingPolicy);
            getScalingPolicy().add(autoScalingPolicyResource);
        }
    }

    private void loadLifecycleHook(AutoScalingClient client) {
        getLifecycleHook().clear();

        DescribeLifecycleHooksResponse lifecycleHooksResponse = client.describeLifecycleHooks(r -> r.autoScalingGroupName(getAutoScalingGroupName()));

        for (LifecycleHook lifecycleHook : lifecycleHooksResponse.lifecycleHooks()) {
            AutoScalingGroupLifecycleHookResource lifecycleHookResource = newSubresource(AutoScalingGroupLifecycleHookResource.class);
            lifecycleHookResource.copyFrom(lifecycleHook);
            getLifecycleHook().add(lifecycleHookResource);
        }
    }

    private void loadScheduledAction(AutoScalingClient client) {
        getScheduledAction().clear();

        DescribeScheduledActionsResponse scheduledActionsResponse = client.describeScheduledActions(
            r -> r.autoScalingGroupName(getAutoScalingGroupName())
        );

        for (ScheduledUpdateGroupAction scheduledUpdateGroupAction : scheduledActionsResponse.scheduledUpdateGroupActions()) {
            AutoScalingGroupScheduledActionResource scheduledActionResource = newSubresource(AutoScalingGroupScheduledActionResource.class);
            scheduledActionResource.copyFrom(scheduledUpdateGroupAction);
            getScheduledAction().add(scheduledActionResource);
        }
    }

    private void loadNotification(AutoScalingClient client) {
        getAutoScalingNotification().clear();

        DescribeNotificationConfigurationsResponse notificationResponse = client.describeNotificationConfigurations(
            r -> r.autoScalingGroupNames(Collections.singletonList(getAutoScalingGroupName()))
        );

        for (NotificationConfiguration notificationConfiguration : notificationResponse.notificationConfigurations()) {
            AutoScalingGroupNotificationResource notificationResource = newSubresource(AutoScalingGroupNotificationResource.class);
            notificationResource.copyFrom(notificationConfiguration);
            getAutoScalingNotification().add(notificationResource);
        }
    }

    private void saveLoadBalancerNames(AutoScalingClient client, Set<LoadBalancerResource> oldLoadBalancers) {
        Set<String> removeLoadBalancerNames = oldLoadBalancers.stream()
            .map(LoadBalancerResource::getLoadBalancerName)
            .collect(Collectors.toSet());

        removeLoadBalancerNames.removeAll(getClassicLoadBalancers().stream()
            .map(LoadBalancerResource::getLoadBalancerName)
            .collect(Collectors.toSet()));

        if (!removeLoadBalancerNames.isEmpty()) {
            client.detachLoadBalancers(
                r -> r.autoScalingGroupName(getAutoScalingGroupName()).loadBalancerNames(removeLoadBalancerNames)
            );
        }

        Set<String> addLoadbalancerNames = getClassicLoadBalancers().stream()
            .map(LoadBalancerResource::getLoadBalancerName)
            .collect(Collectors.toSet());

        addLoadbalancerNames.removeAll(oldLoadBalancers.stream()
            .map(LoadBalancerResource::getLoadBalancerName)
            .collect(Collectors.toSet()));

        if (!addLoadbalancerNames.isEmpty()) {
            client.attachLoadBalancers(
                r -> r.autoScalingGroupName(getAutoScalingGroupName()).loadBalancerNames(addLoadbalancerNames)
            );
        }
    }

    private void saveTargetGroupArns(AutoScalingClient client, Set<TargetGroupResource> oldTargetGroups) {
        Set<String> removeTargetGroupArns = oldTargetGroups.stream().map(TargetGroupResource::getArn).collect(Collectors.toSet());

        removeTargetGroupArns.removeAll(getTargetGroups().stream().map(TargetGroupResource::getArn).collect(Collectors.toSet()));

        if (!removeTargetGroupArns.isEmpty()) {
            client.detachLoadBalancerTargetGroups(
                r -> r.autoScalingGroupName(getAutoScalingGroupName()).targetGroupARNs(removeTargetGroupArns)
            );
        }

        Set<String> addTargetGroupArns = getTargetGroups().stream().map(TargetGroupResource::getArn).collect(Collectors.toSet());

        addTargetGroupArns.removeAll(oldTargetGroups.stream().map(TargetGroupResource::getArn).collect(Collectors.toSet()));

        if (!addTargetGroupArns.isEmpty()) {
            client.attachLoadBalancerTargetGroups(
                r -> r.autoScalingGroupName(getAutoScalingGroupName()).targetGroupARNs(addTargetGroupArns)
            );
        }
    }

    private Integer getCalculatedDesiredCapacity(int actualDesiredCapacity) {
        Integer calculatedDesiredCapacity;

        if (getMaxSize() != null && actualDesiredCapacity > getMaxSize()) { // if actual more than the pending max
            calculatedDesiredCapacity = getMaxSize();
        } else if (getMinSize() != null && actualDesiredCapacity < getMinSize()) { // if actual less than the pending min
            calculatedDesiredCapacity = getMinSize();
        } else {
            calculatedDesiredCapacity = actualDesiredCapacity;
        }

        return calculatedDesiredCapacity;
    }

    private void validate() {
        if (getLaunchTemplate() == null && getLaunchConfiguration() == null && getInstance() == null) {
            throw new GyroException("Either 'launch-template' or 'launch-configuration' or 'instance' is required.");
        }

        if (!getHealthCheckType().equals("ELB") && !getHealthCheckType().equals("EC2")) {
            throw new GyroException("The value - (" + getHealthCheckType()
                + ") is invalid for parameter 'health-check-type'.");
        }

        if (getHealthCheckGracePeriod() < 0) {
            throw new GyroException("The value - (" + getHealthCheckGracePeriod()
                + ") is invalid for parameter 'health-check-grace-period'. Integer value greater or equal to 0.");
        }

        if (getDefaultCooldown() < 0) {
            throw new GyroException("The value - (" + getDefaultCooldown()
                + ") is invalid for parameter 'default-cooldown'. Integer value greater or equal to 0.");
        }

        if (getMaxSize() < 0) {
            throw new GyroException("The value - (" + getMaxSize()
                + ") is invalid for parameter 'max-size'. Integer value greater or equal to 0.");
        }

        if (getMinSize() < 0) {
            throw new GyroException("The value - (" + getMinSize()
                + ") is invalid for parameter 'min-size'. Integer value greater or equal to 0.");
        }

        if (getMinSize() > getMaxSize()) {
            throw new GyroException("The value - (" + getMinSize()
                + ") is invalid for parameter 'min-size'. Integer value less or equal to 'max-size'.");
        }

        if (getDesiredCapacity() != null && (getDesiredCapacity() < getMinSize() || getDesiredCapacity() > getMaxSize())) {
            throw new GyroException("The value - (" + getDesiredCapacity()
                + ") is invalid for parameter 'desired-capacity'. Integer value between the 'min-size' and 'max-size'.");
        }

        if (!getEnableMetricsCollection() && !getDisabledMetrics().isEmpty()) {
            throw new GyroException("When 'enabled-metrics-collection' is set to false, 'disabled-metrics' can't have items in it.");
        }

        if (!MASTER_METRIC_SET.containsAll(getDisabledMetrics())) {
            throw new GyroException("Invalid values for parameter 'disabled-metrics'.");
        }

        if (!new HashSet<>(getTags().keySet()).containsAll(getPropagateAtLaunchTags())) {
            throw new GyroException("'propagate-at-launch-tags' cannot contain keys not mentioned under 'tags'.");
        }
    }
}
