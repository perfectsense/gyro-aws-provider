package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.core.Wait;
import gyro.core.resource.ResourceId;
import gyro.core.resource.ResourceUpdatable;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceOutput;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateVpnGatewayResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpnGatewaysResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.GatewayType;
import software.amazon.awssdk.services.ec2.model.VpnGateway;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Create VPN Gateway.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::vpn-gateway vpn-gateway-example
 *         tags: {
 *             Name: "vpn-gateway-example"
 *         }
 *     end
 */
@ResourceType("vpn-gateway")
public class VpnGatewayResource extends Ec2TaggableResource<VpnGateway> {
    private Long amazonSideAsn;
    private VpcResource vpc;

    // Read-only
    private String vpnGatewayId;

    /**
     * The private Autonomous System Number (ASN) for the Amazon side of a BGP session. If you're using a 16-bit ASN, it must be in the ``64512`` to ``65534`` range. If you're using a 32-bit ASN, it must be in the ``4200000000`` to ``4294967294`` range.
     */
    public Long getAmazonSideAsn() {
        if (amazonSideAsn == null) {
            amazonSideAsn = 0L;
        }

        return amazonSideAsn;
    }

    public void setAmazonSideAsn(Long amazonSideAsn) {
        this.amazonSideAsn = amazonSideAsn;
    }

    /**
     * The VPC to create the security group in.
     */
    @ResourceUpdatable
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    /**
     * The id of the vpn gateway.
     */
    @ResourceId
    @ResourceOutput
    public String getVpnGatewayId() {
        return vpnGatewayId;
    }

    public void setVpnGatewayId(String vpnGatewayId) {
        this.vpnGatewayId = vpnGatewayId;
    }

    @Override
    protected String getId() {
        return getVpnGatewayId();
    }

    @Override
    protected boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        VpnGateway vpnGateway = getVpnGateway(client);

        if (vpnGateway == null){
            return false;
        }

        setAmazonSideAsn(vpnGateway.amazonSideAsn());

        if (!vpnGateway.vpcAttachments().isEmpty() && vpnGateway.vpcAttachments().get(0).stateAsString().equals("attached")) {
            setVpc(findById(VpcResource.class, vpnGateway.vpcAttachments().get(0).vpcId()));
        } else {
            setVpc(null);
        }

        return true;
    }

    @Override
    protected void doCreate() {
        Ec2Client client = createClient(Ec2Client.class);

        CreateVpnGatewayResponse response;
        if (getAmazonSideAsn() > 0) {
            response = client.createVpnGateway(r -> r.type(GatewayType.IPSEC_1).amazonSideAsn(getAmazonSideAsn()));
        } else {
            response = client.createVpnGateway(r -> r.type(GatewayType.IPSEC_1));
        }

        setVpnGatewayId(response.vpnGateway().vpnGatewayId());

        if (getVpc() != null) {
            attachVpc(client);
        }
    }

    @Override
    protected void doUpdate(AwsResource config, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        VpnGatewayResource oldResource = (VpnGatewayResource) config;

        if (oldResource.getVpc() != null) {
            client.detachVpnGateway(r -> r.vpcId(oldResource.getVpc().getId()).vpnGatewayId(getVpnGatewayId()));
        }

        if (getVpc() != null) {
            Wait.atMost(1, TimeUnit.MINUTES)
                .checkEvery(10, TimeUnit.SECONDS)
                .prompt(true)
                .until(() -> replaceVpc(client)
                );
        }
    }

    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);

        if (getVpc() != null) {
            client.detachVpnGateway(r -> r.vpcId(getVpc().getId()).vpnGatewayId(getVpnGatewayId()));
        }

        client.deleteVpnGateway(r -> r.vpnGatewayId(getVpnGatewayId()));
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        if (getVpnGatewayId() != null) {
            sb.append(getVpnGatewayId());
        } else {
            sb.append("virtual private gateway");
        }

        return sb.toString();
    }

    private boolean replaceVpc(Ec2Client client) {
        try {
            attachVpc(client);

            return true;
        } catch (Ec2Exception ex) {
            if (ex.awsErrorDetails().errorMessage().startsWith("The vpnGateway ID '" + getVpnGatewayId() + "' does not exist")) {
                return false;
            }

            throw ex;
        }
    }

    private void attachVpc(Ec2Client client) {
        client.attachVpnGateway(r -> r.vpcId(getVpc().getId()).vpnGatewayId(getVpnGatewayId()));

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> isVpcAttached(client));
    }

    private boolean isVpcAttached(Ec2Client client) {
        VpnGateway vpnGateway = getVpnGateway(client);

        return vpnGateway != null
            && !vpnGateway.vpcAttachments().isEmpty()
            && vpnGateway.vpcAttachments().get(0).stateAsString().equalsIgnoreCase("attached");
    }

    private VpnGateway getVpnGateway(Ec2Client client) {
        VpnGateway vpnGateway = null;

        try {
            DescribeVpnGatewaysResponse response = client.describeVpnGateways(r -> r.vpnGatewayIds(getVpnGatewayId()));

            if (!response.vpnGateways().isEmpty()) {
                vpnGateway = response.vpnGateways().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return vpnGateway;
    }
}
