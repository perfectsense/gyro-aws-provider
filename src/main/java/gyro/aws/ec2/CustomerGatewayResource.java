/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import gyro.core.validation.Required;
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

    private String id;
    private String publicIp;
    private Integer bgpAsn;

    /**
     * Public IP address for the gateway's external interface. See `Customer Gateway <https://docs.aws.amazon.com/vpc/latest/adminguide/Introduction.html/>`_. (Required)
     */
    @Required
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
        return getId();
    }

    /**
     * The ID of the Customer Gateway.
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
    public void copyFrom(CustomerGateway customerGateway) {
        setId(customerGateway.customerGatewayId());
        setPublicIp(customerGateway.ipAddress());
        setBgpAsn(!ObjectUtils.isBlank(customerGateway.bgpAsn()) ? Integer.parseInt(customerGateway.bgpAsn()) : null);

        refreshTags();
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

        setId(response.customerGateway().customerGatewayId());
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteCustomerGateway(r -> r.customerGatewayId(getId()));
    }

    private CustomerGateway getCustomerGateway(Ec2Client client) {
        CustomerGateway customerGateway = null;

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load customer gateway.");
        }

        try {
            DescribeCustomerGatewaysResponse response = client.describeCustomerGateways(r -> r.customerGatewayIds(getId()));

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
