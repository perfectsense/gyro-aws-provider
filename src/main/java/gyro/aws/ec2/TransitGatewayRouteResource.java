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

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Add a route to a transit gateway route table.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *      route
 *          destination-cidr-block: 10.0.0.0/16
 *          blackhole: true
 *      end
 */
public class TransitGatewayRouteResource extends AwsResource implements Copyable<TransitGatewayRoute> {

    private String destinationCidrBlock;
    private Boolean blackhole;
    private TransitGatewayPeeringAttachmentResource peeringAttachment;
    private TransitGatewayVpcAttachmentResource vpcAttachment;

    /**
     * The Cidr block for which the route needs to be created. (Required)
     */
    @Required
    public String getDestinationCidrBlock() {
        return destinationCidrBlock;
    }

    public void setDestinationCidrBlock(String destinationCidrBlock) {
        this.destinationCidrBlock = destinationCidrBlock;
    }

    /**
     * Enable blackhole to drop all the traffic that matches this route.
     */
    @Updatable
    @ConflictsWith({"peering-attachment", "vpc-attachment"})
    public Boolean getBlackhole() {
        return blackhole;
    }

    public void setBlackhole(Boolean blackhole) {
        this.blackhole = blackhole;
    }

    /**
     * The Peering attachment for the route.
     */
    @Updatable
    @ConflictsWith({"vpc-attachment", "blackhole"})
    public TransitGatewayPeeringAttachmentResource getPeeringAttachment() {
        return peeringAttachment;
    }

    public void setPeeringAttachment(TransitGatewayPeeringAttachmentResource peeringAttachment) {
        this.peeringAttachment = peeringAttachment;
    }

    /**
     * The Vpc attachment for the route.
     */
    @Updatable
    @ConflictsWith({"peering-attachment", "blackhole"})
    public TransitGatewayVpcAttachmentResource getVpcAttachment() {
        return vpcAttachment;
    }

    public void setVpcAttachment(TransitGatewayVpcAttachmentResource vpcAttachment) {
        this.vpcAttachment = vpcAttachment;
    }

    @Override
    public void copyFrom(TransitGatewayRoute model) {
        setDestinationCidrBlock(model.destinationCidrBlock());
        setBlackhole(model.hasTransitGatewayAttachments());
        if (model.hasTransitGatewayAttachments()) {
            TransitGatewayRouteAttachment attachment = model.transitGatewayAttachments().get(0);
            if (attachment.resourceTypeAsString().equals("peering")) {
                setPeeringAttachment(findById(TransitGatewayPeeringAttachmentResource.class, attachment.transitGatewayAttachmentId()));
            } else if (attachment.resourceTypeAsString().equals("vpc")) {
                setVpcAttachment(findById(TransitGatewayVpcAttachmentResource.class, attachment.transitGatewayAttachmentId()));
            }
        }
    }

    @Override
    public String primaryKey() {
        return getDestinationCidrBlock();
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);

        CreateTransitGatewayRouteRequest.Builder builder = CreateTransitGatewayRouteRequest.builder().destinationCidrBlock(getDestinationCidrBlock()).transitGatewayRouteTableId(((TransitGatewayRouteTableResource) parent()).getId());

        if (getBlackhole() != null && getBlackhole().equals(Boolean.TRUE)) {
            builder.blackhole(getBlackhole());
        } else {
            if (getPeeringAttachment() != null) {
                builder.transitGatewayAttachmentId(getPeeringAttachment().getId());
            } else if (getVpcAttachment() != null) {
                builder.transitGatewayAttachmentId(getVpcAttachment().getId());
            }
        }

        client.createTransitGatewayRoute(builder.build());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);

        TransitGatewayRouteResource currentRoute = (TransitGatewayRouteResource) current;

        ReplaceTransitGatewayRouteRequest.Builder builder = ReplaceTransitGatewayRouteRequest.builder().destinationCidrBlock(getDestinationCidrBlock()).transitGatewayRouteTableId(((TransitGatewayRouteTableResource) parent()).getId());

        if (getBlackhole() != null && getBlackhole().equals(Boolean.TRUE)) {
            builder.blackhole(getBlackhole());
        } else {
            if (getPeeringAttachment() != null) {
                builder.transitGatewayAttachmentId(getPeeringAttachment().getId());
            } else if (getVpcAttachment() != null) {
                builder.transitGatewayAttachmentId(getVpcAttachment().getId());
            }
        }

        client.replaceTransitGatewayRoute(builder.build());
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);
        client.deleteTransitGatewayRoute(r -> r.destinationCidrBlock(getDestinationCidrBlock()).transitGatewayRouteTableId(((TransitGatewayRouteTableResource) parent()).getId()));
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<ValidationError>();

        if (!configuredFields.contains("peering-attachment") && !configuredFields.contains("vpc-attachment") && !configuredFields.contains("blackhole")) {
            errors.add(new ValidationError(this, null, "exactly one of 'peering-attachment-resource' or 'vpc-attachment-resource' or 'blackhole' is required"));
        }

        return errors;
    }
}
