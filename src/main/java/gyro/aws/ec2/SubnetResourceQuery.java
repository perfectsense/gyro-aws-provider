package gyro.aws.ec2;

import gyro.aws.AwsResourceQuery;
import gyro.core.diff.ResourceName;
import gyro.lang.ast.query.ApiFilterable;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Filter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ResourceName("subnet")
public class SubnetResourceQuery extends AwsResourceQuery<SubnetResource> {

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
    // private Strign tag*;
    private String tagKey;
    private String vpcId;

    @ApiFilterable(filter = "availability-zone")
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    @ApiFilterable(filter = "availability-zone-id")
    public String getAvailabilityZoneId() {
        return availabilityZoneId;
    }

    public void setAvailabilityZoneId(String availabilityZoneId) {
        this.availabilityZoneId = availabilityZoneId;
    }

    @ApiFilterable(filter = "available-ip-address-count")
    public String getAvailableIpAddressCount() {
        return availableIpAddressCount;
    }

    public void setAvailableIpAddressCount(String availableIpAddressCount) {
        this.availableIpAddressCount = availableIpAddressCount;
    }

    @ApiFilterable(filter = "cidr-block")
    public String getCidrBlock() {
        return cidrBlock;
    }

    public void setCidrBlock(String cidrBlock) {
        this.cidrBlock = cidrBlock;
    }

    @ApiFilterable(filter = "default-for-az")
    public String getDefaultForAz() {
        return defaultForAz;
    }

    public void setDefaultForAz(String defaultForAz) {
        this.defaultForAz = defaultForAz;
    }

    @ApiFilterable(filter = "ipv6-cidr-block-association.ipv6-cidr-block")
    public String getIpV6CidrBlock() {
        return ipV6CidrBlock;
    }

    public void setIpV6CidrBlock(String ipV6CidrBlock) {
        this.ipV6CidrBlock = ipV6CidrBlock;
    }

    @ApiFilterable(filter = "ipv6-cidr-block-association.association-id")
    public String getIpV6CiderBlockAssociationId() {
        return ipV6CiderBlockAssociationId;
    }

    public void setIpV6CiderBlockAssociationId(String ipV6CiderBlockAssociationId) {
        this.ipV6CiderBlockAssociationId = ipV6CiderBlockAssociationId;
    }

    @ApiFilterable(filter = "ipv6-cidr-block-association.state")
    public String getIpV6CiderBlockAssociationState() {
        return ipV6CiderBlockAssociationState;
    }

    public void setIpV6CiderBlockAssociationState(String ipV6CiderBlockAssociationState) {
        this.ipV6CiderBlockAssociationState = ipV6CiderBlockAssociationState;
    }

    @ApiFilterable(filter = "owner-id")
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @ApiFilterable(filter = "state")
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @ApiFilterable(filter = "subnet-arn")
    public String getSubnetArn() {
        return subnetArn;
    }

    public void setSubnetArn(String subnetArn) {
        this.subnetArn = subnetArn;
    }

    @ApiFilterable(filter = "subnet-id")
    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    @ApiFilterable(filter = "tag-key")
    public String getTagKey() {
        return tagKey;
    }

    public void setTagKey(String tagKey) {
        this.tagKey = tagKey;
    }

    @ApiFilterable(filter = "vpc-id")
    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    @Override
    public List<SubnetResource> query(Map<String, String> filters) {
        Ec2Client client = createClient(Ec2Client.class);
        List<Filter> queryFilters = queryFilters(filters);

        return client.describeSubnets(r -> r.filters(queryFilters)).subnets()
            .stream()
            .map(s -> new SubnetResource(s))
            .collect(Collectors.toList());
    }

    @Override
    public List<SubnetResource> queryAll() {
        Ec2Client client = createClient(Ec2Client.class);

        return client.describeSubnets().subnets()
            .stream()
            .map(s -> new SubnetResource(s))
            .collect(Collectors.toList());
    }
}
