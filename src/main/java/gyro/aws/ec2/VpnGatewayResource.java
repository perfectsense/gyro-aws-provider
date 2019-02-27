package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import gyro.core.diff.ResourceOutput;
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
@ResourceName("vpn-gateway")
public class VpnGatewayResource extends Ec2TaggableResource<VpnGateway> {

    private String vpnGatewayId;
    private Long amazonSideAsn;
    private String vpcId;

    @ResourceOutput
    public String getVpnGatewayId() {
        return vpnGatewayId;
    }

    public void setVpnGatewayId(String vpnGatewayId) {
        this.vpnGatewayId = vpnGatewayId;
    }

    public Long getAmazonSideAsn() {
        return amazonSideAsn;
    }

    public void setAmazonSideAsn(Long amazonSideAsn) {
        this.amazonSideAsn = amazonSideAsn;
    }

    /**
     * VPC ID to be attached to the gateway.
     */
    @ResourceDiffProperty(updatable = true)
    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
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
            setVpcId(vpnGateway.vpcAttachments().isEmpty() ? "" : vpnGateway.vpcAttachments().get(0).vpcId());

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

        if (!StringUtils.isEmpty(getVpcId())) {
            client.attachVpnGateway(r -> r.vpcId(getVpcId()).vpnGatewayId(getVpnGatewayId()));
        }
    }

    @Override
    protected void doUpdate(AwsResource config, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        VpnGatewayResource oldResource = (VpnGatewayResource) config;
        boolean detach = false;
        if (!StringUtils.isEmpty(oldResource.getVpcId())) {
            client.detachVpnGateway(r -> r.vpcId(oldResource.getVpcId()).vpnGatewayId(getVpnGatewayId()));
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

        client.attachVpnGateway(r -> r.vpcId(getVpcId()).vpnGatewayId(getVpnGatewayId()));

    }

    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);

        if (!StringUtils.isEmpty(getVpcId())) {
            client.detachVpnGateway(r -> r.vpcId(getVpcId()).vpnGatewayId(getVpnGatewayId()));
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
