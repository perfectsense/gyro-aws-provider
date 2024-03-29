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

package gyro.aws.ec2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.InstanceProfileResource;
import gyro.core.GyroException;
import gyro.core.GyroInstance;
import gyro.core.GyroUI;
import gyro.core.TimeoutSettings;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.DiffableInternals;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import org.apache.commons.codec.binary.Base64;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AttributeBooleanValue;
import software.amazon.awssdk.services.ec2.model.CapacityReservationSpecification;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceAttributeResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeNetworkInterfaceAttributeResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.IamInstanceProfileSpecification;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceAttributeName;
import software.amazon.awssdk.services.ec2.model.InstanceStateName;
import software.amazon.awssdk.services.ec2.model.MonitoringState;
import software.amazon.awssdk.services.ec2.model.NetworkInterfaceAttribute;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.ec2.model.ShutdownBehavior;
import software.amazon.awssdk.utils.builder.SdkBuilder;

/**
 * Creates an Instance with the specified AMI, Subnet and Security group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::instance instance-example
 *         ami: "amzn-ami-hvm-2018.03.0.20181129-x86_64-gp2"
 *         shutdown-behavior: "STOP"
 *         instance-type: "t2.micro"
 *         key: "example"
 *         subnet: $(aws::subnet subnet)
 *         security-groups: [
 *             $(aws::security-group security-group)
 *         ]
 *         disable-api-termination: false
 *         ebs-optimized: false
 *         source-dest-check: true
 *
 *         tags: {
 *             Name: "instance-example"
 *         }
 *
 *         block-device-mapping
 *             device-name: "/dev/sdb"
 *             volume-size: 100
 *             auto-enable-io: false
 *         end
 *
 *         capacity-reservation: "none"
 *     end
 */
@Type("instance")
public class InstanceResource extends Ec2TaggableResource<Instance> implements GyroInstance, Copyable<Instance> {

    private AmiResource ami;
    private Integer coreCount;
    private Integer threadPerCore;
    private Boolean ebsOptimized;
    private Boolean configureHibernateOption;
    private String shutdownBehavior;
    private String instanceType;
    private KeyPairResource key;
    private Boolean enableMonitoring;
    private Set<SecurityGroupResource> securityGroups;
    private SubnetResource subnet;
    private Boolean disableApiTermination;
    private Boolean sourceDestCheck;
    private String userData;
    private String capacityReservation;
    private Set<BlockDeviceMapping> blockDeviceMapping;
    private InstanceProfileResource instanceProfile;
    private LaunchTemplateSpecificationResource launchTemplate;
    private String privateIpAddress;
    private String status;

    // Read-only
    private String id;
    private String publicIpAddress;
    private String publicDnsName;
    private String instanceState;
    private Date launchDate;

    /**
     * The AMI to be used to launch the Instance.
     */
    public AmiResource getAmi() {
        return ami;
    }

    public void setAmi(AmiResource ami) {
        this.ami = ami;
    }

    /**
     * Launch instances with defined number of cores. Defaults to ``0`` which sets its to the instance type defaults. See `Optimizing CPU Options <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-optimize-cpu.html>`_.
     */
    public Integer getCoreCount() {
        if (coreCount == null) {
            coreCount = 0;
        }

        return coreCount;
    }

    public void setCoreCount(Integer coreCount) {
        this.coreCount = coreCount;
    }

    /**
     * Launch instances with defined number of threads per cores. Defaults to ``0`` which sets its to the instance type defaults. See `Optimizing CPU Options <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-optimize-cpu.html>`_.
     */
    public Integer getThreadPerCore() {
        if (threadPerCore == null) {
            threadPerCore = 0;
        }

        return threadPerCore;
    }

    public void setThreadPerCore(Integer threadPerCore) {
        this.threadPerCore = threadPerCore;
    }

    /**
     * Enable EBS optimization for an instance. Defaults to ``false``. See `Amazon EBS–Optimized Instances <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/EBSOptimized.html>`_.
     */
    @Updatable
    public Boolean getEbsOptimized() {
        if (ebsOptimized == null) {
            ebsOptimized = false;
        }

        return ebsOptimized;
    }

    public void setEbsOptimized(Boolean ebsOptimized) {
        this.ebsOptimized = ebsOptimized;
    }

    /**
     * Enable Hibernate options for an instance. Defaults to ``false``. See `Hibernate your Instances <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/Hibernate.html>`_.
     */
    public Boolean getConfigureHibernateOption() {
        if (configureHibernateOption == null) {
            configureHibernateOption = false;
        }

        return configureHibernateOption;
    }

    public void setConfigureHibernateOption(Boolean configureHibernateOption) {
        this.configureHibernateOption = configureHibernateOption;
    }

    /**
     * Change the Shutdown Behavior options for an instance. Defaults to ``Stop``. See `Changing the Instance Initiated Shutdown Behavior <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/terminating-instances.html#Using_ChangingInstanceInitiatedShutdownBehavior>`_.
     */
    @Updatable
    @ValidStrings({ "stop", "terminate" })
    public String getShutdownBehavior() {
        return shutdownBehavior != null ? shutdownBehavior.toLowerCase() : ShutdownBehavior.STOP.toString();
    }

    public void setShutdownBehavior(String shutdownBehavior) {
        this.shutdownBehavior = shutdownBehavior;
    }

    /**
     * Launch instance with the type of hardware you desire. See `Instance Types <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-types.html>`_.
     */
    @Updatable
    public String getInstanceType() {
        return instanceType != null ? instanceType.toLowerCase() : instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    /**
     * Launch instance with the key name of an EC2 Key Pair. This is a certificate required to access your instance. See `Amazon EC2 Key Pairs <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-key-pairs.html>`_.
     */
    public KeyPairResource getKey() {
        return key;
    }

    public void setKey(KeyPairResource key) {
        this.key = key;
    }

    /**
     * Enable or Disable monitoring for your instance. See `Monitoring Your Instances <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-cloudwatch.html>`_.
     */
    public Boolean getEnableMonitoring() {
        if (enableMonitoring == null) {
            enableMonitoring = false;
        }
        return enableMonitoring;
    }

    public void setEnableMonitoring(Boolean enableMonitoring) {
        this.enableMonitoring = enableMonitoring;
    }

    /**
     * Launch instance with the security groups specified. See `Amazon EC2 Security Groups for Linux Instances <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-network-security.html>`_.
     */
    @Updatable
    public Set<SecurityGroupResource> getSecurityGroups() {
        if (securityGroups == null) {
            securityGroups = new HashSet<>();

        }
        return securityGroups;
    }

    public void setSecurityGroups(Set<SecurityGroupResource> securityGroups) {
        this.securityGroups = securityGroups;
    }

    /**
     * Launch instance with the subnet specified. See `Vpcs and Subnets <https://docs.aws.amazon.com/vpc/latest/userguide/VPC_Subnets.html>`_. Required if no launch template used, or launch template with security groups used.
     */
    public SubnetResource getSubnet() {
        return subnet;
    }

    public void setSubnet(SubnetResource subnet) {
        this.subnet = subnet;
    }

    /**
     * Enable or Disable api termination of an instance. See `Enabling Termination Protection for an Instance <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/terminating-instances.html#Using_ChangingDisableAPITermination>`_.
     */
    @Updatable
    public Boolean getDisableApiTermination() {
        if (disableApiTermination == null) {
            disableApiTermination = false;
        }

        return disableApiTermination;
    }

    public void setDisableApiTermination(Boolean disableApiTermination) {
        this.disableApiTermination = disableApiTermination;
    }

    /**
     * Enable or Disable Source/Dest Check for an instance. Defaults to ``true``. See `Disabling Source/Destination Checks <https://docs.aws.amazon.com/vpc/latest/userguide/VPC_NAT_Instance.html#EIP_Disable_SrcDestCheck>`_.
     */
    @Updatable
    public Boolean getSourceDestCheck() {
        if (sourceDestCheck == null) {
            sourceDestCheck = true;
        }
        return sourceDestCheck;
    }

    public void setSourceDestCheck(Boolean sourceDestCheck) {
        this.sourceDestCheck = sourceDestCheck;
    }

    /**
     * Set user data for your instance. See `Instance Metadata and User Data <https://docs.aws.amazon.com/AWSEC2/latest/WindowsGuide/ec2-instance-metadata.html>`_.
     */
    @Updatable
    public String getUserData() {
        if (userData == null) {
            userData = "";
        } else {
            userData = userData.trim();
        }

        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    /**
     * Capacity reservation for the instance.
     */
    @Updatable
    public String getCapacityReservation() {
        if (capacityReservation == null) {
            capacityReservation = "none";
        }

        return capacityReservation;
    }

    public void setCapacityReservation(String capacityReservation) {
        this.capacityReservation = capacityReservation;
    }

    /**
     * Set Block device Mapping for the instance.
     *
     * @subresource gyro.aws.ec2.BlockDeviceMappingResource
     */
    public Set<BlockDeviceMapping> getBlockDeviceMapping() {
        if (blockDeviceMapping == null) {
            blockDeviceMapping = new HashSet<>();
        }

        return blockDeviceMapping;
    }

    public void setBlockDeviceMapping(Set<BlockDeviceMapping> blockDeviceMapping) {
        this.blockDeviceMapping = blockDeviceMapping;
    }

    /**
     * Attach IAM Instance profile.
     */
    public InstanceProfileResource getInstanceProfile() {
        return instanceProfile;
    }

    public void setInstanceProfile(InstanceProfileResource instanceProfile) {
        this.instanceProfile = instanceProfile;
    }

    /**
     * The launch template specification to use to create the instance.
     *
     * @subresource gyro.aws.ec2.LaunchTemplateSpecificationResource
     */
    public LaunchTemplateSpecificationResource getLaunchTemplate() {
        return launchTemplate;
    }

    public void setLaunchTemplate(LaunchTemplateSpecificationResource launchTemplate) {
        this.launchTemplate = launchTemplate;
    }

    /**
     * The state of the instance.
     */
    @ValidStrings({"running", "stopped"})
    @Updatable
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Instance ID of this instance.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The private IP of this instance.
     */
    public String getPrivateIpAddress() {
        return privateIpAddress;
    }

    public void setPrivateIpAddress(String privateIpAddress) {
        this.privateIpAddress = privateIpAddress;
    }

    /**
     * The public IP of this instance, if launched in a public subnet.
     */
    @Output
    public String getPublicIpAddress() {
        return publicIpAddress;
    }

    public void setPublicIpAddress(String publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }

    /**
     * The public dns name of this instance, if launched in a public subnet.
     */
    @Output
    public String getPublicDnsName() {
        return publicDnsName;
    }

    public void setPublicDnsName(String publicDnsName) {
        this.publicDnsName = publicDnsName;
    }

    /**
     * Current state of this instance (running, pending, terminated, stopped).
     */
    @Output
    public String getInstanceState() {
        return instanceState;
    }

    public void setInstanceState(String instanceState) {
        this.instanceState = instanceState;
    }

    public void setInstanceLaunchDate(Date launchDate) {
        this.launchDate = launchDate;
    }

    /**
     * The date and time this instance was launched.
     */
    @Output
    public Date getInstanceLaunchDate() {
        return launchDate;
    }

    // -- GyroInstance Implementation

    @Override
    public String getGyroInstanceId() {
        return getId();
    }

    @Override
    public String getGyroInstanceState() {
        return getInstanceState();
    }

    @Override
    public String getGyroInstanceHostname() {
        return getPublicDnsName();
    }

    @Override
    public String getGyroInstancePrivateIpAddress() {
        return getPrivateIpAddress();
    }

    @Output
    public String getGyroInstancePublicIpAddress() {
        return getPublicIpAddress();
    }

    @Override
    public String getGyroInstanceName() {
        if (getTags().isEmpty()) {
            return DiffableInternals.getName(this);
        }

        return getTags().get("Name");
    }

    @Override
    public String getGyroInstanceLaunchDate() {
        if (getInstanceLaunchDate() != null) {
            return getInstanceLaunchDate().toString();
        }

        return "";
    }

    @Override
    public String getGyroInstanceLocation() {
        if (getSubnet() != null) {
            return getSubnet().getAvailabilityZone() != null
                    ? getSubnet().getAvailabilityZone()
                    : getSubnet().getVpc() != null
                            ? getSubnet().getVpc().getRegion()
                            : "";
        }
        return "";
    }

    @Override
    protected String getResourceId() {
        return getId();
    }

    @Override
    public void copyFrom(Instance instance) {
        Ec2Client client = createClient(Ec2Client.class);
        setId(instance.instanceId());
        init(instance, client);
    }

    @Override
    protected boolean doRefresh() {

        Ec2Client client = createClient(Ec2Client.class);

        Instance instance = getInstance(client);

        if (instance == null || instance.state().name() == InstanceStateName.TERMINATED || instance.state().name() == InstanceStateName.SHUTTING_DOWN) {
            return false;
        }

        copyFrom(instance);

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        RunInstancesRequest.Builder builder = RunInstancesRequest.builder();
        builder = builder.imageId(getAmi() != null ? getAmi().getId() : null)
            .ebsOptimized(getEbsOptimized())
            .hibernationOptions(o -> o.configured(getConfigureHibernateOption()))
            .instanceInitiatedShutdownBehavior(getShutdownBehavior())
            .cpuOptions(getCoreCount() > 0
                ? o -> o.threadsPerCore(getThreadPerCore()).coreCount(getCoreCount()).build()
                : SdkBuilder::build)
            .instanceType(getInstanceType())
            .keyName(getKey() != null ? getKey().getName() : null)
            .maxCount(1)
            .minCount(1)
            .monitoring(o -> o.enabled(getEnableMonitoring()))
            .securityGroupIds(!getSecurityGroups().isEmpty() ? getSecurityGroups().stream()
                .map(SecurityGroupResource::getId)
                .collect(Collectors.toList()) : null)
            .subnetId(getSubnet() != null ? getSubnet().getId() : null)
            .disableApiTermination(getDisableApiTermination())
            .userData(new String(Base64.encodeBase64(getUserData().trim().getBytes())))
            .capacityReservationSpecification(getCapacityReservationSpecification())
            .iamInstanceProfile(getIamInstanceProfile());

        if (!ObjectUtils.isBlank(getGyroInstancePrivateIpAddress())) {
            builder = builder.privateIpAddress(getGyroInstancePrivateIpAddress());
        }

        if (!getBlockDeviceMapping().isEmpty()) {
            builder = builder.blockDeviceMappings(
                getBlockDeviceMapping().stream()
                    .map(BlockDeviceMapping::getBlockDeviceMapping)
                    .collect(Collectors.toList())
            );
        }

        if (getLaunchTemplate() != null) {
            builder.launchTemplate(getLaunchTemplate().toLaunchTemplateSpecification());
        }

        RunInstancesRequest request = builder.build();

        boolean status = Wait.atMost(60, TimeUnit.SECONDS)
            .prompt(false)
            .checkEvery(10, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.CREATE)
            .until(() -> createInstance(client, request));

        if (!status) {
            throw new GyroException(String.format("Value (%s) for parameter iamInstanceProfile.arn is invalid.", getInstanceProfile().getArn()));
        }

        state.save();

        boolean waitResult = Wait.atMost(3, TimeUnit.MINUTES)
            .prompt(false)
            .checkEvery(10, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.CREATE)
            .until(() -> isInstanceRunning(client));

        if (!waitResult) {
            throw new GyroException("Unable to reach 'running' state for ec2 instance - " + getGyroInstanceId());
        }

        Instance instance = getInstance(client);

        if (instance != null) {
            setPublicDnsName(instance.publicDnsName());
            setPublicIpAddress(instance.publicIpAddress());
            setPrivateIpAddress(instance.privateIpAddress());
            setInstanceState(instance.state().nameAsString());
            setInstanceLaunchDate(Date.from(instance.launchTime()));
            setStatus("running");
        }
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        if (changedProperties.contains("shutdown-behavior")) {
            client.modifyInstanceAttribute(
                r -> r.instanceId(getId())
                    .instanceInitiatedShutdownBehavior(o -> o.value(getShutdownBehavior()))
            );
        }

        if (changedProperties.contains("disable-api-termination")) {
            client.modifyInstanceAttribute(
                r -> r.instanceId(getId())
                    .disableApiTermination(o -> o.value(getDisableApiTermination()))
            );
        }

        if (changedProperties.contains("source-dest-check")) {
            Instance instance = getInstance(client);

            if (instance != null) {
                client.modifyNetworkInterfaceAttribute(
                    r -> r.networkInterfaceId(instance.networkInterfaces().get(0).networkInterfaceId())
                        .sourceDestCheck(a -> a.value(getSourceDestCheck()))
                );
            }
        }

        if (changedProperties.contains("security-groups")) {
            List<String> securityGroupIds = new ArrayList<>();
            getSecurityGroups().forEach(r -> securityGroupIds.add(r.getId()));

            client.modifyInstanceAttribute(
                r -> r.instanceId(getId())
                    .groups(securityGroupIds)
            );
        }

        if (changedProperties.contains("status") && "stopped".equals(getStatus())) {
            client.stopInstances(
                r -> r.instanceIds(getId())
                    .force(true)
            );

            Wait.atMost(3, TimeUnit.MINUTES)
                .checkEvery(10, TimeUnit.SECONDS)
                .resourceOverrides(this, TimeoutSettings.Action.UPDATE)
                .prompt(false)
                .until(() -> isInstanceStopped(client));
        }

        boolean instanceStopped = isInstanceStopped(client);

        if (changedProperties.contains("instance-type")
                && validateInstanceStop(ui, instanceStopped, "instance-type", getInstanceType())) {
            client.modifyInstanceAttribute(
                    r -> r.instanceId(getId())
                            .instanceType(o -> o.value(getInstanceType()))
            );
        }

        if (changedProperties.contains("ebs-optimized")
            && validateInstanceStop(ui, instanceStopped, "ebs-optimized", getEbsOptimized().toString())) {
            client.modifyInstanceAttribute(
                r -> r.instanceId(getId())
                    .ebsOptimized(o -> o.value(getEbsOptimized()))
            );
        }

        if (changedProperties.contains("user-data")
            && validateInstanceStop(ui, instanceStopped, "user-data", getUserData())) {
            client.modifyInstanceAttribute(
                r -> r.instanceId(getId())
                    .userData(o -> o.value(SdkBytes.fromByteArray(getUserData().getBytes())))
            );
        }

        if (changedProperties.contains("capacity-reservation")
            && validateInstanceStop(ui, instanceStopped, "capacity-reservation", getCapacityReservation())) {
            client.modifyInstanceCapacityReservationAttributes(
                r -> r.instanceId(getId())
                    .capacityReservationSpecification(getCapacityReservationSpecification())
            );
        }

        if (changedProperties.contains("status") && "running".equals(getStatus())) {
            client.startInstances(
                r -> r.instanceIds(getId())
            );

            Wait.atMost(3, TimeUnit.MINUTES)
                .checkEvery(10, TimeUnit.SECONDS)
                .resourceOverrides(this, TimeoutSettings.Action.UPDATE)
                .prompt(false)
                .until(() -> isInstanceRunning(client));
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        if (getDisableApiTermination()) {
            throw new GyroException("The instance (" + getId() + ") cannot be terminated when 'disableApiTermination' is set to True.");
        }

        Ec2Client client = createClient(Ec2Client.class);

        client.terminateInstances(r -> r.instanceIds(Collections.singletonList(getId())));

        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.DELETE)
            .prompt(true)
            .until(() -> isInstanceTerminated(client));
    }

    private void init(Instance instance, Ec2Client client) {
        setAmi(findById(AmiResource.class, instance.imageId()));
        setCoreCount(instance.cpuOptions().coreCount());
        setThreadPerCore(instance.cpuOptions().threadsPerCore());
        setEbsOptimized(instance.ebsOptimized());
        setConfigureHibernateOption(instance.hibernationOptions().configured());
        setInstanceType(instance.instanceType().toString());
        setKey(!ObjectUtils.isBlank(instance.keyName()) ? findById(KeyPairResource.class, instance.keyName()) : null);
        setEnableMonitoring(instance.monitoring().state().equals(MonitoringState.ENABLED));
        setSecurityGroups(instance.securityGroups().stream().map(r -> findById(SecurityGroupResource.class, r.groupId())).collect(Collectors.toSet()));
        setSubnet(findById(SubnetResource.class, instance.subnetId()));
        setPublicDnsName(instance.publicDnsName());
        setPublicIpAddress(instance.publicIpAddress());
        setPrivateIpAddress(instance.privateIpAddress());
        setInstanceState(instance.state().nameAsString());
        setInstanceLaunchDate(Date.from(instance.launchTime()));
        setInstanceProfile(instance.iamInstanceProfile() != null ? findById(InstanceProfileResource.class, instance.iamInstanceProfile().arn()) : null);

        if (instance.capacityReservationSpecification() != null) {
            setCapacityReservation(
                instance.capacityReservationSpecification().capacityReservationTarget() != null
                    ? instance.capacityReservationSpecification().capacityReservationTarget().capacityReservationId()
                    : instance.capacityReservationSpecification().capacityReservationPreferenceAsString()
            );
        }

        DescribeInstanceAttributeResponse attributeResponse = client.describeInstanceAttribute(
            r -> r.instanceId(getId()).attribute(InstanceAttributeName.INSTANCE_INITIATED_SHUTDOWN_BEHAVIOR)
        );
        setShutdownBehavior(attributeResponse.instanceInitiatedShutdownBehavior().value());

        attributeResponse = client.describeInstanceAttribute(
            r -> r.instanceId(getId()).attribute(InstanceAttributeName.DISABLE_API_TERMINATION)
        );
        setDisableApiTermination(attributeResponse.disableApiTermination().equals(AttributeBooleanValue.builder().value(true).build()));

        DescribeNetworkInterfaceAttributeResponse response = client.describeNetworkInterfaceAttribute(
            r -> r.networkInterfaceId(instance.networkInterfaces().get(0).networkInterfaceId())
                .attribute(NetworkInterfaceAttribute.SOURCE_DEST_CHECK)
        );
        setSourceDestCheck(response.sourceDestCheck().value());

        attributeResponse = client.describeInstanceAttribute(
            r -> r.instanceId(getId()).attribute(InstanceAttributeName.USER_DATA)
        );
        setUserData(attributeResponse.userData().value() == null
            ? "" : new String(Base64.decodeBase64(attributeResponse.userData().value())).trim());

        setStatus("running".equals(instance.state().nameAsString()) ? "running" : "stopped");

        refreshTags();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if ((getLaunchTemplate() == null || ObjectUtils.isBlank(getLaunchTemplate().getLaunchTemplate()
            .getInstanceType())) && ObjectUtils.isBlank(getInstanceType())) {
            errors.add(new ValidationError(this, "instance-type",
                "'instance-type' is required if it is not specified in 'launch-template'."));
        }

        if (!getCapacityReservation().equalsIgnoreCase("none") && !getCapacityReservation().equalsIgnoreCase("open")
            && !getCapacityReservation().startsWith("cr-")) {
            errors.add(new ValidationError(this, "capacity-reservation",
                "The value - (" + getCapacityReservation() + ") is invalid for parameter 'capacity-reservation'. "
                    + "Valid values [ 'open', 'none', capacity reservation id like cr-% ]"));
        }

        if ((getLaunchTemplate() == null || ObjectUtils.isBlank(getLaunchTemplate().getLaunchTemplate()
            .getAmi())) && ObjectUtils.isBlank(getAmi())) {
            errors.add(new ValidationError(this, "ami",
                "'ami' is required if it is not specified in 'launch-template'."));
        }

        if ((!getSecurityGroups().isEmpty() || (getLaunchTemplate() != null && !getLaunchTemplate().getLaunchTemplate()
            .getSecurityGroups().isEmpty())) && getSubnet() == null) {
            errors.add(new ValidationError(this, "subnet",
                "'subnet' is required when 'security-groups' configured in the instance or in the 'launch-template'."));
        }

        return errors;
    }

    private Instance getInstance(Ec2Client client) {
        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load instance.");
        }

        try {
            DescribeInstancesResponse response = client.describeInstances(r -> r.instanceIds(getId()));

            if (!response.reservations().isEmpty() && !response.reservations().get(0).instances().isEmpty()) {
                return response.reservations().get(0).instances().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return null;
    }

    private boolean isInstanceRunning(Ec2Client client) {
        Instance instance = getInstance(client);

        return instance != null && "running".equals(instance.state().nameAsString());
    }

    private boolean isInstanceStopped(Ec2Client client) {
        Instance instance = getInstance(client);

        return instance != null && "stopped".equals(instance.state().nameAsString());
    }

    private boolean isInstanceTerminated(Ec2Client client) {
        Instance instance = getInstance(client);

        return instance != null && "terminated".equals(instance.state().nameAsString());
    }

    private boolean validateInstanceStop(GyroUI ui, boolean instanceStopped, String param, String value) {
        if (!instanceStopped) {
            ui.write("\n@|bold,blue Skipping update of %s since instance"
                + " must be stopped to change parameter %s to %s|@", param, param, value);
            return false;
        }

        return true;
    }

    private CapacityReservationSpecification getCapacityReservationSpecification() {
        if (("none").equals(getCapacityReservation()) || ("open").equals(getCapacityReservation())) {
            return CapacityReservationSpecification.builder()
                .capacityReservationPreference(getCapacityReservation().toLowerCase())
                .build();
        } else {
            return CapacityReservationSpecification.builder()
                .capacityReservationTarget(r -> r.capacityReservationId(getCapacityReservation()))
                .build();
        }
    }

    private IamInstanceProfileSpecification getIamInstanceProfile() {
        if (getInstanceProfile() == null) {
            return null;
        }

        return IamInstanceProfileSpecification.builder()
            .arn(getInstanceProfile().getArn())
            .build();
    }

    private boolean createInstance(Ec2Client client, RunInstancesRequest request) {
        try {
            RunInstancesResponse response = client.runInstances(request);
            if (!response.instances().isEmpty()) {
                setId(response.instances().get(0).instanceId());

                if (!getSourceDestCheck()) {
                    client.modifyNetworkInterfaceAttribute(
                        r -> r.networkInterfaceId(response.instances().get(0).networkInterfaces().get(0).networkInterfaceId())
                            .sourceDestCheck(a -> a.value(getSourceDestCheck()))
                    );
                }
            }
        } catch (Ec2Exception ex) {
            if (getInstanceProfile() != null
                && ex.awsErrorDetails().errorMessage().startsWith(String.format("Value (%s) for parameter iamInstanceProfile.arn is invalid", getInstanceProfile().getArn()))) {
                return false;
            } else {
                throw ex;
            }
        }

        return true;
    }
}
