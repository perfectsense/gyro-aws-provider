package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ec2.model.IpPermission;
import software.amazon.awssdk.services.ec2.model.IpRange;
import software.amazon.awssdk.services.ec2.model.Ipv6Range;
import software.amazon.awssdk.services.ec2.model.UserIdGroupPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class SecurityGroupRuleResource extends AwsResource {

    private String cidrBlock;
    private String ipv6CidrBlock;
    private String protocol;
    private String description;
    private Integer fromPort;
    private Integer toPort;
    private SecurityGroupResource securityGroup;

    /**
     * Protocol for this Security Group Rule. `-1` is equivalent to "all". Other valid values are "tcp", "udp", or "icmp". Defaults to "tcp".
     */
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
    public Integer getFromPort() {
        return fromPort;
    }

    public void setFromPort(Integer fromPort) {
        this.fromPort = fromPort;
    }

    /**
     * Ending port for this Security Group Rule. (Required)
     */
    public Integer getToPort() {
        return toPort;
    }

    public void setToPort(Integer toPort) {
        this.toPort = toPort;
    }

    /**
     * The IPv4 CIDR block to apply this Security Group Rule to. Required if `ipv6-cidr-block` or `security-group` not mentioned.
     */
    public String getCidrBlock() {
        return cidrBlock;
    }

    public void setCidrBlock(String cidrBlock) {
        this.cidrBlock = cidrBlock;
    }

    /**
     * The IPv6 CIDR blocks to apply this Security Group Rule to. Required if `cidr-block` or `security-group` not mentioned.
     */
    public String getIpv6CidrBlock() {
        return ipv6CidrBlock;
    }

    public void setIpv6CidrBlock(String ipv6CidrBlock) {
        this.ipv6CidrBlock = ipv6CidrBlock;
    }

    /**
     * The security group referenced by this Security Group Rule.
     */
    public SecurityGroupResource getSecurityGroup() {
        return securityGroup;
    }

    public void setSecurityGroup(SecurityGroupResource securityGroup) {
        this.securityGroup = securityGroup;
    }

    @Override
    public String primaryKey() {
        StringBuilder sb = new StringBuilder();

        sb.append(getToPort()).append(" ")
            .append(getFromPort()).append(" ")
            .append(getProtocol()).append(" ");

        if (!ObjectUtils.isBlank(getCidrBlock())) {
            sb.append(getCidrBlock());
        } else if (!ObjectUtils.isBlank(getIpv6CidrBlock())) {
            sb.append(getIpv6CidrBlock());
        } else if (getSecurityGroup() != null && !ObjectUtils.isBlank(getSecurityGroup().getId())){
            sb.append(getSecurityGroup().getId());
        }

        return sb.toString();
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
            return parent.getId();
        }

        return null;
    }

    IpPermission getIpPermissionRequest() {
        IpPermission.Builder permissionBuilder = IpPermission.builder();

        if (!ObjectUtils.isBlank(getCidrBlock())) {
            permissionBuilder.ipRanges(IpRange.builder().cidrIp(getCidrBlock()).description(getDescription()).build());
        }

        if (!ObjectUtils.isBlank(getIpv6CidrBlock())) {
            permissionBuilder.ipv6Ranges(Ipv6Range.builder().cidrIpv6(getIpv6CidrBlock()).description(getDescription()).build());
        }

        if (getSecurityGroup() != null) {
            permissionBuilder.userIdGroupPairs(UserIdGroupPair.builder().groupId(getSecurityGroup().getId()).description(getDescription()).build());
        }

        return permissionBuilder
            .fromPort(getFromPort())
            .ipProtocol(getProtocol())
            .toPort(getToPort())
            .build();
    }

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();
        int status = (ObjectUtils.isBlank(getCidrBlock()) ? 0 : 1) + (ObjectUtils.isBlank(getIpv6CidrBlock()) ? 0 : 1) + (getSecurityGroup() == null ? 0 : 1);
        if (status != 1) {
            errors.add(new ValidationError(this, null,"Only one of 'cidr-blocks', 'ipv6-cidr-blocks' or 'security-groups' needs to be configured!"));
        }

        return errors;
    }

    void copyPortProtocol(IpPermission permission) {
        setProtocol(permission.ipProtocol());
        setToPort(permission.toPort());
        setFromPort(permission.fromPort());
    }
}

