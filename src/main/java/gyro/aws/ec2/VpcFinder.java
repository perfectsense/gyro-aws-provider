package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.resource.ResourceType;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Vpc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ResourceType("vpc")
public class VpcFinder extends AwsFinder<Ec2Client, Vpc, VpcResource> {

    private String cidr;
    private String dhcpOptionsId;
    private String ipv4CidrBlock;
    private String ipv4CidrBlockAssociationId;
    private String ipv4CidrBlockAssociationState;
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

    public String getDhcpOptionsId() {
        return dhcpOptionsId;
    }

    public void setDhcpOptionsId(String dhcpOptionsId) {
        this.dhcpOptionsId = dhcpOptionsId;
    }

    @Filter("cidr-block-association.cidr-block")
    public String getIpv4CidrBlock() {
        return ipv4CidrBlock;
    }

    public void setIpv4CidrBlock(String ipv4CidrBlock) {
        this.ipv4CidrBlock = ipv4CidrBlock;
    }

    @Filter("cidr-block-association.association-id")
    public String getIpv4CidrBlockAssociationId() {
        return ipv4CidrBlockAssociationId;
    }

    public void setIpv4CidrBlockAssociationId(String ipv4CidrBlockAssociationId) {
        this.ipv4CidrBlockAssociationId = ipv4CidrBlockAssociationId;
    }

    @Filter("cidr-block-association.state")
    public String getIpv4CidrBlockAssociationState() {
        return ipv4CidrBlockAssociationState;
    }

    public void setIpv4CidrBlockAssociationState(String ipv4CidrBlockAssociationState) {
        this.ipv4CidrBlockAssociationState = ipv4CidrBlockAssociationState;
    }

    @Filter("ipv6-cidr-block-association.ipv6-cidr-block")
    public String getIpv6CidrBlock() {
        return ipv6CidrBlock;
    }

    public void setIpv6CidrBlock(String ipv6CidrBlock) {
        this.ipv6CidrBlock = ipv6CidrBlock;
    }

    @Filter("ipv6-cidr-block-association.association-id")
    public String getIpv6CidrBlockAssociationId() {
        return ipv6CidrBlockAssociationId;
    }

    public void setIpv6CidrBlockAssociationId(String ipv6CidrBlockAssociationId) {
        this.ipv6CidrBlockAssociationId = ipv6CidrBlockAssociationId;
    }

    @Filter("ipv6-cidr-block-association.state")
    public String getIpv6CidrBlockAssociationState() {
        return ipv6CidrBlockAssociationState;
    }

    public void setIpv6CidrBlockAssociationState(String ipv6CidrBlockAssociationState) {
        this.ipv6CidrBlockAssociationState = ipv6CidrBlockAssociationState;
    }

    @Filter("isDefault")
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
    public List<Vpc> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeVpcs(r -> r.filters(createFilters(filters))).vpcs();
    }

    @Override
    public List<Vpc> findAllAws(Ec2Client client) {
        return client.describeVpcs().vpcs();
    }
}
