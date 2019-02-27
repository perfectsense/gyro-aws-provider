package gyro.aws.autoscaling;

import gyro.aws.AwsResource;
import gyro.core.BeamException;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import gyro.lang.Resource;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StringUtils;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
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
import software.amazon.awssdk.services.ec2.model.Ec2Exception;

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
 *         launch-configuration-name: $(aws::launch-configuration launch-configuration-auto-scaling-group-example | launch-configuration-name)
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
 *         launch-template-id: $(aws::launch-template launch-template-auto-scaling-group-example | launch-template-id)
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
@ResourceName("auto-scaling-group")
public class AutoScalingGroupResource extends AwsResource {

    private String autoScalingGroupName;
    private String launchTemplateId;
    private List<String> availabilityZones;
    private Integer maxSize;
    private Integer minSize;
    private Integer desiredCapacity;
    private Integer defaultCooldown;
    private String healthCheckType;
    private Integer healthCheckGracePeriod;
    private String launchConfigurationName;
    private Boolean newInstancesProtectedFromScaleIn;
    private List<String> subnetIds;
    private String arn;
    private Boolean enableMetricsCollection;
    private List<String> disabledMetrics;
    private Map<String, String> tags;
    private List<String> propagateAtLaunchTags;
    private String serviceLinkedRoleArn;
    private String placementGroup;
    private String instanceId;
    private List<String> loadBalancerNames;
    private List<String> targetGroupArns;
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
    public String getAutoScalingGroupName() {
        return autoScalingGroupName;
    }

    public void setAutoScalingGroupName(String autoScalingGroupName) {
        this.autoScalingGroupName = autoScalingGroupName;
    }

    /**
     * The ID of an launched template that would be used as a skeleton to create the Auto scaling group. Required if launch configuration name not provided.
     */
    @ResourceDiffProperty(updatable = true)
    public String getLaunchTemplateId() {
        return launchTemplateId;
    }

    public void setLaunchTemplateId(String launchTemplateId) {
        this.launchTemplateId = launchTemplateId;
    }

    /**
     *  A list of availability zones for the auto scale group to be active in. See `Distributing Instances Across Availability Zones <https://docs.aws.amazon.com/autoscaling/ec2/userguide/auto-scaling-benefits.html#arch-AutoScalingMultiAZ/>`_. (Required)
     */
    @ResourceDiffProperty(updatable = true)
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
    @ResourceDiffProperty(updatable = true)
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
    @ResourceDiffProperty(updatable = true)
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
    @ResourceDiffProperty(updatable = true)
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
    @ResourceDiffProperty(updatable = true)
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
    @ResourceDiffProperty(updatable = true)
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
    @ResourceDiffProperty(updatable = true)
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
    @ResourceDiffProperty(updatable = true)
    public String getLaunchConfigurationName() {
        return launchConfigurationName;
    }

    public void setLaunchConfigurationName(String launchConfigurationName) {
        this.launchConfigurationName = launchConfigurationName;
    }

    /**
     * Enable protection of instances from auto scale group scale in. Defaults to false. see `Controlling Which Auto Scaling Instances Terminate During Scale In <https://docs.aws.amazon.com/autoscaling/ec2/userguide/as-instance-termination.html/>`_.
     */
    @ResourceDiffProperty(updatable = true)
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
     * A list of subnet identifiers. If Availability Zone is provided, subnet's need to be part of that. See `Launching Auto Scaling Instances in a VPC <https://docs.aws.amazon.com/autoscaling/ec2/userguide/asg-in-vpc.html/>`_.
     */
    @ResourceDiffProperty(updatable = true, nullable = true)
    public List<String> getSubnetIds() {
        if (subnetIds == null) {
            subnetIds = new ArrayList<>();
        }
        return subnetIds;
    }

    public void setSubnetIds(List<String> subnetIds) {
        this.subnetIds = subnetIds;
    }

    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * Enable/Disable cloud watch metrics for your auto scaling group. Defaults to false. See `Monitoring your Auto Scaling Groups <https://docs.aws.amazon.com/autoscaling/ec2/userguide/as-instance-monitoring.html/>`_.
     */
    @ResourceDiffProperty(updatable = true)
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
    @ResourceDiffProperty(updatable = true, nullable = true)
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
    @ResourceDiffProperty(updatable = true, nullable = true)
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
    @ResourceDiffProperty(updatable = true, nullable = true)
    public List<String> getPropagateAtLaunchTags() {
        if (propagateAtLaunchTags == null) {
            propagateAtLaunchTags = new ArrayList<>();
        }
        return propagateAtLaunchTags;
    }

    public void setPropagateAtLaunchTags(List<String> propagateAtLaunchTags) {
        this.propagateAtLaunchTags = propagateAtLaunchTags;
    }

    public String getServiceLinkedRoleArn() {
        return serviceLinkedRoleArn;
    }

    public void setServiceLinkedRoleArn(String serviceLinkedRoleArn) {
        this.serviceLinkedRoleArn = serviceLinkedRoleArn;
    }

    public String getPlacementGroup() {
        return placementGroup;
    }

    public void setPlacementGroup(String placementGroup) {
        this.placementGroup = placementGroup;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @ResourceDiffProperty(updatable = true, nullable = true)
    public List<String> getLoadBalancerNames() {
        if (loadBalancerNames == null) {
            loadBalancerNames = new ArrayList<>();
        }

        return loadBalancerNames;
    }

    public void setLoadBalancerNames(List<String> loadBalancerNames) {
        this.loadBalancerNames = loadBalancerNames;
    }

    @ResourceDiffProperty(updatable = true, nullable = true)
    public List<String> getTargetGroupArns() {
        if (targetGroupArns == null) {
            targetGroupArns = new ArrayList<>();
        }

        return targetGroupArns;
    }

    public void setTargetGroupArns(List<String> targetGroupArns) {
        this.targetGroupArns = targetGroupArns;
    }

    @ResourceDiffProperty(updatable = true, nullable = true)
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
    @ResourceDiffProperty(nullable = true, subresource = true)
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
     *
     * @subresource gyro.aws.autoscaling.AutoScalingGroupLifecycleHookResource
     */
    @ResourceDiffProperty(nullable = true, subresource = true)
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
     *
     * @subresource gyro.aws.autoscaling.AutoScalingGroupScheduledActionResource
     */
    @ResourceDiffProperty(nullable = true, subresource = true)
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
     *
     * @subresource gyro.aws.autoscaling.AutoScalingGroupNotificationResource
     */
    @ResourceDiffProperty(nullable = true, subresource = true)
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
    public boolean refresh() {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        AutoScalingGroup autoScalingGroup = getAutoScalingGroup(client);

        if (autoScalingGroup == null) {
            return false;
        }

        setArn(autoScalingGroup.autoScalingGroupARN());
        setLaunchTemplateId(autoScalingGroup.launchTemplate() == null ? null : autoScalingGroup.launchTemplate().launchTemplateId());
        setMaxSize(autoScalingGroup.maxSize());
        setMinSize(autoScalingGroup.minSize());
        setAvailabilityZones(autoScalingGroup.availabilityZones());
        setDesiredCapacity(autoScalingGroup.desiredCapacity());
        setDefaultCooldown(autoScalingGroup.defaultCooldown());
        setHealthCheckType(autoScalingGroup.healthCheckType());
        setHealthCheckGracePeriod(autoScalingGroup.healthCheckGracePeriod());
        setLaunchConfigurationName(autoScalingGroup.launchConfigurationName());
        setNewInstancesProtectedFromScaleIn(autoScalingGroup.newInstancesProtectedFromScaleIn());
        setServiceLinkedRoleArn(autoScalingGroup.serviceLinkedRoleARN());
        setPlacementGroup(autoScalingGroup.placementGroup());
        setStatus(autoScalingGroup.status());
        setCreatedTime(Date.from(autoScalingGroup.createdTime()));
        setSubnetIds(autoScalingGroup.vpcZoneIdentifier().equals("")
            ? new ArrayList<>() : Arrays.asList(autoScalingGroup.vpcZoneIdentifier().split(",")));

        setLoadBalancerNames(autoScalingGroup.loadBalancerNames());
        setTargetGroupArns(autoScalingGroup.targetGroupARNs());
        setTerminationPolicies(autoScalingGroup.terminationPolicies());

        loadMetrics(autoScalingGroup.enabledMetrics());

        loadTags(autoScalingGroup.tags());

        loadScalingPolicy(client);

        loadLifecycleHook(client);

        loadScheduledAction(client);

        loadNotification(client);

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
                .launchConfigurationName(getLaunchConfigurationName())
                .newInstancesProtectedFromScaleIn(getNewInstancesProtectedFromScaleIn())
                .vpcZoneIdentifier(getSubnetIds().isEmpty() ? " " : StringUtils.join(getSubnetIds(), ","))
                .launchTemplate(LaunchTemplateSpecification.builder().launchTemplateId(getLaunchTemplateId()).build())
                .tags(getAutoScaleGroupTags(getTags(), getPropagateAtLaunchTags()))
                .serviceLinkedRoleARN(getServiceLinkedRoleArn())
                .placementGroup(getPlacementGroup())
                .loadBalancerNames(getLoadBalancerNames())
                .targetGroupARNs(getTargetGroupArns())
                .instanceId(getInstanceId())
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
    public void update(Resource current, Set<String> changedProperties) {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        validate();

        client.updateAutoScalingGroup(
            r -> r.autoScalingGroupName(getAutoScalingGroupName())
                .launchTemplate(LaunchTemplateSpecification.builder().launchTemplateId(getLaunchTemplateId()).build())
                .maxSize(getMaxSize())
                .minSize(getMinSize())
                .availabilityZones(getAvailabilityZones())
                .desiredCapacity(getDesiredCapacity())
                .defaultCooldown(getDefaultCooldown())
                .healthCheckType(getHealthCheckType())
                .healthCheckGracePeriod(getHealthCheckGracePeriod())
                .launchConfigurationName(getLaunchConfigurationName())
                .newInstancesProtectedFromScaleIn(getNewInstancesProtectedFromScaleIn())
                .vpcZoneIdentifier(getSubnetIds().isEmpty() ? " " : StringUtils.join(getSubnetIds(), ","))
                .terminationPolicies(getTerminationPolicies())
        );

        if (changedProperties.contains("enable-metrics-collection") || changedProperties.contains("disabled-metrics")) {
            if (getEnableMetricsCollection()) {
                saveMetrics(client);
            } else {
                client.disableMetricsCollection(
                    r -> r.autoScalingGroupName(getAutoScalingGroupName())
                );
            }
        }

        AutoScalingGroupResource oldResource = (AutoScalingGroupResource) current;

        if (changedProperties.contains("tags") || changedProperties.contains("propagate-at-launch-tags")) {
            if (!getTags().isEmpty()) {
                saveTags(client, getTags(), getPropagateAtLaunchTags(), false);

                removeStaleTags(client, oldResource);
            } else {
                saveTags(client, oldResource.getTags(), oldResource.getPropagateAtLaunchTags(), true);
            }
        }

        if (changedProperties.contains("load-balancer-names")) {
            saveLoadBalancerNames(client, oldResource.getLoadBalancerNames());
        }

        if (changedProperties.contains("target-group-arns")) {
            saveTargetGroupArns(client, oldResource.getTargetGroupArns());
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
            throw new BeamException("auto-scale-group-name is missing, unable to load auto scale group.");
        }

        try {
            DescribeAutoScalingGroupsResponse response = client.describeAutoScalingGroups(
                r -> r.autoScalingGroupNames(Collections.singleton(getAutoScalingGroupName()))
            );

            if (response.autoScalingGroups().isEmpty()) {
                return null;
            }

            return response.autoScalingGroups().get(0);
        } catch (Ec2Exception ex) {
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
        if (ObjectUtils.isBlank(getLaunchTemplateId()) && ObjectUtils.isBlank(getLaunchConfigurationName())) {
            throw new BeamException("Either Launch template id or a launch configuration name is required.");
        }

        if (!getHealthCheckType().equals("ELB") && !getHealthCheckType().equals("EC2")) {
            throw new BeamException("The value - (" + getHealthCheckType()
                + ") is invalid for parameter Health Check Type.");
        }

        if (getHealthCheckGracePeriod() < 0) {
            throw new BeamException("The value - (" + getHealthCheckGracePeriod()
                + ") is invalid for parameter Health Check Grace period. Integer value grater or equal to 0.");
        }

        if (getMaxSize() < 0) {
            throw new BeamException("The value - (" + getMaxSize()
                + ") is invalid for parameter Max size. Integer value grater or equal to 0.");
        }

        if (getMinSize() < 0) {
            throw new BeamException("The value - (" + getMinSize()
                + ") is invalid for parameter Min size. Integer value grater or equal to 0.");
        }

        if (getDefaultCooldown() < 0) {
            throw new BeamException("The value - (" + getDefaultCooldown()
                + ") is invalid for parameter Default cool down. Integer value grater or equal to 0.");
        }

        if (getDesiredCapacity() < 0) {
            throw new BeamException("The value - (" + getDesiredCapacity()
                + ") is invalid for parameter Desired capacity. Integer value grater or equal to 0.");
        }

        if (!getEnableMetricsCollection() && !getDisabledMetrics().isEmpty()) {
            throw new BeamException("When Enabled Metrics Collection is set to false, disabled metrics can't have items in it.");
        }

        if (!masterMetricSet.containsAll(getDisabledMetrics())) {
            throw new BeamException("Invalid values for parameter Disabled Metrics.");
        }

        if (!new HashSet<>(getTags().keySet()).containsAll(getPropagateAtLaunchTags())) {
            throw new BeamException("Propagate at launch tags cannot contain keys not mentioned under tags.");
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
            AutoScalingPolicyResource autoScalingPolicyResource = new AutoScalingPolicyResource(scalingPolicy);
            autoScalingPolicyResource.parent(this);
            getScalingPolicy().add(autoScalingPolicyResource);
        }
    }

    private void loadLifecycleHook(AutoScalingClient client) {
        getLifecycleHook().clear();

        DescribeLifecycleHooksResponse lifecycleHooksResponse = client.describeLifecycleHooks(r -> r.autoScalingGroupName(getAutoScalingGroupName()));

        for (LifecycleHook lifecycleHook : lifecycleHooksResponse.lifecycleHooks()) {
            AutoScalingGroupLifecycleHookResource lifecycleHookResource = new AutoScalingGroupLifecycleHookResource(lifecycleHook);
            lifecycleHookResource.parent(this);
            getLifecycleHook().add(lifecycleHookResource);
        }
    }

    private void loadScheduledAction(AutoScalingClient client) {
        getScheduledAction().clear();

        DescribeScheduledActionsResponse scheduledActionsResponse = client.describeScheduledActions(
            r -> r.autoScalingGroupName(getAutoScalingGroupName())
        );

        for (ScheduledUpdateGroupAction scheduledUpdateGroupAction : scheduledActionsResponse.scheduledUpdateGroupActions()) {
            AutoScalingGroupScheduledActionResource scheduledActionResource = new AutoScalingGroupScheduledActionResource(scheduledUpdateGroupAction);
            scheduledActionResource.parent(this);
            getScheduledAction().add(scheduledActionResource);
        }
    }

    private void loadNotification(AutoScalingClient client) {
        getAutoScalingNotification().clear();

        DescribeNotificationConfigurationsResponse notificationResponse = client.describeNotificationConfigurations(
            r -> r.autoScalingGroupNames(Collections.singletonList(getAutoScalingGroupName()))
        );

        for (NotificationConfiguration notificationConfiguration : notificationResponse.notificationConfigurations()) {
            AutoScalingGroupNotificationResource notificationResource = new AutoScalingGroupNotificationResource(notificationConfiguration);
            notificationResource.parent(this);
            getAutoScalingNotification().add(notificationResource);
        }
    }

    private void saveLoadBalancerNames(AutoScalingClient client, List<String> oldLoadBalancerNames) {
        List<String> removeLoadBalancerNames = new ArrayList<>(oldLoadBalancerNames);

        removeLoadBalancerNames.removeAll(getLoadBalancerNames());

        if (!removeLoadBalancerNames.isEmpty()) {
            client.detachLoadBalancers(
                r -> r.autoScalingGroupName(getAutoScalingGroupName()).loadBalancerNames(removeLoadBalancerNames)
            );
        }

        List<String> addLoadbalancerNames = new ArrayList<>(getLoadBalancerNames());

        addLoadbalancerNames.removeAll(oldLoadBalancerNames);

        if (!addLoadbalancerNames.isEmpty()) {
            client.attachLoadBalancers(
                r -> r.autoScalingGroupName(getAutoScalingGroupName()).loadBalancerNames(addLoadbalancerNames)
            );
        }
    }

    private void saveTargetGroupArns(AutoScalingClient client, List<String> oldTargetGroupArns) {
        List<String> removeTargetGroupArns = new ArrayList<>(oldTargetGroupArns);

        removeTargetGroupArns.removeAll(getTargetGroupArns());

        if (!removeTargetGroupArns.isEmpty()) {
            client.detachLoadBalancerTargetGroups(
                r -> r.autoScalingGroupName(getAutoScalingGroupName()).targetGroupARNs(removeTargetGroupArns)
            );
        }

        List<String> addTargetGroupArns = new ArrayList<>(getTargetGroupArns());

        addTargetGroupArns.removeAll(oldTargetGroupArns);

        if (!addTargetGroupArns.isEmpty()) {
            client.attachLoadBalancerTargetGroups(
                r -> r.autoScalingGroupName(getAutoScalingGroupName()).targetGroupARNs(addTargetGroupArns)
            );
        }
    }
}
