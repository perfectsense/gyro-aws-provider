package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query security group.
 *
 * .. code-block:: gyro
 *
 *    security-group: $(aws::security-group EXTERNAL/* | group-name = '')
 */
@Type("security-group")
public class SecurityGroupFinder extends AwsFinder<Ec2Client, SecurityGroup, SecurityGroupResource> {

    private String description;
    private String egressCidr;
    private String egressFromPort;
    private String egressGroupId;
    private String egressGroupName;
    private String egressIpv6Cidr;
    private String egressPrefixListId;
    private String egressProtocol;
    private String egressToPort;
    private String egressUserId;
    private String groupId;
    private String groupName;
    private String ingressCidr;
    private String ingressFromPort;
    private String ingressGroupId;
    private String ingressGroupName;
    private String ingressIpv6Cidr;
    private String ingressPrefixListId;
    private String ingressProtocol;
    private String ingressToPort;
    private String ingressUserId;
    private String ownerId;
    private Map<String, String> tag;
    private String tagKey;
    private String vpcId;

    /**
     * The description of the security group.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * An IPv4 CIDR block for an outbound security group rule.
     */
    @Filter("egress.ip-permission.cidr")
    public String getEgressCidr() {
        return egressCidr;
    }

    public void setEgressCidr(String egressCidr) {
        this.egressCidr = egressCidr;
    }

    /**
     * For an outbound rule, the start of port range for the TCP and UDP protocols, or an ICMP type number.
     */
    @Filter("egress.ip-permission.from-port")
    public String getEgressFromPort() {
        return egressFromPort;
    }

    public void setEgressFromPort(String egressFromPort) {
        this.egressFromPort = egressFromPort;
    }

    /**
     * The ID of a security group that has been referenced in an outbound security group rule.
     */
    @Filter("egress.ip-permission.group-id")
    public String getEgressGroupId() {
        return egressGroupId;
    }

    public void setEgressGroupId(String egressGroupId) {
        this.egressGroupId = egressGroupId;
    }

    /**
     * The name of a security group that has been referenced in an outbound security group rule.
     */
    @Filter("egress.ip-permission.group-name")
    public String getEgressGroupName() {
        return egressGroupName;
    }

    public void setEgressGroupName(String egressGroupName) {
        this.egressGroupName = egressGroupName;
    }

    /**
     * An IPv6 CIDR block for an outbound security group rule.
     */
    @Filter("egress.ip-permission.ipv6-cidr")
    public String getEgressIpv6Cidr() {
        return egressIpv6Cidr;
    }

    public void setEgressIpv6Cidr(String egressIpv6Cidr) {
        this.egressIpv6Cidr = egressIpv6Cidr;
    }

    /**
     * The ID (prefix) of the AWS service to which a security group rule allows outbound access.
     */
    @Filter("egress.ip-permission.prefix-list-id")
    public String getEgressPrefixListId() {
        return egressPrefixListId;
    }

    public void setEgressPrefixListId(String egressPrefixListId) {
        this.egressPrefixListId = egressPrefixListId;
    }

    /**
     * The IP protocol for an outbound security group rule . Valid values are `` tcp `` or `` udp `` or `` icmp or a protocol number``.
     */
    @Filter("egress.ip-permission.protocol")
    public String getEgressProtocol() {
        return egressProtocol;
    }

    public void setEgressProtocol(String egressProtocol) {
        this.egressProtocol = egressProtocol;
    }

    /**
     * For an outbound rule, the end of port range for the TCP and UDP protocols, or an ICMP code.
     */
    @Filter("egress.ip-permission.to-port")
    public String getEgressToPort() {
        return egressToPort;
    }

    public void setEgressToPort(String egressToPort) {
        this.egressToPort = egressToPort;
    }

    /**
     * The ID of an AWS account that has been referenced in an outbound security group rule.
     */
    @Filter("egress.ip-permission.user-id")
    public String getEgressUserId() {
        return egressUserId;
    }

    public void setEgressUserId(String egressUserId) {
        this.egressUserId = egressUserId;
    }

    /**
     * The ID of the security group.
     */
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * The name of the security group.
     */
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * An IPv4 CIDR block for an inbound security group rule.
     */
    @Filter("ip-permission.cidr")
    public String getIngressCidr() {
        return ingressCidr;
    }

    public void setIngressCidr(String ingressCidr) {
        this.ingressCidr = ingressCidr;
    }

    /**
     * For an inbound rule, the start of port range for the TCP and UDP protocols, or an ICMP type number.
     */
    @Filter("ip-permission.from-port")
    public String getIngressFromPort() {
        return ingressFromPort;
    }

    public void setIngressFromPort(String ingressFromPort) {
        this.ingressFromPort = ingressFromPort;
    }

    /**
     * The ID of a security group that has been referenced in an inbound security group rule.
     */
    @Filter("ip-permission.group-id")
    public String getIngressGroupId() {
        return ingressGroupId;
    }

    public void setIngressGroupId(String ingressGroupId) {
        this.ingressGroupId = ingressGroupId;
    }

    /**
     * The name of a security group that has been referenced in an inbound security group rule.
     */
    @Filter("ip-permission.group-name")
    public String getIngressGroupName() {
        return ingressGroupName;
    }

    public void setIngressGroupName(String ingressGroupName) {
        this.ingressGroupName = ingressGroupName;
    }

    /**
     * An IPv6 CIDR block for an inbound security group rule.
     */
    @Filter("ip-permission.ipv6-cidr")
    public String getIngressIpv6Cidr() {
        return ingressIpv6Cidr;
    }

    public void setIngressIpv6Cidr(String ingressIpv6Cidr) {
        this.ingressIpv6Cidr = ingressIpv6Cidr;
    }

    /**
     * The ID (prefix) of the AWS service from which a security group rule allows inbound access.
     */
    @Filter("ip-permission.prefix-list-id")
    public String getIngressPrefixListId() {
        return ingressPrefixListId;
    }

    public void setIngressPrefixListId(String ingressPrefixListId) {
        this.ingressPrefixListId = ingressPrefixListId;
    }

    /**
     * The IP protocol for an inbound security group rule . Valid values are ``tcp `` or `` udp `` or `` icmp or a protocol number``.
     */
    @Filter("ip-permission.protocol")
    public String getIngressProtocol() {
        return ingressProtocol;
    }

    public void setIngressProtocol(String ingressProtocol) {
        this.ingressProtocol = ingressProtocol;
    }

    /**
     * For an inbound rule, the end of port range for the TCP and UDP protocols, or an ICMP code.
     */
    @Filter("ip-permission.to-port")
    public String getIngressToPort() {
        return ingressToPort;
    }

    public void setIngressToPort(String ingressToPort) {
        this.ingressToPort = ingressToPort;
    }

    /**
     * The ID of an AWS account that has been referenced in an inbound security group rule.
     */
    @Filter("ip-permission.user-id")
    public String getIngressUserId() {
        return ingressUserId;
    }

    public void setIngressUserId(String ingressUserId) {
        this.ingressUserId = ingressUserId;
    }

    /**
     * The AWS account ID of the owner of the security group.
     */
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
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
     * The ID of the VPC specified when the security group was created.
     */
    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    @Override
    protected List<SecurityGroup> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeSecurityGroupsPaginator(r -> r.filters(createFilters(filters))).securityGroups().stream().collect(Collectors.toList());
    }

    @Override
    protected List<SecurityGroup> findAllAws(Ec2Client client) {
        return client.describeSecurityGroupsPaginator().securityGroups().stream().collect(Collectors.toList());
    }
}
