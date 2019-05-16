package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.core.resource.ResourceId;
import gyro.core.resource.ResourceUpdatable;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceOutput;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupResponse;
import software.amazon.awssdk.services.ec2.model.DescribeSecurityGroupsResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.IpPermission;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Create a security group with specified rules.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::security-group security-group-example
 *         group-name: "security-group-example"
 *         vpc-id: $(aws::vpc vpc-security-group-example | vpc-id)
 *         description: "security group example"
 *
 *         ingress
 *             description: "allow inbound http traffic"
 *             cidr-blocks: ["0.0.0.0/0"]
 *             protocol: "TCP"
 *             from-port: 80
 *             to-port: 80
 *         end
 *     end
 */
@ResourceType("security-group")
public class SecurityGroupResource extends Ec2TaggableResource<SecurityGroup> {

    private String groupName;
    private VpcResource vpc;
    private String description;
    private List<SecurityGroupIngressRuleResource> ingress;
    private List<SecurityGroupEgressRuleResource> egress;
    private Boolean keepDefaultEgressRules;

    // Read-only
    private String groupId;
    private String ownerId;

    /**
     * The name of the security group. (Required)
     */
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * The VPC to create the security group in. (Required)
     */
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    /**
     * The description of this security group.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * A list of ingress rules to block inbound traffic.
     *
     * @subresource gyro.aws.ec2.SecurityGroupIngressRuleResource
     */
    public List<SecurityGroupIngressRuleResource> getIngress() {
        if (ingress == null) {
            ingress = new ArrayList<>();
        }

        return ingress;
    }

    public void setIngress(List<SecurityGroupIngressRuleResource> ingress) {
        this.ingress = ingress;
    }

    /**
     * A list of egress rules to block outbound traffic.
     *
     * @subresource gyro.aws.ec2.SecurityGroupEgressRuleResource
     */
    public List<SecurityGroupEgressRuleResource> getEgress() {
        if (egress == null) {
            egress = new ArrayList<>();
        }

        return egress;
    }

    public void setEgress(List<SecurityGroupEgressRuleResource> egress) {
        this.egress = egress;
    }

    /**
     * Whether to keep the default egress rule. If false, the rule will be deleted.
     */
    @ResourceUpdatable
    public boolean isKeepDefaultEgressRules() {
        if (keepDefaultEgressRules == null) {
            keepDefaultEgressRules = true;
        }

        return keepDefaultEgressRules;
    }

    public void setKeepDefaultEgressRules(boolean keepDefaultEgressRules) {
        this.keepDefaultEgressRules = keepDefaultEgressRules;
    }

    /**
     * The id of the security group.
     */
    @ResourceId
    @ResourceOutput
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    protected String getId() {
        return getGroupId();
    }

    /**
     * The owner id of the security group.
     */
    @ResourceOutput
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    protected boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        List<Filter> filters = new ArrayList<>();
        Filter nameFilter = Filter.builder().name("group-id").values(getGroupId()).build();
        filters.add(nameFilter);

        if (getVpc() != null) {
            Filter vpcFilter = Filter.builder().name("vpc-id").values(getVpc().getId()).build();
            filters.add(vpcFilter);
        }

        DescribeSecurityGroupsResponse response = client.describeSecurityGroups(
            r -> r.filters(filters)
        );

        for (SecurityGroup group : response.securityGroups()) {
            setOwnerId(group.ownerId());
            setDescription(group.description());

            getEgress().clear();
            for (IpPermission permission : group.ipPermissionsEgress()) {
                if (isKeepDefaultEgressRules() && permission.ipProtocol().equals("-1")
                    && permission.fromPort() == null && permission.toPort() == null
                    && permission.ipRanges().get(0).cidrIp().equals("0.0.0.0/0")) {
                    continue;
                }

                SecurityGroupEgressRuleResource rule = new SecurityGroupEgressRuleResource(permission);
                getEgress().add(rule);
            }

            getIngress().clear();
            for (IpPermission permission : group.ipPermissions()) {
                SecurityGroupIngressRuleResource rule = new SecurityGroupIngressRuleResource(permission);
                getIngress().add(rule);
            }

            return true;
        }

        return false;
    }

    @Override
    protected void doCreate() {
        Ec2Client client = createClient(Ec2Client.class);

        CreateSecurityGroupResponse response = client.createSecurityGroup(
            r -> r.vpcId(getVpc().getId()).description(getDescription()).groupName(getGroupName())
        );

        setGroupId(response.groupId());
    }

    @Override
    protected void doUpdate(AwsResource config, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteSecurityGroup(r -> r.groupId(getGroupId()));
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        String groupId = getGroupId();

        sb.append("security group");
        if (groupId != null) {
            sb.append(" ").append(groupId);
        }

        String groupName = getGroupName();

        if (groupName != null) {
            sb.append(" - ");
            sb.append(groupName);
        }

        return sb.toString();
    }

}

