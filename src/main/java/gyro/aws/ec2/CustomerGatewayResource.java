package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.scope.State;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateCustomerGatewayRequest;
import software.amazon.awssdk.services.ec2.model.CreateCustomerGatewayResponse;
import software.amazon.awssdk.services.ec2.model.CustomerGateway;
import software.amazon.awssdk.services.ec2.model.DescribeCustomerGatewaysResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.GatewayType;

import java.util.Set;

/**
 * Create Customer Gateway based on the provided Public IP.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::customer-gateway customer-gateway-example
 *         public-ip: "38.140.23.146"
 *
 *         tags: {
 *             Name: "customer-gateway-example"
 *         }
 *     end
 */
@Type("customer-gateway")
public class CustomerGatewayResource extends Ec2TaggableResource<CustomerGateway> implements Copyable<CustomerGateway> {

    private String customerGatewayId;
    private String publicIp;
    private Integer bgpAsn;

    /**
     * Public IP address for the gateway's external interface. See `Customer Gateway <https://docs.aws.amazon.com/vpc/latest/adminguide/Introduction.html/>`_. (Required)
     */
    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    /**
     * the Border Gateway Protocol Autonomous System Number of the gateway.
     */
    public Integer getBgpAsn() {
        if (bgpAsn == null) {
            bgpAsn = 0;
        }

        return bgpAsn;
    }

    public void setBgpAsn(Integer bgpAsn) {
        this.bgpAsn = bgpAsn;
    }

    @Override
    protected String getResourceId() {
        return getCustomerGatewayId();
    }

    /**
     * The ID of the Customer Gateway.
     */
    @Id
    @Output
    public String getCustomerGatewayId() {
        return customerGatewayId;
    }

    public void setCustomerGatewayId(String customerGatewayId) {
        this.customerGatewayId = customerGatewayId;
    }

    @Override
    public void copyFrom(CustomerGateway customerGateway) {
        setCustomerGatewayId(customerGateway.customerGatewayId());
        setPublicIp(customerGateway.ipAddress());
        setBgpAsn(!ObjectUtils.isBlank(customerGateway.bgpAsn()) ? Integer.parseInt(customerGateway.bgpAsn()) : null);
    }

    @Override
    protected boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        CustomerGateway customerGateway = getCustomerGateway(client);

        if (customerGateway == null) {
            return false;
        }

        copyFrom(customerGateway);

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        CreateCustomerGatewayRequest.Builder builder = CreateCustomerGatewayRequest.builder();
        builder.publicIp(getPublicIp());
        builder.type(GatewayType.IPSEC_1);

        if (getBgpAsn() > 0) {
            builder.bgpAsn(getBgpAsn());
        } else {
            builder.bgpAsn(65000);
        }

        Ec2Client client = createClient(Ec2Client.class);

        CreateCustomerGatewayResponse response = client.createCustomerGateway(builder.build());

        setCustomerGatewayId(response.customerGateway().customerGatewayId());
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteCustomerGateway(r -> r.customerGatewayId(getCustomerGatewayId()));
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("customer gateway");

        if (!ObjectUtils.isBlank(getCustomerGatewayId())) {
            sb.append(getCustomerGatewayId());
        }

        return sb.toString();
    }

    private CustomerGateway getCustomerGateway(Ec2Client client) {
        CustomerGateway customerGateway = null;

        if (ObjectUtils.isBlank(getCustomerGatewayId())) {
            throw new GyroException("customer-gateway-id is missing, unable to load customer gateway.");
        }

        try {
            DescribeCustomerGatewaysResponse response = client.describeCustomerGateways(r -> r.customerGatewayIds(getCustomerGatewayId()));

            if (!response.customerGateways().isEmpty()) {
                customerGateway = response.customerGateways().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return customerGateway;
    }
}
