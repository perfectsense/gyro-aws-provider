package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.scope.State;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateNatGatewayResponse;
import software.amazon.awssdk.services.ec2.model.DescribeNatGatewaysResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.NatGateway;
import software.amazon.awssdk.services.ec2.model.NatGatewayState;

import java.util.Set;
import java.util.concurrent.TimeUnit;

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
    private InternetGatewayResource internetGateway;

    /**
     * Associated Elastic IP for the Nat Gateway. (Required)
     */
    public ElasticIpResource getElasticIp() {
        return elasticIp;
    }

    public void setElasticIp(ElasticIpResource elasticIp) {
        this.elasticIp = elasticIp;
    }

    /**
     * Associated Subnet for the Nat Gateway. (Required)
     */
    public SubnetResource getSubnet() {
        return subnet;
    }

    public void setSubnet(SubnetResource subnet) {
        this.subnet = subnet;
    }

    /**
     * The Internet Gateway required for the Nat Gateway to be created.
     */
    public InternetGatewayResource getInternetGateway() {
        return internetGateway;
    }

    public void setInternetGateway(InternetGatewayResource internetGateway) {
        this.internetGateway = internetGateway;
    }

    /**
     * The ID of the Nat Gateway.
     */
    @Id
    @Output
    public String getNatGatewayId() {
        return natGatewayId;
    }

    public void setNatGatewayId(String natGatewayId) {
        this.natGatewayId = natGatewayId;
    }

    @Override
    protected String getId() {
        return getNatGatewayId();
    }

    @Override
    public void copyFrom(NatGateway natGateway) {
        setNatGatewayId(natGateway.natGatewayId());
        setSubnet(findById(SubnetResource.class, natGateway.subnetId()));
        setElasticIp(findById(ElasticIpResource.class, natGateway.natGatewayAddresses().get(0).allocationId()));
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
    protected void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        validate();

        CreateNatGatewayResponse response = client.createNatGateway(
            r -> r.allocationId(getElasticIp().getAllocationId())
                .subnetId(getSubnet().getSubnetId())
        );

        NatGateway natGateway = response.natGateway();
        setNatGatewayId(natGateway.natGatewayId());

        Wait.atMost(1, TimeUnit.HOURS)
            .checkEvery(10, TimeUnit.SECONDS)
            .until(() -> isAvailable(client));
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteNatGateway(
            r -> r.natGatewayId(getNatGatewayId())
        );

        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> isDeleted(client));
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("nat gateway");

        if (!ObjectUtils.isBlank(getNatGatewayId())) {
            sb.append(" - ").append(getNatGatewayId());

        }

        return sb.toString();
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

    private boolean isAvailable(Ec2Client client) {
        NatGateway natGateway = getNatGateway(client);

        if (natGateway != null) {
            if (natGateway.state().equals(NatGatewayState.FAILED)) {
                throw new GyroException(String.format("Nat Gateway creation failed - %s", natGateway.failureMessage()));
            } else {
                return natGateway.state().equals(NatGatewayState.AVAILABLE);
            }
        } else {
            return false;
        }
    }

    private boolean isDeleted(Ec2Client client) {
        NatGateway natGateway = getNatGateway(client);

        return natGateway == null || natGateway.state().equals(NatGatewayState.DELETED);
    }

    private void validate() {
        if (getSubnet() == null) {
            throw new GyroException("subnet is required.");
        }

        if (getInternetGateway() == null) {
            throw new GyroException("internet-gateway is required");
        }

        if (!getSubnet().getVpc().getVpcId().equals(getInternetGateway().getVpc().getVpcId())) {
            throw new GyroException("The subnet and intern-gateway needs to belong to the same vpc.");
        }
    }
}
