package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.scope.State;
import org.apache.commons.lang.NotImplementedException;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateVpnGatewayRequest;
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
@Type("vpn-gateway")
public class VpnGatewayResource extends Ec2TaggableResource<VpnGateway> implements Copyable<VpnGateway> {
    private Long amazonSideAsn;
    private VpcResource vpc;
    private String availabilityZone;

    // Read-only
    private String id;

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
     * The VPC to be attached with the VPN Gateway.
     */
    @Updatable
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    /**
     * The availability zone for the gateway.
     */
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * The ID of the VPN Gateway.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected String getResourceId() {
        return getId();
    }

    @Override
    public void copyFrom(VpnGateway vpnGateway) {
        setId(vpnGateway.vpnGatewayId());
        setAmazonSideAsn(vpnGateway.amazonSideAsn());

        if (!vpnGateway.vpcAttachments().isEmpty()
            && vpnGateway.vpcAttachments().get(0).stateAsString().equals("attached")
            && !ObjectUtils.isBlank(vpnGateway.vpcAttachments().get(0).vpcId())) {
            setVpc(findById(VpcResource.class, vpnGateway.vpcAttachments().get(0).vpcId()));
        } else {
            setVpc(null);
        }
    }

    @Override
    protected boolean doRefresh() {
        throw new NotImplementedException();
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        throw new NotImplementedException();
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {
        throw new NotImplementedException();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        throw new NotImplementedException();
    }

    private boolean replaceVpc(Ec2Client client) {
        try {
            attachVpc(client);

            return true;
        } catch (Ec2Exception ex) {
            if (ex.awsErrorDetails().errorMessage().startsWith("The vpnGateway ID '" + getId() + "' does not exist")) {
                return false;
            }

            throw ex;
        }
    }

    private void attachVpc(Ec2Client client) {
        client.attachVpnGateway(r -> r.vpcId(getVpc().getResourceId()).vpnGatewayId(getId()));

        boolean waitResult = Wait.atMost(90, TimeUnit.SECONDS)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> isVpcAttached(client));

        if (!waitResult) {
            throw new GyroException("Unable to attach vpc " + getVpc().getId() + " with vpn gateway - " + getId());
        }
    }

    private boolean isVpcAttached(Ec2Client client) {
        VpnGateway vpnGateway = getVpnGateway(client);

        return vpnGateway != null
            && !vpnGateway.vpcAttachments().isEmpty()
            && vpnGateway.vpcAttachments().get(0).stateAsString().equalsIgnoreCase("attached");
    }

    private VpnGateway getVpnGateway(Ec2Client client) {
        VpnGateway vpnGateway = null;
        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to vpn gateway.");
        }

        try {
            DescribeVpnGatewaysResponse response = client.describeVpnGateways(r -> r.vpnGatewayIds(getId()));

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

    private boolean isVpnDeleted(Ec2Client client) {
        VpnGateway vpnGateway = getVpnGateway(client);

        return vpnGateway == null || vpnGateway.stateAsString().equalsIgnoreCase("deleted");
    }
}
