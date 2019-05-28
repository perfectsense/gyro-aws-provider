package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.NetworkInterface;

import java.util.List;
import java.util.Map;

@Type("network-interface")
public class NetworkInterfaceResourceFinder extends AwsFinder<Ec2Client, NetworkInterface, NetworkInterfaceResource> {
    private String addressesPrivateIpAddress;
    private String addressesPrimary;
    private String addressesAssociationPublicIp;
    private String addressesAssociationOwnerId;
    private String associationAssociationId;
    private String associationAllocationId;
    private String associationIpOwnerId;
    private String associationPublicIp;
    private String associationPublicDnsName;
    private String attachmentAttachmentId;
    private String attachmentAttachTime;
    private String attachmentDeleteOnTermination;
    private String attachmentDeviceIndex;
    private String attachmentInstanceId;
    private String attachmentInstanceOwnerId;
    private String attachmentNatGatewayId;
    private String attachmentStatus;
    private String availabilityZone;
    private String description;
    private String groupId;
    private String groupName;
    private String ipv6AddressesIpv6Address;
    private String macAddress;
    private String networkInterfaceId;
    private String ownerId;
    private String privateIpAddress;
    private String privateDnsName;
    private String requesterId;
    private String requesterManaged;
    private String sourceDestCheck;
    private String status;
    private String subnetId;
    private Map<String, String> tag;
    private String tagKey;
    private String vpcId;

    @Filter("addresses.private-ip-address")
    public String getAddressesPrivateIpAddress() {
        return addressesPrivateIpAddress;
    }

    public void setAddressesPrivateIpAddress(String addressesPrivateIpAddress) {
        this.addressesPrivateIpAddress = addressesPrivateIpAddress;
    }

    @Filter("addresses.primary")
    public String getAddressesPrimary() {
        return addressesPrimary;
    }

    public void setAddressesPrimary(String addressesPrimary) {
        this.addressesPrimary = addressesPrimary;
    }

    @Filter("addresses.association.public-ip")
    public String getAddressesAssociationPublicIp() {
        return addressesAssociationPublicIp;
    }

    public void setAddressesAssociationPublicIp(String addressesAssociationPublicIp) {
        this.addressesAssociationPublicIp = addressesAssociationPublicIp;
    }

    @Filter("addresses.association.owner-id")
    public String getAddressesAssociationOwnerId() {
        return addressesAssociationOwnerId;
    }

    public void setAddressesAssociationOwnerId(String addressesAssociationOwnerId) {
        this.addressesAssociationOwnerId = addressesAssociationOwnerId;
    }

    @Filter("association.association-id")
    public String getAssociationAssociationId() {
        return associationAssociationId;
    }

    public void setAssociationAssociationId(String associationAssociationId) {
        this.associationAssociationId = associationAssociationId;
    }

    @Filter("association.allocation-id")
    public String getAssociationAllocationId() {
        return associationAllocationId;
    }

    public void setAssociationAllocationId(String associationAllocationId) {
        this.associationAllocationId = associationAllocationId;
    }

    @Filter("association.ip-owner-id")
    public String getAssociationIpOwnerId() {
        return associationIpOwnerId;
    }

    public void setAssociationIpOwnerId(String associationIpOwnerId) {
        this.associationIpOwnerId = associationIpOwnerId;
    }

    @Filter("association.public-ip")
    public String getAssociationPublicIp() {
        return associationPublicIp;
    }

    public void setAssociationPublicIp(String associationPublicIp) {
        this.associationPublicIp = associationPublicIp;
    }

    @Filter("association.public-dns-name")
    public String getAssociationPublicDnsName() {
        return associationPublicDnsName;
    }

    public void setAssociationPublicDnsName(String associationPublicDnsName) {
        this.associationPublicDnsName = associationPublicDnsName;
    }

    @Filter("attachment.attachment-id")
    public String getAttachmentAttachmentId() {
        return attachmentAttachmentId;
    }

    public void setAttachmentAttachmentId(String attachmentAttachmentId) {
        this.attachmentAttachmentId = attachmentAttachmentId;
    }

    @Filter("attachment.attach.time")
    public String getAttachmentAttachTime() {
        return attachmentAttachTime;
    }

    public void setAttachmentAttachTime(String attachmentAttachTime) {
        this.attachmentAttachTime = attachmentAttachTime;
    }

    @Filter("attachment.delete-on-termination")
    public String getAttachmentDeleteOnTermination() {
        return attachmentDeleteOnTermination;
    }

    public void setAttachmentDeleteOnTermination(String attachmentDeleteOnTermination) {
        this.attachmentDeleteOnTermination = attachmentDeleteOnTermination;
    }

    @Filter("attachment.device-index")
    public String getAttachmentDeviceIndex() {
        return attachmentDeviceIndex;
    }

    public void setAttachmentDeviceIndex(String attachmentDeviceIndex) {
        this.attachmentDeviceIndex = attachmentDeviceIndex;
    }

    @Filter("attachment.instance-id")
    public String getAttachmentInstanceId() {
        return attachmentInstanceId;
    }

    public void setAttachmentInstanceId(String attachmentInstanceId) {
        this.attachmentInstanceId = attachmentInstanceId;
    }

    @Filter("attachment.instance-owner-id")
    public String getAttachmentInstanceOwnerId() {
        return attachmentInstanceOwnerId;
    }

    public void setAttachmentInstanceOwnerId(String attachmentInstanceOwnerId) {
        this.attachmentInstanceOwnerId = attachmentInstanceOwnerId;
    }

    @Filter("attachment.nat-gateway-id")
    public String getAttachmentNatGatewayId() {
        return attachmentNatGatewayId;
    }

    public void setAttachmentNatGatewayId(String attachmentNatGatewayId) {
        this.attachmentNatGatewayId = attachmentNatGatewayId;
    }

    @Filter("attachment.status")
    public String getAttachmentStatus() {
        return attachmentStatus;
    }

    public void setAttachmentStatus(String attachmentStatus) {
        this.attachmentStatus = attachmentStatus;
    }

    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    @Filter("ipv6-addresses.ipv6-address")
    public String getIpv6AddressesIpv6Address() {
        return ipv6AddressesIpv6Address;
    }

    public void setIpv6AddressesIpv6Address(String ipv6AddressesIpv6Address) {
        this.ipv6AddressesIpv6Address = ipv6AddressesIpv6Address;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getNetworkInterfaceId() {
        return networkInterfaceId;
    }

    public void setNetworkInterfaceId(String networkInterfaceId) {
        this.networkInterfaceId = networkInterfaceId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getPrivateIpAddress() {
        return privateIpAddress;
    }

    public void setPrivateIpAddress(String privateIpAddress) {
        this.privateIpAddress = privateIpAddress;
    }

    public String getPrivateDnsName() {
        return privateDnsName;
    }

    public void setPrivateDnsName(String privateDnsName) {
        this.privateDnsName = privateDnsName;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterManaged() {
        return requesterManaged;
    }

    public void setRequesterManaged(String requesterManaged) {
        this.requesterManaged = requesterManaged;
    }

    public String getSourceDestCheck() {
        return sourceDestCheck;
    }

    public void setSourceDestCheck(String sourceDestCheck) {
        this.sourceDestCheck = sourceDestCheck;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public Map<String, String> getTag() {
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

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    @Override
    protected List<NetworkInterface> findAllAws(Ec2Client client) {
        return client.describeNetworkInterfaces().networkInterfaces();
    }

    @Override
    protected List<NetworkInterface> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeNetworkInterfaces(r -> r.filters(createFilters(filters))).networkInterfaces();
    }
}
