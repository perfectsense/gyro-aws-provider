package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private String egressIpPermissionCidr;
    private String egressIpPermissionFromPort;
    private String egressIpPermissionGroupId;
    private String egressIpPermissionGroupName;
    private String egressIpPermissionIpv6Cidr;
    private String egressIpPermissionPrefixListId;
    private String egressIpPermissionProtocol;
    private String egressIpPermissionToPort;
    private String egressIpPermissionUserId;
    private String groupId;
    private String groupName;
    private String ipPermissionCidr;
    private String ipPermissionFromPort;
    private String ipPermissionGroupId;
    private String ipPermissionGroupName;
    private String ipPermissionIpv6Cidr;
    private String ipPermissionPrefixListId;
    private String ipPermissionProtocol;
    private String ipPermissionToPort;
    private String ipPermissionUserId;
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
    public String getEgressIpPermissionCidr() {
        return egressIpPermissionCidr;
    }

    public void setEgressIpPermissionCidr(String egressIpPermissionCidr) {
        this.egressIpPermissionCidr = egressIpPermissionCidr;
    }

    /**
     * For an outbound rule, the start of port range for the TCP and UDP protocols, or an ICMP type number.
     */
    @Filter("egress.ip-permission.from-port")
    public String getEgressIpPermissionFromPort() {
        return egressIpPermissionFromPort;
    }

    public void setEgressIpPermissionFromPort(String egressIpPermissionFromPort) {
        this.egressIpPermissionFromPort = egressIpPermissionFromPort;
    }

    /**
     * The ID of a security group that has been referenced in an outbound security group rule.
     */
    @Filter("egress.ip-permission.group-id")
    public String getEgressIpPermissionGroupId() {
        return egressIpPermissionGroupId;
    }

    public void setEgressIpPermissionGroupId(String egressIpPermissionGroupId) {
        this.egressIpPermissionGroupId = egressIpPermissionGroupId;
    }

    /**
     * The name of a security group that has been referenced in an outbound security group rule.
     */
    @Filter("egress.ip-permission.group-name")
    public String getEgressIpPermissionGroupName() {
        return egressIpPermissionGroupName;
    }

    public void setEgressIpPermissionGroupName(String egressIpPermissionGroupName) {
        this.egressIpPermissionGroupName = egressIpPermissionGroupName;
    }

    /**
     * An IPv6 CIDR block for an outbound security group rule.
     */
    @Filter("egress.ip-permission.ipv6-cidr")
    public String getEgressIpPermissionIpv6Cidr() {
        return egressIpPermissionIpv6Cidr;
    }

    public void setEgressIpPermissionIpv6Cidr(String egressIpPermissionIpv6Cidr) {
        this.egressIpPermissionIpv6Cidr = egressIpPermissionIpv6Cidr;
    }

    /**
     * The ID (prefix) of the AWS service to which a security group rule allows outbound access.
     */
    @Filter("egress.ip-permission.prefix-list-id")
    public String getEgressIpPermissionPrefixListId() {
        return egressIpPermissionPrefixListId;
    }

    public void setEgressIpPermissionPrefixListId(String egressIpPermissionPrefixListId) {
        this.egressIpPermissionPrefixListId = egressIpPermissionPrefixListId;
    }

    /**
     * The IP protocol for an outbound security group rule . Valid values are `` tcp `` or `` udp `` or `` icmp or a protocol number``.
     */
    @Filter("egress.ip-permission.protocol")
    public String getEgressIpPermissionProtocol() {
        return egressIpPermissionProtocol;
    }

    public void setEgressIpPermissionProtocol(String egressIpPermissionProtocol) {
        this.egressIpPermissionProtocol = egressIpPermissionProtocol;
    }

    /**
     * For an outbound rule, the end of port range for the TCP and UDP protocols, or an ICMP code.
     */
    @Filter("egress.ip-permission.to-port")
    public String getEgressIpPermissionToPort() {
        return egressIpPermissionToPort;
    }

    public void setEgressIpPermissionToPort(String egressIpPermissionToPort) {
        this.egressIpPermissionToPort = egressIpPermissionToPort;
    }

    /**
     * The ID of an AWS account that has been referenced in an outbound security group rule.
     */
    @Filter("egress.ip-permission.user-id")
    public String getEgressIpPermissionUserId() {
        return egressIpPermissionUserId;
    }

    public void setEgressIpPermissionUserId(String egressIpPermissionUserId) {
        this.egressIpPermissionUserId = egressIpPermissionUserId;
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
    public String getIpPermissionCidr() {
        return ipPermissionCidr;
    }

    public void setIpPermissionCidr(String ipPermissionCidr) {
        this.ipPermissionCidr = ipPermissionCidr;
    }

    /**
     * For an inbound rule, the start of port range for the TCP and UDP protocols, or an ICMP type number.
     */
    @Filter("ip-permission.from-port")
    public String getIpPermissionFromPort() {
        return ipPermissionFromPort;
    }

    public void setIpPermissionFromPort(String ipPermissionFromPort) {
        this.ipPermissionFromPort = ipPermissionFromPort;
    }

    /**
     * The ID of a security group that has been referenced in an inbound security group rule.
     */
    @Filter("ip-permission.group-id")
    public String getIpPermissionGroupId() {
        return ipPermissionGroupId;
    }

    public void setIpPermissionGroupId(String ipPermissionGroupId) {
        this.ipPermissionGroupId = ipPermissionGroupId;
    }

    /**
     * The name of a security group that has been referenced in an inbound security group rule.
     */
    @Filter("ip-permission.group-name")
    public String getIpPermissionGroupName() {
        return ipPermissionGroupName;
    }

    public void setIpPermissionGroupName(String ipPermissionGroupName) {
        this.ipPermissionGroupName = ipPermissionGroupName;
    }

    /**
     * An IPv6 CIDR block for an inbound security group rule.
     */
    @Filter("ip-permission.ipv6-cidr")
    public String getIpPermissionIpv6Cidr() {
        return ipPermissionIpv6Cidr;
    }

    public void setIpPermissionIpv6Cidr(String ipPermissionIpv6Cidr) {
        this.ipPermissionIpv6Cidr = ipPermissionIpv6Cidr;
    }

    /**
     * The ID (prefix) of the AWS service from which a security group rule allows inbound access.
     */
    @Filter("ip-permission.prefix-list-id")
    public String getIpPermissionPrefixListId() {
        return ipPermissionPrefixListId;
    }

    public void setIpPermissionPrefixListId(String ipPermissionPrefixListId) {
        this.ipPermissionPrefixListId = ipPermissionPrefixListId;
    }

    /**
     * The IP protocol for an inbound security group rule . Valid values are ``tcp `` or `` udp `` or `` icmp or a protocol number``.
     */
    @Filter("ip-permission.protocol")
    public String getIpPermissionProtocol() {
        return ipPermissionProtocol;
    }

    public void setIpPermissionProtocol(String ipPermissionProtocol) {
        this.ipPermissionProtocol = ipPermissionProtocol;
    }

    /**
     * For an inbound rule, the end of port range for the TCP and UDP protocols, or an ICMP code.
     */
    @Filter("ip-permission.to-port")
    public String getIpPermissionToPort() {
        return ipPermissionToPort;
    }

    public void setIpPermissionToPort(String ipPermissionToPort) {
        this.ipPermissionToPort = ipPermissionToPort;
    }

    /**
     * The ID of an AWS account that has been referenced in an inbound security group rule.
     */
    @Filter("ip-permission.user-id")
    public String getIpPermissionUserId() {
        return ipPermissionUserId;
    }

    public void setIpPermissionUserId(String ipPermissionUserId) {
        this.ipPermissionUserId = ipPermissionUserId;
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
        return client.describeSecurityGroups(r -> r.filters(createFilters(filters))).securityGroups();
    }

    @Override
    protected List<SecurityGroup> findAllAws(Ec2Client client) {
        return client.describeSecurityGroups().securityGroups();
    }
}
