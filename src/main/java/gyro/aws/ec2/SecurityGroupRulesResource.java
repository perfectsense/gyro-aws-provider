package gyro.aws.ec2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeSecurityGroupsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.IpPermission;
import software.amazon.awssdk.services.ec2.model.IpRange;
import software.amazon.awssdk.services.ec2.model.Ipv6Range;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;
import software.amazon.awssdk.services.ec2.model.UserIdGroupPair;

/**
 * Add ingress and egress rules to a security group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::security-group backend
 *         vpc: $(aws::vpc vpc)
 *
 *         name: "backend"
 *         description: "backend"
 *     end
 *
 *     aws::security-group-rules backend
 *         security-group: $(aws::security-group backend)
 *
 *         ingress
 *             protocol: -1
 *             security-group: $SELF.security-group
 *         end
 *
 *         ingress
 *             protocol: -1
 *             security-group: $(aws::security-group master)
 *         end
 *     end
 */
@Type("security-group-rules")
public class SecurityGroupRulesResource extends AwsResource {

    private SecurityGroupResource securityGroup;
    private List<SecurityGroupIngressRuleResource> ingress;
    private List<SecurityGroupEgressRuleResource> egress;
    private Boolean keepDefaultEgressRules;

    /**
     * The security group to apply rules to.
     */
    @Required
    public SecurityGroupResource getSecurityGroup() {
        return securityGroup;
    }

    public void setSecurityGroup(SecurityGroupResource securityGroup) {
        this.securityGroup = securityGroup;
    }

    /**
     * A list of ingress rules to block inbound traffic.
     *
     * @subresource gyro.aws.ec2.SecurityGroupIngressRuleResource
     */
    @Updatable
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
    @Updatable
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
    public Boolean getKeepDefaultEgressRules() {
        if (keepDefaultEgressRules == null) {
            keepDefaultEgressRules = true;
        }

        return keepDefaultEgressRules;
    }

    public void setKeepDefaultEgressRules(Boolean keepDefaultEgressRules) {
        this.keepDefaultEgressRules = keepDefaultEgressRules;
    }

    @Override
    public boolean refresh() {
        Ec2Client client = createClient(Ec2Client.class);

        SecurityGroup group = getSecurityGroup(client);
        if (group == null) {
            return false;
        }

        copyFrom(group);

        return true;
    }

    public void copyFrom(SecurityGroup group) {
        getEgress().clear();
        for (IpPermission permission : group.ipPermissionsEgress()) {
            for (IpRange ipRange : permission.ipRanges()) {
                if (getKeepDefaultEgressRules() && permission.ipProtocol().equals("-1") && ipRange.cidrIp()
                    .equals("0.0.0.0/0")) {
                    continue;
                }

                SecurityGroupEgressRuleResource rule = newSubresource(SecurityGroupEgressRuleResource.class);
                rule.copyPortProtocol(permission);
                rule.setCidrBlock(ipRange.cidrIp());
                rule.setDescription(ipRange.description());
                getEgress().add(rule);
            }

            for (Ipv6Range ipRange : permission.ipv6Ranges()) {
                if (getKeepDefaultEgressRules() && permission.ipProtocol().equals("-1") && ipRange.cidrIpv6()
                    .equals("::/0")) {
                    continue;
                }

                SecurityGroupEgressRuleResource rule = newSubresource(SecurityGroupEgressRuleResource.class);
                rule.copyPortProtocol(permission);
                rule.setIpv6CidrBlock(ipRange.cidrIpv6());
                rule.setDescription(ipRange.description());
                getEgress().add(rule);
            }

            for (UserIdGroupPair groupPair : permission.userIdGroupPairs()) {
                SecurityGroupEgressRuleResource rule = newSubresource(SecurityGroupEgressRuleResource.class);
                rule.copyPortProtocol(permission);
                rule.setSecurityGroup(findById(SecurityGroupResource.class, groupPair.groupId()));
                rule.setDescription(groupPair.description());
                getEgress().add(rule);
            }
        }

        getIngress().clear();
        for (IpPermission permission : group.ipPermissions()) {
            for (IpRange ipRange : permission.ipRanges()) {
                SecurityGroupIngressRuleResource rule = newSubresource(SecurityGroupIngressRuleResource.class);
                rule.copyPortProtocol(permission);
                rule.setCidrBlock(ipRange.cidrIp());
                rule.setDescription(ipRange.description());
                getIngress().add(rule);
            }

            for (Ipv6Range ipRange : permission.ipv6Ranges()) {
                SecurityGroupIngressRuleResource rule = newSubresource(SecurityGroupIngressRuleResource.class);
                rule.copyPortProtocol(permission);
                rule.setIpv6CidrBlock(ipRange.cidrIpv6());
                rule.setDescription(ipRange.description());
                getIngress().add(rule);
            }

            for (UserIdGroupPair groupPair : permission.userIdGroupPairs()) {
                SecurityGroupIngressRuleResource rule = newSubresource(SecurityGroupIngressRuleResource.class);
                rule.copyPortProtocol(permission);
                rule.setSecurityGroup(findById(SecurityGroupResource.class, groupPair.groupId()));
                rule.setDescription(groupPair.description());
                getIngress().add(rule);
            }
        }
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);

        if (!getKeepDefaultEgressRules()) {
            deleteDefaultEgressRule(client);
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource resource, Set<String> changedFieldNames) throws Exception {
        SecurityGroupRulesResource current = (SecurityGroupRulesResource) resource;

        if (!current.getKeepDefaultEgressRules() && getKeepDefaultEgressRules()) {
            throw new GyroException("Default rule removed. Cannot be undone.");
        }

        if (current.getKeepDefaultEgressRules() && !getKeepDefaultEgressRules()) {
            deleteDefaultEgressRule(createClient(Ec2Client.class));
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {

    }

    private SecurityGroup getSecurityGroup(Ec2Client client) {
        SecurityGroup securityGroup = null;

        if (ObjectUtils.isBlank(getSecurityGroup())) {
            throw new GyroException("security group is missing");
        }

        try {
            DescribeSecurityGroupsResponse response = client.describeSecurityGroups(
                r -> r.filters(
                    f -> f.name("group-id").values(getSecurityGroup().getId()),
                    f -> f.name("vpc-id").values(getSecurityGroup().getVpc().getId())
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

        IpPermission permission = group.ipPermissionsEgress().stream()
            .filter(o -> o.ipProtocol().equals("-1") && o.ipRanges()
                .stream()
                .anyMatch(oo -> oo.cidrIp().equals("0.0.0.0/0")))
            .findFirst().orElse(null);

        if (permission != null) {
            SecurityGroupEgressRuleResource defaultRule = newSubresource(SecurityGroupEgressRuleResource.class);
            defaultRule.copyPortProtocol(permission);
            defaultRule.setDescription(permission.ipRanges()
                .stream()
                .filter(o -> o.cidrIp().equals("0.0.0.0/0"))
                .findFirst()
                .get()
                .description());
            defaultRule.setCidrBlock("0.0.0.0/0");
            defaultRule.delete(client, getSecurityGroup().getId());
        }

        IpPermission permissionIpv6 = group.ipPermissionsEgress().stream()
            .filter(o -> o.ipProtocol().equals("-1") && o.ipv6Ranges()
                .stream()
                .anyMatch(oo -> oo.cidrIpv6().equals("::/0")))
            .findFirst().orElse(null);

        if (permissionIpv6 != null) {
            SecurityGroupEgressRuleResource defaultRule = newSubresource(SecurityGroupEgressRuleResource.class);
            defaultRule.copyPortProtocol(permissionIpv6);
            defaultRule.setDescription(permissionIpv6.ipv6Ranges()
                .stream()
                .filter(o -> o.cidrIpv6().equals("::/0"))
                .findFirst()
                .get()
                .description());
            defaultRule.setIpv6CidrBlock("::/0");
            defaultRule.delete(client, getSecurityGroup().getId());
        }
    }

}
