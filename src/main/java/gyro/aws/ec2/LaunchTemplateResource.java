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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.InstanceProfileResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.DependsOn;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import org.apache.commons.codec.binary.Base64;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateLaunchTemplateResponse;
import software.amazon.awssdk.services.ec2.model.CreateLaunchTemplateVersionResponse;
import software.amazon.awssdk.services.ec2.model.DescribeLaunchTemplatesResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.LaunchTemplate;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateIamInstanceProfileSpecificationRequest;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateInstanceNetworkInterfaceSpecificationRequest;
import software.amazon.awssdk.services.ec2.model.RequestLaunchTemplateData;

/**
 * Creates a Launch Template from config or an existing Instance Id.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::launch-template launch-template
 *         name: "launch-template-gyro-1"
 *         ami: "amzn-ami-hvm-2018.03.0.20181129-x86_64-gp2"
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

    private String name;
    private AmiResource ami;
    private Boolean ebsOptimized;
    private Boolean configureHibernateOption;
    private String shutdownBehavior;
    private String instanceType;
    private String keyName;
    private List<SecurityGroupResource> securityGroups;
    private Boolean disableApiTermination;
    private String userData;
    private List<BlockDeviceMapping> blockDeviceMapping;
    private LaunchTemplateCapacityReservation capacityReservation;
    private InstanceProfileResource instanceProfile;
    private Set<NetworkInterfaceResource> networkInterfaces;
    private LaunchTemplateMetadataOptions metadataOptions;
    private LaunchTemplateCreditSpecification creditSpecification;
    private List<LaunchTemplateElasticGpuSpecification> elasticGpuSpecification;
    private List<LaunchTemplateElasticInferenceAccelerator> inferenceAccelerator;
    private LaunchTemplateEnclaveOptions enclaveOptions;
    private LaunchTemplateHibernationOptions hibernationOptions;
    private LaunchTemplateInstanceMarketOptions marketOptions;
    private String kernelId;
    private String ramDiskId;
    private LaunchTemplatePlacement placement;
    private List<LaunchTemplateTagSpecification> tagSpecification;
    private LaunchTemplateCpuOptions cpuOptions;
    private LaunchTemplateMonitoring monitoring;

    // Read-only
    private String id;
    private Long version;

    /**
     * The name of the launch template.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The AMI to be used to launch the Instance created by this Template.
     */
    @Updatable
    public AmiResource getAmi() {
        return ami;
    }

    public void setAmi(AmiResource ami) {
        this.ami = ami;
    }

    /**
     * When set to ``true``, EBS optimization for an instance is enabled. Defaults to false. See `Amazon EBS–Optimized Instances <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/EBSOptimized.html/>`_.
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
     * When set to ``true``, hibernate options for an instance are enabled. Defaults to false. See `Hibernate your Instances <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/Hibernate.html/>`_.
     */
    @Updatable
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
     * The shutdown behavior options for an instance. See `Changing the Instance Initiated Shutdown Behavior <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/terminating-instances.html#Using_ChangingInstanceInitiatedShutdownBehavior/>`_.
     */
    @Updatable
    @ValidStrings({ "stop", "terminate" })
    public String getShutdownBehavior() {
        return shutdownBehavior;
    }

    public void setShutdownBehavior(String shutdownBehavior) {
        this.shutdownBehavior = shutdownBehavior;
    }

    /**
     * The launch instance with the type of hardware you desire. See `Instance Types <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-types.html/>`_.
     */
    @Updatable
    public String getInstanceType() {
        return instanceType != null ? instanceType.toLowerCase() : instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    /**
     * The launch instance with the key name of an EC2 Key Pair. This is a certificate required to access your instance. See `Amazon EC2 Key Pairs < https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-key-pairs.html/>`_.
     */
    @Updatable
    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    /**
     * The security groups associated with the launch instance. See `Amazon EC2 Security Groups for Linux Instances <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-network-security.html/>`_. Required if Network Interface not configured.
     */
    @Updatable
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
     * When set to ``true``, api termination of an instance is enabled. See `Enabling Termination Protection for an Instance <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/terminating-instances.html#Using_ChangingDisableAPITermination/>`_.
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
     * The user data for your instance. See `Instance Metadata and User Data <https://docs.aws.amazon.com/AWSEC2/latest/WindowsGuide/ec2-instance-metadata.html/>`_.
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
     * The block device Mapping for the instances being launched using this template.
     *
     * @subresource gyro.aws.ec2.AmiBlockDeviceMapping
     */
    @Updatable
    public List<BlockDeviceMapping> getBlockDeviceMapping() {
        if (blockDeviceMapping == null) {
            blockDeviceMapping = new ArrayList<>();
        }

        return blockDeviceMapping;
    }

    public void setBlockDeviceMapping(List<BlockDeviceMapping> blockDeviceMapping) {
        this.blockDeviceMapping = blockDeviceMapping;
    }

    /**
     * The capacity reservation for the instances being launched using this template.
     */
    @Updatable
    public LaunchTemplateCapacityReservation getCapacityReservation() {
        return capacityReservation;
    }

    public void setCapacityReservation(LaunchTemplateCapacityReservation capacityReservation) {
        this.capacityReservation = capacityReservation;
    }

    /**
     * Iam instance profile to be linked with the instances being launched using this template.
     */
    @Updatable
    public InstanceProfileResource getInstanceProfile() {
        return instanceProfile;
    }

    public void setInstanceProfile(InstanceProfileResource instanceProfile) {
        this.instanceProfile = instanceProfile;
    }

    /**
     * The set of Network Interfaces to be attached to the instances being launched using this template. Required if Security Group not provided.
     */
    @Updatable
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
     * The metadata options for the instance.
     *
     * @subresource gyro.aws.ec2.LaunchTemplateMetadataOptions
     */
    @Updatable
    public LaunchTemplateMetadataOptions getMetadataOptions() {
        return metadataOptions;
    }

    public void setMetadataOptions(LaunchTemplateMetadataOptions metadataOptions) {
        this.metadataOptions = metadataOptions;
    }

    /**
     * The credit specifications for the instance.
     */
    @Updatable
    public LaunchTemplateCreditSpecification getCreditSpecification() {
        return creditSpecification;
    }

    public void setCreditSpecification(LaunchTemplateCreditSpecification creditSpecification) {
        this.creditSpecification = creditSpecification;
    }

    /**
     * The elastic GPU to associate with the instance.
     */
    @Updatable
    public List<LaunchTemplateElasticGpuSpecification> getElasticGpuSpecification() {
        if (elasticGpuSpecification == null) {
            elasticGpuSpecification = new ArrayList<>();
        }

        return elasticGpuSpecification;
    }

    public void setElasticGpuSpecification(List<LaunchTemplateElasticGpuSpecification> elasticGpuSpecification) {
        this.elasticGpuSpecification = elasticGpuSpecification;
    }

    /**
     * The elastic inference accelerator for the instance.
     */
    @Updatable
    public List<LaunchTemplateElasticInferenceAccelerator> getInferenceAccelerator() {
        if (inferenceAccelerator == null) {
            inferenceAccelerator = new ArrayList<>();
        }

        return inferenceAccelerator;
    }

    public void setInferenceAccelerator(List<LaunchTemplateElasticInferenceAccelerator> inferenceAccelerator) {
        this.inferenceAccelerator = inferenceAccelerator;
    }

    /**
     * The enclave options for the instance.
     */
    @Updatable
    public LaunchTemplateEnclaveOptions getEnclaveOptions() {
        return enclaveOptions;
    }

    public void setEnclaveOptions(LaunchTemplateEnclaveOptions enclaveOptions) {
        this.enclaveOptions = enclaveOptions;
    }

    /**
     * The hibernation options for the instance.
     */
    @Updatable
    public LaunchTemplateHibernationOptions getHibernationOptions() {
        return hibernationOptions;
    }

    public void setHibernationOptions(LaunchTemplateHibernationOptions hibernationOptions) {
        this.hibernationOptions = hibernationOptions;
    }

    /**
     * The spot market options for the instance.
     */
    @Updatable
    public LaunchTemplateInstanceMarketOptions getMarketOptions() {
        return marketOptions;
    }

    public void setMarketOptions(LaunchTemplateInstanceMarketOptions marketOptions) {
        this.marketOptions = marketOptions;
    }

    /**
     * The tags to apply to the resources during launch.
     *
     * @subresource gyro.aws.ec2.LaunchTemplateTagSpecification
     */
    @Updatable
    public List<LaunchTemplateTagSpecification> getTagSpecification() {
        if (tagSpecification == null) {
            tagSpecification = new ArrayList<>();
        }

        return tagSpecification;
    }

    public void setTagSpecification(List<LaunchTemplateTagSpecification> tagSpecification) {
        this.tagSpecification = tagSpecification;
    }

    /**
     * The ID of the kernel.
     */
    @Updatable
    public String getKernelId() {
        return kernelId;
    }

    public void setKernelId(String kernelId) {
        this.kernelId = kernelId;
    }

    /**
     * The ID of the RAM disk.
     */
    @Updatable
    @DependsOn("kernel")
    public String getRamDiskId() {
        return ramDiskId;
    }

    public void setRamDiskId(String ramDiskId) {
        this.ramDiskId = ramDiskId;
    }

    /**
     * The placement for the instance;
     */
    @Updatable
    public LaunchTemplatePlacement getPlacement() {
        return placement;
    }

    public void setPlacement(LaunchTemplatePlacement placement) {
        this.placement = placement;
    }

    /**
     * The cpu options for the instance.
     */
    @Updatable
    public LaunchTemplateCpuOptions getCpuOptions() {
        return cpuOptions;
    }

    public void setCpuOptions(LaunchTemplateCpuOptions cpuOptions) {
        this.cpuOptions = cpuOptions;
    }

    /**
     * The monitoring for the instance.
     */
    @Updatable
    public LaunchTemplateMonitoring getMonitoring() {
        return monitoring;
    }

    public void setMonitoring(LaunchTemplateMonitoring monitoring) {
        this.monitoring = monitoring;
    }

    /**
     * The ID of the launch template.
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
        return getId();
    }

    @Override
    public void copyFrom(LaunchTemplate launchTemplate) {
        setId(launchTemplate.launchTemplateId());
        setName(launchTemplate.launchTemplateName());
        setVersion(launchTemplate.latestVersionNumber());

        refreshTags();
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

        CreateLaunchTemplateResponse response = client.createLaunchTemplate(
            r -> r.launchTemplateName(getName())
                .launchTemplateData(requestLaunchTemplateData())
        );

        setId(response.launchTemplate().launchTemplateId());
        setVersion(response.launchTemplate().latestVersionNumber());
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        CreateLaunchTemplateVersionResponse response = client.createLaunchTemplateVersion(r -> r.launchTemplateId(getId())
            .launchTemplateData(requestLaunchTemplateData())
        );

        client.modifyLaunchTemplate(r -> r.launchTemplateId(getId())
            .defaultVersion(getVersion().toString())
        );

        setVersion(response.launchTemplateVersion().versionNumber());
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteLaunchTemplate(r -> r.launchTemplateId(getId()));
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (!getSecurityGroups().isEmpty() && !getNetworkInterfaces().isEmpty()) {
            new ValidationError(this, null,
                "Either 'security-groups' or 'network-interfaces' should be provided, not both.");
        }

        return errors;
    }

    private LaunchTemplate getLaunchTemplate(Ec2Client client) {
        LaunchTemplate launchTemplate = null;

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load instance.");
        }

        try {
            DescribeLaunchTemplatesResponse response = client.describeLaunchTemplates(r ->
                r.launchTemplateIds(getId()));

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

    private LaunchTemplateIamInstanceProfileSpecificationRequest getLaunchTemplateInstanceProfile() {
        if (getInstanceProfile() == null) {
            return null;
        }

        return LaunchTemplateIamInstanceProfileSpecificationRequest.builder()
            .arn(getInstanceProfile().getArn())
            .build();
    }

    private List<LaunchTemplateInstanceNetworkInterfaceSpecificationRequest> toNetworkInterfaceSpecificationRequest() {
        AtomicInteger deviceIndex = new AtomicInteger();
        return getNetworkInterfaces().stream().map(o -> LaunchTemplateInstanceNetworkInterfaceSpecificationRequest
            .builder().networkInterfaceId(o.getId()).deviceIndex(deviceIndex.getAndIncrement()).build()
        ).collect(Collectors.toList());
    }

    private RequestLaunchTemplateData requestLaunchTemplateData() {
        RequestLaunchTemplateData.Builder builder = RequestLaunchTemplateData.builder()
            .disableApiTermination(getDisableApiTermination())
            .ebsOptimized(getEbsOptimized())
            .hibernationOptions(o -> o.configured(getConfigureHibernateOption()))
            .imageId(getAmi() == null ? null : getAmi().getId())
            .instanceType(getInstanceType())
            .instanceInitiatedShutdownBehavior(getShutdownBehavior())
            .keyName(getKeyName())
            .securityGroupIds(!getSecurityGroups().isEmpty() ? getSecurityGroups().stream()
                .map(SecurityGroupResource::getId)
                .collect(Collectors.toList()) : null)
            .userData(new String(Base64.encodeBase64(getUserData().trim().getBytes())))
            .blockDeviceMappings(!getBlockDeviceMapping().isEmpty() ?
                getBlockDeviceMapping()
                    .stream().map(BlockDeviceMapping::getLaunchTemplateBlockDeviceMapping)
                    .collect(Collectors.toList()) : null
            )
            .capacityReservationSpecification(getCapacityReservation() == null ? null
                : getCapacityReservation().toLaunchTemplateCapacityReservationSpecificationRequest())
            .iamInstanceProfile(getLaunchTemplateInstanceProfile())
            .networkInterfaces(!getNetworkInterfaces().isEmpty()
                ? toNetworkInterfaceSpecificationRequest() : null)
            .kernelId(getKernelId())
            .ramDiskId(getRamDiskId())
            .metadataOptions(getMetadataOptions() == null ? null : getMetadataOptions().toMetadataOptions());

        if (getCreditSpecification() != null) {
            builder.creditSpecification(getCreditSpecification().toCreditSpecification());
        }

        if (!getElasticGpuSpecification().isEmpty()) {
            builder.elasticGpuSpecifications(getElasticGpuSpecification().stream()
                .map(LaunchTemplateElasticGpuSpecification::toElasticGpuSpecification)
                .collect(Collectors.toList()));
        }

        if (!getInferenceAccelerator().isEmpty()) {
            builder.elasticInferenceAccelerators(getInferenceAccelerator().stream()
                .map(LaunchTemplateElasticInferenceAccelerator::toLaunchTemplateElasticInferenceAccelerator)
                .collect(Collectors.toList()));
        }

        if (getEnclaveOptions() != null) {
            builder.enclaveOptions(getEnclaveOptions().toLaunchTemplateEnclaveOptionsRequest());
        }

        if (getHibernationOptions() != null) {
            builder.hibernationOptions(getHibernationOptions().toLaunchTemplateHibernationOptionsRequest());
        }

        if (getMarketOptions() != null) {
            builder.instanceMarketOptions(getMarketOptions().toLaunchTemplateInstanceMarketOptionsRequest());
        }

        if (getPlacement() != null) {
            builder.placement(getPlacement().toLaunchTemplatePlacementRequest());
        }

        if (!getTagSpecification().isEmpty()) {
            builder.tagSpecifications(getTagSpecification().stream()
                .map(LaunchTemplateTagSpecification::toLaunchTemplateTagSpecificationRequest)
                .collect(Collectors.toList()));
        }

        if (getCpuOptions() != null) {
            builder.cpuOptions(getCpuOptions().toLaunchTemplateCpuOptionsRequest());
        }

        if (getMonitoring() != null) {
            builder.monitoring(getMonitoring().toLaunchTemplatesMonitoringRequest()).build();
        }

        return builder.build();
    }
}
