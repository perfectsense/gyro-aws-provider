package gyro.aws.ec2;

import java.util.List;
import java.util.Map;

import gyro.core.Type;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.VpnConnection;

@Type("vpn-connection")
public class VpnConnectionFinder extends Ec2TaggableAwsFinder<Ec2Client, VpnConnection, VpnConnectionResource> {

    private String customerGatewayConfiguration;
    private String customerGatewayId;
    private String state;
    private String staticRoutesOnly;
    private String destinationCidrBlock;
    private String bgpAsn;
    private Map<String, String> tag;
    private String tagKey;
    private String type;
    private String vpnConnectionId;
    private String vpnGatewayId;
    private String transitGatewayId;

    /**
     * The configuration information for the VPN connection's customer gateway.
     */
    public String getCustomerGatewayConfiguration() {
        return customerGatewayConfiguration;
    }

    public void setCustomerGatewayConfiguration(String customerGatewayConfiguration) {
        this.customerGatewayConfiguration = customerGatewayConfiguration;
    }

    /**
     * The ID of the customer gateway at your end of the VPN connection.
     */
    public String getCustomerGatewayId() {
        return customerGatewayId;
    }

    public void setCustomerGatewayId(String customerGatewayId) {
        this.customerGatewayId = customerGatewayId;
    }

    /**
     * The current state of the VPN connection.
     */
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * The static routes associated with the VPN connection.
     */
    @Filter("option.static-routes-only")
    public String getStaticRoutesOnly() {
        return staticRoutesOnly;
    }

    public void setStaticRoutesOnly(String staticRoutesOnly) {
        this.staticRoutesOnly = staticRoutesOnly;
    }

    /**
     * The destination cidr block.
     */
    @Filter("route.destination-cidr-block")
    public String getDestinationCidrBlock() {
        return destinationCidrBlock;
    }

    public void setDestinationCidrBlock(String destinationCidrBlock) {
        this.destinationCidrBlock = destinationCidrBlock;
    }

    /**
     * The customer gateway's Border Gateway Protocol (BGP) Autonomous System Number (ASN).
     */
    public String getBgpAsn() {
        return bgpAsn;
    }

    public void setBgpAsn(String bgpAsn) {
        this.bgpAsn = bgpAsn;
    }

    /**
     * The tags associated with the VPN connection.
     */
    public Map<String, String> getTag() {
        return tag;
    }

    public void setTag(Map<String, String> tag) {
        this.tag = tag;
    }

    /**
     * The key of a tag assigned to the resource.
     */
    public String getTagKey() {
        return tagKey;
    }

    public void setTagKey(String tagKey) {
        this.tagKey = tagKey;
    }

    /**
     * The type of VPN connection.
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The ID of the VPN connection.
     */
    public String getVpnConnectionId() {
        return vpnConnectionId;
    }

    public void setVpnConnectionId(String vpnConnectionId) {
        this.vpnConnectionId = vpnConnectionId;
    }

    /**
     * The ID of the VPN gateway.
     */
    public String getVpnGatewayId() {
        return vpnGatewayId;
    }

    public void setVpnGatewayId(String vpnGatewayId) {
        this.vpnGatewayId = vpnGatewayId;
    }

    /**
     * The ID of the transit gateway.
     */
    public String getTransitGatewayId() {
        return transitGatewayId;
    }

    public void setTransitGatewayId(String transitGatewayId) {
        this.transitGatewayId = transitGatewayId;
    }

    @Override
    protected List<VpnConnection> findAllAws(Ec2Client client) {
        return client.describeVpnConnections().vpnConnections();
    }

    @Override
    protected List<VpnConnection> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeVpnConnections(r -> r.filters(createFilters(filters))).vpnConnections();
    }
}
