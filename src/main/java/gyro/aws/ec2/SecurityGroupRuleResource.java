package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.ec2.model.IpPermission;
import software.amazon.awssdk.services.ec2.model.IpRange;
import software.amazon.awssdk.services.ec2.model.Ipv6Range;

import java.util.ArrayList;
import java.util.List;

public abstract class SecurityGroupRuleResource extends AwsResource {

    private List<String> cidrBlocks;
    private List<String> ipv6CidrBlocks;
    private String protocol;
    private String description;
    private Integer fromPort;
    private Integer toPort;

    public SecurityGroupRuleResource() {
    }

    public SecurityGroupRuleResource(IpPermission permission) {
        setProtocol(permission.ipProtocol());
        setFromPort(permission.fromPort());
        setToPort(permission.toPort());

        if (!permission.ipRanges().isEmpty()) {
            for (IpRange range : permission.ipRanges()) {
                getCidrBlocks().add(range.cidrIp());
                setDescription(range.description());
            }
        }

        if (!permission.ipv6Ranges().isEmpty()) {
            for (Ipv6Range range : permission.ipv6Ranges()) {
                getIpv6CidrBlocks().add(range.cidrIpv6());
                setDescription(range.description());
            }
        }
    }

    public String getGroupId() {
        SecurityGroupResource parent = (SecurityGroupResource) parent();
        if (parent != null) {
            return parent.getGroupId();
        }

        return null;
    }

    /**
     * Protocol for this rule. `-1` is equivalent to "all". Other valid values are "tcp", "udp", or "icmp". Defaults to "tcp".
     */
    @Updatable
    public String getProtocol() {
        if (protocol != null) {
            return protocol.toLowerCase();
        }

        return "tcp";
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Description for this security group rule.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Starting port for this rule. (Required)
     */
    @Updatable
    public Integer getFromPort() {
        return fromPort;
    }

    public void setFromPort(Integer fromPort) {
        this.fromPort = fromPort;
    }

    /**
     * Ending port for this rule. (Required)
     */
    @Updatable
    public Integer getToPort() {
        return toPort;
    }

    public void setToPort(Integer toPort) {
        this.toPort = toPort;
    }

    /**
     * List of IPv4 cidr blocks to apply this rule to. Required if `ipv6-cidr-blocks` not mentioned.
     */
    @Updatable
    public List<String> getCidrBlocks() {
        if (cidrBlocks == null) {
            cidrBlocks = new ArrayList<>();
        }

        return cidrBlocks;
    }

    public void setCidrBlocks(List<String> cidrBlocks) {
        this.cidrBlocks = cidrBlocks;
    }

    /**
     * List of IPv6 cidr blocks to apply this rule to. Required if `cidr-blocks` not mentioned.
     */
    @Updatable
    public List<String> getIpv6CidrBlocks() {
        if (ipv6CidrBlocks == null) {
            ipv6CidrBlocks = new ArrayList<>();
        }

        return ipv6CidrBlocks;
    }

    public void setIpv6CidrBlocks(List<String> ipv6CidrBlocks) {
        this.ipv6CidrBlocks = ipv6CidrBlocks;
    }

    @Override
    public String primaryKey() {
        return String.format("%s %d %d", getProtocol(), getFromPort(), getToPort());
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append(name());
        sb.append(" security rule - ");
        sb.append(getProtocol());
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

        if (!ObjectUtils.isBlank(getDescription())) {
            sb.append(" ");
            sb.append(getDescription());
        }

        return sb.toString();
    }

    IpPermission getIpPermissionRequest() {
        IpPermission.Builder permissionBuilder = IpPermission.builder();

        if (!getCidrBlocks().isEmpty()) {
            IpRange.Builder ipv4builder = IpRange.builder();
            ipv4builder.description(getDescription());
            for (String cidr : getCidrBlocks()) {
                ipv4builder.cidrIp(cidr);
            }

            permissionBuilder.ipRanges(ipv4builder.build());
        }

        if (!getIpv6CidrBlocks().isEmpty()) {
            Ipv6Range.Builder ipv6builder = Ipv6Range.builder();
            ipv6builder.description(getDescription());
            for (String cidr : getIpv6CidrBlocks()) {
                ipv6builder.cidrIpv6(cidr);
            }

            permissionBuilder.ipv6Ranges(ipv6builder.build());
        }

        return permissionBuilder
            .fromPort(getFromPort())
            .ipProtocol(getProtocol())
            .toPort(getToPort())
            .build();
    }

}

