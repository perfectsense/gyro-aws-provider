/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.scope.State;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupResponse;
import software.amazon.awssdk.services.ec2.model.DescribeSecurityGroupsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.IpPermission;
import software.amazon.awssdk.services.ec2.model.IpRange;
import software.amazon.awssdk.services.ec2.model.Ipv6Range;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;
import software.amazon.awssdk.services.ec2.model.UserIdGroupPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Create a security group with specified rules.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::security-group security-group-example
 *         name: "security-group-example"
 *         vpc: $(aws::vpc vpc-security-group-example)
 *         description: "security group example"
 *
 *         ingress
 *             description: "allow inbound http traffic"
 *             cidr-block: "0.0.0.0/0"
 *             protocol: "TCP"
 *             from-port: 80
 *             to-port: 80
 *         end
 *     end
 */
@Type("security-group")
public class SecurityGroupResource extends Ec2TaggableResource<SecurityGroup> implements Copyable<SecurityGroup> {

    private String name;
    private VpcResource vpc;
    private String description;
    private List<SecurityGroupIngressRuleResource> ingress;
    private List<SecurityGroupEgressRuleResource> egress;
    private Boolean keepDefaultEgressRules;

    // Read-only
    private String id;
    private String ownerId;

    /**
     * The name of the Security Group. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The VPC to create the Security Group in. (Required)
     */
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    /**
     * The description of this Security Group.
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
     * The ID of the Security Group.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The owner ID of the Security Group.
     */
    @Output
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    protected String getResourceId() {
        return getId();
    }

    @Override
    public void copyFrom(SecurityGroup group) {
        setVpc(findById(VpcResource.class, group.vpcId()));
        setId(group.groupId());
        setOwnerId(group.ownerId());
        setDescription(group.description());

        getEgress().clear();
        for (IpPermission permission : group.ipPermissionsEgress()) {
            for (IpRange ipRange : permission.ipRanges()) {
                if (isKeepDefaultEgressRules() && permission.ipProtocol().equals("-1") && ipRange.cidrIp().equals("0.0.0.0/0")) {
                    continue;
                }

                SecurityGroupEgressRuleResource rule = newSubresource(SecurityGroupEgressRuleResource.class);
                rule.copyPortProtocol(permission);
                rule.setCidrBlock(ipRange.cidrIp());
                rule.setDescription(ipRange.description());
                getEgress().add(rule);
            }

            for (Ipv6Range ipRange : permission.ipv6Ranges()) {
                if (isKeepDefaultEgressRules() && permission.ipProtocol().equals("-1") && ipRange.cidrIpv6().equals("::/0")) {
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

        refreshTags();
    }

    @Override
    protected boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        SecurityGroup group = getSecurityGroup(client);

        if (group == null) {
            return false;
        }

        copyFrom(group);

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        CreateSecurityGroupResponse response = client.createSecurityGroup(
            r -> r.vpcId(getVpc().getId()).description(getDescription()).groupName(getName())
        );

        setId(response.groupId());

        if (!isKeepDefaultEgressRules()) {
            deleteDefaultEgressRule(client);
        }
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {
        SecurityGroupResource current = (SecurityGroupResource) config;

        if (!current.isKeepDefaultEgressRules() && isKeepDefaultEgressRules()) {
            throw new GyroException("Default rule removed. Cannot be undone.");
        }

        if (current.isKeepDefaultEgressRules() && !isKeepDefaultEgressRules()) {
            deleteDefaultEgressRule(createClient(Ec2Client.class));
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(2, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> {
                    try {
                        client.deleteSecurityGroup(r -> r.groupId(getId()));
                    } catch (Ec2Exception e) {
                        // DependencyViolation should be retried since this resource may be waiting for a
                        // previously deleted resource to finish deleting.
                        if ("DependencyViolation".equals(e.awsErrorDetails().errorCode())) {
                            return false;
                        }
                    }

                    return true;
                }
            );
    }

    private SecurityGroup getSecurityGroup(Ec2Client client) {
        SecurityGroup securityGroup = null;

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load security group.");
        }

        if (getVpc() == null) {
            throw new GyroException("vpc is missing, unable to load security group.");
        }

        try {
            DescribeSecurityGroupsResponse response = client.describeSecurityGroups(
                r -> r.filters(
                    f -> f.name("group-id").values(getId()),
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

        IpPermission permission = group.ipPermissionsEgress().stream()
            .filter(o -> o.ipProtocol().equals("-1") && o.ipRanges().stream().anyMatch(oo -> oo.cidrIp().equals("0.0.0.0/0")))
            .findFirst().orElse(null);

        if (permission != null) {
            SecurityGroupEgressRuleResource defaultRule = newSubresource(SecurityGroupEgressRuleResource.class);
            defaultRule.copyPortProtocol(permission);
            defaultRule.setDescription(permission.ipRanges().stream().filter(o -> o.cidrIp().equals("0.0.0.0/0")).findFirst().get().description());
            defaultRule.setCidrBlock("0.0.0.0/0");
            defaultRule.delete(client, getId());
        }

        IpPermission permissionIpv6 = group.ipPermissionsEgress().stream()
            .filter(o -> o.ipProtocol().equals("-1") && o.ipv6Ranges().stream().anyMatch(oo -> oo.cidrIpv6().equals("::/0")))
            .findFirst().orElse(null);

        if (permissionIpv6 != null) {
            SecurityGroupEgressRuleResource defaultRule = newSubresource(SecurityGroupEgressRuleResource.class);
            defaultRule.copyPortProtocol(permissionIpv6);
            defaultRule.setDescription(permissionIpv6.ipv6Ranges().stream().filter(o -> o.cidrIpv6().equals("::/0")).findFirst().get().description());
            defaultRule.setIpv6CidrBlock("::/0");
            defaultRule.delete(client, getId());
        }
    }
}
