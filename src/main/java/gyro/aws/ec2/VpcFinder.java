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
 *    vpc: $(aws::vpc EXTERNAL/* | cidr = '####')
 */
@Type("vpc")
public class VpcFinder extends AwsFinder<Ec2Client, Vpc, VpcResource> {

    private String cidr;
    private String cidrBlockAssociationCidrBlock;
    private String cidrBlockAssociationAssociationId;
    private String cidrBlockAssociationState;
    private String dhcpOptionsId;
    private String ipv6CidrBlockAssociationIpv6CidrBlock;
    private String ipv6CidrBlockAssociationAssociationId;
    private String ipv6CidrBlockAssociationState;
    private String isdefault;
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
    public String getCidrBlockAssociationCidrBlock() {
        return cidrBlockAssociationCidrBlock;
    }

    public void setCidrBlockAssociationCidrBlock(String cidrBlockAssociationCidrBlock) {
        this.cidrBlockAssociationCidrBlock = cidrBlockAssociationCidrBlock;
    }

    /**
     * The association ID for an IPv4 CIDR block associated with the VPC.
     */
    @Filter("cidr-block-association.association-id")
    public String getCidrBlockAssociationAssociationId() {
        return cidrBlockAssociationAssociationId;
    }

    public void setCidrBlockAssociationAssociationId(String cidrBlockAssociationAssociationId) {
        this.cidrBlockAssociationAssociationId = cidrBlockAssociationAssociationId;
    }

    /**
     * The state of an IPv4 CIDR block associated with the VPC.
     */
    @Filter("cidr-block-association.state")
    public String getCidrBlockAssociationState() {
        return cidrBlockAssociationState;
    }

    public void setCidrBlockAssociationState(String cidrBlockAssociationState) {
        this.cidrBlockAssociationState = cidrBlockAssociationState;
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
    public String getIpv6CidrBlockAssociationIpv6CidrBlock() {
        return ipv6CidrBlockAssociationIpv6CidrBlock;
    }

    public void setIpv6CidrBlockAssociationIpv6CidrBlock(String ipv6CidrBlockAssociationIpv6CidrBlock) {
        this.ipv6CidrBlockAssociationIpv6CidrBlock = ipv6CidrBlockAssociationIpv6CidrBlock;
    }

    /**
     * The association ID for an IPv6 CIDR block associated with the VPC.
     */
    @Filter("ipv6-cidr-block-association.association-id")
    public String getIpv6CidrBlockAssociationAssociationId() {
        return ipv6CidrBlockAssociationAssociationId;
    }

    public void setIpv6CidrBlockAssociationAssociationId(String ipv6CidrBlockAssociationAssociationId) {
        this.ipv6CidrBlockAssociationAssociationId = ipv6CidrBlockAssociationAssociationId;
    }

    /**
     * The state of an IPv6 CIDR block associated with the VPC.
     */
    @Filter("ipv6-cidr-block-association.state")
    public String getIpv6CidrBlockAssociationState() {
        return ipv6CidrBlockAssociationState;
    }

    public void setIpv6CidrBlockAssociationState(String ipv6CidrBlockAssociationState) {
        this.ipv6CidrBlockAssociationState = ipv6CidrBlockAssociationState;
    }

    /**
     * Indicates whether the VPC is the default VPC.
     */
    public String getIsdefault() {
        return isdefault;
    }

    public void setIsdefault(String isdefault) {
        this.isdefault = isdefault;
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