package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.NetworkInterface;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Query network interface.
 *
 * .. code-block:: gyro
 *
 *    network-interface: $(aws::network-interface EXTERNAL/* | description = '')
 */
@Type("network-interface")
public class NetworkInterfaceFinder extends AwsFinder<Ec2Client, NetworkInterface, NetworkInterfaceResource> {

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

    /**
     * The private IPv4 addresses associated with the network interface.
     */
    @Filter("addresses.private-ip-address")
    public String getAddressesPrivateIpAddress() {
        return addressesPrivateIpAddress;
    }

    public void setAddressesPrivateIpAddress(String addressesPrivateIpAddress) {
        this.addressesPrivateIpAddress = addressesPrivateIpAddress;
    }

    /**
     * Whether the private IPv4 address is the primary IP address associated with the network interface.
     */
    @Filter("addresses.primary")
    public String getAddressesPrimary() {
        return addressesPrimary;
    }

    public void setAddressesPrimary(String addressesPrimary) {
        this.addressesPrimary = addressesPrimary;
    }

    /**
     * The association ID returned when the network interface was associated with the Elastic IP address (IPv4).
     */
    @Filter("addresses.association.public-ip")
    public String getAddressesAssociationPublicIp() {
        return addressesAssociationPublicIp;
    }

    public void setAddressesAssociationPublicIp(String addressesAssociationPublicIp) {
        this.addressesAssociationPublicIp = addressesAssociationPublicIp;
    }

    /**
     * The owner ID of the addresses associated with the network interface.
     */
    @Filter("addresses.association.owner-id")
    public String getAddressesAssociationOwnerId() {
        return addressesAssociationOwnerId;
    }

    public void setAddressesAssociationOwnerId(String addressesAssociationOwnerId) {
        this.addressesAssociationOwnerId = addressesAssociationOwnerId;
    }

    /**
     * The association ID returned when the network interface was associated with an IPv4 address.
     */
    @Filter("association.association-id")
    public String getAssociationAssociationId() {
        return associationAssociationId;
    }

    public void setAssociationAssociationId(String associationAssociationId) {
        this.associationAssociationId = associationAssociationId;
    }

    /**
     * The allocation ID returned when you allocated the Elastic IP address (IPv4) for your network interface.
     */
    @Filter("association.allocation-id")
    public String getAssociationAllocationId() {
        return associationAllocationId;
    }

    public void setAssociationAllocationId(String associationAllocationId) {
        this.associationAllocationId = associationAllocationId;
    }

    /**
     * The owner of the Elastic IP address (IPv4) associated with the network interface.
     */
    @Filter("association.ip-owner-id")
    public String getAssociationIpOwnerId() {
        return associationIpOwnerId;
    }

    public void setAssociationIpOwnerId(String associationIpOwnerId) {
        this.associationIpOwnerId = associationIpOwnerId;
    }

    /**
     * The address of the Elastic IP address (IPv4) bound to the network interface.
     */
    @Filter("association.public-ip")
    public String getAssociationPublicIp() {
        return associationPublicIp;
    }

    public void setAssociationPublicIp(String associationPublicIp) {
        this.associationPublicIp = associationPublicIp;
    }

    /**
     * The public DNS name for the network interface (IPv4).
     */
    @Filter("association.public-dns-name")
    public String getAssociationPublicDnsName() {
        return associationPublicDnsName;
    }

    public void setAssociationPublicDnsName(String associationPublicDnsName) {
        this.associationPublicDnsName = associationPublicDnsName;
    }

    /**
     * The ID of the interface attachment.
     */
    @Filter("attachment.attachment-id")
    public String getAttachmentAttachmentId() {
        return attachmentAttachmentId;
    }

    public void setAttachmentAttachmentId(String attachmentAttachmentId) {
        this.attachmentAttachmentId = attachmentAttachmentId;
    }

    /**
     * The time that the network interface was attached to an instance.
     */
    @Filter("attachment.attach.time")
    public String getAttachmentAttachTime() {
        return attachmentAttachTime;
    }

    public void setAttachmentAttachTime(String attachmentAttachTime) {
        this.attachmentAttachTime = attachmentAttachTime;
    }

    /**
     * Indicates whether the attachment is deleted when an instance is terminated.
     */
    @Filter("attachment.delete-on-termination")
    public String getAttachmentDeleteOnTermination() {
        return attachmentDeleteOnTermination;
    }

    public void setAttachmentDeleteOnTermination(String attachmentDeleteOnTermination) {
        this.attachmentDeleteOnTermination = attachmentDeleteOnTermination;
    }

    /**
     * The device index to which the network interface is attached.
     */
    @Filter("attachment.device-index")
    public String getAttachmentDeviceIndex() {
        return attachmentDeviceIndex;
    }

    public void setAttachmentDeviceIndex(String attachmentDeviceIndex) {
        this.attachmentDeviceIndex = attachmentDeviceIndex;
    }

    /**
     * The ID of the instance to which the network interface is attached.
     */
    @Filter("attachment.instance-id")
    public String getAttachmentInstanceId() {
        return attachmentInstanceId;
    }

    public void setAttachmentInstanceId(String attachmentInstanceId) {
        this.attachmentInstanceId = attachmentInstanceId;
    }

    /**
     * The owner ID of the instance to which the network interface is attached.
     */
    @Filter("attachment.instance-owner-id")
    public String getAttachmentInstanceOwnerId() {
        return attachmentInstanceOwnerId;
    }

    public void setAttachmentInstanceOwnerId(String attachmentInstanceOwnerId) {
        this.attachmentInstanceOwnerId = attachmentInstanceOwnerId;
    }

    /**
     * The ID of the NAT gateway to which the network interface is attached.
     */
    @Filter("attachment.nat-gateway-id")
    public String getAttachmentNatGatewayId() {
        return attachmentNatGatewayId;
    }

    public void setAttachmentNatGatewayId(String attachmentNatGatewayId) {
        this.attachmentNatGatewayId = attachmentNatGatewayId;
    }

    /**
     * The status of the attachment . Valid values are ``attaching `` or `` attached `` or `` detaching `` or `` detached``.
     */
    @Filter("attachment.status")
    public String getAttachmentStatus() {
        return attachmentStatus;
    }

    public void setAttachmentStatus(String attachmentStatus) {
        this.attachmentStatus = attachmentStatus;
    }

    /**
     * The Availability Zone of the network interface.
     */
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * The description of the network interface.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The ID of a security group associated with the network interface.
     */
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * The name of a security group associated with the network interface.
     */
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * An IPv6 address associated with the network interface.
     */
    @Filter("ipv6-addresses.ipv6-address")
    public String getIpv6AddressesIpv6Address() {
        return ipv6AddressesIpv6Address;
    }

    public void setIpv6AddressesIpv6Address(String ipv6AddressesIpv6Address) {
        this.ipv6AddressesIpv6Address = ipv6AddressesIpv6Address;
    }

    /**
     * The MAC address of the network interface.
     */
    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    /**
     * The ID of the network interface.
     */
    public String getNetworkInterfaceId() {
        return networkInterfaceId;
    }

    public void setNetworkInterfaceId(String networkInterfaceId) {
        this.networkInterfaceId = networkInterfaceId;
    }

    /**
     * The AWS account ID of the network interface owner.
     */
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * The private IPv4 address or addresses of the network interface.
     */
    public String getPrivateIpAddress() {
        return privateIpAddress;
    }

    public void setPrivateIpAddress(String privateIpAddress) {
        this.privateIpAddress = privateIpAddress;
    }

    /**
     * The private DNS name of the network interface (IPv4).
     */
    public String getPrivateDnsName() {
        return privateDnsName;
    }

    public void setPrivateDnsName(String privateDnsName) {
        this.privateDnsName = privateDnsName;
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
     * Indicates whether the network interface is being managed by an AWS service (for example, AWS Management Console, Auto Scaling, and so on).
     */
    public String getRequesterManaged() {
        return requesterManaged;
    }

    public void setRequesterManaged(String requesterManaged) {
        this.requesterManaged = requesterManaged;
    }

    /**
     * Indicates whether the network interface performs source/destination checking. A value of true means checking is enabled, and false means checking is disabled. The value must be false for the network interface to perform network address translation (NAT) in your VPC.
     */
    public String getSourceDestCheck() {
        return sourceDestCheck;
    }

    public void setSourceDestCheck(String sourceDestCheck) {
        this.sourceDestCheck = sourceDestCheck;
    }

    /**
     * The status of the network interface. If the network interface is not attached to an instance, the status is available; if a network interface is attached to an instance the status is in-use.
     */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * The ID of the subnet for the network interface.
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
     * The key of a tag assigned to the resource. Use this filter to find all resources assigned a tag with a specific key, regardless of the tag value.
     */
    public String getTagKey() {
        return tagKey;
    }

    public void setTagKey(String tagKey) {
        this.tagKey = tagKey;
    }

    /**
     * The ID of the VPC for the network interface.
     */
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
