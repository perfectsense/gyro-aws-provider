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
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateTransitGatewayPeeringAttachmentRequest;
import software.amazon.awssdk.services.ec2.model.CreateTransitGatewayPeeringAttachmentResponse;
import software.amazon.awssdk.services.ec2.model.DescribeTransitGatewayPeeringAttachmentsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.TransitGatewayAttachmentState;
import software.amazon.awssdk.services.ec2.model.TransitGatewayPeeringAttachment;

/**
 * Creates a transit gateway peering attachment.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *      aws::transit-gateway-peering-attachment transit-gateway-peering-attachment-example
 *          transit-gateway: $(aws::transit-gateway example-transit-gateway)
 *          peer-transit-gateway: $(aws::transit-gateway example-peer-transit-gateway)
 *          peer-region: "us-east-1"
 *      end
 *
 *      tags: {
 *          "Name": "transit-gateway-peering-attachment-example"
 *      }
 */
@Type("transit-gateway-peering-attachment")
public class TransitGatewayPeeringAttachmentResource extends Ec2TaggableResource<TransitGatewayPeeringAttachment>
    implements Copyable<TransitGatewayPeeringAttachment> {

    private String peerRegion;
    private TransitGatewayResource peerTransitGateway;
    private TransitGatewayResource transitGateway;

    // Output

    private String id;

    /**
     * The region of the peer transit gateway. (Required)
     */
    @Required
    public String getPeerRegion() {
        return peerRegion;
    }

    public void setPeerRegion(String peerRegion) {
        this.peerRegion = peerRegion;
    }

    /**
     * The accepter transit gateway that has to be attached to your transit gateway. (Required)
     */
    @Required
    public TransitGatewayResource getPeerTransitGateway() {
        return peerTransitGateway;
    }

    public void setPeerTransitGateway(TransitGatewayResource peerTransitGateway) {
        this.peerTransitGateway = peerTransitGateway;
    }

    /**
     * The requester transit gateway that needs the peering attachment. (Required)
     */
    @Required
    public TransitGatewayResource getTransitGateway() {
        return transitGateway;
    }

    public void setTransitGateway(TransitGatewayResource transitGateway) {
        this.transitGateway = transitGateway;
    }

    /**
     * The ID of the transit gateway attachment.
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
    public void copyFrom(TransitGatewayPeeringAttachment model) {
        setId(model.transitGatewayAttachmentId());
        setTransitGateway(findById(TransitGatewayResource.class, model.requesterTgwInfo().transitGatewayId()));
        setPeerTransitGateway(findById(TransitGatewayResource.class, model.accepterTgwInfo().transitGatewayId()));
        setPeerRegion(model.accepterTgwInfo().region());

        refreshTags();
    }

    @Override
    protected boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        TransitGatewayPeeringAttachment attachment = getTransitGatewayPeeringAttachment(client);
        if (attachment == null || attachment.state().equals(TransitGatewayAttachmentState.DELETING)) {
            return false;
        }

        copyFrom(attachment);

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        if (getPeerTransitGateway().getArn() == null) {
            throw new GyroException(
                "Due to account restrictions, please enter an external query for your peer transit gateway");
        }

        CreateTransitGatewayPeeringAttachmentResponse response = client.createTransitGatewayPeeringAttachment(
            CreateTransitGatewayPeeringAttachmentRequest.builder()
                .transitGatewayId(getTransitGateway().getId())
                .peerTransitGatewayId(getPeerTransitGateway().getId())
                .peerAccountId(getPeerTransitGateway().getOwnerId())
                .peerRegion(getPeerRegion())
                .build());

        TransitGatewayPeeringAttachment peeringAttachment = response.transitGatewayPeeringAttachment();

        setId(peeringAttachment.transitGatewayAttachmentId());

        Wait.atMost(3, TimeUnit.MINUTES)
            .checkEvery(1, TimeUnit.MINUTES)
            .prompt(false)
            .until(() -> {
                TransitGatewayPeeringAttachment attachment = getTransitGatewayPeeringAttachment(client);
                return attachment != null && attachment.state()
                    .equals(TransitGatewayAttachmentState.PENDING_ACCEPTANCE);
            });

        getPeerTransitGateway().acceptTransitGatewayPeeringAttachment(getId());

        Wait.atMost(10, TimeUnit.MINUTES)
            .checkEvery(2, TimeUnit.MINUTES)
            .prompt(false)
            .until(() -> {
                TransitGatewayPeeringAttachment attachment = getTransitGatewayPeeringAttachment(client);
                return attachment != null && attachment.state().equals(TransitGatewayAttachmentState.AVAILABLE);
            });
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteTransitGatewayPeeringAttachment(r -> r.transitGatewayAttachmentId(getId()));

        Wait.atMost(5, TimeUnit.MINUTES)
            .checkEvery(1, TimeUnit.MINUTES)
            .prompt(false)
            .until(() -> getTransitGatewayPeeringAttachment(client) == null);
    }

    private TransitGatewayPeeringAttachment getTransitGatewayPeeringAttachment(Ec2Client client) {
        TransitGatewayPeeringAttachment attachment = null;

        try {
            DescribeTransitGatewayPeeringAttachmentsResponse response = client.describeTransitGatewayPeeringAttachments(
                r -> r.transitGatewayAttachmentIds(getId()));

            List<TransitGatewayPeeringAttachment> transitGatewayPeeringAttachments = response.transitGatewayPeeringAttachments();
            if (!transitGatewayPeeringAttachments.isEmpty() && !transitGatewayPeeringAttachments.get(0)
                .state()
                .equals(TransitGatewayAttachmentState.DELETED)) {
                attachment = transitGatewayPeeringAttachments.get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.awsErrorDetails().errorCode().equals("InvalidTransitGatewayPeeringAttachmentId.NotFound")) {
                throw ex;
            }
        }

        return attachment;
    }
}
