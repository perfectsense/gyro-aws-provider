package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.elasticache.CacheClusterFinder;
import gyro.core.GyroException;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.Waiter;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateInternetGatewayResponse;
import software.amazon.awssdk.services.ec2.model.DeleteInternetGatewayResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInternetGatewaysResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.InternetGateway;

import java.util.Set;
import java.util.concurrent.TimeUnit;

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
public class InternetGatewayResource extends Ec2TaggableResource<InternetGateway> implements Copyable<InternetGateway> {

    private String internetGatewayId;
    private VpcResource vpc;

    /**
     * The VPC to create an internet gateway in. (Required)
     */
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    /**
     * The ID of the Internet Gateway.
     */
    @Id
    @Output
    public String getInternetGatewayId() {
        return internetGatewayId;
    }

    public void setInternetGatewayId(String internetGatewayId) {
        this.internetGatewayId = internetGatewayId;
    }

    @Override
    protected String getId() {
        return getInternetGatewayId();
    }

    @Override
    public void copyFrom(InternetGateway internetGateway) {
        setInternetGatewayId(internetGateway.internetGatewayId());

        if (!internetGateway.attachments().isEmpty() && !ObjectUtils.isBlank(internetGateway.attachments().get(0).vpcId())) {
            setVpc(findById(VpcResource.class, internetGateway.attachments().get(0).vpcId()));
        } else {
            setVpc(null);
        }
    }

    @Override
    public boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        InternetGateway internetGateway = getInternetGateway(client);

        if (internetGateway == null) {
            return false;
        }

        copyFrom(internetGateway);

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
            Wait.atMost(3, TimeUnit.SECONDS)
                .checkEvery(2, TimeUnit.SECONDS)
                .prompt(false)
                .until(() -> {
                    try {
                        client.detachInternetGateway(
                            r -> r.internetGatewayId(getInternetGatewayId()).vpcId(internetGateway.attachments().get(0).vpcId())
                        );
                    } catch (Ec2Exception e) {
                        // DependencyViolation should be retried since this resource may be waiting for a
                        // previously deleted resource to finish deleting.
                        if ("DependencyViolation".equals(e.awsErrorDetails().errorCode())) {
                            return false;
                        }
                    }

                    return true;
                }
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

        if (ObjectUtils.isBlank(getInternetGatewayId())) {
            throw new GyroException("internet-gateway-id is missing, unable to load internet gateway.");
        }

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
