package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceStateName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query instance.
 *
 * .. code-block:: gyro
 *
 *    instance: $(external-query aws::instance { affinity: 'default'})
 */
@Type("instance")
public class InstanceFinder extends Ec2TaggableAwsFinder<Ec2Client, Instance, InstanceResource> {

    private String affinity;
    private String architecture;
    private String availabilityZone;
    private String blockDeviceMappingAttachTime;
    private String blockDeviceMappingDeleteOnTermination;
    private String blockDeviceMappingDeviceName;
    private String blockDeviceMappingStatus;
    private String blockDeviceMappingVolumeId;
    private String clientToken;
    private String dnsName;
    private String groupId;
    private String groupName;
    private String hibernationOptionsConfigured;
    private String hostId;
    private String hypervisor;
    private String iamInstanceProfileArn;
    private String imageId;
    private String instanceId;
    private String instanceLifecycle;
    private String instanceStateCode;
    private String instanceStateName;
    private String instanceType;
    private String instanceGroupId;
    private String instanceGroupName;
    private String ipAddress;
    private String kernelId;
    private String keyName;
    private String launchIndex;
    private String launchTime;
    private String monitoringState;
    private String networkInterfaceAddressesPrivateIpAddress;
    private String networkInterfaceAddressesPrimary;
    private String networkInterfaceAddressesAssociationPublicIp;
    private String networkInterfaceAddressesAssociationIpOwnerId;
    private String networkInterfaceAssociationPublicIp;
    private String networkInterfaceAssociationIpOwnerId;
    private String networkInterfaceAssociationAllocationId;
    private String networkInterfaceAssociationAssociationId;
    private String networkInterfaceAttachmentAttachmentId;
    private String networkInterfaceAttachmentInstanceId;
    private String networkInterfaceAttachmentInstanceOwnerId;
    private String networkInterfaceAttachmentDeviceIndex;
    private String networkInterfaceAttachmentStatus;
    private String networkInterfaceAttachmentAttachTime;
    private String networkInterfaceAttachmentDeleteOnTermination;
    private String networkInterfaceAvailabilityZone;
    private String networkInterfaceDescription;
    private String networkInterfaceGroupId;
    private String networkInterfaceGroupName;
    private String networkInterfaceIpv6AddressesIpv6Address;
    private String networkInterfaceMacAddress;
    private String networkInterfaceNetworkInterfaceId;
    private String networkInterfaceOwnerId;
    private String networkInterfacePrivateDnsName;
    private String networkInterfaceRequesterId;
    private String networkInterfaceRequesterManaged;
    private String networkInterfaceStatus;
    private String networkInterfaceSourceDestCheck;
    private String networkInterfaceSubnetId;
    private String networkInterfaceVpcId;
    private String ownerId;
    private String placementGroupName;
    private String placementPartitionNumber;
    private String platform;
    private String privateDnsName;
    private String privateIpAddress;
    private String productCode;
    private String productCodeType;
    private String ramdiskId;
    private String reason;
    private String requesterId;
    private String reservationId;
    private String rootDeviceName;
    private String rootDeviceType;
    private String sourceDestCheck;
    private String spotInstanceRequestId;
    private String stateReasonCode;
    private String stateReasonMessage;
    private String subnetId;
    private Map<String, String> tag;
    private String tagKey;
    private String tenancy;
    private String virtualizationType;
    private String vpcId;

    /**
     * The affinity setting for an instance running on a Dedicated Host . Valid values are ``default`` or ``host``.
     */
    public String getAffinity() {
        return affinity;
    }

    public void setAffinity(String affinity) {
        this.affinity = affinity;
    }

    /**
     * The instance architecture . Valid values are ``i386`` or ``x86_64``.
     */
    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    /**
     * The Availability Zone of the instance.
     */
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * The attach time for an EBS volume mapped to the instance, for example, ``2010-09-15T17:15:20.000Z``.
     */
    @Filter("block-device-mapping.attach-time")
    public String getBlockDeviceMappingAttachTime() {
        return blockDeviceMappingAttachTime;
    }

    public void setBlockDeviceMappingAttachTime(String blockDeviceMappingAttachTime) {
        this.blockDeviceMappingAttachTime = blockDeviceMappingAttachTime;
    }

    /**
     * A Boolean that indicates whether the EBS volume is deleted on instance termination.
     */
    @Filter("block-device-mapping.delete-on-termination")
    public String getBlockDeviceMappingDeleteOnTermination() {
        return blockDeviceMappingDeleteOnTermination;
    }

    public void setBlockDeviceMappingDeleteOnTermination(String blockDeviceMappingDeleteOnTermination) {
        this.blockDeviceMappingDeleteOnTermination = blockDeviceMappingDeleteOnTermination;
    }

    /**
     * The device name specified in the block device mapping (for example, /dev/sdh or xvdh).
     */
    @Filter("block-device-mapping.device-name")
    public String getBlockDeviceMappingDeviceName() {
        return blockDeviceMappingDeviceName;
    }

    public void setBlockDeviceMappingDeviceName(String blockDeviceMappingDeviceName) {
        this.blockDeviceMappingDeviceName = blockDeviceMappingDeviceName;
    }

    /**
     * The status for the EBS volume . Valid values are ``attaching`` or ``attached`` or ``detaching`` or ``detached``.
     */
    @Filter("block-device-mapping.status")
    public String getBlockDeviceMappingStatus() {
        return blockDeviceMappingStatus;
    }

    public void setBlockDeviceMappingStatus(String blockDeviceMappingStatus) {
        this.blockDeviceMappingStatus = blockDeviceMappingStatus;
    }

    /**
     * The volume ID of the EBS volume.
     */
    @Filter("block-device-mapping.volume-id")
    public String getBlockDeviceMappingVolumeId() {
        return blockDeviceMappingVolumeId;
    }

    public void setBlockDeviceMappingVolumeId(String blockDeviceMappingVolumeId) {
        this.blockDeviceMappingVolumeId = blockDeviceMappingVolumeId;
    }

    /**
     * The idempotency token you provided when you launched the instance.
     */
    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    /**
     * The public DNS name of the instance.
     */
    public String getDnsName() {
        return dnsName;
    }

    public void setDnsName(String dnsName) {
        this.dnsName = dnsName;
    }

    /**
     * The ID of the security group for the instance. EC2-Classic only.
     */
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * The name of the security group for the instance. EC2-Classic only.
     */
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * A Boolean that indicates whether the instance is enabled for hibernation. A value of true means that the instance is enabled for hibernation.
     */
    @Filter("hibernation-options.configured")
    public String getHibernationOptionsConfigured() {
        return hibernationOptionsConfigured;
    }

    public void setHibernationOptionsConfigured(String hibernationOptionsConfigured) {
        this.hibernationOptionsConfigured = hibernationOptionsConfigured;
    }

    /**
     * The ID of the Dedicated Host on which the instance is running, if applicable.
     */
    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    /**
     * The hypervisor type of the instance . Valid values are ``ovm`` or ``xen``.
     */
    public String getHypervisor() {
        return hypervisor;
    }

    public void setHypervisor(String hypervisor) {
        this.hypervisor = hypervisor;
    }

    /**
     * The instance profile associated with the instance. Specified as an ARN.
     */
    @Filter("iam-instance-profile.arn")
    public String getIamInstanceProfileArn() {
        return iamInstanceProfileArn;
    }

    public void setIamInstanceProfileArn(String iamInstanceProfileArn) {
        this.iamInstanceProfileArn = iamInstanceProfileArn;
    }

    /**
     * The ID of the image used to launch the instance.
     */
    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    /**
     * The ID of the instance.
     */
    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * Indicates whether this is a Spot Instance or a Scheduled Instance . Valid values are `` spot`` or ``scheduled``.
     */
    public String getInstanceLifecycle() {
        return instanceLifecycle;
    }

    public void setInstanceLifecycle(String instanceLifecycle) {
        this.instanceLifecycle = instanceLifecycle;
    }

    /**
     * The state of the instance, as a 16-bit unsigned integer. The high byte is used for internal purposes and should be ignored. The low byte is set based on the state represented. The valid values are: ``0`` (pending) or ``16`` (running) or ``32`` (shutting-down) or ``48`` (terminated) or ``64`` (stopping) or ``80`` (stopped).
     */
    public String getInstanceStateCode() {
        return instanceStateCode;
    }

    public void setInstanceStateCode(String instanceStateCode) {
        this.instanceStateCode = instanceStateCode;
    }

    /**
     * The state of the instance . Valid values are ``pending`` or ``running`` or ``shutting-down`` or ``terminated`` or ``stopping`` or ``stopped``.
     */
    public String getInstanceStateName() {
        return instanceStateName;
    }

    public void setInstanceStateName(String instanceStateName) {
        this.instanceStateName = instanceStateName;
    }

    /**
     * The type of instance (for example, t2.micro).
     */
    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    /**
     * The ID of the security group for the instance.
     */
    @Filter("instance.group-id")
    public String getInstanceGroupId() {
        return instanceGroupId;
    }

    public void setInstanceGroupId(String instanceGroupId) {
        this.instanceGroupId = instanceGroupId;
    }

    /**
     * The name of the security group for the instance.
     */
    @Filter("instance.group-name")
    public String getInstanceGroupName() {
        return instanceGroupName;
    }

    public void setInstanceGroupName(String instanceGroupName) {
        this.instanceGroupName = instanceGroupName;
    }

    /**
     * The public IPv4 address of the instance.
     */
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * The kernel ID.
     */
    public String getKernelId() {
        return kernelId;
    }

    public void setKernelId(String kernelId) {
        this.kernelId = kernelId;
    }

    /**
     * The name of the key pair used when the instance was launched.
     */
    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    /**
     * When launching multiple instances, this is the index for the instance in the launch group (for example, 0, 1, 2, and so on).
     */
    public String getLaunchIndex() {
        return launchIndex;
    }

    public void setLaunchIndex(String launchIndex) {
        this.launchIndex = launchIndex;
    }

    /**
     * The time when the instance was launched.
     */
    public String getLaunchTime() {
        return launchTime;
    }

    public void setLaunchTime(String launchTime) {
        this.launchTime = launchTime;
    }

    /**
     * Indicates whether detailed monitoring is enabled . Valid values are ``disabled`` or ``enabled``.
     */
    public String getMonitoringState() {
        return monitoringState;
    }

    public void setMonitoringState(String monitoringState) {
        this.monitoringState = monitoringState;
    }

    /**
     * The private IPv4 address associated with the network interface.
     */
    @Filter("network-interface.addresses.private-ip-address")
    public String getNetworkInterfaceAddressesPrivateIpAddress() {
        return networkInterfaceAddressesPrivateIpAddress;
    }

    public void setNetworkInterfaceAddressesPrivateIpAddress(String networkInterfaceAddressesPrivateIpAddress) {
        this.networkInterfaceAddressesPrivateIpAddress = networkInterfaceAddressesPrivateIpAddress;
    }

    /**
     * Specifies whether the IPv4 address of the network interface is the primary private IPv4 address.
     */
    @Filter("network-interface.addresses.primary")
    public String getNetworkInterfaceAddressesPrimary() {
        return networkInterfaceAddressesPrimary;
    }

    public void setNetworkInterfaceAddressesPrimary(String networkInterfaceAddressesPrimary) {
        this.networkInterfaceAddressesPrimary = networkInterfaceAddressesPrimary;
    }

    /**
     * The ID of the association of an Elastic IP address (IPv4) with a network interface.
     */
    @Filter("network-interface.addresses.association.public-ip")
    public String getNetworkInterfaceAddressesAssociationPublicIp() {
        return networkInterfaceAddressesAssociationPublicIp;
    }

    public void setNetworkInterfaceAddressesAssociationPublicIp(String networkInterfaceAddressesAssociationPublicIp) {
        this.networkInterfaceAddressesAssociationPublicIp = networkInterfaceAddressesAssociationPublicIp;
    }

    /**
     * The owner ID of the private IPv4 address associated with the network interface.
     */
    @Filter("network-interface.addresses.association.ip-owner-id")
    public String getNetworkInterfaceAddressesAssociationIpOwnerId() {
        return networkInterfaceAddressesAssociationIpOwnerId;
    }

    public void setNetworkInterfaceAddressesAssociationIpOwnerId(String networkInterfaceAddressesAssociationIpOwnerId) {
        this.networkInterfaceAddressesAssociationIpOwnerId = networkInterfaceAddressesAssociationIpOwnerId;
    }

    /**
     * The address of the Elastic IP address (IPv4) bound to the network interface.
     */
    @Filter("network-interface.association.public-ip")
    public String getNetworkInterfaceAssociationPublicIp() {
        return networkInterfaceAssociationPublicIp;
    }

    public void setNetworkInterfaceAssociationPublicIp(String networkInterfaceAssociationPublicIp) {
        this.networkInterfaceAssociationPublicIp = networkInterfaceAssociationPublicIp;
    }

    /**
     * The owner of the Elastic IP address (IPv4) associated with the network interface.
     */
    @Filter("network-interface.association.ip-owner-id")
    public String getNetworkInterfaceAssociationIpOwnerId() {
        return networkInterfaceAssociationIpOwnerId;
    }

    public void setNetworkInterfaceAssociationIpOwnerId(String networkInterfaceAssociationIpOwnerId) {
        this.networkInterfaceAssociationIpOwnerId = networkInterfaceAssociationIpOwnerId;
    }

    /**
     * The allocation ID returned when you allocated the Elastic IP address (IPv4) for your network interface.
     */
    @Filter("network-interface.association.allocation-id")
    public String getNetworkInterfaceAssociationAllocationId() {
        return networkInterfaceAssociationAllocationId;
    }

    public void setNetworkInterfaceAssociationAllocationId(String networkInterfaceAssociationAllocationId) {
        this.networkInterfaceAssociationAllocationId = networkInterfaceAssociationAllocationId;
    }

    /**
     * The association ID returned when the network interface was associated with an IPv4 address.
     */
    @Filter("network-interface.association.association-id")
    public String getNetworkInterfaceAssociationAssociationId() {
        return networkInterfaceAssociationAssociationId;
    }

    public void setNetworkInterfaceAssociationAssociationId(String networkInterfaceAssociationAssociationId) {
        this.networkInterfaceAssociationAssociationId = networkInterfaceAssociationAssociationId;
    }

    /**
     * The ID of the interface attachment.
     */
    @Filter("network-interface.attachment.attachment-id")
    public String getNetworkInterfaceAttachmentAttachmentId() {
        return networkInterfaceAttachmentAttachmentId;
    }

    public void setNetworkInterfaceAttachmentAttachmentId(String networkInterfaceAttachmentAttachmentId) {
        this.networkInterfaceAttachmentAttachmentId = networkInterfaceAttachmentAttachmentId;
    }

    /**
     * The ID of the instance to which the network interface is attached.
     */
    @Filter("network-interface.attachment.instance-id")
    public String getNetworkInterfaceAttachmentInstanceId() {
        return networkInterfaceAttachmentInstanceId;
    }

    public void setNetworkInterfaceAttachmentInstanceId(String networkInterfaceAttachmentInstanceId) {
        this.networkInterfaceAttachmentInstanceId = networkInterfaceAttachmentInstanceId;
    }

    /**
     * The owner ID of the instance to which the network interface is attached.
     */
    @Filter("network-interface.attachment.instance-owner-id")
    public String getNetworkInterfaceAttachmentInstanceOwnerId() {
        return networkInterfaceAttachmentInstanceOwnerId;
    }

    public void setNetworkInterfaceAttachmentInstanceOwnerId(String networkInterfaceAttachmentInstanceOwnerId) {
        this.networkInterfaceAttachmentInstanceOwnerId = networkInterfaceAttachmentInstanceOwnerId;
    }

    /**
     * The device index to which the network interface is attached.
     */
    @Filter("network-interface.attachment.device-index")
    public String getNetworkInterfaceAttachmentDeviceIndex() {
        return networkInterfaceAttachmentDeviceIndex;
    }

    public void setNetworkInterfaceAttachmentDeviceIndex(String networkInterfaceAttachmentDeviceIndex) {
        this.networkInterfaceAttachmentDeviceIndex = networkInterfaceAttachmentDeviceIndex;
    }

    /**
     * The status of the attachment . Valid values are ``attaching`` or ``attached`` or ``detaching`` or ``detached``.
     */
    @Filter("network-interface.attachment.status")
    public String getNetworkInterfaceAttachmentStatus() {
        return networkInterfaceAttachmentStatus;
    }

    public void setNetworkInterfaceAttachmentStatus(String networkInterfaceAttachmentStatus) {
        this.networkInterfaceAttachmentStatus = networkInterfaceAttachmentStatus;
    }

    /**
     * The time that the network interface was attached to an instance.
     */
    @Filter("network-interface.attachment.attach-time")
    public String getNetworkInterfaceAttachmentAttachTime() {
        return networkInterfaceAttachmentAttachTime;
    }

    public void setNetworkInterfaceAttachmentAttachTime(String networkInterfaceAttachmentAttachTime) {
        this.networkInterfaceAttachmentAttachTime = networkInterfaceAttachmentAttachTime;
    }

    /**
     * Specifies whether the attachment is deleted when an instance is terminated.
     */
    @Filter("network-interface.attachment.delete-on-termination")
    public String getNetworkInterfaceAttachmentDeleteOnTermination() {
        return networkInterfaceAttachmentDeleteOnTermination;
    }

    public void setNetworkInterfaceAttachmentDeleteOnTermination(String networkInterfaceAttachmentDeleteOnTermination) {
        this.networkInterfaceAttachmentDeleteOnTermination = networkInterfaceAttachmentDeleteOnTermination;
    }

    /**
     * The Availability Zone for the network interface.
     */
    @Filter("network-interface.availability-zone")
    public String getNetworkInterfaceAvailabilityZone() {
        return networkInterfaceAvailabilityZone;
    }

    public void setNetworkInterfaceAvailabilityZone(String networkInterfaceAvailabilityZone) {
        this.networkInterfaceAvailabilityZone = networkInterfaceAvailabilityZone;
    }

    /**
     * The description of the network interface.
     */
    @Filter("network-interface.description")
    public String getNetworkInterfaceDescription() {
        return networkInterfaceDescription;
    }

    public void setNetworkInterfaceDescription(String networkInterfaceDescription) {
        this.networkInterfaceDescription = networkInterfaceDescription;
    }

    /**
     * The ID of a security group associated with the network interface.
     */
    @Filter("network-interface.group-id")
    public String getNetworkInterfaceGroupId() {
        return networkInterfaceGroupId;
    }

    public void setNetworkInterfaceGroupId(String networkInterfaceGroupId) {
        this.networkInterfaceGroupId = networkInterfaceGroupId;
    }

    /**
     * The name of a security group associated with the network interface.
     */
    @Filter("network-interface.group-name")
    public String getNetworkInterfaceGroupName() {
        return networkInterfaceGroupName;
    }

    public void setNetworkInterfaceGroupName(String networkInterfaceGroupName) {
        this.networkInterfaceGroupName = networkInterfaceGroupName;
    }

    /**
     * The IPv6 address associated with the network interface.
     */
    @Filter("network-interface.ipv6-addresses.ipv6-address")
    public String getNetworkInterfaceIpv6AddressesIpv6Address() {
        return networkInterfaceIpv6AddressesIpv6Address;
    }

    public void setNetworkInterfaceIpv6AddressesIpv6Address(String networkInterfaceIpv6AddressesIpv6Address) {
        this.networkInterfaceIpv6AddressesIpv6Address = networkInterfaceIpv6AddressesIpv6Address;
    }

    /**
     * The MAC address of the network interface.
     */
    @Filter("network-interface.mac-address")
    public String getNetworkInterfaceMacAddress() {
        return networkInterfaceMacAddress;
    }

    public void setNetworkInterfaceMacAddress(String networkInterfaceMacAddress) {
        this.networkInterfaceMacAddress = networkInterfaceMacAddress;
    }

    /**
     * The ID of the network interface.
     */
    @Filter("network-interface.network-interface-id")
    public String getNetworkInterfaceNetworkInterfaceId() {
        return networkInterfaceNetworkInterfaceId;
    }

    public void setNetworkInterfaceNetworkInterfaceId(String networkInterfaceNetworkInterfaceId) {
        this.networkInterfaceNetworkInterfaceId = networkInterfaceNetworkInterfaceId;
    }

    /**
     * The ID of the owner of the network interface.
     */
    @Filter("network-interface.owner-id")
    public String getNetworkInterfaceOwnerId() {
        return networkInterfaceOwnerId;
    }

    public void setNetworkInterfaceOwnerId(String networkInterfaceOwnerId) {
        this.networkInterfaceOwnerId = networkInterfaceOwnerId;
    }

    /**
     * The private DNS name of the network interface.
     */
    @Filter("network-interface.private-dns-name")
    public String getNetworkInterfacePrivateDnsName() {
        return networkInterfacePrivateDnsName;
    }

    public void setNetworkInterfacePrivateDnsName(String networkInterfacePrivateDnsName) {
        this.networkInterfacePrivateDnsName = networkInterfacePrivateDnsName;
    }

    /**
     * The requester ID for the network interface.
     */
    @Filter("network-interface.requester-id")
    public String getNetworkInterfaceRequesterId() {
        return networkInterfaceRequesterId;
    }

    public void setNetworkInterfaceRequesterId(String networkInterfaceRequesterId) {
        this.networkInterfaceRequesterId = networkInterfaceRequesterId;
    }

    /**
     * Indicates whether the network interface is being managed by AWS.
     */
    @Filter("network-interface.requester-managed")
    public String getNetworkInterfaceRequesterManaged() {
        return networkInterfaceRequesterManaged;
    }

    public void setNetworkInterfaceRequesterManaged(String networkInterfaceRequesterManaged) {
        this.networkInterfaceRequesterManaged = networkInterfaceRequesterManaged;
    }

    /**
     * The status of the network interface . Valid values are ``available`` or ``in-use``.
     */
    @Filter("network-interface.status")
    public String getNetworkInterfaceStatus() {
        return networkInterfaceStatus;
    }

    public void setNetworkInterfaceStatus(String networkInterfaceStatus) {
        this.networkInterfaceStatus = networkInterfaceStatus;
    }

    /**
     * Whether the network interface performs source/destination checking. A value of true means that checking is enabled, and false means that checking is disabled. The value must be false for the network interface to perform network address translation (NAT) in your VPC.
     */
    @Filter("network-interface.source-dest-check")
    public String getNetworkInterfaceSourceDestCheck() {
        return networkInterfaceSourceDestCheck;
    }

    public void setNetworkInterfaceSourceDestCheck(String networkInterfaceSourceDestCheck) {
        this.networkInterfaceSourceDestCheck = networkInterfaceSourceDestCheck;
    }

    /**
     * The ID of the subnet for the network interface.
     */
    @Filter("network-interface.subnet-id")
    public String getNetworkInterfaceSubnetId() {
        return networkInterfaceSubnetId;
    }

    public void setNetworkInterfaceSubnetId(String networkInterfaceSubnetId) {
        this.networkInterfaceSubnetId = networkInterfaceSubnetId;
    }

    /**
     * The ID of the VPC for the network interface.
     */
    @Filter("network-interface.vpc-id")
    public String getNetworkInterfaceVpcId() {
        return networkInterfaceVpcId;
    }

    public void setNetworkInterfaceVpcId(String networkInterfaceVpcId) {
        this.networkInterfaceVpcId = networkInterfaceVpcId;
    }

    /**
     * The AWS account ID of the instance owner.
     */
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * The name of the placement group for the instance.
     */
    public String getPlacementGroupName() {
        return placementGroupName;
    }

    public void setPlacementGroupName(String placementGroupName) {
        this.placementGroupName = placementGroupName;
    }

    /**
     * The partition in which the instance is located.
     */
    public String getPlacementPartitionNumber() {
        return placementPartitionNumber;
    }

    public void setPlacementPartitionNumber(String placementPartitionNumber) {
        this.placementPartitionNumber = placementPartitionNumber;
    }

    /**
     * The platform. To list only Windows instances, use windows.
     */
    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    /**
     * The private IPv4 DNS name of the instance.
     */
    public String getPrivateDnsName() {
        return privateDnsName;
    }

    public void setPrivateDnsName(String privateDnsName) {
        this.privateDnsName = privateDnsName;
    }

    /**
     * The private IPv4 address of the instance.
     */
    public String getPrivateIpAddress() {
        return privateIpAddress;
    }

    public void setPrivateIpAddress(String privateIpAddress) {
        this.privateIpAddress = privateIpAddress;
    }

    /**
     * The product code associated with the AMI used to launch the instance.
     */
    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    /**
     * The type of product code . Valid values are ``devpay`` or ``marketplace``.
     */
    @Filter("product-code.type")
    public String getProductCodeType() {
        return productCodeType;
    }

    public void setProductCodeType(String productCodeType) {
        this.productCodeType = productCodeType;
    }

    /**
     * The RAM disk ID.
     */
    public String getRamdiskId() {
        return ramdiskId;
    }

    public void setRamdiskId(String ramdiskId) {
        this.ramdiskId = ramdiskId;
    }

    /**
     * The reason for the current state of the instance (for example, shows "User Initiated [date]" when you stop or terminate the instance). Similar to the state-reason-code filter.
     */
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * The ID of the entity that launched the instance on your behalf (for example, AWS Management Console, Auto Scaling, and so on).
     */
    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    /**
     * The ID of the instance's reservation. A reservation ID is created any time you launch an instance. A reservation ID has a one-to-one relationship with an instance launch request, but can be associated with more than one instance if you launch multiple instances using the same launch request. For example, if you launch one instance, you get one reservation ID. If you launch ten instances using the same launch request, you also get one reservation ID.
     */
    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    /**
     * The device name of the root device volume (for example, /dev/sda1).
     */
    public String getRootDeviceName() {
        return rootDeviceName;
    }

    public void setRootDeviceName(String rootDeviceName) {
        this.rootDeviceName = rootDeviceName;
    }

    /**
     * The type of the root device volume . Valid values are ``ebs`` or ``instance-store``.
     */
    public String getRootDeviceType() {
        return rootDeviceType;
    }

    public void setRootDeviceType(String rootDeviceType) {
        this.rootDeviceType = rootDeviceType;
    }

    /**
     * Indicates whether the instance performs source/destination checking. A value of true means that checking is enabled, and false means that checking is disabled. The value must be false for the instance to perform network address translation (NAT) in your VPC.
     */
    public String getSourceDestCheck() {
        return sourceDestCheck;
    }

    public void setSourceDestCheck(String sourceDestCheck) {
        this.sourceDestCheck = sourceDestCheck;
    }

    /**
     * The ID of the Spot Instance request.
     */
    public String getSpotInstanceRequestId() {
        return spotInstanceRequestId;
    }

    public void setSpotInstanceRequestId(String spotInstanceRequestId) {
        this.spotInstanceRequestId = spotInstanceRequestId;
    }

    /**
     * The reason code for the state change.
     */
    public String getStateReasonCode() {
        return stateReasonCode;
    }

    public void setStateReasonCode(String stateReasonCode) {
        this.stateReasonCode = stateReasonCode;
    }

    /**
     * A message that describes the state change.
     */
    public String getStateReasonMessage() {
        return stateReasonMessage;
    }

    public void setStateReasonMessage(String stateReasonMessage) {
        this.stateReasonMessage = stateReasonMessage;
    }

    /**
     * The ID of the subnet for the instance.
     */
    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    /**
     * The key/value combination of a tag assigned to the resource.
     */
    public Map<String, String> getTag() {
        if (tag == null) {
            tag = new HashMap<>();
        }

        return tag;
    }

    public void setTag(Map<String, String> tag) {
        this.tag = tag;
    }

    /**
     * The key of a tag assigned to the resource. Use this filter to find all resources that have a tag with a specific key, regardless of the tag value.
     */
    public String getTagKey() {
        return tagKey;
    }

    public void setTagKey(String tagKey) {
        this.tagKey = tagKey;
    }

    /**
     * The tenancy of an instance . Valid values are ``dedicated`` or ``default`` or ``host``.
     */
    public String getTenancy() {
        return tenancy;
    }

    public void setTenancy(String tenancy) {
        this.tenancy = tenancy;
    }

    /**
     * The virtualization type of the instance . Valid values are ``paravirtual`` or ``hvm``.
     */
    public String getVirtualizationType() {
        return virtualizationType;
    }

    public void setVirtualizationType(String virtualizationType) {
        this.virtualizationType = virtualizationType;
    }

    /**
     * The ID of the VPC that the instance is running in.
     */
    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    @Override
    protected List<Instance> findAllAws(Ec2Client client) {
        return client.describeInstancesPaginator().reservations()
            .stream().flatMap(o -> o.instances().stream())
            .filter(i -> !i.state().name().equals(InstanceStateName.TERMINATED))
            .collect(Collectors.toList());
    }

    @Override
    protected List<Instance> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeInstancesPaginator(r -> r.filters(createFilters(filters))).reservations()
            .stream().flatMap(o -> o.instances().stream())
            .filter(i -> !i.state().name().equals(InstanceStateName.TERMINATED))
            .collect(Collectors.toList());
    }
}