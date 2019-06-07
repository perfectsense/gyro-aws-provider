package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Vpc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Query vpc.
 *
 * .. code-block:: gyro
 *
 *    vpc: $(aws::vpc EXTERNAL/* | tag.Name = "vpc-example-for-network-acl")
 */
@Type("vpc")
public class VpcFinder extends AwsFinder<Ec2Client, Vpc, VpcResource> {

    private String cidr;
    private String ipv4CidrBlock;
    private String ipv4AssociationId;
    private String ipv4State;
    private String dhcpOptionsId;
    private String ipv6CidrBlock;
    private String ipv6AssociationId;
    private String ipv6State;
    private String isDefault;
    private String ownerId;
    private String state;
    private Map<String, String> tag;
    private String tagKey;
    private String vpcId;

    /**
     * The primary IPv4 CIDR block of the VPC. The CIDR block you specify must exactly match the VPC's CIDR block for information to be returned for the VPC. Must contain the slash followed by one or two digits (for example, /28).
     */
    public String getCidr() {
        return cidr;
    }

    public void setCidr(String cidr) {
        this.cidr = cidr;
    }

    /**
     * An IPv4 CIDR block associated with the VPC.
     */
    @Filter("cidr-block-association.cidr-block")
    public String getIpv4CidrBlock() {
        return ipv4CidrBlock;
    }

    public void setIpv4CidrBlock(String ipv4CidrBlock) {
        this.ipv4CidrBlock = ipv4CidrBlock;
    }

    /**
     * The association ID for an IPv4 CIDR block associated with the VPC.
     */
    @Filter("cidr-block-association.association-id")
    public String getIpv4AssociationId() {
        return ipv4AssociationId;
    }

    public void setIpv4AssociationId(String ipv4AssociationId) {
        this.ipv4AssociationId = ipv4AssociationId;
    }

    /**
     * The state of an IPv4 CIDR block associated with the VPC.
     */
    @Filter("cidr-block-association.state")
    public String getIpv4State() {
        return ipv4State;
    }

    public void setIpv4State(String ipv4State) {
        this.ipv4State = ipv4State;
    }

    /**
     * The ID of a set of DHCP options.
     */
    public String getDhcpOptionsId() {
        return dhcpOptionsId;
    }

    public void setDhcpOptionsId(String dhcpOptionsId) {
        this.dhcpOptionsId = dhcpOptionsId;
    }

    /**
     * An IPv6 CIDR block associated with the VPC.
     */
    @Filter("ipv6-cidr-block-association.ipv6-cidr-block")
    public String getIpv6CidrBlock() {
        return ipv6CidrBlock;
    }

    public void setIpv6CidrBlock(String ipv6CidrBlock) {
        this.ipv6CidrBlock = ipv6CidrBlock;
    }

    /**
     * The association ID for an IPv6 CIDR block associated with the VPC.
     */
    @Filter("ipv6-cidr-block-association.association-id")
    public String getIpv6AssociationId() {
        return ipv6AssociationId;
    }

    public void setIpv6AssociationId(String ipv6AssociationId) {
        this.ipv6AssociationId = ipv6AssociationId;
    }

    /**
     * The state of an IPv6 CIDR block associated with the VPC.
     */
    @Filter("ipv6-cidr-block-association.state")
    public String getIpv6State() {
        return ipv6State;
    }

    public void setIpv6State(String ipv6State) {
        this.ipv6State = ipv6State;
    }

    /**
     * Indicates whether the VPC is the default VPC.
     */
    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * The ID of the AWS account that owns the VPC.
     */
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * The state of the VPC . Valid values are ``pending `` or `` available``.
     */
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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
     * The ID of the VPC.
     */
    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    @Override
    protected List<Vpc> findAllAws(Ec2Client client) {
        return client.describeVpcs().vpcs();
    }

    @Override
    protected List<Vpc> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeVpcs(r -> r.filters(createFilters(filters))).vpcs();
    }
}