package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.core.BeamException;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import gyro.lang.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.NetworkAclEntry;

import java.util.Set;

/**
 * Create Network ACL rule entry in the provided VPC.
 *
 * Example
 * -------
 *
 * .. code-block:: beam
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
@ResourceName(parent = "network-acl", value = "rule")
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
     * Rule number for this rule entry
     */
    public Integer getRuleNumber() {
        return ruleNumber;
    }

    public void setRuleNumber(Integer ruleNumber) {
        this.ruleNumber = ruleNumber;
    }

    /**
     * Indicates whether to allow or deny the traffic that matches the rule.
     */
    @ResourceDiffProperty(updatable = true)
    public String getRuleAction() {
        return ruleAction;
    }

    public void setRuleAction(String ruleAction) {
        this.ruleAction = ruleAction;
    }

    /**
     * Protocol for this rule. `-1` is equivalent to "all". Required if specifying protocol 6 (TCP) or 17 (UDP).
     */
    @ResourceDiffProperty(updatable = true)
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Create egress rule entry if set to true and ingress rule entry if set to false.
     */
    @ResourceDiffProperty(updatable = true)
    public Boolean getEgressRule() {
        return egressRule;
    }

    public void setEgressRule(Boolean egressRule) {
        this.egressRule = egressRule;
    }

    /**
     * The IPv4 network range to allow or deny.
     */
    @ResourceDiffProperty(updatable = true)
    public String getCidrBlock() {
        return cidrBlock;
    }

    public void setCidrBlock(String cidrBlock) {
        this.cidrBlock = cidrBlock;
    }

    /**
     * The IPv6 network range to allow or deny.
     */
    @ResourceDiffProperty(updatable = true)
    public String getIpv6CidrBlock() {
        return ipv6CidrBlock;
    }

    public void setIpv6CidrBlock(String ipv6CidrBlock) {
        this.ipv6CidrBlock = ipv6CidrBlock;
    }


    /**
     * Starting port for this rule.
     */
    @ResourceDiffProperty(updatable = true)
    public Integer getFromPort() {
        return fromPort;
    }

    public void setFromPort(Integer fromPort) {
        this.fromPort = fromPort;
    }

    /**
     * Ending port for this rule.
     */
    @ResourceDiffProperty(updatable = true)
    public Integer getToPort() {
        return toPort;
    }

    public void setToPort(Integer toPort) {
        this.toPort = toPort;
    }

    /**
     * The ICMP or ICMPv6 type. Required if specifying protocol 1 (ICMP) or protocol 58 (ICMPv6) with an IPv6 CIDR
     */
    @ResourceDiffProperty(updatable = true)
    public Integer getIcmpType() {
        return icmpType;
    }

    public void setIcmpType(Integer icmpType) {
        this.icmpType = icmpType;
    }

    /**
     * The ICMP or ICMPv6 code. Required if specifying protocol 1 (ICMP) or protocol 58 (ICMPv6) with an IPv6 CIDR
     */
    @ResourceDiffProperty(updatable = true)
    public Integer getIcmpCode() {
        return icmpCode;
    }

    public void setIcmpCode(Integer icmpCode) {
        this.icmpCode = icmpCode;
    }

    /**
     * A unique ID for this subresource.
     */
    public String getId() {
        NetworkAclResource parent = (NetworkAclResource) parentResource();
        if (parent != null) {
            return parent.getId();
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
                client.createNetworkAclEntry(r -> r.networkAclId(getId())
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
                throw new BeamException("Traffic on all ports are allowed for this protocol");
            } else {
                client.createNetworkAclEntry(r -> r.networkAclId(getId())
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
        return getRuleNumber().toString();
    }

    @Override
    public String resourceIdentifier() {
        return null;
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        client.replaceNetworkAclEntry(r -> r.networkAclId(getId())
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
        client.deleteNetworkAclEntry(d -> d.networkAclId(getId())
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
