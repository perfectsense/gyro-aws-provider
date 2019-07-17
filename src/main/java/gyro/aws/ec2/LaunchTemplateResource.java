package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.InstanceProfileResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.scope.State;
import org.apache.commons.codec.binary.Base64;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateLaunchTemplateResponse;
import software.amazon.awssdk.services.ec2.model.DescribeImagesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeLaunchTemplatesResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.LaunchTemplate;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateCapacityReservationSpecificationRequest;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateIamInstanceProfileSpecificationRequest;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateInstanceNetworkInterfaceSpecificationRequest;
import software.amazon.awssdk.services.ec2.model.ShutdownBehavior;
import software.amazon.awssdk.utils.builder.SdkBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a Launch Template from config or an existing Instance Id.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::launch-template launch-template
 *         launch-template-name: "launch-template-gyro-1"
 *         ami-name: "amzn-ami-hvm-2018.03.0.20181129-x86_64-gp2"
 *         shutdown-behavior: "STOP"
 *         instance-type: "t2.micro"
 *         key-name: "example"
 *         security-groups: [
 *             $(aws::security-group security-group-launch-template-example-1),
 *             $(aws::security-group security-group-launch-template-example-2)
 *         ]
 *         disable-api-termination: false
 *         ebs-optimized: false
 *
 *         block-device-mapping
 *             device-name: "/dev/sdb"
 *             volume-size: 100
 *             auto-enable-io: false
 *         end
 *
 *         capacity-reservation: "open"
 *
 *         network-interfaces:[
 *             $(aws::network-interface nic-example-launch-template-1),
 *             $(aws::network-interface nic-example-launch-template-2)
 *         ]
 *
 *         tags: {
 *             Name: "launch-template-example-1"
 *         }
 *     end
 *
 */
@Type("launch-template")
public class LaunchTemplateResource extends Ec2TaggableResource<LaunchTemplate> implements Copyable<LaunchTemplate> {

    private String launchTemplateId;
    private String launchTemplateName;

    private String amiId;
    private String amiName;
    private Integer coreCount;
    private Integer threadPerCore;
    private Boolean ebsOptimized;
    private Boolean configureHibernateOption;
    private String shutdownBehavior;
    private String instanceType;
    private String keyName;
    private Boolean enableMonitoring;
    private List<SecurityGroupResource> securityGroups;
    private Boolean disableApiTermination;
    private String userData;
    private List<BlockDeviceMappingResource> blockDeviceMapping;
    private String capacityReservation;
    private InstanceProfileResource instanceProfile;
    private Set<NetworkInterfaceResource> networkInterfaces;

    private Long version;

    /**
     * The name of the launch template. (Required)
     */
    public String getLaunchTemplateName() {
        return launchTemplateName;
    }

    public void setLaunchTemplateName(String launchTemplateName) {
        this.launchTemplateName = launchTemplateName;
    }

    /**
     * The ID of an AMI that would be used to launch the instance. Required if AMI Name not provided. See `Finding an AMI <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/finding-an-ami.html/>`_.
     */
    public String getAmiId() {
        return amiId;
    }

    public void setAmiId(String amiId) {
        this.amiId = amiId;
    }

    /**
     * The Name of an AMI that would be used to launch the instance. Required if AMI Id not provided. See `Amazon Machine Images (AMI) <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/AMIs.html/>`_.
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
    public String getShutdownBehavior() {
        return shutdownBehavior != null ? shutdownBehavior.toLowerCase() : ShutdownBehavior.STOP.toString();
    }

    public void setShutdownBehavior(String shutdownBehavior) {
        this.shutdownBehavior = shutdownBehavior;
    }

    /**
     * Launch instance with the type of hardware you desire. See `Instance Types <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-types.html/>`_. (Required)
     */
    public String getInstanceType() {
        return instanceType != null ? instanceType.toLowerCase() : instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    /**
     * Launch instance with the key name of an EC2 Key Pair. This is a certificate required to access your instance. See `Amazon EC2 Key Pairs < https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-key-pairs.html/>`_. (Required)
     */
    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
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
    public List<SecurityGroupResource> getSecurityGroups() {
        if (securityGroups == null) {
            securityGroups = new ArrayList<>();
        }

        return securityGroups;
    }

    public void setSecurityGroups(List<SecurityGroupResource> securityGroups) {
        this.securityGroups = securityGroups;
    }

    /**
     * Enable or Disable api termination of an instance. See `Enabling Termination Protection for an Instance <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/terminating-instances.html#Using_ChangingDisableAPITermination/>`_.
     */
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
     * Set user data for your instance. See `Instance Metadata and User Data <https://docs.aws.amazon.com/AWSEC2/latest/WindowsGuide/ec2-instance-metadata.html/>`_.
     */
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
     * Set Block device Mapping for the instances being launched using this template.
     */
    public List<BlockDeviceMappingResource> getBlockDeviceMapping() {
        if (blockDeviceMapping == null) {
            blockDeviceMapping = new ArrayList<>();
        }

        return blockDeviceMapping;
    }

    public void setBlockDeviceMapping(List<BlockDeviceMappingResource> blockDeviceMapping) {
        this.blockDeviceMapping = blockDeviceMapping;
    }

    /**
     * The capacity reservation for the instances being launched using this template.
     */
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
     * Iam instance profile to be linked with the instances being launched using this template.
     */
    public InstanceProfileResource getInstanceProfile() {
        return instanceProfile;
    }

    public void setInstanceProfile(InstanceProfileResource instanceProfile) {
        this.instanceProfile = instanceProfile;
    }

    /**
     * A set of Network Interfaces to be attached to the instances being launched using this template.
     */
    public Set<NetworkInterfaceResource> getNetworkInterfaces() {
        if (networkInterfaces == null) {
            networkInterfaces = new HashSet<>();
        }

        return networkInterfaces;
    }

    public void setNetworkInterfaces(Set<NetworkInterfaceResource> networkInterfaces) {
        this.networkInterfaces = networkInterfaces;
    }

    /**
     * The ID of the launch template.
     */
    @Id
    @Output
    public String getLaunchTemplateId() {
        return launchTemplateId;
    }

    public void setLaunchTemplateId(String launchTemplateId) {
        this.launchTemplateId = launchTemplateId;
    }

    /**
     * The version of the Launch Template.
     */
    @Output
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    protected String getResourceId() {
        return getLaunchTemplateId();
    }

    @Override
    public void copyFrom(LaunchTemplate launchTemplate) {
        setLaunchTemplateId(launchTemplate.launchTemplateId());
        setLaunchTemplateName(launchTemplate.launchTemplateName());
        setVersion(launchTemplate.latestVersionNumber());
    }

    @Override
    protected boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        LaunchTemplate launchTemplate = getLaunchTemplate(client);

        if (launchTemplate == null) {
            return false;
        }

        copyFrom(launchTemplate);

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        validate(client);

        CreateLaunchTemplateResponse response = client.createLaunchTemplate(
            r -> r.launchTemplateName(getLaunchTemplateName())
                .launchTemplateData(
                    l -> l.cpuOptions(getCoreCount() > 0
                        ? o -> o.threadsPerCore(getThreadPerCore()).coreCount(getCoreCount()).build() : SdkBuilder::build)
                        .disableApiTermination(getDisableApiTermination())
                        .ebsOptimized(getEbsOptimized())
                        .hibernationOptions(o -> o.configured(getConfigureHibernateOption()))
                        .imageId(getAmiId())
                        .instanceType(getInstanceType())
                        .instanceInitiatedShutdownBehavior(getShutdownBehavior())
                        .keyName(getKeyName())
                        .monitoring(o -> o.enabled(getEnableMonitoring()))
                        .securityGroupIds(getSecurityGroups().stream().map(SecurityGroupResource::getGroupId).collect(Collectors.toList()))
                        .userData(new String(Base64.encodeBase64(getUserData().trim().getBytes())))
                        .blockDeviceMappings(!getBlockDeviceMapping().isEmpty() ?
                            getBlockDeviceMapping()
                                .stream()
                                .map(BlockDeviceMappingResource::getLaunchTemplateBlockDeviceMapping)
                                .collect(Collectors.toList()) : null
                        )
                        .capacityReservationSpecification(getCapacityReservationSpecification())
                        .iamInstanceProfile(getLaunchTemplateInstanceProfile())
                        .networkInterfaces(!getNetworkInterfaces().isEmpty()
                            ? getNetworkInterfaces().stream()
                            .map(o -> LaunchTemplateInstanceNetworkInterfaceSpecificationRequest.builder()
                                .networkInterfaceId(o.getNetworkInterfaceId()).build())
                            .collect(Collectors.toList()) : null)));

        setLaunchTemplateId(response.launchTemplate().launchTemplateId());
        setVersion(response.launchTemplate().latestVersionNumber());
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteLaunchTemplate(r -> r.launchTemplateId(getLaunchTemplateId()));
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        String launchTemplateId = getLaunchTemplateId();

        sb.append("launch template");

        if (!ObjectUtils.isBlank(launchTemplateId)) {
            sb.append(" - ").append(launchTemplateId);

        }

        return sb.toString();
    }

    private void validate(Ec2Client client) {
        if (ObjectUtils.isBlank(getInstanceType()) || InstanceType.fromValue(getInstanceType()).equals(InstanceType.UNKNOWN_TO_SDK_VERSION)) {
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

        DescribeImagesRequest amiRequest;

        if (ObjectUtils.isBlank(getAmiId())) {
            if (ObjectUtils.isBlank(getAmiName())) {
                throw new GyroException("AMI name cannot be blank when AMI Id is not provided.");
            }

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

    private LaunchTemplate getLaunchTemplate(Ec2Client client) {
        LaunchTemplate launchTemplate = null;

        if (ObjectUtils.isBlank(getLaunchTemplateId())) {
            throw new GyroException("launch-template-id is missing, unable to load instance.");
        }

        try {
            DescribeLaunchTemplatesResponse response = client.describeLaunchTemplates(r -> r.launchTemplateIds(getLaunchTemplateId()));

            if (!response.launchTemplates().isEmpty()) {
                launchTemplate = response.launchTemplates().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return launchTemplate;
    }

    private LaunchTemplateCapacityReservationSpecificationRequest getCapacityReservationSpecification() {
        if (("none").equals(getCapacityReservation()) || ("open").equals(getCapacityReservation())) {
            return LaunchTemplateCapacityReservationSpecificationRequest.builder()
                .capacityReservationPreference(getCapacityReservation().toLowerCase())
                .build();
        } else {
            return LaunchTemplateCapacityReservationSpecificationRequest.builder()
                .capacityReservationTarget(r -> r.capacityReservationId(getCapacityReservation()))
                .build();
        }
    }

    private LaunchTemplateIamInstanceProfileSpecificationRequest getLaunchTemplateInstanceProfile() {
        if (getInstanceProfile() == null) {
            return null;
        }

        return LaunchTemplateIamInstanceProfileSpecificationRequest.builder()
            .arn(getInstanceProfile().getArn())
            .build();
    }
}