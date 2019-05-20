package gyro.aws.ec2;

import gyro.aws.AwsResourceFinder;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceFilter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Subnet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ResourceType("subnet")
public class SubnetResourceFinder extends AwsResourceFinder<Ec2Client, Subnet, SubnetResource> {

    private String availabilityZone;
    private String availabilityZoneId;
    private String availableIpAddressCount;
    private String cidrBlock;
    private String defaultForAz;
    private String ipV6CidrBlock;
    private String ipV6CiderBlockAssociationId;
    private String ipV6CiderBlockAssociationState;
    private String ownerId;
    private String state;
    private String subnetArn;
    private String subnetId;
    private Map<String, String> tag;
    private String tagKey;
    private String vpcId;

    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    public String getAvailabilityZoneId() {
        return availabilityZoneId;
    }

    public void setAvailabilityZoneId(String availabilityZoneId) {
        this.availabilityZoneId = availabilityZoneId;
    }

    public String getAvailableIpAddressCount() {
        return availableIpAddressCount;
    }

    public void setAvailableIpAddressCount(String availableIpAddressCount) {
        this.availableIpAddressCount = availableIpAddressCount;
    }

    public String getCidrBlock() {
        return cidrBlock;
    }

    public void setCidrBlock(String cidrBlock) {
        this.cidrBlock = cidrBlock;
    }

    public String getDefaultForAz() {
        return defaultForAz;
    }

    public void setDefaultForAz(String defaultForAz) {
        this.defaultForAz = defaultForAz;
    }

    @ResourceFilter("ipv6-cidr-block-association.ipv6-cidr-block")
    public String getIpV6CidrBlock() {
        return ipV6CidrBlock;
    }

    public void setIpV6CidrBlock(String ipV6CidrBlock) {
        this.ipV6CidrBlock = ipV6CidrBlock;
    }

    @ResourceFilter("ipv6-cidr-block-association.association-id")
    public String getIpV6CiderBlockAssociationId() {
        return ipV6CiderBlockAssociationId;
    }

    public void setIpV6CiderBlockAssociationId(String ipV6CiderBlockAssociationId) {
        this.ipV6CiderBlockAssociationId = ipV6CiderBlockAssociationId;
    }

    @ResourceFilter("ipv6-cidr-block-association.state")
    public String getIpV6CiderBlockAssociationState() {
        return ipV6CiderBlockAssociationState;
    }

    public void setIpV6CiderBlockAssociationState(String ipV6CiderBlockAssociationState) {
        this.ipV6CiderBlockAssociationState = ipV6CiderBlockAssociationState;
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

    public String getSubnetArn() {
        return subnetArn;
    }

    public void setSubnetArn(String subnetArn) {
        this.subnetArn = subnetArn;
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

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    @Override
    public List<Subnet> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeSubnets(r -> r.filters(createFilters(filters))).subnets();
    }

    @Override
    public List<Subnet> findAllAws(Ec2Client client) {
        return client.describeSubnets().subnets();
    }

    @Override
    public void populateAws(Ec2Client client, SubnetResource subnetResource, Subnet subnet) {
        subnetResource.setSubnetId(subnet.subnetId());
        subnetResource.setCidrBlock(subnet.cidrBlock());
        subnetResource.setAvailabilityZone(subnet.availabilityZone());
        subnetResource.setMapPublicIpOnLaunch(subnet.mapPublicIpOnLaunch());
        subnetResource.setVpc(subnetResource.findById(VpcResource.class, subnet.vpcId()));
    }
}
