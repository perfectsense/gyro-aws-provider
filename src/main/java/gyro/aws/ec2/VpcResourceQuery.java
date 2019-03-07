package gyro.aws.ec2;

import gyro.aws.AwsResourceQuery;
import gyro.core.diff.ResourceName;
import gyro.lang.ast.query.ResourceFilter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ResourceName("vpc")
public class VpcResourceQuery extends AwsResourceQuery<VpcResource> {

    private String cidr;
    private String ipv4CidrBlock;
    private String cidrBlockAssociationId;
    private String cidrBlockAssociationState;
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

    public String getCidr() {
        return cidr;
    }

    public void setCidr(String cidr) {
        this.cidr = cidr;
    }

    @ResourceFilter("cidr-block-association.cidr-block")
    public String getIpv4CidrBlock() {
        return ipv4CidrBlock;
    }

    public void setIpv4CidrBlock(String ipv4CidrBlock) {
        this.ipv4CidrBlock = ipv4CidrBlock;
    }

    @ResourceFilter("cidr-block-association.association-id")
    public String getCidrBlockAssociationId() {
        return cidrBlockAssociationId;
    }

    public void setCidrBlockAssociationId(String cidrBlockAssociationId) {
        this.cidrBlockAssociationId = cidrBlockAssociationId;
    }

    @ResourceFilter("cidr-block-association.state")
    public String getCidrBlockAssociationState() {
        return cidrBlockAssociationState;
    }

    public void setCidrBlockAssociationState(String cidrBlockAssociationState) {
        this.cidrBlockAssociationState = cidrBlockAssociationState;
    }

    public String getDhcpOptionsId() {
        return dhcpOptionsId;
    }

    public void setDhcpOptionsId(String dhcpOptionsId) {
        this.dhcpOptionsId = dhcpOptionsId;
    }

    @ResourceFilter("ipv6-cidr-block-association.ipv6-cidr-block")
    public String getIpv6CidrBlock() {
        return ipv6CidrBlock;
    }

    public void setIpv6CidrBlock(String ipv6CidrBlock) {
        this.ipv6CidrBlock = ipv6CidrBlock;
    }

    @ResourceFilter("ipv6-cidr-block-association.association-id")
    public String getIpv6CidrBlockAssociationId() {
        return ipv6CidrBlockAssociationId;
    }

    public void setIpv6CidrBlockAssociationId(String ipv6CidrBlockAssociationId) {
        this.ipv6CidrBlockAssociationId = ipv6CidrBlockAssociationId;
    }

    @ResourceFilter("ipv6-cidr-block-association.state")
    public String getIpv6CidrBlockAssociationState() {
        return ipv6CidrBlockAssociationState;
    }

    public void setIpv6CidrBlockAssociationState(String ipv6CidrBlockAssociationState) {
        this.ipv6CidrBlockAssociationState = ipv6CidrBlockAssociationState;
    }

    @ResourceFilter("isDefault")
    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    @Override
    public List query(Map<String, String> filters) {
        Ec2Client client = createClient(Ec2Client.class);
        List<Filter> queryFilters = createFilters(filters);

        return client.describeVpcs(r -> r.filters(queryFilters)).vpcs()
            .stream()
            .map(v -> new VpcResource(client, v))
            .collect(Collectors.toList());
    }

    @Override
    public List queryAll() {
        Ec2Client client = createClient(Ec2Client.class);

        return client.describeVpcs().vpcs()
            .stream()
            .map(v -> new VpcResource(client, v))
            .collect(Collectors.toList());
    }
}
