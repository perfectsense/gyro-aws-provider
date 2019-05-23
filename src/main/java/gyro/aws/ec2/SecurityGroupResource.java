package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.core.GyroException;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupResponse;
import software.amazon.awssdk.services.ec2.model.DescribeSecurityGroupsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
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
@Type("security-group")
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
    @Updatable
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
    @Id
    @Output
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
    @Output
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    protected boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        SecurityGroup group = getSecurityGroup(client);

        if (group == null) {
            return false;
        }

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

    @Override
    protected void doCreate() {
        Ec2Client client = createClient(Ec2Client.class);

        CreateSecurityGroupResponse response = client.createSecurityGroup(
            r -> r.vpcId(getVpc().getId()).description(getDescription()).groupName(getGroupName())
        );

        setGroupId(response.groupId());

        if (!isKeepDefaultEgressRules()) {
            deleteDefaultEgressRule(client);
        }
    }

    @Override
    protected void doUpdate(AwsResource config, Set<String> changedProperties) {
        SecurityGroupResource current = (SecurityGroupResource) config;

        if (!current.isKeepDefaultEgressRules() && isKeepDefaultEgressRules()) {
            throw new GyroException("Default rule removed. Cannot be undone.");
        }

        if (current.isKeepDefaultEgressRules() && !isKeepDefaultEgressRules()) {
            if (getEgress()
                .stream()
                .noneMatch(
                    o -> o.getToPort() == null
                        && o.getFromPort() == null
                        && o.getProtocol().equals("-1")
                        && o.getCidrBlocks().contains("0.0.0.0/0"))
                ) {

                deleteDefaultEgressRule(createClient(Ec2Client.class));
            }
        }
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

    private SecurityGroup getSecurityGroup(Ec2Client client) {
        SecurityGroup securityGroup = null;
        if (ObjectUtils.isBlank(getGroupId())) {
            throw new GyroException("group-id is missing, unable to load security group.");
        }

        if (getVpc() == null) {
            throw new GyroException("vpc is missing, unable to load security group.");
        }

        try {
            DescribeSecurityGroupsResponse response = client.describeSecurityGroups(
                r -> r.filters(
                    f -> f.name("group-id").values(getGroupId()),
                    f -> f.name("vpc-id").values(getVpc().getId())
                )
            );

            if (!response.securityGroups().isEmpty()) {
                securityGroup = response.securityGroups().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return securityGroup;
    }

    private void deleteDefaultEgressRule(Ec2Client client) {
        SecurityGroup group = getSecurityGroup(client);

        IpPermission permission = group.ipPermissionsEgress().stream().filter(o -> o.ipProtocol().equals("-1")
            && o.fromPort() == null && o.toPort() == null
            && o.ipRanges().get(0).cidrIp().equals("0.0.0.0/0")).findFirst().orElse(null);

        SecurityGroupEgressRuleResource defaultRule = new SecurityGroupEgressRuleResource(permission);
        defaultRule.delete(client, getGroupId());
    }
}

