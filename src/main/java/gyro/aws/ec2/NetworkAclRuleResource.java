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

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.resource.Updatable;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.NetworkAclEntry;

public abstract class NetworkAclRuleResource extends AwsResource implements Copyable<NetworkAclEntry> {

    private Integer ruleNumber;
    private String ruleAction;
    private String protocol;
    private Integer fromPort;
    private Integer toPort;
    private String cidrBlock;
    private String ipv6CidrBlock;
    private Integer icmpType;
    private Integer icmpCode;

    /**
     * A number that determines the rule's processing order. (Required)
     */
    @Required
    public Integer getRuleNumber() {
        return ruleNumber;
    }

    public void setRuleNumber(Integer ruleNumber) {
        this.ruleNumber = ruleNumber;
    }

    /**
     * The action of the rule. Valid values are: ``allow`` or ``deny``. (Required)
     */
    @Required
    @Updatable
    public String getRuleAction() {
        return ruleAction;
    }

    public void setRuleAction(String ruleAction) {
        this.ruleAction = ruleAction;
    }

    /**
     * The protocol of the rule. ``-1`` means all protocols. Traffic on all ports is allowed if protocol is ``-1`` or a number other than ``6`` (TCP), ``17`` (UDP) and ``1`` (ICMP). (Required)
     */
    @Required
    @Updatable
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * The IPv4 cidr block to apply the rule to.
     */
    @Updatable
    public String getCidrBlock() {
        return cidrBlock;
    }

    public void setCidrBlock(String cidrBlock) {
        this.cidrBlock = cidrBlock;
    }

    /**
     * The IPv6 cidr block to apply the rule to.
     */
    @Updatable
    public String getIpv6CidrBlock() {
        return ipv6CidrBlock;
    }

    public void setIpv6CidrBlock(String ipv6CidrBlock) {
        this.ipv6CidrBlock = ipv6CidrBlock;
    }

    /**
     * The starting port of the rule.
     */
    @Updatable
    public Integer getFromPort() {
        return fromPort;
    }

    public void setFromPort(Integer fromPort) {
        this.fromPort = fromPort;
    }

    /**
     * The ending port of the rule.
     */
    @Updatable
    public Integer getToPort() {
        return toPort;
    }

    public void setToPort(Integer toPort) {
        this.toPort = toPort;
    }

    /**
     * The ICMP type used for an ICMP request.
     */
    @Updatable
    public Integer getIcmpType() {
        return icmpType;
    }

    public void setIcmpType(Integer icmpType) {
        this.icmpType = icmpType;
    }

    /**
     * The ICMP code used for an ICMP request.
     */
    @Updatable
    public Integer getIcmpCode() {
        return icmpCode;
    }

    public void setIcmpCode(Integer icmpCode) {
        this.icmpCode = icmpCode;
    }

    @Override
    public void copyFrom(NetworkAclEntry networkAclEntry) {
        setCidrBlock(networkAclEntry.cidrBlock());
        setIpv6CidrBlock(networkAclEntry.ipv6CidrBlock());
        setProtocol(networkAclEntry.protocol());

        if (networkAclEntry.portRange() != null) {
            setFromPort(networkAclEntry.portRange().from());
            setToPort(networkAclEntry.portRange().to());
        }

        if (networkAclEntry.icmpTypeCode() != null) {
            setIcmpCode(networkAclEntry.icmpTypeCode().code());
            setIcmpType(networkAclEntry.icmpTypeCode().type());
        }

        setRuleAction(networkAclEntry.ruleActionAsString());
        setRuleNumber(networkAclEntry.ruleNumber());
    }

    @Override
    public boolean refresh() {
        return false;
    }

    public void create(boolean egress) {
        Ec2Client client = createClient(Ec2Client.class);

        if (getProtocol().equals("1") || getProtocol().equals("6") || getProtocol().equals("17")) {
            if ((getToPort() != null && getFromPort() != null) || (getIcmpType() != null && getIcmpCode() != null)) {
                client.createNetworkAclEntry(r -> r.networkAclId(getNetworkAclId())
                    .cidrBlock(getCidrBlock())
                    .ipv6CidrBlock(getIpv6CidrBlock())
                    .ruleNumber(getRuleNumber())
                    .ruleAction(getRuleAction())
                    .egress(egress)
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
                    .egress(egress)
                    .protocol(getProtocol()));
            }
        }
    }

    public void update(boolean egress) {
        Ec2Client client = createClient(Ec2Client.class);

        client.replaceNetworkAclEntry(r -> r.networkAclId(getNetworkAclId())
            .ruleNumber(getRuleNumber())
            .ruleAction(getRuleAction())
            .cidrBlock(getCidrBlock())
            .ipv6CidrBlock(getIpv6CidrBlock())
            .egress(egress)
            .protocol(getProtocol())
            .icmpTypeCode(c -> c.type(getIcmpType())
                .code(getIcmpCode()))
            .portRange(r1 -> r1.from(getFromPort()).to(getToPort())));
    }

    public void delete(boolean egress) {
        Ec2Client client = createClient(Ec2Client.class);
        client.deleteNetworkAclEntry(d -> d.networkAclId(getNetworkAclId())
            .egress(egress)
            .ruleNumber(getRuleNumber()));
    }

    public String toDisplayString(boolean egress) {
        StringBuilder sb = new StringBuilder();

        if (egress) {
            sb.append("Outbound entry");
        } else {
            sb.append("Inbound entry");
        }

        sb.append(" rule #").append(getRuleNumber());

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

    private String getNetworkAclId() {
        NetworkAclResource parent = (NetworkAclResource) parentResource();
        if (parent != null) {
            return parent.getId();
        }
        return null;
    }
}
