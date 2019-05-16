package gyro.aws.ec2;

import gyro.aws.AwsResourceFinder;
import gyro.core.resource.ResourceFilter;
import gyro.core.resource.ResourceType;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ResourceType("security-group")
public class SecurityGroupResourceFinder extends AwsResourceFinder<Ec2Client, SecurityGroup, SecurityGroupResource> {
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
    private String ingressIpPermissionCidr;
    private String ingressIpPermissionFromPort;
    private String ingressIpPermissionGroupId;
    private String ingressIpPermissionGroupName;
    private String ingressIpPermissionIpv6Cidr;
    private String ingressIpPermissionPrefixListId;
    private String ingressIpPermissionProtocol;
    private String ingressIpPermissionToPort;
    private String ingressIpPermissionUserId;
    private String ownerId;
    private Map<String, String> tag;
    private String tagKey;
    private String vpcId;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ResourceFilter("egress.ip-permission.cidr")
    public String getEgressIpPermissionCidr() {
        return egressIpPermissionCidr;
    }

    public void setEgressIpPermissionCidr(String egressIpPermissionCidr) {
        this.egressIpPermissionCidr = egressIpPermissionCidr;
    }

    @ResourceFilter("egress.ip-permission.from-port")
    public String getEgressIpPermissionFromPort() {
        return egressIpPermissionFromPort;
    }

    public void setEgressIpPermissionFromPort(String egressIpPermissionFromPort) {
        this.egressIpPermissionFromPort = egressIpPermissionFromPort;
    }

    @ResourceFilter("egress.ip-permission.group-id")
    public String getEgressIpPermissionGroupId() {
        return egressIpPermissionGroupId;
    }

    public void setEgressIpPermissionGroupId(String egressIpPermissionGroupId) {
        this.egressIpPermissionGroupId = egressIpPermissionGroupId;
    }

    @ResourceFilter("egress.ip-permission.group-name")
    public String getEgressIpPermissionGroupName() {
        return egressIpPermissionGroupName;
    }

    public void setEgressIpPermissionGroupName(String egressIpPermissionGroupName) {
        this.egressIpPermissionGroupName = egressIpPermissionGroupName;
    }

    @ResourceFilter("egress.ip-permission.ipv6-cidr")
    public String getEgressIpPermissionIpv6Cidr() {
        return egressIpPermissionIpv6Cidr;
    }

    public void setEgressIpPermissionIpv6Cidr(String egressIpPermissionIpv6Cidr) {
        this.egressIpPermissionIpv6Cidr = egressIpPermissionIpv6Cidr;
    }

    @ResourceFilter("egress.ip-permission.prefix-list-id")
    public String getEgressIpPermissionPrefixListId() {
        return egressIpPermissionPrefixListId;
    }

    public void setEgressIpPermissionPrefixListId(String egressIpPermissionPrefixListId) {
        this.egressIpPermissionPrefixListId = egressIpPermissionPrefixListId;
    }

    @ResourceFilter("egress.ip-permission.protocol")
    public String getEgressIpPermissionProtocol() {
        return egressIpPermissionProtocol;
    }

    public void setEgressIpPermissionProtocol(String egressIpPermissionProtocol) {
        this.egressIpPermissionProtocol = egressIpPermissionProtocol;
    }

    @ResourceFilter("egress.ip-permission.to-port")
    public String getEgressIpPermissionToPort() {
        return egressIpPermissionToPort;
    }

    public void setEgressIpPermissionToPort(String egressIpPermissionToPort) {
        this.egressIpPermissionToPort = egressIpPermissionToPort;
    }

    @ResourceFilter("egress.ip-permission.user-id")
    public String getEgressIpPermissionUserId() {
        return egressIpPermissionUserId;
    }

    public void setEgressIpPermissionUserId(String egressIpPermissionUserId) {
        this.egressIpPermissionUserId = egressIpPermissionUserId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @ResourceFilter("ip-permission.cidr")
    public String getIngressIpPermissionCidr() {
        return ingressIpPermissionCidr;
    }

    public void setIngressIpPermissionCidr(String ingressIpPermissionCidr) {
        this.ingressIpPermissionCidr = ingressIpPermissionCidr;
    }

    @ResourceFilter("ip-permission.from-port")
    public String getIngressIpPermissionFromPort() {
        return ingressIpPermissionFromPort;
    }

    public void setIngressIpPermissionFromPort(String ingressIpPermissionFromPort) {
        this.ingressIpPermissionFromPort = ingressIpPermissionFromPort;
    }

    @ResourceFilter("ip-permission.group-id")
    public String getIngressIpPermissionGroupId() {
        return ingressIpPermissionGroupId;
    }

    public void setIngressIpPermissionGroupId(String ingressIpPermissionGroupId) {
        this.ingressIpPermissionGroupId = ingressIpPermissionGroupId;
    }

    @ResourceFilter("ip-permission.group-name")
    public String getIngressIpPermissionGroupName() {
        return ingressIpPermissionGroupName;
    }

    public void setIngressIpPermissionGroupName(String ingressIpPermissionGroupName) {
        this.ingressIpPermissionGroupName = ingressIpPermissionGroupName;
    }

    @ResourceFilter("ip-permission.ipv6-cidr")
    public String getIngressIpPermissionIpv6Cidr() {
        return ingressIpPermissionIpv6Cidr;
    }

    public void setIngressIpPermissionIpv6Cidr(String ingressIpPermissionIpv6Cidr) {
        this.ingressIpPermissionIpv6Cidr = ingressIpPermissionIpv6Cidr;
    }

    @ResourceFilter("ip-permission.prefix-list-id")
    public String getIngressIpPermissionPrefixListId() {
        return ingressIpPermissionPrefixListId;
    }

    public void setIngressIpPermissionPrefixListId(String ingressIpPermissionPrefixListId) {
        this.ingressIpPermissionPrefixListId = ingressIpPermissionPrefixListId;
    }

    @ResourceFilter("ip-permission.protocol")
    public String getIngressIpPermissionProtocol() {
        return ingressIpPermissionProtocol;
    }

    public void setIngressIpPermissionProtocol(String ingressIpPermissionProtocol) {
        this.ingressIpPermissionProtocol = ingressIpPermissionProtocol;
    }

    @ResourceFilter("ip-permission.to-port")
    public String getIngressIpPermissionToPort() {
        return ingressIpPermissionToPort;
    }

    public void setIngressIpPermissionToPort(String ingressIpPermissionToPort) {
        this.ingressIpPermissionToPort = ingressIpPermissionToPort;
    }

    @ResourceFilter("ip-permission.user-id")
    public String getIngressIpPermissionUserId() {
        return ingressIpPermissionUserId;
    }

    public void setIngressIpPermissionUserId(String ingressIpPermissionUserId) {
        this.ingressIpPermissionUserId = ingressIpPermissionUserId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
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
    protected List<SecurityGroup> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeSecurityGroups(r -> r.filters(createFilters(filters))).securityGroups();
    }

    @Override
    protected List<SecurityGroup> findAllAws(Ec2Client client) {
        return client.describeSecurityGroups().securityGroups();
    }
}
