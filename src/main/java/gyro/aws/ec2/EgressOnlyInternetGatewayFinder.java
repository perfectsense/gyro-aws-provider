package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeEgressOnlyInternetGatewaysRequest;
import software.amazon.awssdk.services.ec2.model.DescribeEgressOnlyInternetGatewaysResponse;
import software.amazon.awssdk.services.ec2.model.EgressOnlyInternetGateway;

import java.util.ArrayList;
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
        return getEgressOnlyInternetGateways(client, null);
    }

    @Override
    protected List<EgressOnlyInternetGateway> findAws(Ec2Client client, Map<String, String> filters) {
        return getEgressOnlyInternetGateways(client, filters);
    }

    private List<EgressOnlyInternetGateway> getEgressOnlyInternetGateways(Ec2Client client, Map<String, String> filters) {
        List<EgressOnlyInternetGateway> egressOnlyInternetGateways = new ArrayList<>();

        DescribeEgressOnlyInternetGatewaysRequest.Builder builder = DescribeEgressOnlyInternetGatewaysRequest.builder();

        if (filters != null) {
            if (filters.containsKey("egress-only-internet-gateway-id")) {
                builder = builder.egressOnlyInternetGatewayIds(filters.get("egress-only-internet-gateway-id"));
            }
        }

        String marker = null;
        DescribeEgressOnlyInternetGatewaysResponse response;

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.describeEgressOnlyInternetGateways(builder.build());
            } else {
                response = client.describeEgressOnlyInternetGateways(builder.nextToken(marker).build());
            }

            marker = response.nextToken();
            egressOnlyInternetGateways.addAll(response.egressOnlyInternetGateways());
        } while (!ObjectUtils.isBlank(marker));

        return egressOnlyInternetGateways;
    }
}
