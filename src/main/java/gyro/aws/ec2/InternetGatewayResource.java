package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateInternetGatewayResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInternetGatewaysResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.InternetGateway;
import software.amazon.awssdk.services.ec2.model.InternetGatewayAttachment;

import java.util.Set;

/**
 * Create an internet gateway.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::internet-gateway example-gateway
 *         vpc: $(aws::vpc vpc-example)
 *     end
 */
@Type("internet-gateway")
public class InternetGatewayResource extends Ec2TaggableResource<InternetGateway> {

    private String internetGatewayId;
    private VpcResource vpc;

    /**
     * The id of the internet gateway.
     */
    @Id
    @Output
    public String getInternetGatewayId() {
        return internetGatewayId;
    }

    public void setInternetGatewayId(String internetGatewayId) {
        this.internetGatewayId = internetGatewayId;
    }

    /**
     * The VPC to create an internet gateway in. (Required)
     */
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }


    @Override
    protected String getId() {
        return getInternetGatewayId();
    }

    @Override
    public boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        InternetGateway internetGateway = getInternetGateway(client);

        if (internetGateway == null) {
            return false;
        }

        if (!internetGateway.attachments().isEmpty()) {
            setVpc(findById(VpcResource.class, internetGateway.attachments().get(0).vpcId()));
        }

        return true;
    }

    @Override
    protected void doCreate() {
        Ec2Client client = createClient(Ec2Client.class);

        CreateInternetGatewayResponse response = client.createInternetGateway();

        setInternetGatewayId(response.internetGateway().internetGatewayId());

        if (getVpc() != null) {
            client.attachInternetGateway(r -> r.internetGatewayId(getInternetGatewayId())
                    .vpcId(getVpc().getVpcId())
            );
        }
    }

    @Override
    protected void doUpdate(AwsResource config, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);

        InternetGateway internetGateway = getInternetGateway(client);

        if (internetGateway != null && !internetGateway.attachments().isEmpty()) {
            client.detachInternetGateway(
                r -> r.internetGatewayId(getInternetGatewayId()).vpcId(internetGateway.attachments().get(0).vpcId())
            );
        }

        client.deleteInternetGateway(r -> r.internetGatewayId(getInternetGatewayId()));
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("internet gateway");

        if (getInternetGatewayId() != null) {
            sb.append(" - ").append(getInternetGatewayId());

        }

        return sb.toString();
    }

    private InternetGateway getInternetGateway(Ec2Client client) {
        InternetGateway internetGateway = null;

        try {
            DescribeInternetGatewaysResponse response = client.describeInternetGateways(
                r -> r.internetGatewayIds(getInternetGatewayId())
            );

            if (!response.internetGateways().isEmpty()) {
                internetGateway = response.internetGateways().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return internetGateway;
    }

}
