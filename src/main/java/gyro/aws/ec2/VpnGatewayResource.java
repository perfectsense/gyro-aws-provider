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
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AttachmentStatus;
import software.amazon.awssdk.services.ec2.model.CreateVpnGatewayRequest;
import software.amazon.awssdk.services.ec2.model.CreateVpnGatewayResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpnGatewaysResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.GatewayType;
import software.amazon.awssdk.services.ec2.model.VpnGateway;
import software.amazon.awssdk.services.ec2.model.VpnState;

/**
 * Create VPN Gateway.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::vpn-gateway vpn-gateway-example
 *         tags: {
 *             Name: "vpn-gateway-example"
 *         }
 *     end
 */
@Type("vpn-gateway")
public class VpnGatewayResource extends Ec2TaggableResource<VpnGateway> implements Copyable<VpnGateway> {

    private Long amazonSideAsn;
    private VpcResource vpc;
    private String availabilityZone;

    // Read-only
    private String id;

    /**
     * The private Autonomous System Number (ASN) for the Amazon side of a BGP session. If you're using a 16-bit ASN, it must be in the ``64512`` to ``65534`` range. If you're using a 32-bit ASN, it must be in the ``4200000000`` to ``4294967294`` range.
     */
    public Long getAmazonSideAsn() {
        if (amazonSideAsn == null) {
            amazonSideAsn = 0L;
        }

        return amazonSideAsn;
    }

    public void setAmazonSideAsn(Long amazonSideAsn) {
        this.amazonSideAsn = amazonSideAsn;
    }

    /**
     * The VPC to be attached with the VPN Gateway.
     */
    @Updatable
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    /**
     * The availability zone for the gateway.
     */
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * The ID of the VPN Gateway.
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
    public void copyFrom(VpnGateway vpnGateway) {
        setId(vpnGateway.vpnGatewayId());
        setAmazonSideAsn(vpnGateway.amazonSideAsn());

        if (!vpnGateway.vpcAttachments().isEmpty()
            && vpnGateway.vpcAttachments().get(0).state().equals(AttachmentStatus.ATTACHED)
            && !ObjectUtils.isBlank(vpnGateway.vpcAttachments().get(0).vpcId())) {
            setVpc(findById(VpcResource.class, vpnGateway.vpcAttachments().get(0).vpcId()));
        } else {
            setVpc(null);
        }

        refreshTags();
    }

    @Override
    protected boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        VpnGateway vpnGateway = getVpnGateway(client);

        if (vpnGateway == null){
            return false;
        }

        copyFrom(vpnGateway);

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        CreateVpnGatewayRequest.Builder builder = CreateVpnGatewayRequest.builder().type(GatewayType.IPSEC_1);

        if (getAmazonSideAsn() > 0) {
            builder = builder.amazonSideAsn(getAmazonSideAsn());
        }

        if (!ObjectUtils.isBlank(getAvailabilityZone())) {
            builder = builder.availabilityZone(getAvailabilityZone());
        }

        CreateVpnGatewayResponse response = client.createVpnGateway(builder.build());

        setId(response.vpnGateway().vpnGatewayId());

        state.save();

        if (getVpc() != null) {
            attachVpc(client);
        }
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        VpnGatewayResource oldResource = (VpnGatewayResource) config;

        if (oldResource.getVpc() != null) {
            client.detachVpnGateway(r -> r.vpcId(oldResource.getVpc().getResourceId()).vpnGatewayId(getId()));
        }

        if (getVpc() != null) {
            Wait.atMost(1, TimeUnit.MINUTES)
                .checkEvery(10, TimeUnit.SECONDS)
                .prompt(true)
                .until(() -> replaceVpc(client));
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        if (getVpc() != null) {
            client.detachVpnGateway(r -> r.vpcId(getVpc().getResourceId()).vpnGatewayId(getId()));
        }

        client.deleteVpnGateway(r -> r.vpnGatewayId(getId()));

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> isVpnDeleted(client));

        // Delay for the vpc to be fully cleared.
        try {
            Thread.sleep(30000);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean replaceVpc(Ec2Client client) {
        try {
            attachVpc(client);

            return true;
        } catch (Ec2Exception ex) {
            if (ex.awsErrorDetails().errorMessage().startsWith("The vpnGateway ID '" + getId() + "' does not exist")) {
                return false;
            }

            throw ex;
        }
    }

    private void attachVpc(Ec2Client client) {
        client.attachVpnGateway(r -> r.vpcId(getVpc().getResourceId()).vpnGatewayId(getId()));

        boolean waitResult = Wait.atMost(90, TimeUnit.SECONDS)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> isVpcAttached(client));

        if (!waitResult) {
            throw new GyroException("Unable to attach vpc " + getVpc().getId() + " with vpn gateway - " + getId());
        }
    }

    private boolean isVpcAttached(Ec2Client client) {
        VpnGateway vpnGateway = getVpnGateway(client);

        return vpnGateway != null
            && !vpnGateway.vpcAttachments().isEmpty()
            && vpnGateway.vpcAttachments().get(0).state().equals(AttachmentStatus.ATTACHED);
    }

    private VpnGateway getVpnGateway(Ec2Client client) {
        VpnGateway vpnGateway = null;
        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to vpn gateway.");
        }

        try {
            DescribeVpnGatewaysResponse response = client.describeVpnGateways(r -> r.vpnGatewayIds(getId()));

            if (!response.vpnGateways().isEmpty()) {
                vpnGateway = response.vpnGateways().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return vpnGateway;
    }

    private boolean isVpnDeleted(Ec2Client client) {
        VpnGateway vpnGateway = getVpnGateway(client);

        return vpnGateway == null || vpnGateway.state().equals(VpnState.DELETED);
    }
}
