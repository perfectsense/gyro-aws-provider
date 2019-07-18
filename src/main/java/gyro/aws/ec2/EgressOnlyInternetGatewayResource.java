package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateEgressOnlyInternetGatewayResponse;
import software.amazon.awssdk.services.ec2.model.DescribeEgressOnlyInternetGatewaysResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.EgressOnlyInternetGateway;

import java.util.Set;

/**
 * Create an egress only internet gateway.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::egress-only-internet-gateway example-egress-gateway
 *         vpc: $(aws::vpc vpc-example)
 *     end
 */
@Type("egress-only-internet-gateway")
public class EgressOnlyInternetGatewayResource extends AwsResource implements Copyable<EgressOnlyInternetGateway> {

    private VpcResource vpc;
    private String gatewayId;

    /**
     * The VPC to create the egress only internet gateway in. (Required)
     */
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    /**
     * The id of the egress only internet gateway.
     */
    @Id
    @Output
    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    @Override
    public void copyFrom(EgressOnlyInternetGateway egressOnlyInternetGateway) {
        setGatewayId(egressOnlyInternetGateway.egressOnlyInternetGatewayId());

        if (!egressOnlyInternetGateway.attachments().isEmpty()) {
            setVpc(findById(VpcResource.class, egressOnlyInternetGateway.attachments().get(0).vpcId()));
        }
    }

    @Override
    public boolean refresh() {
        Ec2Client client = createClient(Ec2Client.class);

        EgressOnlyInternetGateway egressOnlyInternetGateway = getEgressOnlyInternetGateway(client);

        if (egressOnlyInternetGateway == null) {
            return false;
        }

        copyFrom(egressOnlyInternetGateway);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        if (getVpc() != null) {
            CreateEgressOnlyInternetGatewayResponse response = client.createEgressOnlyInternetGateway(r -> r.vpcId(getVpc().getVpcId()));
            EgressOnlyInternetGateway gateway = response.egressOnlyInternetGateway();
            setGatewayId(gateway.egressOnlyInternetGatewayId());
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {

    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);
        client.deleteEgressOnlyInternetGateway(r -> r.egressOnlyInternetGatewayId(getGatewayId()));
    }

    private EgressOnlyInternetGateway getEgressOnlyInternetGateway(Ec2Client client) {
        EgressOnlyInternetGateway egressOnlyInternetGateway = null;

        if (ObjectUtils.isBlank(getGatewayId())) {
            throw new GyroException("gateway-id is missing, unable to load egress gateway.");
        }

        try {
            DescribeEgressOnlyInternetGatewaysResponse response = client.describeEgressOnlyInternetGateways(
                r -> r.egressOnlyInternetGatewayIds(getGatewayId())
            );

            if (!response.egressOnlyInternetGateways().isEmpty()) {
                egressOnlyInternetGateway = response.egressOnlyInternetGateways().get(0);
            }
        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return egressOnlyInternetGateway;
    }
}
