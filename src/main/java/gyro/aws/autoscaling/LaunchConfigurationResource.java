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

package gyro.aws.autoscaling;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.ec2.AmiResource;
import gyro.aws.ec2.BlockDeviceMapping;
import gyro.aws.ec2.InstanceResource;
import gyro.aws.ec2.KeyPairResource;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.aws.iam.InstanceProfileResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.TimeoutSettings;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import org.apache.commons.codec.binary.Base64;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingException;
import software.amazon.awssdk.services.autoscaling.model.CreateLaunchConfigurationRequest;
import software.amazon.awssdk.services.autoscaling.model.DescribeLaunchConfigurationsResponse;
import software.amazon.awssdk.services.autoscaling.model.LaunchConfiguration;
import software.amazon.awssdk.services.ec2.model.InstanceType;

/**
 * Creates a Launch Configuration from config or an existing Instance Id.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::launch-configuration launch-configuration
 *         name: "launch-configuration-gyro-1"
 *         ami: "ami-01e24be29428c15b2"
 *         instance-type: "t2.micro"
 *         key: "key-example"
 *         security-groups: [
 *             $(aws::security-group security-group-launch-configuration-example-1),
 *             $(aws::security-group security-group-launch-configuration-example-2)
 *         ]
 *         ebs-optimized: false
 *         enable-monitoring: true
 *         associate-public-ip: true
 *     end
 *
 * .. code-block:: gyro
 *
 *     aws::launch-configuration launch-configuration
 *         name: "launch-configuration-gyro-1"
 *         instance: $(aws:instance instance)
 *         key: "instance-static"
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

    private String name;
    private InstanceResource instance;
    private AmiResource ami;
    private String classicLinkVpcId;
    private List<String> classicLinkVpcSecurityGroups;
    private Boolean ebsOptimized;
    private String instanceType;
    private String kernelId;
    private KeyPairResource key;
    private Boolean enableMonitoring;
    private String placementTenacy;
    private String ramdiskId;
    private Set<SecurityGroupResource> securityGroups;
    private String spotPrice;
    private String userData;
    private Boolean associatePublicIp;
    private Set<BlockDeviceMapping> blockDeviceMapping;
    private InstanceProfileResource instanceProfile;

    /**
     * The name of the launch configuration.
     */
    @Required
    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The launched instance that would be used as a skeleton to create the launch configuration. Required if AMI Name/ AMI ID not provided.
     */
    public InstanceResource getInstance() {
        return instance;
    }

    public void setInstance(InstanceResource instance) {
        this.instance = instance;
    }

    /**
     * The AMI that would be used to launch the instance. Required if Instance not provided. See `Finding an AMI <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/finding-an-ami.html>`_.
     */
    public AmiResource getAmi() {
        return ami;
    }

    public void setAmi(AmiResource ami) {
        this.ami = ami;
    }

    /**
     * The ID of the ClassicLink-enabled VPC.
     */
    public String getClassicLinkVpcId() {
        return classicLinkVpcId;
    }

    public void setClassicLinkVpcId(String classicLinkVpcId) {
        this.classicLinkVpcId = classicLinkVpcId;
    }

    /**
     * The IDs of the security groups for the specified ClassicLink-enabled VPC.
     */
    public List<String> getClassicLinkVpcSecurityGroups() {
        return classicLinkVpcSecurityGroups;
    }

    public void setClassicLinkVpcSecurityGroups(List<String> classicLinkVpcSecurityGroups) {
        this.classicLinkVpcSecurityGroups = classicLinkVpcSecurityGroups;
    }

    /**
     * When set to ``true``, EBS optimization for an instance is enabled. Defaults to ``false``. See `Amazon EBS–Optimized Instances <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/EBSOptimized.html>`_.
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
     * The launch instance with the type of hardware you desire. See `Instance Types <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-types.html>`_.
     */
    public String getInstanceType() {
        return instanceType != null ? instanceType.toLowerCase() : instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    /**
     * The ID of the kernel associated with the AMI.
     */
    public String getKernelId() {
        return kernelId;
    }

    public void setKernelId(String kernelId) {
        this.kernelId = kernelId;
    }

    /**
     * The launch instance with an EC2 Key Pair. This is a certificate required to access your instance. See `Amazon EC2 Key Pairs <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-key-pairs.html>`_.
     */
    public KeyPairResource getKey() {
        return key;
    }

    public void setKey(KeyPairResource key) {
        this.key = key;
    }

    /**
     * When set to ``true``, monitoring for your instance is enabled. See `Monitoring Your Instances <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-cloudwatch.html>`_.
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
     * The tenancy of the instance of the launch configuration.
     */
    @ValidStrings({ "default", "dedicated" })
    public String getPlacementTenacy() {
        return placementTenacy;
    }

    public void setPlacementTenacy(String placementTenacy) {
        this.placementTenacy = placementTenacy;
    }

    /**
     * The ID of the RAM disk to select for the launch configuration.
     */
    public String getRamdiskId() {
        return ramdiskId;
    }

    public void setRamdiskId(String ramdiskId) {
        this.ramdiskId = ramdiskId;
    }

    /**
     * The launch instance with the security groups specified. See `Amazon EC2 Security Groups for Linux Instances <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-network-security.html>`_.
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
     * The maximum hourly price for any Spot instance launched.
     */
    public String getSpotPrice() {
        return spotPrice;
    }

    public void setSpotPrice(String spotPrice) {
        this.spotPrice = spotPrice;
    }

    /**
     * The user data for your instance. See `Instance Metadata and User Data <https://docs.aws.amazon.com/AWSEC2/latest/WindowsGuide/ec2-instance-metadata.html>`_.
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
     * When set to ``true``, set public IP for launched instances. See `Creating Launch Configuration <https://docs.aws.amazon.com/autoscaling/ec2/userguide/create-launch-config.html>`_.
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
     * The block device mapping to initialize the instances with.
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
     * The IAM instance profile to be linked with the instances being launched using this.
     */
    public InstanceProfileResource getInstanceProfile() {
        return instanceProfile;
    }

    public void setInstanceProfile(InstanceProfileResource instanceProfile) {
        this.instanceProfile = instanceProfile;
    }

    @Override
    public void copyFrom(LaunchConfiguration launchConfiguration) {
        setClassicLinkVpcId(launchConfiguration.classicLinkVPCId());
        setClassicLinkVpcSecurityGroups(launchConfiguration.classicLinkVPCSecurityGroups());
        setAssociatePublicIp(launchConfiguration.associatePublicIpAddress());
        setInstanceType(launchConfiguration.instanceType());
        setKernelId(launchConfiguration.kernelId());
        setKey(!ObjectUtils.isBlank(launchConfiguration.keyName()) ? findById(
            KeyPairResource.class, launchConfiguration.keyName()) : null);
        setUserData(new String(Base64.decodeBase64(launchConfiguration.userData())));
        setEnableMonitoring(launchConfiguration.instanceMonitoring().enabled());
        setPlacementTenacy(launchConfiguration.placementTenancy());
        setRamdiskId(launchConfiguration.ramdiskId());
        setEbsOptimized(launchConfiguration.ebsOptimized());
        setName(launchConfiguration.launchConfigurationName());
        setSpotPrice(launchConfiguration.spotPrice());
        setSecurityGroups(launchConfiguration.securityGroups().stream().map(o ->
            findById(SecurityGroupResource.class, o)).collect(Collectors.toSet()));
        setInstanceProfile(!ObjectUtils.isBlank(launchConfiguration.iamInstanceProfile())
            ? findById(InstanceProfileResource.class, launchConfiguration.iamInstanceProfile()) : null);
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
    public void create(GyroUI ui, State state) {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        CreateLaunchConfigurationRequest request = CreateLaunchConfigurationRequest.builder()
            .launchConfigurationName(getName())
            .classicLinkVPCId(getClassicLinkVpcId())
            .classicLinkVPCSecurityGroups(getClassicLinkVpcSecurityGroups())
            .ebsOptimized(getEbsOptimized())
            .imageId(getInstance() == null ? getAmi().getId() : null)
            .instanceMonitoring(o -> o.enabled(getEnableMonitoring()))
            .kernelId(getKernelId())
            .placementTenancy(getPlacementTenacy())
            .ramdiskId(getRamdiskId())
            .securityGroups(getSecurityGroups().stream().map(SecurityGroupResource::getId).collect(Collectors.toList()))
            .spotPrice(getSpotPrice())
            .userData(new String(Base64.encodeBase64(getUserData().trim().getBytes())))
            .keyName(getKey() != null ? getKey().getName() : null)
            .instanceType(getInstance() == null ? getInstanceType() : null)
            .instanceId(getInstance() != null ? getInstance().getId() : null)
            .associatePublicIpAddress(getAssociatePublicIp())
            .blockDeviceMappings(!getBlockDeviceMapping().isEmpty() ?
                getBlockDeviceMapping().stream().map(BlockDeviceMapping::getAutoscalingBlockDeviceMapping)
                    .collect(Collectors.toList()) : null)
            .iamInstanceProfile(getInstanceProfile() != null ? getInstanceProfile().getArn() : null)
            .build();

        // Wait for instance profile to be ready for use if present
        boolean status = Wait.atMost(60, TimeUnit.SECONDS)
            .prompt(false)
            .resourceOverrides(this, TimeoutSettings.Action.CREATE)
            .checkEvery(10, TimeUnit.SECONDS)
            .until(() -> createLaunchConfig(client, request));

        if (!status) {
            throw new GyroException("Invalid IamInstanceProfile: " + getInstanceProfile().getArn());
        }
    }

    private boolean createLaunchConfig(AutoScalingClient client, CreateLaunchConfigurationRequest request) {
        try {
            client.createLaunchConfiguration(request);
        } catch (AutoScalingException ex) {
            if (getInstanceProfile() != null && ex.awsErrorDetails()
                .errorMessage().equals("Invalid IamInstanceProfile: " + getInstanceProfile().getArn())) {
                return false;
            } else {
                throw ex;
            }
        }

        return true;
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {

    }

    @Override
    public void delete(GyroUI ui, State state) {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        client.deleteLaunchConfiguration(r -> r.launchConfigurationName(getName()));
    }

    private LaunchConfiguration getLaunchConfiguration(AutoScalingClient client) {
        LaunchConfiguration launchConfiguration = null;

        if (ObjectUtils.isBlank(getName())) {
            throw new GyroException("name is missing, unable to load launch configuration.");
        }

        try {
            DescribeLaunchConfigurationsResponse response = client.describeLaunchConfigurations(
                r -> r.launchConfigurationNames(getName())
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

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getInstance() == null) {

            if (ObjectUtils.isBlank(getInstanceType()) || InstanceType.fromValue(getInstanceType())
                .equals(InstanceType.UNKNOWN_TO_SDK_VERSION)) {
                errors.add(new ValidationError(this, null,
                    "The value - (" + getInstanceType() + ") is invalid for parameter Instance Type."));
            }

            if (getSecurityGroups().isEmpty()) {
                errors.add(new ValidationError(this, null, "At least one security group is required."));
            }
        }

        return errors;
    }
}
