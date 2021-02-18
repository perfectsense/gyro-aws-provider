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
import software.amazon.awssdk.services.ec2.model.CreateTransitGatewayRouteRequest;
import software.amazon.awssdk.services.ec2.model.ReplaceTransitGatewayRouteRequest;
import software.amazon.awssdk.services.ec2.model.TransitGatewayAttachmentResourceType;
import software.amazon.awssdk.services.ec2.model.TransitGatewayRoute;
import software.amazon.awssdk.services.ec2.model.TransitGatewayRouteAttachment;

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
     * The Cidr block for which the route needs to be created.
     */
    @Required
    public String getDestinationCidrBlock() {
        return destinationCidrBlock;
    }

    public void setDestinationCidrBlock(String destinationCidrBlock) {
        this.destinationCidrBlock = destinationCidrBlock;
    }

    /**
     * When set to ``true``, blackhole is enabled to drop all the traffic that matches this route.
     */
    @Updatable
    public Boolean getBlackhole() {
        return blackhole;
    }

    public void setBlackhole(Boolean blackhole) {
        this.blackhole = blackhole;
    }

    /**
     * The peering attachment for the route.
     */
    @Updatable
    @ConflictsWith("vpc-attachment")
    public TransitGatewayPeeringAttachmentResource getPeeringAttachment() {
        return peeringAttachment;
    }

    public void setPeeringAttachment(TransitGatewayPeeringAttachmentResource peeringAttachment) {
        this.peeringAttachment = peeringAttachment;
    }

    /**
     * The VPC attachment for the route.
     */
    @Updatable
    @ConflictsWith("peering-attachment")
    public TransitGatewayVpcAttachmentResource getVpcAttachment() {
        return vpcAttachment;
    }

    public void setVpcAttachment(TransitGatewayVpcAttachmentResource vpcAttachment) {
        this.vpcAttachment = vpcAttachment;
    }

    @Override
    public String primaryKey() {
        return getDestinationCidrBlock();
    }

    @Override
    public void copyFrom(TransitGatewayRoute model) {
        setDestinationCidrBlock(model.destinationCidrBlock());
        setBlackhole(model.hasTransitGatewayAttachments());
        if (model.hasTransitGatewayAttachments()) {
            TransitGatewayRouteAttachment attachment = model.transitGatewayAttachments().get(0);
            if (attachment.resourceType().equals(TransitGatewayAttachmentResourceType.PEERING)) {
                setPeeringAttachment(findById(
                    TransitGatewayPeeringAttachmentResource.class,
                    attachment.transitGatewayAttachmentId()));
            } else if (attachment.resourceType().equals(TransitGatewayAttachmentResourceType.VPC)) {
                setVpcAttachment(findById(
                    TransitGatewayVpcAttachmentResource.class,
                    attachment.transitGatewayAttachmentId()));
            }
        }
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);

        CreateTransitGatewayRouteRequest.Builder builder = CreateTransitGatewayRouteRequest.builder()
            .destinationCidrBlock(getDestinationCidrBlock())
            .transitGatewayRouteTableId(((TransitGatewayRouteTableResource) parent()).getId());

        if (getBlackhole() != null && getBlackhole().equals(Boolean.TRUE)) {
            builder.blackhole(getBlackhole());
        } else {
            builder.transitGatewayAttachmentId(getAttachmentId());
        }

        client.createTransitGatewayRoute(builder.build());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);

        TransitGatewayRouteResource currentRoute = (TransitGatewayRouteResource) current;

        ReplaceTransitGatewayRouteRequest.Builder builder = ReplaceTransitGatewayRouteRequest.builder()
            .destinationCidrBlock(getDestinationCidrBlock())
            .transitGatewayRouteTableId(((TransitGatewayRouteTableResource) parent()).getId());

        if (getBlackhole() != null && getBlackhole().equals(Boolean.TRUE)) {
            builder.blackhole(getBlackhole());
        } else {
            builder.transitGatewayAttachmentId(getAttachmentId());
        }

        client.replaceTransitGatewayRoute(builder.build());
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteTransitGatewayRoute(r -> r.destinationCidrBlock(getDestinationCidrBlock())
            .transitGatewayRouteTableId(((TransitGatewayRouteTableResource) parent()).getId()));
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (!configuredFields.contains("peering-attachment") && !configuredFields.contains("vpc-attachment")) {
            if (!configuredFields.contains("blackhole")) {
                errors.add(new ValidationError(
                    this,
                    null,
                    "At least one of 'peering-attachment' or 'vpc-attachment' or 'blackhole' is required"));
            } else if (getBlackhole().equals(Boolean.FALSE)) {
                errors.add(new ValidationError(
                    this,
                    null,
                    "At least one of 'peering-attachment' or 'vpc-attachment' is required if 'blackhole' is set to 'false'"));
            }
        } else if (configuredFields.contains("blackhole") && getBlackhole().equals(Boolean.TRUE)) {
            errors.add(new ValidationError(
                this,
                null,
                "Cannot provide 'peering-attachment' or 'vpc-attachment' if 'blackhole' is set to 'true'"));
        }

        return errors;
    }

    public String getAttachmentId() {
        return getPeeringAttachment() != null ? getPeeringAttachment().getId() : getVpcAttachment().getId();
    }
}
