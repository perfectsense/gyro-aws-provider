package gyro.aws.ec2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.ec2.model.ModifyVpnConnectionOptionsRequest;
import software.amazon.awssdk.services.ec2.model.TunnelInsideIpVersion;
import software.amazon.awssdk.services.ec2.model.VpnConnectionOptions;
import software.amazon.awssdk.services.ec2.model.VpnConnectionOptionsSpecification;

public class TransitGatewayVpnConnectionOptions extends Diffable implements Copyable<VpnConnectionOptions> {

    private Boolean enableAcceleration;
    private String localIpv4NetworkCidr;
    private String localIpv6NetworkCidr;
    private String remoteIpv4NetworkCidr;
    private String remoteIpv6NetworkCidr;
    private Boolean staticRoutesOnly;
    private TunnelInsideIpVersion tunnelInsideIpVersion;

    /**
     * When set to ``true`` acceleration is enabled for the VPN connection.
     */
    public Boolean getEnableAcceleration() {
        return enableAcceleration;
    }

    public void setEnableAcceleration(Boolean enableAcceleration) {
        this.enableAcceleration = enableAcceleration;
    }

    /**
     * The IPv4 CIDR on the customer gateway side of the VPN connection.
     */
    @Updatable
    public String getLocalIpv4NetworkCidr() {
        return localIpv4NetworkCidr;
    }

    public void setLocalIpv4NetworkCidr(String localIpv4NetworkCidr) {
        this.localIpv4NetworkCidr = localIpv4NetworkCidr;
    }

    /**
     * The IPv6 CIDR on the customer gateway side of the VPN connection.
     */
    @Updatable
    public String getLocalIpv6NetworkCidr() {
        return localIpv6NetworkCidr;
    }

    public void setLocalIpv6NetworkCidr(String localIpv6NetworkCidr) {
        this.localIpv6NetworkCidr = localIpv6NetworkCidr;
    }

    /**
     * The IPv4 CIDR on the AWS side of the VPN connection.
     */
    @Updatable
    public String getRemoteIpv4NetworkCidr() {
        return remoteIpv4NetworkCidr;
    }

    public void setRemoteIpv4NetworkCidr(String remoteIpv4NetworkCidr) {
        this.remoteIpv4NetworkCidr = remoteIpv4NetworkCidr;
    }

    /**
     * The IPv6 CIDR on the AWS side of the VPN connection.
     */
    @Updatable
    public String getRemoteIpv6NetworkCidr() {
        return remoteIpv6NetworkCidr;
    }

    public void setRemoteIpv6NetworkCidr(String remoteIpv6NetworkCidr) {
        this.remoteIpv6NetworkCidr = remoteIpv6NetworkCidr;
    }

    /**
     * When set to ``true`` the VPN connection uses static routes only.
     */
    public Boolean getStaticRoutesOnly() {
        return staticRoutesOnly;
    }

    public void setStaticRoutesOnly(Boolean staticRoutesOnly) {
        this.staticRoutesOnly = staticRoutesOnly;
    }

    /**
     * The version of traffic that the VPN tunnels should process.
     */
    @ValidStrings({ "IPV4", "IPV6" })
    public TunnelInsideIpVersion getTunnelInsideIpVersion() {
        return tunnelInsideIpVersion;
    }

    public void setTunnelInsideIpVersion(TunnelInsideIpVersion tunnelInsideIpVersion) {
        this.tunnelInsideIpVersion = tunnelInsideIpVersion;
    }

    @Override
    public void copyFrom(VpnConnectionOptions model) {
        setEnableAcceleration(model.enableAcceleration());
        setLocalIpv4NetworkCidr(model.localIpv4NetworkCidr());
        setLocalIpv6NetworkCidr(model.localIpv6NetworkCidr());
        setRemoteIpv4NetworkCidr(model.remoteIpv4NetworkCidr());
        setRemoteIpv6NetworkCidr(model.remoteIpv6NetworkCidr());
        setStaticRoutesOnly(model.staticRoutesOnly());
        setTunnelInsideIpVersion(model.tunnelInsideIpVersion());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    VpnConnectionOptionsSpecification toVpnConnectionOptionsSpecification() {
        return VpnConnectionOptionsSpecification.builder().enableAcceleration(getEnableAcceleration())
            .localIpv4NetworkCidr(getLocalIpv4NetworkCidr()).localIpv6NetworkCidr(getLocalIpv6NetworkCidr())
            .remoteIpv4NetworkCidr(getRemoteIpv4NetworkCidr()).remoteIpv6NetworkCidr(getRemoteIpv6NetworkCidr())
            .staticRoutesOnly(getStaticRoutesOnly()).tunnelInsideIpVersion(getTunnelInsideIpVersion()).build();
    }

    ModifyVpnConnectionOptionsRequest toModifyVpnConnectionOptionsRequest(String connectionId) {
        return ModifyVpnConnectionOptionsRequest.builder().localIpv4NetworkCidr(getLocalIpv4NetworkCidr())
            .localIpv6NetworkCidr(getLocalIpv6NetworkCidr()).remoteIpv4NetworkCidr(getRemoteIpv4NetworkCidr())
            .remoteIpv6NetworkCidr(getRemoteIpv6NetworkCidr()).vpnConnectionId(connectionId).build();
    }
}
