package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.InstanceProfileResource;
import gyro.core.GyroException;
import gyro.core.GyroInstance;
import gyro.core.GyroUI;
import gyro.core.Wait;
import gyro.core.resource.DiffableInternals;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.diff.Context;
import org.apache.commons.codec.binary.Base64;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AttributeBooleanValue;
import software.amazon.awssdk.services.ec2.model.CapacityReservationSpecification;
import software.amazon.awssdk.services.ec2.model.DescribeImagesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceAttributeResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeNetworkInterfaceAttributeResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.IamInstanceProfileSpecification;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceAttributeName;
import software.amazon.awssdk.services.ec2.model.InstanceBlockDeviceMapping;
import software.amazon.awssdk.services.ec2.model.InstanceStateName;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.MonitoringState;
import software.amazon.awssdk.services.ec2.model.NetworkInterfaceAttribute;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.ec2.model.ShutdownBehavior;
import software.amazon.awssdk.utils.builder.SdkBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Creates an Instance with the specified AMI, Subnet and Security group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::instance instance-example
 *         ami-name: "amzn-ami-hvm-2018.03.0.20181129-x86_64-gp2"
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
 *         volume
 *             device-name: "/dev/sde"
 *             volume: $(aws::ebs-volume volume)
 *         end
 *
 *         capacity-reservation: "none"
 *     end
 */
@Type("instance")
public class InstanceResource extends Ec2TaggableResource<Instance> implements GyroInstance, Copyable<Instance> {

    private String amiId;
    private String amiName;
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
    private Set<BlockDeviceMappingResource> blockDeviceMapping;
    private Set<InstanceVolumeAttachment> volume;
    private InstanceProfileResource instanceProfile;

    // -- Readonly

    private String instanceId;
    private String privateIpAddress;
    private String publicIpAddress;
    private String publicDnsName;
    private String instanceState;
    private Date launchDate;

    /**
     * The ID of an AMI that would be used to launch the instance. Required if AMI Name not provided. See Finding an AMI `<https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/finding-an-ami.html/>`_.
     */
    public String getAmiId() {
        return amiId;
    }

    public void setAmiId(String amiId) {
        this.amiId = amiId;
    }

    /**
     * The Name of an AMI that would be used to launch the instance. Required if AMI Id not provided. See Amazon Machine Images (AMI) `<https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/AMIs.html/>`_.
     */
    public String getAmiName() {
        return amiName;
    }

    public void setAmiName(String amiName) {
        this.amiName = amiName;
    }

    /**
     * Launch instances with defined number of cores. Defaults to 0 which sets its to the instance type defaults. See `Optimizing CPU Options <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-optimize-cpu.html/>`_.
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
     * Launch instances with defined number of threads per cores. Defaults to 0 which sets its to the instance type defaults. See `Optimizing CPU Options <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-optimize-cpu.html/>`_.
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
     * Enable EBS optimization for an instance. Defaults to false. See `Amazon EBS–Optimized Instances <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/EBSOptimized.html/>`_.
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
     * Enable Hibernate options for an instance. Defaults to false. See `Hibernate your Instances <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/Hibernate.html/>`_.
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
     * Change the Shutdown Behavior options for an instance. Defaults to Stop. See `Changing the Instance Initiated Shutdown Behavior <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/terminating-instances.html#Using_ChangingInstanceInitiatedShutdownBehavior/>`_.
     */
    @Updatable
    public String getShutdownBehavior() {
        return shutdownBehavior != null ? shutdownBehavior.toLowerCase() : ShutdownBehavior.STOP.toString();
    }

    public void setShutdownBehavior(String shutdownBehavior) {
        this.shutdownBehavior = shutdownBehavior;
    }

    /**
     * Launch instance with the type of hardware you desire. See `Instance Types <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-types.html/>`_. (Required)
     */
    @Updatable
    public String getInstanceType() {
        return instanceType != null ? instanceType.toLowerCase() : instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    /**
     * Launch instance with the key name of an EC2 Key Pair. This is a certificate required to access your instance. See `Amazon EC2 Key Pairs < https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-key-pairs.html/>`_. (Required)
     */
    public KeyPairResource getKey() {
        return key;
    }

    public void setKey(KeyPairResource key) {
        this.key = key;
    }

    /**
     * Enable or Disable monitoring for your instance. See `Monitoring Your Instances <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-cloudwatch.html/>`_.
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
     * Launch instance with the security groups specified. See `Amazon EC2 Security Groups for Linux Instances <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-network-security.html/>`_. (Required)
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
     * Launch instance with the subnet specified. See `Vpcs and Subnets <https://docs.aws.amazon.com/vpc/latest/userguide/VPC_Subnets.html/>`_. (Required)
     */
    public SubnetResource getSubnet() {
        return subnet;
    }

    public void setSubnet(SubnetResource subnet) {
        this.subnet = subnet;
    }

    /**
     * Enable or Disable api termination of an instance. See `Enabling Termination Protection for an Instance <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/terminating-instances.html#Using_ChangingDisableAPITermination/>`_.
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
     * Enable or Disable Source/Dest Check for an instance. Defaults to true. See `Disabling Source/Destination Checks <https://docs.aws.amazon.com/vpc/latest/userguide/VPC_NAT_Instance.html#EIP_Disable_SrcDestCheck/>`_.
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
     * Set user data for your instance. See `Instance Metadata and User Data <https://docs.aws.amazon.com/AWSEC2/latest/WindowsGuide/ec2-instance-metadata.html/>`_.
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
     * @subresource gyro.core.ec2.BlockDeviceMappingResource
     */
    public Set<BlockDeviceMappingResource> getBlockDeviceMapping() {
        if (blockDeviceMapping == null) {
            blockDeviceMapping = new HashSet<>();
        }

        return blockDeviceMapping;
    }

    public void setBlockDeviceMapping(Set<BlockDeviceMappingResource> blockDeviceMapping) {
        this.blockDeviceMapping = blockDeviceMapping;
    }

    /**
     * Attach existing volumes to the instance.
     *
     * @subresource gyro.core.ec2.InstanceVolumeAttachment
     */
    @Updatable
    public Set<InstanceVolumeAttachment> getVolume() {
        if (volume == null) {
            volume = new HashSet<>();
        }

        return volume;
    }

    public void setVolume(Set<InstanceVolumeAttachment> volume) {
        this.volume = volume;
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
     * Instance ID of this instance.
     */
    @Id
    @Output
    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * The private IP of this instance.
     */
    @Output
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
    public String getState() {
        return getInstanceState();
    }

    @Override
    public String getHostname() {
        return getPublicDnsName();
    }

    @Override
    public String getName() {
        if (getTags().isEmpty()) {
            return DiffableInternals.getName(this);
        }

        return getTags().get("Name");
    }

    @Override
    public String getLaunchDate() {
        if (getInstanceLaunchDate() != null) {
            return getInstanceLaunchDate().toString();
        }

        return "";
    }

    @Override
    protected String getId() {
        return getInstanceId();
    }

    @Override
    public void copyFrom(Instance instance) {
        Ec2Client client = createClient(Ec2Client.class);
        setInstanceId(instance.instanceId());
        init(instance, client);
    }

    @Override
    protected boolean doRefresh() {

        Ec2Client client = createClient(Ec2Client.class);

        Instance instance = getInstance(client);

        if (instance == null || instance.state().name() == InstanceStateName.TERMINATED) {
            return false;
        }

        copyFrom(instance);

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, Context context) {
        Ec2Client client = createClient(Ec2Client.class);

        validate(true);

        loadAmi(client);

        RunInstancesRequest.Builder builder = RunInstancesRequest.builder();
        builder = builder.imageId(getAmiId())
            .ebsOptimized(getEbsOptimized())
            .hibernationOptions(o -> o.configured(getConfigureHibernateOption()))
            .instanceInitiatedShutdownBehavior(getShutdownBehavior())
            .cpuOptions(getCoreCount() > 0 ? o -> o.threadsPerCore(getThreadPerCore()).coreCount(getCoreCount()).build() : SdkBuilder::build)
            .instanceType(getInstanceType())
            .keyName(getKey() != null ? getKey().getKeyName() : null)
            .maxCount(1)
            .minCount(1)
            .monitoring(o -> o.enabled(getEnableMonitoring()))
            .securityGroupIds(getSecurityGroups().stream().map(SecurityGroupResource::getGroupId).collect(Collectors.toList()))
            .subnetId(getSubnet().getSubnetId())
            .disableApiTermination(getDisableApiTermination())
            .userData(new String(Base64.encodeBase64(getUserData().trim().getBytes())))
            .capacityReservationSpecification(getCapacityReservationSpecification())
            .iamInstanceProfile(getIamInstanceProfile());

        if (!getBlockDeviceMapping().isEmpty()) {
            builder = builder.blockDeviceMappings(
                getBlockDeviceMapping().stream()
                    .map(BlockDeviceMappingResource::getBlockDeviceMapping)
                    .collect(Collectors.toList())
            );
        }

        RunInstancesRequest request = builder.build();

        boolean status = Wait.atMost(60, TimeUnit.SECONDS)
            .prompt(false)
            .checkEvery(10, TimeUnit.SECONDS)
            .until(() -> createInstance(client, request));

        if (!status) {
            throw new GyroException(String.format("Value (%s) for parameter iamInstanceProfile.arn is invalid.", getInstanceProfile().getArn()));
        }

        context.save();

        boolean waitResult = Wait.atMost(3, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> isInstanceRunning(client));

        if (!waitResult) {
            throw new GyroException("Unable to reach 'running' state for ec2 instance - " + getInstanceId());
        }

        Instance instance = getInstance(client);

        if (instance != null) {
            setPublicDnsName(instance.publicDnsName());
            setPublicIpAddress(instance.publicIpAddress());
            setPrivateIpAddress(instance.privateIpAddress());
            setInstanceState(instance.state().nameAsString());
            setInstanceLaunchDate(Date.from(instance.launchTime()));
            
            loadVolume(getInstance(client));
        }
    }

    @Override
    protected void doUpdate(GyroUI ui, Context context, AwsResource config, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        validate(false);

        if (changedProperties.contains("shutdown-behavior")) {
            client.modifyInstanceAttribute(
                r -> r.instanceId(getInstanceId())
                    .instanceInitiatedShutdownBehavior(o -> o.value(getShutdownBehavior()))
            );
        }

        if (changedProperties.contains("disable-api-termination")) {
            client.modifyInstanceAttribute(
                r -> r.instanceId(getInstanceId())
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
            getSecurityGroups().forEach(r -> securityGroupIds.add(r.getGroupId()));

            client.modifyInstanceAttribute(
                r -> r.instanceId(getInstanceId())
                    .groups(securityGroupIds)
            );
        }

        boolean instanceStopped = isInstanceStopped(client);

        if (changedProperties.contains("instance-type")
            && validateInstanceStop(ui, instanceStopped, "instance-type", getInstanceType())) {
            client.modifyInstanceAttribute(
                r -> r.instanceId(getInstanceId())
                    .instanceType(o -> o.value(getInstanceType()))
            );
        }

        if (changedProperties.contains("ebs-optimized")
            && validateInstanceStop(ui, instanceStopped, "ebs-optimized", getEbsOptimized().toString())) {
            client.modifyInstanceAttribute(
                r -> r.instanceId(getInstanceId())
                    .ebsOptimized(o -> o.value(getEbsOptimized()))
            );
        }

        if (changedProperties.contains("user-data")
            && validateInstanceStop(ui, instanceStopped, "user-data", getUserData())) {
            client.modifyInstanceAttribute(
                r -> r.instanceId(getInstanceId())
                    .userData(o -> o.value(SdkBytes.fromByteArray(getUserData().getBytes())))
            );
        }

        if (changedProperties.contains("capacity-reservation")
            && validateInstanceStop(ui, instanceStopped, "capacity-reservation", getCapacityReservation())) {
            client.modifyInstanceCapacityReservationAttributes(
                r -> r.instanceId(getInstanceId())
                    .capacityReservationSpecification(getCapacityReservationSpecification())
            );
        }
    }

    @Override
    public void delete(GyroUI ui, Context context) {
        if (getDisableApiTermination()) {
            throw new GyroException("The instance (" + getInstanceId() + ") cannot be terminated when 'disableApiTermination' is set to True.");
        }

        Ec2Client client = createClient(Ec2Client.class);

        client.terminateInstances(r -> r.instanceIds(Collections.singletonList(getInstanceId())));

        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> isInstanceTerminated(client));
    }

    private void init(Instance instance, Ec2Client client) {
        setAmiId(instance.imageId());
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
            r -> r.instanceId(getInstanceId()).attribute(InstanceAttributeName.INSTANCE_INITIATED_SHUTDOWN_BEHAVIOR)
        );
        setShutdownBehavior(attributeResponse.instanceInitiatedShutdownBehavior().value());

        attributeResponse = client.describeInstanceAttribute(
            r -> r.instanceId(getInstanceId()).attribute(InstanceAttributeName.DISABLE_API_TERMINATION)
        );
        setDisableApiTermination(attributeResponse.disableApiTermination().equals(AttributeBooleanValue.builder().value(true).build()));

        DescribeNetworkInterfaceAttributeResponse response = client.describeNetworkInterfaceAttribute(
            r -> r.networkInterfaceId(instance.networkInterfaces().get(0).networkInterfaceId())
                .attribute(NetworkInterfaceAttribute.SOURCE_DEST_CHECK)
        );
        setSourceDestCheck(response.sourceDestCheck().value());

        attributeResponse = client.describeInstanceAttribute(
            r -> r.instanceId(getInstanceId()).attribute(InstanceAttributeName.USER_DATA)
        );
        setUserData(attributeResponse.userData().value() == null
            ? "" : new String(Base64.decodeBase64(attributeResponse.userData().value())).trim());

        loadVolume(instance);
    }

    private void validate(boolean isCreate) {
        if (ObjectUtils.isBlank(getShutdownBehavior())
            || ShutdownBehavior.fromValue(getShutdownBehavior()).equals(ShutdownBehavior.UNKNOWN_TO_SDK_VERSION)) {
            throw new GyroException("The value - (" + getShutdownBehavior() + ") is invalid for parameter Shutdown Behavior.");
        }

        if (ObjectUtils.isBlank(getInstanceType())
            || InstanceType.fromValue(getInstanceType()).equals(InstanceType.UNKNOWN_TO_SDK_VERSION)) {
            throw new GyroException("The value - (" + getInstanceType() + ") is invalid for parameter Instance Type.");
        }

        if (getSecurityGroups().isEmpty()) {
            throw new GyroException("At least one security group is required.");
        }

        if (!getCapacityReservation().equalsIgnoreCase("none")
            && !getCapacityReservation().equalsIgnoreCase("open")
            && !getCapacityReservation().startsWith("cr-")) {
            throw new GyroException("The value - (" + getCapacityReservation() + ") is invalid for parameter 'capacity-reservation'. "
                + "Valid values [ 'open', 'none', capacity reservation id like cr-% ]");
        }

        if (ObjectUtils.isBlank(getAmiId()) && ObjectUtils.isBlank(getAmiName())) {
                throw new GyroException("AMI name cannot be blank when AMI Id is not provided.");
        }
    }

    private void loadAmi(Ec2Client client) {
        DescribeImagesRequest amiRequest;

        if (ObjectUtils.isBlank(getAmiId())) {
            amiRequest = DescribeImagesRequest.builder().filters(
                Collections.singletonList(Filter.builder().name("name").values(getAmiName()).build())
            ).build();

        } else {
            amiRequest = DescribeImagesRequest.builder().imageIds(getAmiId()).build();
        }

        try {
            DescribeImagesResponse response = client.describeImages(amiRequest);
            if (response.images().isEmpty()) {
                throw new GyroException("No AMI found for value - (" + getAmiName() + ") as an AMI Name.");
            }
            setAmiId(response.images().get(0).imageId());
        } catch (Ec2Exception ex) {
            if (ex.awsErrorDetails().errorCode().equalsIgnoreCase("InvalidAMIID.Malformed")) {
                throw new GyroException("No AMI found for value - (" + getAmiId() + ") as an AMI Id.");
            }

            throw ex;
        }
    }

    private Instance getInstance(Ec2Client client) {
        if (ObjectUtils.isBlank(getInstanceId())) {
            throw new GyroException("instance-id is missing, unable to load instance.");
        }

        try {
            DescribeInstancesResponse response = client.describeInstances(r -> r.instanceIds(getInstanceId()));

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

    private void loadVolume(Instance instance) {
        Set<String> reservedDeviceNameSet = getBlockDeviceMapping()
            .stream()
            .map(BlockDeviceMappingResource::getDeviceName)
            .collect(Collectors.toSet());

        reservedDeviceNameSet.add(instance.rootDeviceName());

        getVolume().clear();

        setVolume(instance.blockDeviceMappings().stream()
            .filter(o -> !reservedDeviceNameSet.contains(o.deviceName()))
            .map(this::getInstanceVolumeAttachment)
            .collect(Collectors.toSet()));
    }


    private InstanceVolumeAttachment getInstanceVolumeAttachment(InstanceBlockDeviceMapping instanceBlockDeviceMapping) {
        InstanceVolumeAttachment instanceVolumeAttachment = newSubresource(InstanceVolumeAttachment.class);
        instanceVolumeAttachment.copyFrom(instanceBlockDeviceMapping);

        return instanceVolumeAttachment;
    }

    private boolean createInstance(Ec2Client client, RunInstancesRequest request) {
        try {
            RunInstancesResponse response = client.runInstances(request);
            if (!response.instances().isEmpty()) {
                setInstanceId(response.instances().get(0).instanceId());
              
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
