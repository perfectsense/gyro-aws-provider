/*
 * Copyright 2020, Perfect Sense, Inc.
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

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.TimeoutSettings;
import gyro.core.Wait;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.TransitGatewayAssociationState;
import software.amazon.awssdk.services.ec2.model.TransitGatewayAttachmentResourceType;
import software.amazon.awssdk.services.ec2.model.TransitGatewayRouteTableAssociation;

/**
 * Associate an attachment to a transit gateway route table.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *      association
 *          peering-attachment: $(aws::transit-gateway-peering-attachment peering-attachment-example)
 *      end
 */
public class TransitGatewayRouteTableAssociationResource extends AwsResource
    implements Copyable<TransitGatewayRouteTableAssociation> {

    private TransitGatewayPeeringAttachmentResource peeringAttachment;
    private TransitGatewayVpcAttachmentResource vpcAttachment;
    private VpnConnectionResource vpnAttachment;

    /**
     * The Peering attachment to associate with the route table.
     */
    @ConflictsWith({ "vpc-attachment", "vpn-attachment" })
    public TransitGatewayPeeringAttachmentResource getPeeringAttachment() {
        return peeringAttachment;
    }

    public void setPeeringAttachment(TransitGatewayPeeringAttachmentResource peeringAttachment) {
        this.peeringAttachment = peeringAttachment;
    }

    /**
     * The Vpc attachment to associate with the route table.
     */
    @ConflictsWith({ "peering-attachment", "vpn-attachment" })
    public TransitGatewayVpcAttachmentResource getVpcAttachment() {
        return vpcAttachment;
    }

    public void setVpcAttachment(TransitGatewayVpcAttachmentResource vpcAttachment) {
        this.vpcAttachment = vpcAttachment;
    }

    /**
     * The VPN connection to associate with the route table.
     */
    @ConflictsWith({ "peering-attachment", "vpc-attachment" })
    public VpnConnectionResource getVpnAttachment() {
        return vpnAttachment;
    }

    public void setVpnAttachment(VpnConnectionResource vpnAttachment) {
        this.vpnAttachment = vpnAttachment;
    }

    @Override
    public void copyFrom(TransitGatewayRouteTableAssociation model) {
        if (model.resourceType().equals(TransitGatewayAttachmentResourceType.PEERING)) {
            setPeeringAttachment(findById(
                TransitGatewayPeeringAttachmentResource.class, model.transitGatewayAttachmentId()));
        } else if (model.resourceType().equals(TransitGatewayAttachmentResourceType.VPC)) {
            setVpcAttachment(findById(TransitGatewayVpcAttachmentResource.class, model.transitGatewayAttachmentId()));
        } else if (model.resourceType().equals(TransitGatewayAttachmentResourceType.VPN)) {
            setVpnAttachment(findById(VpnConnectionResource.class, model.transitGatewayAttachmentId()));
        }
    }

    @Override
    public String primaryKey() {
        return getAttachmentId();
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);

        client.associateTransitGatewayRouteTable(s -> s.transitGatewayRouteTableId(((TransitGatewayRouteTableResource) parent())
            .getId())
            .transitGatewayAttachmentId(getAttachmentId()));

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.CREATE)
            .prompt(false)
            .until(() -> {
                List<TransitGatewayRouteTableAssociation> associations = getTransitGatewayRouteTableAssociations(client);
                return !associations.isEmpty() && associations.get(0)
                    .state()
                    .equals(TransitGatewayAssociationState.ASSOCIATED);
            });
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);

        client.disassociateTransitGatewayRouteTable(s -> s.transitGatewayRouteTableId(((TransitGatewayRouteTableResource) parent())
            .getId())
            .transitGatewayAttachmentId(getAttachmentId()));

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.DELETE)
            .prompt(false)
            .until(() -> {
                List<TransitGatewayRouteTableAssociation> associations = getTransitGatewayRouteTableAssociations(client);
                return associations.isEmpty() || associations.get(0)
                    .state()
                    .equals(TransitGatewayAssociationState.DISASSOCIATED);
            });
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (!configuredFields.contains("peering-attachment") && !configuredFields.contains("vpc-attachment")) {
            errors.add(new ValidationError(
                this,
                null,
                "Exactly one of params 'peering-attachment' or 'vpc-attachment' is required"));
        }

        return errors;
    }

    private String getAttachmentId() {
        return getPeeringAttachment() != null ? getPeeringAttachment().getId()
            : (getVpcAttachment() != null ? getVpcAttachment().getId() : getVpnAttachment().getId());
    }

    private List<TransitGatewayRouteTableAssociation> getTransitGatewayRouteTableAssociations(Ec2Client client) {
        return client.getTransitGatewayRouteTableAssociations(
            s -> s.transitGatewayRouteTableId(((TransitGatewayRouteTableResource) parent()).getId())
                .filters(r -> r.name("transit-gateway-attachment-id").values(getAttachmentId())))
            .associations();
    }
}
