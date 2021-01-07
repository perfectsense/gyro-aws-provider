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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateNatGatewayResponse;
import software.amazon.awssdk.services.ec2.model.DescribeNatGatewaysResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.NatGateway;
import software.amazon.awssdk.services.ec2.model.NatGatewayState;

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
 *         tags: {
 *             Name: elastic-ip-example-for-nat-gateway
 *         }
 *     end
 */
@Type("nat-gateway")
public class NatGatewayResource extends Ec2TaggableResource<NatGateway> implements Copyable<NatGateway> {

    private ElasticIpResource elasticIp;
    private SubnetResource subnet;
    private InternetGatewayResource internetGateway;

    // Read-only
    private String id;

    /**
     * The associated elastic IP for the Nat Gateway.
     */
    @Required
    public ElasticIpResource getElasticIp() {
        return elasticIp;
    }

    public void setElasticIp(ElasticIpResource elasticIp) {
        this.elasticIp = elasticIp;
    }

    /**
     * The associated subnet for the Nat Gateway.
     */
    @Required
    public SubnetResource getSubnet() {
        return subnet;
    }

    public void setSubnet(SubnetResource subnet) {
        this.subnet = subnet;
    }

    /**
     * The internet gateway required for the Nat Gateway to be created.
     */
    @Required
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
    public void copyFrom(NatGateway natGateway) {
        setId(natGateway.natGatewayId());
        setSubnet(findById(SubnetResource.class, natGateway.subnetId()));
        setElasticIp(findById(ElasticIpResource.class, natGateway.natGatewayAddresses().get(0).allocationId()));

        refreshTags();
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
            r -> r.allocationId(getElasticIp().getId())
                .subnetId(getSubnet().getId())
        );

        NatGateway natGateway = response.natGateway();
        setId(natGateway.natGatewayId());

        state.save();

        boolean waitResult = Wait.atMost(7, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> isAvailable(client));

        if (!waitResult) {
            throw new GyroException("Unable to reach 'available' state for nat gateway - " + getId());
        }
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteNatGateway(
            r -> r.natGatewayId(getId())
        );

        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> isDeleted(client));
    }

    private NatGateway getNatGateway(Ec2Client client) {
        NatGateway natGateway = null;

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load nat gateway.");
        }

        try {
            DescribeNatGatewaysResponse response = client.describeNatGateways(
                r -> r.natGatewayIds(getId())
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

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (!getSubnet().getVpc().equals(getInternetGateway().getVpc())) {
            errors.add(new ValidationError(this, null, "The subnet and internet-gateway needs to belong to the same vpc."));
        }

        return errors;
    }
}
