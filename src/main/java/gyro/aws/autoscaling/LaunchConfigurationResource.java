package gyro.aws.autoscaling;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.ec2.InstanceResource;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.aws.ec2.SubnetResource;
import gyro.core.GyroException;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import org.apache.commons.codec.binary.Base64;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingException;
import software.amazon.awssdk.services.autoscaling.model.DescribeLaunchConfigurationsResponse;
import software.amazon.awssdk.services.autoscaling.model.LaunchConfiguration;
import software.amazon.awssdk.services.ec2.model.InstanceType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a Launch Configuration from config or an existing Instance Id.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::launch-configuration launch-configuration
 *         launch-configuration-name: "launch-configuration-gyro-1"
 *         ami-id: "ami-01e24be29428c15b2"
 *         instance-type: "t2.micro"
 *         key-name: "instance-static"
 *         security-groups: [
 *             $(aws::security-group security-group-launch-configuration-example-1),
 *             $(aws::security-group security-group-launch-configuration-example-2)
 *         ]
 *         ebs-optimized: false
 *         enable-monitoring: true
 *         associate-public-ip: true
 *     end
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::launch-configuration launch-configuration
 *         launch-configuration-name: "launch-configuration-gyro-1"
 *         instance: $(aws:instance instance)
 *         key-name: "instance-static"
 *         security-groups: [
 *             $(aws::security-group security-group-launch-configuration-example-1),
 *             $(aws::security-group security-group-launch-configuration-example-2)
 *         ]
 *         ebs-optimized: false
 *         enable-monitoring: true
 *         associate-public-ip: true
 *     end
 */
@Type("launch-configuration")
public class LaunchConfigurationResource extends AwsResource implements Copyable<LaunchConfiguration> {

    private String launchConfigurationName;
    private InstanceResource instance;
    private String amiId;
    private Boolean ebsOptimized;
    private String instanceType;
    private String keyName;
    private Boolean enableMonitoring;
    private Set<SecurityGroupResource> securityGroups;
    private String userData;
    private Boolean associatePublicIp;

    private String arn;

    /**
     * The name of the launch configuration. (Required)
     */
    @Id
    public String getLaunchConfigurationName() {
        return launchConfigurationName;
    }

    public void setLaunchConfigurationName(String launchConfigurationName) {
        this.launchConfigurationName = launchConfigurationName;
    }

    /**
     * A launched instance that would be used as a skeleton to create the launch configuration. Required if AMI Name/ AMI ID not provided.
     */
    public InstanceResource getInstance() {
        return instance;
    }

    public void setInstance(InstanceResource instance) {
        this.instance = instance;
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
     * Enable private Ip to intsances launched. See `Creating Launch Configuration <https://docs.aws.amazon.com/autoscaling/ec2/userguide/create-launch-config.html/>`_.
     */
    public Boolean getAssociatePublicIp() {
        if (associatePublicIp == null) {
            associatePublicIp = false;
        }
        return associatePublicIp;
    }

    public void setAssociatePublicIp(Boolean associatePublicIp) {
        this.associatePublicIp = associatePublicIp;
    }

    /**
     * The arn of the launch configuration
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public boolean refresh() {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        LaunchConfiguration launchConfiguration = getLaunchConfiguration(client);

        if (launchConfiguration == null) {
            return false;
        }

        copyFrom(launchConfiguration);

        return true;
    }

    @Override
    public void create() {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        validate();

        client.createLaunchConfiguration(
            r -> r.launchConfigurationName(getLaunchConfigurationName())
                .ebsOptimized(getEbsOptimized())
                .imageId(getInstance() == null ? getAmiId() : null)
                .instanceMonitoring(o -> o.enabled(getEnableMonitoring()))
                .securityGroups(getSecurityGroups().stream().map(SecurityGroupResource::getGroupId).collect(Collectors.toList()))
                .userData(new String(Base64.encodeBase64(getUserData().trim().getBytes())))
                .keyName(getKeyName())
                .instanceType(getInstance() == null ? getInstanceType() : null)
                .instanceId(getInstance() != null ? getInstance().getInstanceId() : null)
                .associatePublicIpAddress(getAssociatePublicIp())
        );
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {

    }

    @Override
    public void delete() {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        client.deleteLaunchConfiguration(r -> r.launchConfigurationName(getLaunchConfigurationName()));
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("launch configuration");

        if (!ObjectUtils.isBlank(getLaunchConfigurationName())) {
            sb.append(" - ").append(getLaunchConfigurationName());

        }

        return sb.toString();
    }

    @Override
    public void copyFrom(LaunchConfiguration launchConfiguration) {
        setAssociatePublicIp(launchConfiguration.associatePublicIpAddress());
        setInstanceType(launchConfiguration.instanceType());
        setKeyName(launchConfiguration.keyName());
        setUserData(launchConfiguration.userData());
        setEnableMonitoring(launchConfiguration.instanceMonitoring().enabled());
        setEbsOptimized(launchConfiguration.ebsOptimized());
        setArn(launchConfiguration.launchConfigurationARN());
        setLaunchConfigurationName(launchConfiguration.launchConfigurationName());
        setSecurityGroups(launchConfiguration.securityGroups().stream().map(o -> findById(SecurityGroupResource.class, o)).collect(Collectors.toSet()));
    }

    private LaunchConfiguration getLaunchConfiguration(AutoScalingClient client) {
        LaunchConfiguration launchConfiguration = null;

        if (ObjectUtils.isBlank(getLaunchConfigurationName())) {
            throw new GyroException("launch-configuration-name is missing, unable to load launch configuration.");
        }

        try {
            DescribeLaunchConfigurationsResponse response = client.describeLaunchConfigurations(
                r -> r.launchConfigurationNames(getLaunchConfigurationName())
            );

            if (!response.launchConfigurations().isEmpty()) {
                launchConfiguration = response.launchConfigurations().get(0);
            }
        } catch (AutoScalingException ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return launchConfiguration;
    }

    private void validate() {
        if (getInstance() == null) {

            if (ObjectUtils.isBlank(getInstanceType()) || InstanceType.fromValue(getInstanceType()).equals(InstanceType.UNKNOWN_TO_SDK_VERSION)) {
                throw new GyroException("The value - (" + getInstanceType() + ") is invalid for parameter Instance Type.");
            }

            if (getSecurityGroups().isEmpty()) {
                throw new GyroException("At least one security group is required.");
            }
        }
    }
}
