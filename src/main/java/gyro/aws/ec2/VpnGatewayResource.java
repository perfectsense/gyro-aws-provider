package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.core.resource.ResourceId;
import gyro.core.resource.ResourceUpdatable;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceOutput;
import org.apache.commons.lang.StringUtils;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateVpnGatewayResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpnGatewaysResponse;
import software.amazon.awssdk.services.ec2.model.GatewayType;
import software.amazon.awssdk.services.ec2.model.VpnGateway;

import java.util.Set;

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

        DescribeVpnGatewaysResponse response = client.describeVpnGateways(r -> r.vpnGatewayIds(getVpnGatewayId()));

        if (!response.vpnGateways().isEmpty()) {
            VpnGateway vpnGateway = response.vpnGateways().get(0);
            setAmazonSideAsn(vpnGateway.amazonSideAsn());
            setVpc(vpnGateway.vpcAttachments().isEmpty() ? null : findById(VpcResource.class, vpnGateway.vpcAttachments().get(0).vpcId()));

            return true;
        }

        return false;
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
            client.attachVpnGateway(r -> r.vpcId(getVpc().getId()).vpnGatewayId(getVpnGatewayId()));
        }
    }

    @Override
    protected void doUpdate(AwsResource config, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        VpnGatewayResource oldResource = (VpnGatewayResource) config;
        boolean detach = false;
        if (getVpc() != null) {
            client.detachVpnGateway(r -> r.vpcId(oldResource.getVpc().getId()).vpnGatewayId(getVpnGatewayId()));
            detach = true;
        }

        // 2 min wait before another vpc can be attached.
        if (detach) {
            try {
                Thread.sleep(120000);
            } catch (InterruptedException ex) {
                //
            }
        }

        client.attachVpnGateway(r -> r.vpcId(getVpc().getId()).vpnGatewayId(getVpnGatewayId()));

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
}
