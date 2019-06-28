package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.EgressOnlyInternetGateway;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query egress only internet gateway.
 *
 * .. code-block:: gyro
 *
 *    egress-gateway: $(aws::egress-only-internet-gateway EXTERNAL/* | egress-only-internet-gateway-id = 'eigw-0f5c4f2180ecf5127')
 */
@Type("egress-only-internet-gateway")
public class EgressOnlyInternetGatewayFinder extends AwsFinder<Ec2Client, EgressOnlyInternetGateway, EgressOnlyInternetGatewayResource> {
    private String egressOnlyInternetGatewayId;

    /**
     * The ID for the egress only internet gateway..
     */
    public String getEgressOnlyInternetGatewayId() {
        return egressOnlyInternetGatewayId;
    }

    public void setEgressOnlyInternetGatewayId(String egressOnlyInternetGatewayId) {
        this.egressOnlyInternetGatewayId = egressOnlyInternetGatewayId;
    }

    @Override
    protected List<EgressOnlyInternetGateway> findAllAws(Ec2Client client) {
        return client.describeEgressOnlyInternetGateways().egressOnlyInternetGateways();
    }

    @Override
    protected List<EgressOnlyInternetGateway> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeEgressOnlyInternetGateways(
            r -> r.egressOnlyInternetGatewayIds(Collections.singleton(filters.get("egress-only-internet-gateway-id")))
        ).egressOnlyInternetGateways();
    }
}
