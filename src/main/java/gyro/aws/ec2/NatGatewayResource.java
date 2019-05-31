package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateNatGatewayResponse;
import software.amazon.awssdk.services.ec2.model.DescribeNatGatewaysResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.NatGateway;

import java.util.Set;

/**
 * Creates a Nat Gateway with the specified elastic ip allocation id and subnet id.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::nat-gateway nat-gateway-example
 *         elastic-ip: $(aws::elastic-ip elastic-ip-example-for-nat-gateway)
 *         subnet: $(aws::subnet subnet-example-for-nat-gateway)
 *
 *         tags:
 *             Name: elastic-ip-example-for-nat-gateway
 *         end
 *     end
 */
@Type("nat-gateway")
public class NatGatewayResource extends Ec2TaggableResource<NatGateway> implements Copyable<NatGateway> {

    private String natGatewayId;
    private ElasticIpResource elasticIp;
    private SubnetResource subnet;

    /**
     * The id of the Nat Gateway.
     */
    @Id
    @Output
    public String getNatGatewayId() {
        return natGatewayId;
    }

    public void setNatGatewayId(String natGatewayId) {
        this.natGatewayId = natGatewayId;
    }

    /**
     * Associated elastic ip for the nat gateway. (Required)
     */
    public ElasticIpResource getElasticIp() {
        return elasticIp;
    }

    public void setElasticIp(ElasticIpResource elasticIp) {
        this.elasticIp = elasticIp;
    }

    /**
     * Associated subnet for the nat gateway. (Required)
     */
    public SubnetResource getSubnet() {
        return subnet;
    }

    public void setSubnet(SubnetResource subnet) {
        this.subnet = subnet;
    }

    @Override
    protected String getId() {
        return getNatGatewayId();
    }

    @Override
    public boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        NatGateway natGateway = getNatGateway(client);

        if (natGateway == null) {
            return false;
        }

        copyFrom(natGateway);

        return true;
    }

    @Override
    protected void doCreate() {
        Ec2Client client = createClient(Ec2Client.class);

        CreateNatGatewayResponse response = client.createNatGateway(
            r -> r.allocationId(getElasticIp().getAllocationId())
                .subnetId(getSubnet().getSubnetId())
        );

        NatGateway natGateway = response.natGateway();
        setNatGatewayId(natGateway.natGatewayId());
    }

    @Override
    protected void doUpdate(AwsResource config, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteNatGateway(
            r -> r.natGatewayId(getNatGatewayId())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("nat gateway");

        if (getNatGatewayId() != null) {
            sb.append(" - ").append(getNatGatewayId());

        }

        return sb.toString();
    }

    @Override
    public void copyFrom(NatGateway natGateway) {
        setNatGatewayId(natGateway.natGatewayId());
        setSubnet(findById(SubnetResource.class, natGateway.subnetId()));
        setElasticIp(findById(ElasticIpResource.class, natGateway.natGatewayAddresses().get(0).allocationId()));
    }

    private NatGateway getNatGateway(Ec2Client client) {
        NatGateway natGateway = null;

        if (ObjectUtils.isBlank(getNatGatewayId())) {
            throw new GyroException("nat-gateway-id is missing, unable to load nat gateway.");
        }

        try {
            DescribeNatGatewaysResponse response = client.describeNatGateways(
                r -> r.natGatewayIds(getNatGatewayId())
                    .maxResults(5)
            );

            if (!response.natGateways().isEmpty()) {
                natGateway = response.natGateways().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return natGateway;
    }
}
