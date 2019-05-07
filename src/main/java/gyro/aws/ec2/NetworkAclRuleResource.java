package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.core.GyroException;
import gyro.core.resource.ResourceUpdatable;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.NetworkAclEntry;

import java.util.Set;

/**
 * Create a Network ACL rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::network-acl network-acl-example
 *       vpc-id: $(aws::vpc vpc-example-for-network-acl | vpc-id)

 *       tags: {
 *           Name: "network-acl-example"
 *       }

 *       rule
 *           cidr-block: ["0.0.0.0/0"]
 *           protocol : "6"
 *           rule-action : "allow"
 *           rule-number : 103
 *           egress-rule : false
 *           from-port: 443
 *           to-port: 443
 *       end
 *    end
 *
 */
public class NetworkAclRuleResource extends AwsResource {

    private Integer ruleNumber;
    private String ruleAction;
    private String protocol;
    private Integer fromPort;
    private Integer toPort;
    private Boolean egressRule;
    private String cidrBlock;
    private String ipv6CidrBlock;
    private Integer icmpType;
    private Integer icmpCode;

    public NetworkAclRuleResource() {
    }

    public NetworkAclRuleResource(NetworkAclEntry e) {
        setCidrBlock(e.cidrBlock());
        setIpv6CidrBlock(e.ipv6CidrBlock());
        setEgressRule(e.egress());
        setProtocol(e.protocol());

        if (e.portRange() != null) {
            setFromPort(e.portRange().from());
            setToPort(e.portRange().to());
        }

        if (e.icmpTypeCode() != null) {
            setIcmpCode(e.icmpTypeCode().code());
            setIcmpType(e.icmpTypeCode().type());
        }

        setRuleAction(e.ruleActionAsString());
        setRuleNumber(e.ruleNumber());
    }

    /**
     * A number that determines the rule's processing order. (Required)
     */
    public Integer getRuleNumber() {
        return ruleNumber;
    }

    public void setRuleNumber(Integer ruleNumber) {
        this.ruleNumber = ruleNumber;
    }

    /**
     * The action of the rule. Valid values are: ``allow`` or ``deny``. (Required)
     */
    @ResourceUpdatable
    public String getRuleAction() {
        return ruleAction;
    }

    public void setRuleAction(String ruleAction) {
        this.ruleAction = ruleAction;
    }

    /**
     * The protocol of the rule. ``-1`` means all protocols. Traffic on all ports is allowed if protocol is ``-1`` or a number other than ``6`` (TCP), ``17`` (UDP) and ``1`` (ICMP). (Required)
     */
    @ResourceUpdatable
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Indicate whether the rule is an egress rule. (Required)
     */
    public Boolean getEgressRule() {
        return egressRule;
    }

    public void setEgressRule(Boolean egressRule) {
        this.egressRule = egressRule;
    }

    /**
     * The IPv4 cidr block to apply the rule to.
     */
    @ResourceUpdatable
    public String getCidrBlock() {
        return cidrBlock;
    }

    public void setCidrBlock(String cidrBlock) {
        this.cidrBlock = cidrBlock;
    }

    /**
     * The IPv6 cidr block to apply the rule to.
     */
    @ResourceUpdatable
    public String getIpv6CidrBlock() {
        return ipv6CidrBlock;
    }

    public void setIpv6CidrBlock(String ipv6CidrBlock) {
        this.ipv6CidrBlock = ipv6CidrBlock;
    }

    /**
     * The starting port of the rule.
     */
    @ResourceUpdatable
    public Integer getFromPort() {
        return fromPort;
    }

    public void setFromPort(Integer fromPort) {
        this.fromPort = fromPort;
    }

    /**
     * The ending port of the rule.
     */
    @ResourceUpdatable
    public Integer getToPort() {
        return toPort;
    }

    public void setToPort(Integer toPort) {
        this.toPort = toPort;
    }

    /**
     * The ICMP type used for an ICMP request.
     */
    @ResourceUpdatable
    public Integer getIcmpType() {
        return icmpType;
    }

    public void setIcmpType(Integer icmpType) {
        this.icmpType = icmpType;
    }

    /**
     * The ICMP code used for an ICMP request.
     */
    @ResourceUpdatable
    public Integer getIcmpCode() {
        return icmpCode;
    }

    public void setIcmpCode(Integer icmpCode) {
        this.icmpCode = icmpCode;
    }

    /**
     * The Id of the network ACL.
     */
    public String getNetworkAclId() {
        NetworkAclResource parent = (NetworkAclResource) parentResource();
        if (parent != null) {
            return parent.getNetworkAclId();
        }
        return null;
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {
        Ec2Client client = createClient(Ec2Client.class);
        
        if (getProtocol().equals("1") || getProtocol().equals("6") || getProtocol().equals("17")) {
            if ((getToPort() != null && getFromPort() != null) || (getIcmpType() != null && getIcmpCode() != null)) {
                client.createNetworkAclEntry(r -> r.networkAclId(getNetworkAclId())
                    .cidrBlock(getCidrBlock())
                    .ipv6CidrBlock(getIpv6CidrBlock())
                    .ruleNumber(getRuleNumber())
                    .ruleAction(getRuleAction())
                    .egress(getEgressRule())
                    .protocol(getProtocol())
                    .icmpTypeCode(c -> c.type(getIcmpType())
                        .code(getIcmpCode()))
                    .portRange(r1 -> r1.from(getFromPort()).to(getToPort())));
            }
        } else {
            if (getToPort() != null && getFromPort() != null) {
                throw new GyroException("Traffic on all ports are allowed for this protocol");
            } else {
                client.createNetworkAclEntry(r -> r.networkAclId(getNetworkAclId())
                    .cidrBlock(getCidrBlock())
                    .ipv6CidrBlock(getIpv6CidrBlock())
                    .ruleNumber(getRuleNumber())
                    .ruleAction(getRuleAction())
                    .egress(getEgressRule())
                    .protocol(getProtocol()));
            }
        }
    }

    @Override
    public String primaryKey() {
        return String.format("%s, %s", getRuleNumber(), getEgressRule());
    }

    @Override
    public String resourceIdentifier() {
        return null;
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        client.replaceNetworkAclEntry(r -> r.networkAclId(getNetworkAclId())
            .ruleNumber(getRuleNumber())
            .ruleAction(getRuleAction())
            .cidrBlock(getCidrBlock())
            .ipv6CidrBlock(getIpv6CidrBlock())
            .egress(getEgressRule())
            .protocol(getProtocol())
            .icmpTypeCode(c -> c.type(getIcmpType())
                .code(getIcmpCode()))
            .portRange(r1 -> r1.from(getFromPort()).to(getToPort())));
    }

    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);
        client.deleteNetworkAclEntry(d -> d.networkAclId(getNetworkAclId())
            .egress(getEgressRule())
            .ruleNumber(getRuleNumber()));
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        if (getEgressRule().equals(true)) {
            sb.append("Outbound entry");
        } else {
            sb.append("Inbound entry");
        }

        sb.append(" rule #" + getRuleNumber());

        if (getProtocol().equals("6") || getProtocol().equals("17")) {
            sb.append(" TCP/UDP on ports ");
            sb.append("[");
            sb.append(getFromPort());
            sb.append(" to ");
            sb.append(getToPort());
            sb.append("] for block range");
        } else if (getProtocol().equals("1")) {
            sb.append(" traffic on ICMP type and code allowed for block range");

        } else {
            sb.append(" traffic on all ports allowed for block range");
        }

        if (!ObjectUtils.isBlank(getCidrBlock())) {
            sb.append(" [");
            sb.append(getCidrBlock());
            sb.append("]");
        }

        if (!ObjectUtils.isBlank(getIpv6CidrBlock())) {
            sb.append(" [");
            sb.append(getIpv6CidrBlock());
            sb.append("]");
        }

        sb.append(" ");

        return sb.toString();
    }
}
