package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Subnet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Query subnet.
 *
 * .. code-block:: gyro
 *
 *    subnet: $(aws::subnet EXTERNAL/* | availability-zone = '####')
 */
@Type("subnet")
public class SubnetFinder extends AwsFinder<Ec2Client, Subnet, SubnetResource> {

    private String availabilityZone;
    private String availabilityZoneId;
    private String availableIpAddressCount;
    private String cidrBlock;
    private String defaultForAz;
    private String ipv6CidrBlockAssociationIpv6CidrBlock;
    private String ipv6CidrBlockAssociationAssociationId;
    private String ipv6CidrBlockAssociationState;
    private String ownerId;
    private String state;
    private String subnetArn;
    private String subnetId;
    private Map<String, String> tag;
    private String tagKey;
    private String vpcId;

    /**
     * The Availability Zone for the subnet. You can also use availabilityZone as the filter name.
     */
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * The ID of the Availability Zone for the subnet. You can also use availabilityZoneId as the filter name.
     */
    public String getAvailabilityZoneId() {
        return availabilityZoneId;
    }

    public void setAvailabilityZoneId(String availabilityZoneId) {
        this.availabilityZoneId = availabilityZoneId;
    }

    /**
     * The number of IPv4 addresses in the subnet that are available.
     */
    public String getAvailableIpAddressCount() {
        return availableIpAddressCount;
    }

    public void setAvailableIpAddressCount(String availableIpAddressCount) {
        this.availableIpAddressCount = availableIpAddressCount;
    }

    /**
     * The IPv4 CIDR block of the subnet. The CIDR block you specify must exactly match the subnet's CIDR block for information to be returned for the subnet. You can also use cidr or cidrBlock as the filter names.
     */
    public String getCidrBlock() {
        return cidrBlock;
    }

    public void setCidrBlock(String cidrBlock) {
        this.cidrBlock = cidrBlock;
    }

    /**
     * Indicates whether this is the default subnet for the Availability Zone. You can also use defaultForAz as the filter name.
     */
    public String getDefaultForAz() {
        return defaultForAz;
    }

    public void setDefaultForAz(String defaultForAz) {
        this.defaultForAz = defaultForAz;
    }

    /**
     * An IPv6 CIDR block associated with the subnet.
     */
    @Filter("ipv6-cidr-block-association.ipv6-cidr-block")
    public String getIpv6CidrBlockAssociationIpv6CidrBlock() {
        return ipv6CidrBlockAssociationIpv6CidrBlock;
    }

    public void setIpv6CidrBlockAssociationIpv6CidrBlock(String ipv6CidrBlockAssociationIpv6CidrBlock) {
        this.ipv6CidrBlockAssociationIpv6CidrBlock = ipv6CidrBlockAssociationIpv6CidrBlock;
    }

    /**
     * An association ID for an IPv6 CIDR block associated with the subnet.
     */
    @Filter("ipv6-cidr-block-association.association-id")
    public String getIpv6CidrBlockAssociationAssociationId() {
        return ipv6CidrBlockAssociationAssociationId;
    }

    public void setIpv6CidrBlockAssociationAssociationId(String ipv6CidrBlockAssociationAssociationId) {
        this.ipv6CidrBlockAssociationAssociationId = ipv6CidrBlockAssociationAssociationId;
    }

    /**
     * The state of an IPv6 CIDR block associated with the subnet.
     */
    @Filter("ipv6-cidr-block-association.state")
    public String getIpv6CidrBlockAssociationState() {
        return ipv6CidrBlockAssociationState;
    }

    public void setIpv6CidrBlockAssociationState(String ipv6CidrBlockAssociationState) {
        this.ipv6CidrBlockAssociationState = ipv6CidrBlockAssociationState;
    }

    /**
     * The ID of the AWS account that owns the subnet.
     */
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * The state of the subnet . Valid values are ``pending `` or `` available``.
     */
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * The Amazon Resource Name (ARN) of the subnet.
     */
    public String getSubnetArn() {
        return subnetArn;
    }

    public void setSubnetArn(String subnetArn) {
        this.subnetArn = subnetArn;
    }

    /**
     * The ID of the subnet.
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
     * The ID of the VPC for the subnet.
     */
    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    @Override
    protected List<Subnet> findAllAws(Ec2Client client) {
        return client.describeSubnets().subnets();
    }

    @Override
    protected List<Subnet> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeSubnets(r -> r.filters(createFilters(filters))).subnets();
    }
}