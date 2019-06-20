package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeVpcsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeVpcsResponse;
import software.amazon.awssdk.services.ec2.model.Vpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private String ipv4AssociationState;
    private String dhcpOptionsId;
    private String ipv6CidrBlock;
    private String ipv6CidrBlockAssociationId;
    private String ipv6CidrBlockAssociationState;
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
    public String getIpv4AssociationState() {
        return ipv4AssociationState;
    }

    public void setIpv4AssociationState(String ipv4AssociationState) {
        this.ipv4AssociationState = ipv4AssociationState;
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
    public String getIpv6CidrBlockAssociationId() {
        return ipv6CidrBlockAssociationId;
    }

    public void setIpv6CidrBlockAssociationId(String ipv6CidrBlockAssociationId) {
        this.ipv6CidrBlockAssociationId = ipv6CidrBlockAssociationId;
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
    @Filter("isDefault")
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
        return client.describeVpcsPaginator().vpcs().stream().collect(Collectors.toList());
    }

    @Override
    protected List<Vpc> findAws(Ec2Client client, Map<String, String> filters) {
        List<Vpc> vpcs = new ArrayList<>();

        String marker = null;
        DescribeVpcsResponse response;
        do {
            if (!ObjectUtils.isBlank(marker)) {
                response = client.describeVpcs(DescribeVpcsRequest.builder().filters(createFilters(filters)).build());
            } else {
                response = client.describeVpcs(DescribeVpcsRequest.builder().filters(createFilters(filters)).nextToken(marker).build());
            }

            marker = response.nextToken();
            vpcs.addAll(response.vpcs());
        } while (!ObjectUtils.isBlank(marker));

        return vpcs;
    }
}