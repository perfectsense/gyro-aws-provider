package gyro.aws.ec2;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateVpnConnectionResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpnConnectionsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.GatewayType;
import software.amazon.awssdk.services.ec2.model.VpnConnection;
import software.amazon.awssdk.services.ec2.model.VpnState;

@Type("vpn-connection")
public class VpnConnectionResource extends Ec2TaggableResource<VpnConnection> implements Copyable<VpnConnection> {

    private CustomerGatewayResource customerGateway;
    private TransitGatewayVpnConnectionOptions options;
    private TransitGatewayResource transitGateway;
    private VpnGatewayResource vpnGateway;
    private GatewayType type;

    // Read-only
    private String id;
    private VpnState state;

    /**
     * The customer gateway at your end of the VPN connection.
     */
    @Required
    public CustomerGatewayResource getCustomerGateway() {
        return customerGateway;
    }

    public void setCustomerGateway(CustomerGatewayResource customerGateway) {
        this.customerGateway = customerGateway;
    }

    /**
     * The options for the VPN connection.
     *
     * @subresource gyro.aws.ec2.TransitGatewayVpnConnectionOptions
     */
    @Updatable
    public TransitGatewayVpnConnectionOptions getOptions() {
        return options;
    }

    public void setOptions(TransitGatewayVpnConnectionOptions options) {
        this.options = options;
    }

    /**
     * The transit gateway to connect to.
     */
    @Updatable
    @ConflictsWith("vpn-gateway")
    public TransitGatewayResource getTransitGateway() {
        return transitGateway;
    }

    public void setTransitGateway(TransitGatewayResource transitGateway) {
        this.transitGateway = transitGateway;
    }

    /**
     * The VPN gateway to connect to.
     */
    @Updatable
    @ConflictsWith("transit-gateway")
    public VpnGatewayResource getVpnGateway() {
        return vpnGateway;
    }

    public void setVpnGateway(VpnGatewayResource vpnGateway) {
        this.vpnGateway = vpnGateway;
    }

    /**
     * The ID of the virtual private gateway.
     */
    @ValidStrings("ipsec.1")
    public GatewayType getType() {
        return type;
    }

    public void setType(GatewayType type) {
        this.type = type;
    }

    /**
     * The ID of the VPN connection
     */
    @Output
    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The state of the VPN connection.
     */
    @Output
    @ValidStrings({ "PENDING", "AVAILABLE", "DELETING", "DELETED" })
    public VpnState getState() {
        return state;
    }

    public void setState(VpnState state) {
        this.state = state;
    }

    @Override
    protected String getResourceId() {
        return getId();
    }

    @Override
    public void copyFrom(VpnConnection model) {
        setId(model.vpnConnectionId());
        setState(model.state());
        setCustomerGateway(findById(CustomerGatewayResource.class, model.customerGatewayId()));
        setTransitGateway(findById(TransitGatewayResource.class, model.transitGatewayId()));
        setVpnGateway(findById(VpnGatewayResource.class, model.vpnGatewayId()));
        setType(model.type());

        setOptions(null);
        if (model.options() != null) {
            TransitGatewayVpnConnectionOptions transitGatewayVpnConnectionOptions = newSubresource(
                TransitGatewayVpnConnectionOptions.class);
            transitGatewayVpnConnectionOptions.copyFrom(model.options());
            setOptions(transitGatewayVpnConnectionOptions);
        }

        refreshTags();
    }

    @Override
    protected boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        VpnConnection connection = getVpnConnection(client);

        if (connection == null) {
            return false;
        }

        copyFrom(connection);

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        CreateVpnConnectionResponse response = client.createVpnConnection(r -> r.customerGatewayId(
            getCustomerGateway().getId())
            .transitGatewayId(getTransitGateway() == null ? null : getTransitGateway().getId())
            .vpnGatewayId(getVpnGateway() == null ? null : getVpnGateway().getId())
            .options(getOptions() != null ? getOptions().toVpnConnectionOptionsSpecification() : null)
            .type(getType().toString())
        );

        setId(response.vpnConnection().vpnConnectionId());

        waitForAvailability(client);
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        if (changedProperties.contains("options")) {
            client.modifyVpnConnectionOptions(getOptions().toModifyVpnConnectionOptionsRequest(getId()));
            waitForAvailability(client);
        }

        if (changedProperties.contains("transit-gateway") || changedProperties.contains("vpn-gateway")) {
            client.modifyVpnConnection(r -> r.customerGatewayId(
                getCustomerGateway().getId())
                .transitGatewayId(getTransitGateway() == null ? null : getTransitGateway().getId())
                .vpnGatewayId(getVpnGateway() == null ? null : getVpnGateway().getId()));
            waitForAvailability(client);
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteVpnConnection(r -> r.vpnConnectionId(getId()));

        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(30, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> getVpnConnection(client) == null);
    }

    private VpnConnection getVpnConnection(Ec2Client client) {
        VpnConnection connection = null;

        try {
            DescribeVpnConnectionsResponse response = client.describeVpnConnections(r -> r.vpnConnectionIds(getId()));

            List<VpnConnection> vpnConnections = response.vpnConnections();
            if (!vpnConnections.isEmpty() && !vpnConnections.get(0)
                .state()
                .equals(VpnState.DELETED)) {
                connection = vpnConnections.get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.awsErrorDetails().errorCode().equals("InvalidClientVpnConnection.IdNotFound")) {
                throw ex;
            }
        }

        return connection;
    }

    private void waitForAvailability(Ec2Client client) {
        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(30, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> {
                VpnConnection connection = getVpnConnection(client);
                return connection != null && connection.state().equals(VpnState.AVAILABLE);
            });
    }
}
