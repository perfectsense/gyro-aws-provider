package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InstanceResourceFinder extends AwsFinder<Ec2Client, Instance, InstanceResource> {
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

    public String getAffinity() {
        return affinity;
    }

    public void setAffinity(String affinity) {
        this.affinity = affinity;
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    @Filter("block-device-mapping.attach-time")
    public String getBlockDeviceMappingAttachTime() {
        return blockDeviceMappingAttachTime;
    }

    public void setBlockDeviceMappingAttachTime(String blockDeviceMappingAttachTime) {
        this.blockDeviceMappingAttachTime = blockDeviceMappingAttachTime;
    }

    @Filter("block-device-mapping.delete-on-termination")
    public String getBlockDeviceMappingDeleteOnTermination() {
        return blockDeviceMappingDeleteOnTermination;
    }

    public void setBlockDeviceMappingDeleteOnTermination(String blockDeviceMappingDeleteOnTermination) {
        this.blockDeviceMappingDeleteOnTermination = blockDeviceMappingDeleteOnTermination;
    }

    @Filter("block-device-mapping.device-name")
    public String getBlockDeviceMappingDeviceName() {
        return blockDeviceMappingDeviceName;
    }

    public void setBlockDeviceMappingDeviceName(String blockDeviceMappingDeviceName) {
        this.blockDeviceMappingDeviceName = blockDeviceMappingDeviceName;
    }

    @Filter("block-device-mapping.status")
    public String getBlockDeviceMappingStatus() {
        return blockDeviceMappingStatus;
    }

    public void setBlockDeviceMappingStatus(String blockDeviceMappingStatus) {
        this.blockDeviceMappingStatus = blockDeviceMappingStatus;
    }

    @Filter("block-device-mapping.volume-id")
    public String getBlockDeviceMappingVolumeId() {
        return blockDeviceMappingVolumeId;
    }

    public void setBlockDeviceMappingVolumeId(String blockDeviceMappingVolumeId) {
        this.blockDeviceMappingVolumeId = blockDeviceMappingVolumeId;
    }

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public String getDnsName() {
        return dnsName;
    }

    public void setDnsName(String dnsName) {
        this.dnsName = dnsName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Filter("hibernation-options.configured")
    public String getHibernationOptionsConfigured() {
        return hibernationOptionsConfigured;
    }

    public void setHibernationOptionsConfigured(String hibernationOptionsConfigured) {
        this.hibernationOptionsConfigured = hibernationOptionsConfigured;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getHypervisor() {
        return hypervisor;
    }

    public void setHypervisor(String hypervisor) {
        this.hypervisor = hypervisor;
    }

    @Filter("iam-instance-profile.arn")
    public String getIamInstanceProfileArn() {
        return iamInstanceProfileArn;
    }

    public void setIamInstanceProfileArn(String iamInstanceProfileArn) {
        this.iamInstanceProfileArn = iamInstanceProfileArn;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceLifecycle() {
        return instanceLifecycle;
    }

    public void setInstanceLifecycle(String instanceLifecycle) {
        this.instanceLifecycle = instanceLifecycle;
    }

    public String getInstanceStateCode() {
        return instanceStateCode;
    }

    public void setInstanceStateCode(String instanceStateCode) {
        this.instanceStateCode = instanceStateCode;
    }

    public String getInstanceStateName() {
        return instanceStateName;
    }

    public void setInstanceStateName(String instanceStateName) {
        this.instanceStateName = instanceStateName;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    @Filter("instance.group-id")
    public String getInstanceGroupId() {
        return instanceGroupId;
    }

    public void setInstanceGroupId(String instanceGroupId) {
        this.instanceGroupId = instanceGroupId;
    }

    @Filter("instance.group-name")
    public String getInstanceGroupName() {
        return instanceGroupName;
    }

    public void setInstanceGroupName(String instanceGroupName) {
        this.instanceGroupName = instanceGroupName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getKernelId() {
        return kernelId;
    }

    public void setKernelId(String kernelId) {
        this.kernelId = kernelId;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getLaunchIndex() {
        return launchIndex;
    }

    public void setLaunchIndex(String launchIndex) {
        this.launchIndex = launchIndex;
    }

    public String getLaunchTime() {
        return launchTime;
    }

    public void setLaunchTime(String launchTime) {
        this.launchTime = launchTime;
    }

    public String getMonitoringState() {
        return monitoringState;
    }

    public void setMonitoringState(String monitoringState) {
        this.monitoringState = monitoringState;
    }

    @Filter("network-interface.addresses.private-ip-address")
    public String getNetworkInterfaceAddressesPrivateIpAddress() {
        return networkInterfaceAddressesPrivateIpAddress;
    }

    public void setNetworkInterfaceAddressesPrivateIpAddress(String networkInterfaceAddressesPrivateIpAddress) {
        this.networkInterfaceAddressesPrivateIpAddress = networkInterfaceAddressesPrivateIpAddress;
    }

    @Filter("network-interface.addresses.primary")
    public String getNetworkInterfaceAddressesPrimary() {
        return networkInterfaceAddressesPrimary;
    }

    public void setNetworkInterfaceAddressesPrimary(String networkInterfaceAddressesPrimary) {
        this.networkInterfaceAddressesPrimary = networkInterfaceAddressesPrimary;
    }

    @Filter("network-interface.addresses.association.public-ip")
    public String getNetworkInterfaceAddressesAssociationPublicIp() {
        return networkInterfaceAddressesAssociationPublicIp;
    }

    public void setNetworkInterfaceAddressesAssociationPublicIp(String networkInterfaceAddressesAssociationPublicIp) {
        this.networkInterfaceAddressesAssociationPublicIp = networkInterfaceAddressesAssociationPublicIp;
    }

    @Filter("network-interface.addresses.association.ip-owner-id")
    public String getNetworkInterfaceAddressesAssociationIpOwnerId() {
        return networkInterfaceAddressesAssociationIpOwnerId;
    }

    public void setNetworkInterfaceAddressesAssociationIpOwnerId(String networkInterfaceAddressesAssociationIpOwnerId) {
        this.networkInterfaceAddressesAssociationIpOwnerId = networkInterfaceAddressesAssociationIpOwnerId;
    }

    @Filter("network-interface.association.public-ip")
    public String getNetworkInterfaceAssociationPublicIp() {
        return networkInterfaceAssociationPublicIp;
    }

    public void setNetworkInterfaceAssociationPublicIp(String networkInterfaceAssociationPublicIp) {
        this.networkInterfaceAssociationPublicIp = networkInterfaceAssociationPublicIp;
    }

    @Filter("network-interface.association.ip-owner-id")
    public String getNetworkInterfaceAssociationIpOwnerId() {
        return networkInterfaceAssociationIpOwnerId;
    }

    public void setNetworkInterfaceAssociationIpOwnerId(String networkInterfaceAssociationIpOwnerId) {
        this.networkInterfaceAssociationIpOwnerId = networkInterfaceAssociationIpOwnerId;
    }

    @Filter("network-interface.association.allocation-id")
    public String getNetworkInterfaceAssociationAllocationId() {
        return networkInterfaceAssociationAllocationId;
    }

    public void setNetworkInterfaceAssociationAllocationId(String networkInterfaceAssociationAllocationId) {
        this.networkInterfaceAssociationAllocationId = networkInterfaceAssociationAllocationId;
    }

    @Filter("network-interface..association.association-id")
    public String getNetworkInterfaceAssociationAssociationId() {
        return networkInterfaceAssociationAssociationId;
    }

    public void setNetworkInterfaceAssociationAssociationId(String networkInterfaceAssociationAssociationId) {
        this.networkInterfaceAssociationAssociationId = networkInterfaceAssociationAssociationId;
    }

    @Filter("network-interface.attachment.attachment-id")
    public String getNetworkInterfaceAttachmentAttachmentId() {
        return networkInterfaceAttachmentAttachmentId;
    }

    public void setNetworkInterfaceAttachmentAttachmentId(String networkInterfaceAttachmentAttachmentId) {
        this.networkInterfaceAttachmentAttachmentId = networkInterfaceAttachmentAttachmentId;
    }

    @Filter("network-interface.attachment.instance-id")
    public String getNetworkInterfaceAttachmentInstanceId() {
        return networkInterfaceAttachmentInstanceId;
    }

    public void setNetworkInterfaceAttachmentInstanceId(String networkInterfaceAttachmentInstanceId) {
        this.networkInterfaceAttachmentInstanceId = networkInterfaceAttachmentInstanceId;
    }

    @Filter("network-interface.attachment.instance-owner-id")
    public String getNetworkInterfaceAttachmentInstanceOwnerId() {
        return networkInterfaceAttachmentInstanceOwnerId;
    }

    public void setNetworkInterfaceAttachmentInstanceOwnerId(String networkInterfaceAttachmentInstanceOwnerId) {
        this.networkInterfaceAttachmentInstanceOwnerId = networkInterfaceAttachmentInstanceOwnerId;
    }

    @Filter("network-interface.attachment.device-index")
    public String getNetworkInterfaceAttachmentDeviceIndex() {
        return networkInterfaceAttachmentDeviceIndex;
    }

    public void setNetworkInterfaceAttachmentDeviceIndex(String networkInterfaceAttachmentDeviceIndex) {
        this.networkInterfaceAttachmentDeviceIndex = networkInterfaceAttachmentDeviceIndex;
    }

    @Filter("network-interface.attachment.status")
    public String getNetworkInterfaceAttachmentStatus() {
        return networkInterfaceAttachmentStatus;
    }

    public void setNetworkInterfaceAttachmentStatus(String networkInterfaceAttachmentStatus) {
        this.networkInterfaceAttachmentStatus = networkInterfaceAttachmentStatus;
    }

    @Filter("network-interface.attachment.attach-time")
    public String getNetworkInterfaceAttachmentAttachTime() {
        return networkInterfaceAttachmentAttachTime;
    }

    public void setNetworkInterfaceAttachmentAttachTime(String networkInterfaceAttachmentAttachTime) {
        this.networkInterfaceAttachmentAttachTime = networkInterfaceAttachmentAttachTime;
    }

    @Filter("network-interface.attachment.delete-on-termination")
    public String getNetworkInterfaceAttachmentDeleteOnTermination() {
        return networkInterfaceAttachmentDeleteOnTermination;
    }

    public void setNetworkInterfaceAttachmentDeleteOnTermination(String networkInterfaceAttachmentDeleteOnTermination) {
        this.networkInterfaceAttachmentDeleteOnTermination = networkInterfaceAttachmentDeleteOnTermination;
    }

    @Filter("network-interface.availability-zone")
    public String getNetworkInterfaceAvailabilityZone() {
        return networkInterfaceAvailabilityZone;
    }

    public void setNetworkInterfaceAvailabilityZone(String networkInterfaceAvailabilityZone) {
        this.networkInterfaceAvailabilityZone = networkInterfaceAvailabilityZone;
    }

    @Filter("network-interface.description")
    public String getNetworkInterfaceDescription() {
        return networkInterfaceDescription;
    }

    public void setNetworkInterfaceDescription(String networkInterfaceDescription) {
        this.networkInterfaceDescription = networkInterfaceDescription;
    }

    @Filter("network-interface.group-id")
    public String getNetworkInterfaceGroupId() {
        return networkInterfaceGroupId;
    }

    public void setNetworkInterfaceGroupId(String networkInterfaceGroupId) {
        this.networkInterfaceGroupId = networkInterfaceGroupId;
    }

    @Filter("network-interface.group-name")
    public String getNetworkInterfaceGroupName() {
        return networkInterfaceGroupName;
    }

    public void setNetworkInterfaceGroupName(String networkInterfaceGroupName) {
        this.networkInterfaceGroupName = networkInterfaceGroupName;
    }

    @Filter("network-interface.ipv6-addresses.ipv6-address")
    public String getNetworkInterfaceIpv6AddressesIpv6Address() {
        return networkInterfaceIpv6AddressesIpv6Address;
    }

    public void setNetworkInterfaceIpv6AddressesIpv6Address(String networkInterfaceIpv6AddressesIpv6Address) {
        this.networkInterfaceIpv6AddressesIpv6Address = networkInterfaceIpv6AddressesIpv6Address;
    }

    @Filter("network-interface.mac-address")
    public String getNetworkInterfaceMacAddress() {
        return networkInterfaceMacAddress;
    }

    public void setNetworkInterfaceMacAddress(String networkInterfaceMacAddress) {
        this.networkInterfaceMacAddress = networkInterfaceMacAddress;
    }

    @Filter("network-interface.network-interface-id")
    public String getNetworkInterfaceNetworkInterfaceId() {
        return networkInterfaceNetworkInterfaceId;
    }

    public void setNetworkInterfaceNetworkInterfaceId(String networkInterfaceNetworkInterfaceId) {
        this.networkInterfaceNetworkInterfaceId = networkInterfaceNetworkInterfaceId;
    }

    @Filter("network-interface.owner-id")
    public String getNetworkInterfaceOwnerId() {
        return networkInterfaceOwnerId;
    }

    public void setNetworkInterfaceOwnerId(String networkInterfaceOwnerId) {
        this.networkInterfaceOwnerId = networkInterfaceOwnerId;
    }

    @Filter("network-interface.private-dns-name")
    public String getNetworkInterfacePrivateDnsName() {
        return networkInterfacePrivateDnsName;
    }

    public void setNetworkInterfacePrivateDnsName(String networkInterfacePrivateDnsName) {
        this.networkInterfacePrivateDnsName = networkInterfacePrivateDnsName;
    }

    @Filter("network-interface.requester-id")
    public String getNetworkInterfaceRequesterId() {
        return networkInterfaceRequesterId;
    }

    public void setNetworkInterfaceRequesterId(String networkInterfaceRequesterId) {
        this.networkInterfaceRequesterId = networkInterfaceRequesterId;
    }

    @Filter("network-interface.requester-managed")
    public String getNetworkInterfaceRequesterManaged() {
        return networkInterfaceRequesterManaged;
    }

    public void setNetworkInterfaceRequesterManaged(String networkInterfaceRequesterManaged) {
        this.networkInterfaceRequesterManaged = networkInterfaceRequesterManaged;
    }

    @Filter("network-interface.status")
    public String getNetworkInterfaceStatus() {
        return networkInterfaceStatus;
    }

    public void setNetworkInterfaceStatus(String networkInterfaceStatus) {
        this.networkInterfaceStatus = networkInterfaceStatus;
    }

    @Filter("network-interface.source-dest-check")
    public String getNetworkInterfaceSourceDestCheck() {
        return networkInterfaceSourceDestCheck;
    }

    public void setNetworkInterfaceSourceDestCheck(String networkInterfaceSourceDestCheck) {
        this.networkInterfaceSourceDestCheck = networkInterfaceSourceDestCheck;
    }

    @Filter("network-interface.subnet-id")
    public String getNetworkInterfaceSubnetId() {
        return networkInterfaceSubnetId;
    }

    public void setNetworkInterfaceSubnetId(String networkInterfaceSubnetId) {
        this.networkInterfaceSubnetId = networkInterfaceSubnetId;
    }

    @Filter("network-interface.vpc-id")
    public String getNetworkInterfaceVpcId() {
        return networkInterfaceVpcId;
    }

    public void setNetworkInterfaceVpcId(String networkInterfaceVpcId) {
        this.networkInterfaceVpcId = networkInterfaceVpcId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getPlacementGroupName() {
        return placementGroupName;
    }

    public void setPlacementGroupName(String placementGroupName) {
        this.placementGroupName = placementGroupName;
    }

    public String getPlacementPartitionNumber() {
        return placementPartitionNumber;
    }

    public void setPlacementPartitionNumber(String placementPartitionNumber) {
        this.placementPartitionNumber = placementPartitionNumber;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPrivateDnsName() {
        return privateDnsName;
    }

    public void setPrivateDnsName(String privateDnsName) {
        this.privateDnsName = privateDnsName;
    }

    public String getPrivateIpAddress() {
        return privateIpAddress;
    }

    public void setPrivateIpAddress(String privateIpAddress) {
        this.privateIpAddress = privateIpAddress;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductCodeType() {
        return productCodeType;
    }

    public void setProductCodeType(String productCodeType) {
        this.productCodeType = productCodeType;
    }

    public String getRamdiskId() {
        return ramdiskId;
    }

    public void setRamdiskId(String ramdiskId) {
        this.ramdiskId = ramdiskId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getRootDeviceName() {
        return rootDeviceName;
    }

    public void setRootDeviceName(String rootDeviceName) {
        this.rootDeviceName = rootDeviceName;
    }

    public String getRootDeviceType() {
        return rootDeviceType;
    }

    public void setRootDeviceType(String rootDeviceType) {
        this.rootDeviceType = rootDeviceType;
    }

    public String getSourceDestCheck() {
        return sourceDestCheck;
    }

    public void setSourceDestCheck(String sourceDestCheck) {
        this.sourceDestCheck = sourceDestCheck;
    }

    public String getSpotInstanceRequestId() {
        return spotInstanceRequestId;
    }

    public void setSpotInstanceRequestId(String spotInstanceRequestId) {
        this.spotInstanceRequestId = spotInstanceRequestId;
    }

    public String getStateReasonCode() {
        return stateReasonCode;
    }

    public void setStateReasonCode(String stateReasonCode) {
        this.stateReasonCode = stateReasonCode;
    }

    public String getStateReasonMessage() {
        return stateReasonMessage;
    }

    public void setStateReasonMessage(String stateReasonMessage) {
        this.stateReasonMessage = stateReasonMessage;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public Map<String, String> getTag() {
        if (tag == null) {
            tag = new HashMap<>();
        }

        return tag;
    }

    public void setTag(Map<String, String> tag) {
        this.tag = tag;
    }

    public String getTagKey() {
        return tagKey;
    }

    public void setTagKey(String tagKey) {
        this.tagKey = tagKey;
    }

    public String getTenancy() {
        return tenancy;
    }

    public void setTenancy(String tenancy) {
        this.tenancy = tenancy;
    }

    public String getVirtualizationType() {
        return virtualizationType;
    }

    public void setVirtualizationType(String virtualizationType) {
        this.virtualizationType = virtualizationType;
    }

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    @Override
    protected List<Instance> findAllAws(Ec2Client client) {
        List<Reservation> reservations = client.describeInstances().reservations();
        return reservations.stream().flatMap(o -> o.instances().stream()).collect(Collectors.toList());
    }

    @Override
    protected List<Instance> findAws(Ec2Client client, Map<String, String> filters) {
        List<Reservation> reservations = client.describeInstances(r -> r.filters(createFilters(filters))).reservations();
        return reservations.stream().flatMap(o -> o.instances().stream()).collect(Collectors.toList());
    }
}
