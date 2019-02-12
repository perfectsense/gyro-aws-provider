package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.core.diff.ResourceDiffProperty;
import gyro.lang.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NetworkAclRuleResource extends AwsResource {

    private String protocol;
    private String description;
    private Integer fromPort;
    private Integer toPort;
    private Integer ruleNumber;
    private Boolean egressRule;
    private String ruleAction;
    private List<String> cidrBlocks;
    private List<String> ipv6CidrBlocks;
    private Integer icmpType;
    private Integer icmpCode;

    @ResourceDiffProperty(updatable = true)
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @ResourceDiffProperty(updatable = true)
    public Boolean getEgressRule() {
        return egressRule;
    }

    public void setEgressRule(Boolean egressRule) {
        this.egressRule = egressRule;
    }

    @ResourceDiffProperty(updatable = true)
    public String getRuleAction() {
        return ruleAction;
    }

    public void setRuleAction(String ruleAction) {
        this.ruleAction = ruleAction;
    }

    public String getId() {
        NetworkAclResource parent = (NetworkAclResource) parentResource();
        if (parent != null) {
            return parent.getId();
        }
        return null;
    }

    @ResourceDiffProperty(updatable = true)
    public List<String> getCidrBlocks() {
        if (cidrBlocks == null) {
            cidrBlocks = new ArrayList<>();
        }

        return cidrBlocks;    }

    public void setCidrBlocks(List<String> cidrBlocks) {
        this.cidrBlocks = cidrBlocks;
    }

    @ResourceDiffProperty(updatable = true)
    public List<String> getIpv6CidrBlocks() {
        if (ipv6CidrBlocks == null) {
            ipv6CidrBlocks = new ArrayList<>();
        }

        return ipv6CidrBlocks;    }

    public void setIpv6CidrBlocks(List<String> ipv6CidrBlocks) {
        this.ipv6CidrBlocks = ipv6CidrBlocks;
    }

    @ResourceDiffProperty(updatable = true)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ResourceDiffProperty(updatable = true)
    public Integer getFromPort() {
        return fromPort;
    }

    public void setFromPort(Integer fromPort) {
        this.fromPort = fromPort;
    }

    @ResourceDiffProperty(updatable = true)
    public Integer getToPort() {
        return toPort;
    }

    public void setToPort(Integer toPort) {
        this.toPort = toPort;
    }

    @ResourceDiffProperty(updatable = true)
    public Integer getRuleNumber() {
        return ruleNumber;
    }

    public void setRuleNumber(Integer ruleNumber) {
        this.ruleNumber = ruleNumber;
    }

    @ResourceDiffProperty(updatable = true)
    public Integer getIcmpType() {
        return icmpType;
    }

    public void setIcmpType(Integer icmpType) {
        this.icmpType = icmpType;
    }

    @ResourceDiffProperty(updatable = true)
    public Integer getIcmpCode() {
        return icmpCode;
    }

    public void setIcmpCode(Integer icmpCode) {
        this.icmpCode = icmpCode;
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {

    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {

    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append(resourceType());
        sb.append(" Network Access Control List - ");
        if (getProtocol() != null) {
            sb.append(getProtocol());
        } else if (getProtocol() == "1" && getIcmpCode() != null || getIcmpType() != null) {
            sb.append("ICMP");
        }

        sb.append(" [");
        sb.append(getFromPort());
        sb.append(" to ");
        sb.append(getToPort());
        sb.append("]");

        if (!getCidrBlocks().isEmpty()) {
            sb.append(" ");
            sb.append(getCidrBlocks());
        }

        if (!getIpv6CidrBlocks().isEmpty()) {
            sb.append(" ");
            sb.append(getIpv6CidrBlocks());
        }

        sb.append(" ");
        sb.append(getDescription());

        return sb.toString();    }
}
