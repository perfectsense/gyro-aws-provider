package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.ec2.model.IpPermission;
import software.amazon.awssdk.services.ec2.model.IpRange;
import software.amazon.awssdk.services.ec2.model.Ipv6Range;
import software.amazon.awssdk.services.ec2.model.UserIdGroupPair;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class SecurityGroupRuleResource extends AwsResource implements Copyable<IpPermission> {

    private List<String> cidrBlocks;
    private List<String> ipv6CidrBlocks;
    private String protocol;
    private String description;
    private Integer fromPort;
    private Integer toPort;
    private Set<SecurityGroupResource> securityGroups;

    /**
     * Protocol for this Security Group Rule. `-1` is equivalent to "all". Other valid values are "tcp", "udp", or "icmp". Defaults to "tcp".
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
     * Description for this Security Group Rule.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Starting port for this Security Group Rule. (Required)
     */
    @Updatable
    public Integer getFromPort() {
        return fromPort;
    }

    public void setFromPort(Integer fromPort) {
        this.fromPort = fromPort;
    }

    /**
     * Ending port for this Security Group Rule. (Required)
     */
    @Updatable
    public Integer getToPort() {
        return toPort;
    }

    public void setToPort(Integer toPort) {
        this.toPort = toPort;
    }

    /**
     * List of IPv4 CIDR blocks to apply this Security Group Rule to. Required if `ipv6-cidr-blocks` not mentioned.
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
     * List of IPv6 CIDR blocks to apply this Security Group Rule to. Required if `cidr-blocks` not mentioned.
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

    /**
     * List of security groups referenced by this Security Group Rule.
     */
    @Updatable
    public Set<SecurityGroupResource> getSecurityGroups() {
        if (securityGroups == null) {
            securityGroups = new LinkedHashSet<>();
        }

        return securityGroups;
    }

    public void setSecurityGroups(Set<SecurityGroupResource> securityGroups) {
        this.securityGroups = securityGroups;
    }

    @Override
    public void copyFrom(IpPermission permission) {
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

        if (!permission.userIdGroupPairs().isEmpty()) {
            for (UserIdGroupPair groupPair : permission.userIdGroupPairs()) {
                getSecurityGroups().add(findById(SecurityGroupResource.class, groupPair.groupId()));
                setDescription(groupPair.description());
            }
        }
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
    public final void create(GyroUI ui, State state) {
        validate();
        doCreate();
    }

    protected abstract void doCreate();

    @Override
    public final void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        validate();
        doUpdate(ui, state, current, changedFieldNames);
    }

    protected abstract void doUpdate(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception;

    public String getGroupId() {
        SecurityGroupResource parent = (SecurityGroupResource) parent();
        if (parent != null) {
            return parent.getGroupId();
        }

        return null;
    }

    IpPermission getIpPermissionRequest() {
        IpPermission.Builder permissionBuilder = IpPermission.builder();

        if (!getCidrBlocks().isEmpty()) {
            permissionBuilder.ipRanges(
                getCidrBlocks().stream()
                    .map(o -> IpRange.builder().description(getDescription()).cidrIp(o).build())
                    .collect(Collectors.toList())
            );
        }

        if (!getIpv6CidrBlocks().isEmpty()) {
            permissionBuilder.ipv6Ranges(
                getIpv6CidrBlocks().stream()
                    .map(o -> Ipv6Range.builder().description(getDescription()).cidrIpv6(o).build())
                    .collect(Collectors.toList())
            );
        }

        if (!getSecurityGroups().isEmpty()) {
            permissionBuilder.userIdGroupPairs(
                getSecurityGroups().stream()
                    .map(g -> UserIdGroupPair.builder().description(getDescription()).groupId(g.getGroupId()).build())
                    .collect(Collectors.toList())
            );
        }

        return permissionBuilder
            .fromPort(getFromPort())
            .ipProtocol(getProtocol())
            .toPort(getToPort())
            .build();
    }

    private void validate() {
        if (getCidrBlocks().isEmpty() && getIpv6CidrBlocks().isEmpty() && getSecurityGroups().isEmpty()) {
            throw new GyroException("At least one of 'cidr-blocks', 'ipv6-cidr-blocks' or 'security-groups' needs to be configured!");
        }
    }
}

