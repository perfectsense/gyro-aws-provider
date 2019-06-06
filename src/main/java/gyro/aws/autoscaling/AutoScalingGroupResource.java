package gyro.aws.autoscaling;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.ec2.InstanceResource;
import gyro.aws.ec2.LaunchTemplateResource;
import gyro.aws.ec2.SubnetResource;
import gyro.aws.elbv2.LoadBalancerResource;
import gyro.core.GyroException;
import gyro.core.GyroInstance;
import gyro.core.GyroInstances;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StringUtils;
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
 *     aws::auto-scaling-group auto-scaling-group-example
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
 *     aws::auto-scaling-group auto-scaling-group-example
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
 *             topic-arn: "arn:aws:sns:us-west-2:242040583208:gyro-instance-state"
 *             notification-types: [
 *                 "autoscaling:EC2_INSTANCE_LAUNCH_ERROR"
 *             ]
 *         end
 *
 *     end
 */
@Type("auto-scaling-group")
public class AutoScalingGroupResource extends AwsResource implements GyroInstances, Copyable<AutoScalingGroup> {

    private String autoScalingGroupName;
    private LaunchTemplateResource launchTemplate;
    private List<String> availabilityZones;
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
    private List<String> disabledMetrics;
    private Map<String, String> tags;
    private List<String> propagateAtLaunchTags;
    private String serviceLinkedRoleArn;
    private String placementGroup;
    private InstanceResource instance;
    private Set<gyro.aws.elb.LoadBalancerResource> classicLoadBalancers;
    private Set<LoadBalancerResource> loadBalancers;
    private List<String> terminationPolicies;
    private String status;
    private Date createdTime;
    private List<AutoScalingPolicyResource> scalingPolicy;
    private List<AutoScalingGroupLifecycleHookResource> lifecycleHook;
    private List<AutoScalingGroupScheduledActionResource> scheduledAction;
    private List<AutoScalingGroupNotificationResource> autoScalingNotification;

    private final Set<String> masterMetricSet = new HashSet<>(Arrays.asList(
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
     * The name of the auto scaling group, also served as its identifier and thus unique. (Required)
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
     *  A list of availability zones for the auto scale group to be active in. See `Distributing Instances Across Availability Zones <https://docs.aws.amazon.com/autoscaling/ec2/userguide/auto-scaling-benefits.html#arch-AutoScalingMultiAZ/>`_. (Required)
     */
    @Updatable
    public List<String> getAvailabilityZones() {
        if (availabilityZones == null) {
            availabilityZones = new ArrayList<>();
        }

        return availabilityZones;
    }

    public void setAvailabilityZones(List<String> availabilityZones) {
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
        if (desiredCapacity == null) {
            desiredCapacity = 0;
        }

        return desiredCapacity;
    }

    public void setDesiredCapacity(Integer desiredCapacity) {
        this.desiredCapacity = desiredCapacity;
    }

    /**
     * The default cool down period in sec for the auto scale group. Defaults to 300 sec. See `Default Cool downs <https://docs.aws.amazon.com/autoscaling/ec2/userguide/Cooldown.html#cooldown-default/>`_.
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
     * The type of health check to be performed on the auto scale group. Defaults to EC2. Can be 'EC2' or 'ELB'. See `Health Checks for Auto Scaling Instances <https://docs.aws.amazon.com/autoscaling/ec2/userguide/healthcheck.html/>`_.
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
     * The grace period after which health check is started, to give time for the instances to start up. Defaults to 0 sec. See `Health Checks for Auto Scaling Instances <https://docs.aws.amazon.com/autoscaling/ec2/userguide/healthcheck.html/>`_.
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
     * Enable protection of instances from auto scale group scale in. Defaults to false. see `Controlling Which Auto Scaling Instances Terminate During Scale In <https://docs.aws.amazon.com/autoscaling/ec2/userguide/as-instance-termination.html/>`_.
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
     * The anr of the auto scaling group.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * Enable/Disable cloud watch metrics for your auto scaling group. Defaults to false. See `Monitoring your Auto Scaling Groups <https://docs.aws.amazon.com/autoscaling/ec2/userguide/as-instance-monitoring.html/>`_.
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
     * One or more names of cloud watch metrics you want to disable. See `Cloud watch metrics <https://docs.aws.amazon.com/autoscaling/ec2/userguide/as-instance-monitoring.html#as-view-group-metrics/>`_.
     */
    @Updatable
    public List<String> getDisabledMetrics() {
        if (disabledMetrics == null || disabledMetrics.isEmpty()) {
            disabledMetrics = new ArrayList<>();
        }

        return disabledMetrics;
    }

    public void setDisabledMetrics(List<String> disabledMetrics) {
        this.disabledMetrics = disabledMetrics;
    }

    /**
     * Tags for auto scaling groups. See `Tagging Auto Scaling Groups and Instances <https://docs.aws.amazon.com/autoscaling/ec2/userguide/autoscaling-tagging.html/>`_.
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
     * Tags in auto scaling groups that you want instances to have as well. See `Tagging Auto Scaling Groups and Instances <https://docs.aws.amazon.com/autoscaling/ec2/userguide/autoscaling-tagging.html/>`
     */
    @Updatable
    public List<String> getPropagateAtLaunchTags() {
        if (propagateAtLaunchTags == null) {
            propagateAtLaunchTags = new ArrayList<>();
        }
        return propagateAtLaunchTags;
    }

    public void setPropagateAtLaunchTags(List<String> propagateAtLaunchTags) {
        this.propagateAtLaunchTags = propagateAtLaunchTags;
    }

    /**
     * The Amazon Resource Name (ARN) of the service-linked role that the Auto Scaling group uses to call other AWS services.
     */
    public String getServiceLinkedRoleArn() {
        return serviceLinkedRoleArn;
    }

    public void setServiceLinkedRoleArn(String serviceLinkedRoleArn) {
        this.serviceLinkedRoleArn = serviceLinkedRoleArn;
    }

    /**
     * The name of the placement group into which to launch the instances.
     */
    public String getPlacementGroup() {
        return placementGroup;
    }

    public void setPlacementGroup(String placementGroup) {
        this.placementGroup = placementGroup;
    }

    /**
     * The instance used to create a launch configuration for the group.
     */
    public InstanceResource getInstance() {
        return instance;
    }

    public void setInstance(InstanceResource instance) {
        this.instance = instance;
    }

    /**
     * Set of classic load balancer's to be attached to the auto scaling group.
     */
    @Updatable
    public Set<gyro.aws.elb.LoadBalancerResource> getClassicLoadBalancers() {
        if (classicLoadBalancers == null) {
            classicLoadBalancers = new HashSet<>();
        }

        return classicLoadBalancers;
    }

    public void setClassicLoadBalancers(Set<gyro.aws.elb.LoadBalancerResource> classicLoadBalancers) {
        this.classicLoadBalancers = classicLoadBalancers;
    }

    /**
     * Set of application or network load balancer's.
     */
    @Updatable
    public Set<LoadBalancerResource> getLoadBalancers() {
        if (loadBalancers == null) {
            loadBalancers = new HashSet<>();
        }

        return loadBalancers;
    }

    public void setLoadBalancers(Set<LoadBalancerResource> loadBalancers) {
        this.loadBalancers = loadBalancers;
    }

    @Updatable
    public List<String> getTerminationPolicies() {
        if (terminationPolicies == null || terminationPolicies.isEmpty()) {
            terminationPolicies = new ArrayList<>();
            terminationPolicies.add("Default");
        }

        return terminationPolicies;
    }

    public void setTerminationPolicies(List<String> terminationPolicies) {
        this.terminationPolicies = terminationPolicies;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * A policy for triggering scaling this group.
     *
     * @subresource gyro.aws.autoscaling.AutoScalingPolicyResource
     */
    public List<AutoScalingPolicyResource> getScalingPolicy() {
        if (scalingPolicy == null) {
            scalingPolicy = new ArrayList<>();
        }

        return scalingPolicy;
    }

    public void setScalingPolicy(List<AutoScalingPolicyResource> scalingPolicy) {
        this.scalingPolicy = scalingPolicy;
    }

    /**
     * Life cycle hook for the auto scaling group.
     *
     * @subresource gyro.aws.autoscaling.AutoScalingGroupLifecycleHookResource
     */
    public List<AutoScalingGroupLifecycleHookResource> getLifecycleHook() {
        if (lifecycleHook == null) {
            lifecycleHook = new ArrayList<>();
        }

        return lifecycleHook;
    }

    public void setLifecycleHook(List<AutoScalingGroupLifecycleHookResource> lifecycleHook) {
        this.lifecycleHook = lifecycleHook;
    }

    /**
     * Scheduled actions for the auto scaling group.
     *
     * @subresource gyro.aws.autoscaling.AutoScalingGroupScheduledActionResource
     */
    public List<AutoScalingGroupScheduledActionResource> getScheduledAction() {
        if (scheduledAction == null) {
            scheduledAction = new ArrayList<>();
        }

        return scheduledAction;
    }

    public void setScheduledAction(List<AutoScalingGroupScheduledActionResource> scheduledAction) {
        this.scheduledAction = scheduledAction;
    }

    /**
     * Notifications for the auto scaling group.
     *
     * @subresource gyro.aws.autoscaling.AutoScalingGroupNotificationResource
     */
    public List<AutoScalingGroupNotificationResource> getAutoScalingNotification() {
        if (autoScalingNotification == null) {
            autoScalingNotification = new ArrayList<>();
        }

        return autoScalingNotification;
    }

    public void setAutoScalingNotification(List<AutoScalingGroupNotificationResource> autoScalingNotification) {
        this.autoScalingNotification = autoScalingNotification;
    }

    @Override
    public void copyFrom(AutoScalingGroup autoScalingGroup) {
        setArn(autoScalingGroup.autoScalingGroupARN());
        setMaxSize(autoScalingGroup.maxSize());
        setMinSize(autoScalingGroup.minSize());
        setAvailabilityZones(autoScalingGroup.availabilityZones());
        setDesiredCapacity(autoScalingGroup.desiredCapacity());
        setDefaultCooldown(autoScalingGroup.defaultCooldown());
        setHealthCheckType(autoScalingGroup.healthCheckType());
        setHealthCheckGracePeriod(autoScalingGroup.healthCheckGracePeriod());
        setNewInstancesProtectedFromScaleIn(autoScalingGroup.newInstancesProtectedFromScaleIn());
        setServiceLinkedRoleArn(autoScalingGroup.serviceLinkedRoleARN());
        setPlacementGroup(autoScalingGroup.placementGroup());
        setStatus(autoScalingGroup.status());
        setCreatedTime(Date.from(autoScalingGroup.createdTime()));
        setTerminationPolicies(autoScalingGroup.terminationPolicies());

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
                .map(o -> findById(gyro.aws.elb.LoadBalancerResource.class, o))
                .collect(Collectors.toSet())
                : null);
        setLoadBalancers(
            (autoScalingGroup.targetGroupARNs() != null && !autoScalingGroup.targetGroupARNs().isEmpty())
                ? autoScalingGroup.targetGroupARNs().stream()
                .map(o -> findById(LoadBalancerResource.class, o))
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

        copyFrom(autoScalingGroup);

        return true;
    }

    @Override
    public void create() {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        validate();

        client.createAutoScalingGroup(
            r -> r.autoScalingGroupName(getAutoScalingGroupName())
                .maxSize(getMaxSize())
                .minSize(getMinSize())
                .availabilityZones(getAvailabilityZones())
                .desiredCapacity(getDesiredCapacity())
                .defaultCooldown(getDefaultCooldown())
                .healthCheckType(getHealthCheckType())
                .healthCheckGracePeriod(getHealthCheckGracePeriod())
                .launchConfigurationName(getLaunchConfiguration() != null ? getLaunchConfiguration().getLaunchConfigurationName() : null)
                .newInstancesProtectedFromScaleIn(getNewInstancesProtectedFromScaleIn())
                .vpcZoneIdentifier(getSubnets().isEmpty() ? " " : StringUtils.join(getSubnets().stream().map(SubnetResource::getSubnetId).collect(Collectors.toList()), ","))
                .launchTemplate(
                    LaunchTemplateSpecification.builder()
                        .launchTemplateId(getLaunchTemplate() != null ? getLaunchTemplate().getLaunchTemplateId() : null)
                        .build()
                )
                .tags(getAutoScaleGroupTags(getTags(), getPropagateAtLaunchTags()))
                .serviceLinkedRoleARN(getServiceLinkedRoleArn())
                .placementGroup(getPlacementGroup())
                .loadBalancerNames(getClassicLoadBalancers().stream().map(gyro.aws.elb.LoadBalancerResource::getLoadBalancerName).collect(Collectors.toList()))
                .targetGroupARNs(getLoadBalancers().stream().map(LoadBalancerResource::getArn).collect(Collectors.toList()))
                .instanceId(getInstance() != null ? getInstance().getInstanceId() : null)
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
    public void update(Resource current, Set<String> changedFieldNames) {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        validate();

        client.updateAutoScalingGroup(
            r -> r.autoScalingGroupName(getAutoScalingGroupName())
                .launchTemplate(
                    LaunchTemplateSpecification.builder()
                        .launchTemplateId(getLaunchTemplate() != null ? getLaunchTemplate().getLaunchTemplateId() : null)
                        .build()
                )
                .maxSize(getMaxSize())
                .minSize(getMinSize())
                .availabilityZones(getAvailabilityZones())
                .desiredCapacity(getDesiredCapacity())
                .defaultCooldown(getDefaultCooldown())
                .healthCheckType(getHealthCheckType())
                .healthCheckGracePeriod(getHealthCheckGracePeriod())
                .launchConfigurationName(getLaunchConfiguration() != null ? getLaunchConfiguration().getLaunchConfigurationName() : null)
                .newInstancesProtectedFromScaleIn(getNewInstancesProtectedFromScaleIn())
                .vpcZoneIdentifier(getSubnets().isEmpty() ? " " : StringUtils.join(getSubnets().stream().map(SubnetResource::getSubnetId).collect(Collectors.toList()), ","))
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

        if (changedFieldNames.contains("load-balancers")) {
            saveTargetGroupArns(client, oldResource.getLoadBalancers());
        }
    }

    @Override
    public void delete() {
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
            .map(i -> i.instanceId())
            .collect(Collectors.toList());

        Ec2Client ec2Client = createClient(Ec2Client.class);
        DescribeInstancesResponse instancesResponse = ec2Client.describeInstances(r -> r.instanceIds(instanceIds));

        List<GyroInstance> instances = new ArrayList<>();
        for (Reservation reservation : instancesResponse.reservations()) {
            instances.addAll(reservation.instances()
                .stream()
                .map(i -> new InstanceResource(i, ec2Client))
                .collect(Collectors.toList()));
        }

        return instances;
    }

    private List<Tag> getAutoScaleGroupTags(Map<String, String> localTags, List<String> passToInstanceTags) {
        HashSet<String> passToInstanceTagSet = new HashSet<>(passToInstanceTags);

        List<Tag> tags = new ArrayList<>();

        for (String key : localTags.keySet()) {
            tags.add(
                Tag.builder()
                    .key(key)
                    .value(localTags.get(key))
                    .propagateAtLaunch(passToInstanceTagSet.contains(key))
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

    private void saveTags(AutoScalingClient client, Map<String, String> localTags, List<String> passToInstanceTags, boolean isDelete) {
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

    private void validate() {
        if (getLaunchTemplate() == null && getLaunchConfiguration() == null) {
            throw new GyroException("Either Launch template id or a launch configuration name is required.");
        }

        if (!getHealthCheckType().equals("ELB") && !getHealthCheckType().equals("EC2")) {
            throw new GyroException("The value - (" + getHealthCheckType()
                + ") is invalid for parameter Health Check Type.");
        }

        if (getHealthCheckGracePeriod() < 0) {
            throw new GyroException("The value - (" + getHealthCheckGracePeriod()
                + ") is invalid for parameter Health Check Grace period. Integer value grater or equal to 0.");
        }

        if (getMaxSize() < 0) {
            throw new GyroException("The value - (" + getMaxSize()
                + ") is invalid for parameter Max size. Integer value grater or equal to 0.");
        }

        if (getMinSize() < 0) {
            throw new GyroException("The value - (" + getMinSize()
                + ") is invalid for parameter Min size. Integer value grater or equal to 0.");
        }

        if (getDefaultCooldown() < 0) {
            throw new GyroException("The value - (" + getDefaultCooldown()
                + ") is invalid for parameter Default cool down. Integer value grater or equal to 0.");
        }

        if (getDesiredCapacity() < 0) {
            throw new GyroException("The value - (" + getDesiredCapacity()
                + ") is invalid for parameter Desired capacity. Integer value grater or equal to 0.");
        }

        if (!getEnableMetricsCollection() && !getDisabledMetrics().isEmpty()) {
            throw new GyroException("When Enabled Metrics Collection is set to false, disabled metrics can't have items in it.");
        }

        if (!masterMetricSet.containsAll(getDisabledMetrics())) {
            throw new GyroException("Invalid values for parameter Disabled Metrics.");
        }

        if (!new HashSet<>(getTags().keySet()).containsAll(getPropagateAtLaunchTags())) {
            throw new GyroException("Propagate at launch tags cannot contain keys not mentioned under tags.");
        }
    }

    private void loadMetrics(List<EnabledMetric> enabledMetrics) {
        setEnableMetricsCollection(!enabledMetrics.isEmpty());
        Set<String> allMetrics = new HashSet<>(masterMetricSet);
        allMetrics.removeAll(enabledMetrics.stream().map(EnabledMetric::metric).collect(Collectors.toSet()));
        setDisabledMetrics(allMetrics.size() == masterMetricSet.size() ? new ArrayList<>() : new ArrayList<>(allMetrics));
    }

    private void saveMetrics(AutoScalingClient client) {
        Set<String> metrics = new HashSet<>(masterMetricSet);
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

    private void saveLoadBalancerNames(AutoScalingClient client, Set<gyro.aws.elb.LoadBalancerResource> oldLoadBalancers) {
        Set<String> removeLoadBalancerNames = oldLoadBalancers.stream()
            .map(gyro.aws.elb.LoadBalancerResource::getLoadBalancerName)
            .collect(Collectors.toSet());

        removeLoadBalancerNames.removeAll(getClassicLoadBalancers().stream()
            .map(gyro.aws.elb.LoadBalancerResource::getLoadBalancerName)
            .collect(Collectors.toSet()));

        if (!removeLoadBalancerNames.isEmpty()) {
            client.detachLoadBalancers(
                r -> r.autoScalingGroupName(getAutoScalingGroupName()).loadBalancerNames(removeLoadBalancerNames)
            );
        }

        Set<String> addLoadbalancerNames = getClassicLoadBalancers().stream()
            .map(gyro.aws.elb.LoadBalancerResource::getLoadBalancerName)
            .collect(Collectors.toSet());

        addLoadbalancerNames.removeAll(oldLoadBalancers.stream()
            .map(gyro.aws.elb.LoadBalancerResource::getLoadBalancerName)
            .collect(Collectors.toSet()));

        if (!addLoadbalancerNames.isEmpty()) {
            client.attachLoadBalancers(
                r -> r.autoScalingGroupName(getAutoScalingGroupName()).loadBalancerNames(addLoadbalancerNames)
            );
        }
    }

    private void saveTargetGroupArns(AutoScalingClient client, Set<LoadBalancerResource> oldLoadbalancers) {
        Set<String> removeTargetGroupArns = oldLoadbalancers.stream().map(LoadBalancerResource::getArn).collect(Collectors.toSet());

        removeTargetGroupArns.removeAll(getLoadBalancers().stream().map(LoadBalancerResource::getArn).collect(Collectors.toSet()));

        if (!removeTargetGroupArns.isEmpty()) {
            client.detachLoadBalancerTargetGroups(
                r -> r.autoScalingGroupName(getAutoScalingGroupName()).targetGroupARNs(removeTargetGroupArns)
            );
        }

        Set<String> addTargetGroupArns = getLoadBalancers().stream().map(LoadBalancerResource::getArn).collect(Collectors.toSet());

        addTargetGroupArns.removeAll(oldLoadbalancers.stream().map(LoadBalancerResource::getArn).collect(Collectors.toSet()));

        if (!addTargetGroupArns.isEmpty()) {
            client.attachLoadBalancerTargetGroups(
                r -> r.autoScalingGroupName(getAutoScalingGroupName()).targetGroupARNs(addTargetGroupArns)
            );
        }
    }
}
