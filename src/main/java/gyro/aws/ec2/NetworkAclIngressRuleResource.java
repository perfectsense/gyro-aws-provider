package gyro.aws.ec2;

import gyro.core.diff.ResourceName;
import gyro.lang.Resource;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.IpPermission;

import java.util.Set;

@ResourceName(parent = "network-acl", value = "ingress")
    public class NetworkAclIngressRuleResource extends NetworkAclRuleResource  {

    public NetworkAclIngressRuleResource() {}

    @Override
    public void create() {
        Ec2Client client = createClient(Ec2Client.class);

        client.createNetworkAclEntry(r -> r.networkAclId(getId())
            .cidrBlock(getCidrBlocks().size() != 0 ? getCidrBlocks().get(0) : null)
            .ipv6CidrBlock(getIpv6CidrBlocks().size() != 0 ? getIpv6CidrBlocks().get(0) : null)
            .ruleNumber(getRuleNumber())
            .ruleAction(getRuleAction())
            .egress(getEgressRule())
            .protocol(getProtocol())
            .icmpTypeCode(c -> c.type(getIcmpCode() != null ? getIcmpCode() : null)
                .code(getIcmpType() != null ? getIcmpType() : null))
            .portRange(r1 -> r1.from(getFromPort()).to(getToPort())));
    }


    /***
     * Update the rule action, rule number, protocol and ports
     * */
    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        client.replaceNetworkAclEntry(r -> r.networkAclId(getId())
            .ruleNumber(getRuleNumber())
            .ruleAction(getRuleAction())
            .cidrBlock(getCidrBlocks().size() != 0 ? getCidrBlocks().get(0) : null)
            .ipv6CidrBlock(getIpv6CidrBlocks().size() != 0 ? getIpv6CidrBlocks().get(0) : null)
            .egress(getEgressRule())
            .protocol(getProtocol())
            .icmpTypeCode(c -> c.type(getIcmpCode() != null ? getIcmpCode() : null)
                .code(getIcmpType() != null ? getIcmpType() : null))
            .portRange(r1 -> r1.from(getFromPort()).to(getToPort())));
    }

    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);
        client.deleteNetworkAclEntry(d -> d.networkAclId(getId())
            .egress(getEgressRule())
            .ruleNumber(getRuleNumber()));
    }

}
