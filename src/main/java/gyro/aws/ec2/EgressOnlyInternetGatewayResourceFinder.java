package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.EgressOnlyInternetGateway;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Type("egress-gateway")
public class EgressOnlyInternetGatewayResourceFinder extends AwsFinder<Ec2Client, EgressOnlyInternetGateway, EgressOnlyInternetGatewayResource> {
    private String egressOnlyInternetGatewayId;

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
