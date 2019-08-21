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
import org.apache.commons.lang.NotImplementedException;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateInternetGatewayResponse;
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

    private String id;
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
    public void copyFrom(InternetGateway internetGateway) {
        setId(internetGateway.internetGatewayId());

        if (!internetGateway.attachments().isEmpty() && !ObjectUtils.isBlank(internetGateway.attachments().get(0).vpcId())) {
            setVpc(findById(VpcResource.class, internetGateway.attachments().get(0).vpcId()));
        } else {
            setVpc(null);
        }
    }

    @Override
    public boolean doRefresh() {
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

    private InternetGateway getInternetGateway(Ec2Client client) {
        InternetGateway internetGateway = null;

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load internet gateway.");
        }

        try {
            DescribeInternetGatewaysResponse response = client.describeInternetGateways(
                r -> r.internetGatewayIds(getId())
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
