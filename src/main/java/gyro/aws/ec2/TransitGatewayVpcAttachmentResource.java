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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.TimeoutSettings;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateTransitGatewayVpcAttachmentResponse;
import software.amazon.awssdk.services.ec2.model.DescribeTransitGatewayVpcAttachmentsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.TransitGatewayAttachmentState;
import software.amazon.awssdk.services.ec2.model.TransitGatewayVpcAttachment;

/**
 * Creates a transit gateway vpc attachment.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::transit-gateway-vpc-attachment transit-gateway-vpc-attachment-example
 *         subnets: [
 *             $(aws::subnet example-subnet-1),
 *             $(aws::subnet example-subnet-2)
 *         ]
 *         transit-gateway: $(aws::transit-gateway example-transit-gateway)
 *         vpc: $(aws::vpc vpc-example)
 *
 *         tags: {
 *             Name: "Transit-gateway-vpc-attachment-example"
 *         }
 *     end
 */
@Type("transit-gateway-vpc-attachment")
public class TransitGatewayVpcAttachmentResource extends Ec2TaggableResource<TransitGatewayVpcAttachment>
    implements Copyable<TransitGatewayVpcAttachment> {

    private List<SubnetResource> subnets;
    private TransitGatewayResource transitGateway;
    private VpcResource vpc;
    private TransitGatewayVpcAttachmentOptions options;

    // Read-only
    private String id;
    private TransitGatewayAttachmentState state;

    /**
     * List of subnets for the availability zone that the transit gateway uses. Limited to one subnet per availability zone.
     */
    @Required
    @Updatable
    public List<SubnetResource> getSubnets() {
        if (subnets == null) {
            subnets = new ArrayList<>();
        }

        return subnets;
    }

    public void setSubnets(List<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    /**
     * The transit gateway for this attachment.
     */
    @Required
    public TransitGatewayResource getTransitGateway() {
        return transitGateway;
    }

    public void setTransitGateway(TransitGatewayResource transitGateway) {
        this.transitGateway = transitGateway;
    }

    /**
     * The VPC to attach to the transit gateway selected. This VPC should have at least one subnet associated with it.
     */
    @Required
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    /**
     * The VPC attachment options.
     *
     * @subresource gyro.aws.ec2.TransitGatewayVpcAttachmentOptions
     */
    @Updatable
    public TransitGatewayVpcAttachmentOptions getOptions() {
        return options;
    }

    public void setOptions(TransitGatewayVpcAttachmentOptions options) {
        this.options = options;
    }

    /**
     * The ID of the attachment.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The state of the attachment.
     */
    @Output
    public TransitGatewayAttachmentState getState() {
        return state;
    }

    public void setState(TransitGatewayAttachmentState state) {
        this.state = state;
    }

    @Override
    protected String getResourceId() {
        return getId();
    }

    @Override
    public void copyFrom(TransitGatewayVpcAttachment model) {
        copyFrom(model, true);
    }

    @Override
    protected boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        TransitGatewayVpcAttachment attachment = getTransitGatewayVpcAttachment(client);

        if (attachment == null || attachment.state().equals(TransitGatewayAttachmentState.DELETING)) {
            return false;
        }

        copyFrom(attachment);

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        CreateTransitGatewayVpcAttachmentResponse response = client.createTransitGatewayVpcAttachment(r -> r
            .subnetIds(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList()))
            .transitGatewayId(getTransitGateway().getId())
            .vpcId(getVpc().getId())
            .options(getOptions() != null ? getOptions().toCreateTransitGatewayVpcAttachmentOptions() : null));

        copyFrom(response.transitGatewayVpcAttachment(), false);

        waitForAvailability(TimeUnit.MINUTES, client, TimeoutSettings.Action.CREATE);
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        if (changedProperties.contains("options")) {
            client.modifyTransitGatewayVpcAttachment(r -> r.transitGatewayAttachmentId(getId())
                .options(getOptions().toModifyTransitGatewayVpcAttachmentRequestOptions()));
            state.save();
            waitForAvailability(TimeUnit.SECONDS, client, TimeoutSettings.Action.UPDATE);
        }

        if (changedProperties.contains("subnets")) {
            List<String> previousAttachmentSubnetIds = ((TransitGatewayVpcAttachmentResource) config).getSubnets()
                .stream()
                .map(SubnetResource::getId)
                .collect(Collectors.toList());
            List<String> currentAttachmentSubnetIds = getSubnets().stream()
                .map(SubnetResource::getId)
                .collect(Collectors.toList());
            Set<String> subnetsToAdd = new HashSet<>(currentAttachmentSubnetIds);
            subnetsToAdd.removeAll(previousAttachmentSubnetIds);
            Set<String> subnetsToRemove = new HashSet<>(previousAttachmentSubnetIds);
            subnetsToRemove.removeAll(currentAttachmentSubnetIds);

            if (!subnetsToAdd.isEmpty() && !subnetsToRemove.isEmpty()) {
                client.modifyTransitGatewayVpcAttachment(r -> r.transitGatewayAttachmentId(getId())
                    .addSubnetIds(subnetsToAdd).removeSubnetIds(subnetsToRemove));

                waitForAvailability(TimeUnit.MINUTES, client, TimeoutSettings.Action.UPDATE);

            } else if (!subnetsToAdd.isEmpty()) {
                client.modifyTransitGatewayVpcAttachment(r -> r.transitGatewayAttachmentId(getId())
                    .addSubnetIds(subnetsToAdd));

                waitForAvailability(TimeUnit.MINUTES, client, TimeoutSettings.Action.UPDATE);

            } else if (!subnetsToRemove.isEmpty()) {
                client.modifyTransitGatewayVpcAttachment(r -> r.transitGatewayAttachmentId(getId())
                    .removeSubnetIds(subnetsToRemove));

                waitForAvailability(TimeUnit.MINUTES, client, TimeoutSettings.Action.UPDATE);
            }
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteTransitGatewayVpcAttachment(r -> r.transitGatewayAttachmentId(getId()));

        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(30, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.DELETE)
            .prompt(false)
            .until(() -> getTransitGatewayVpcAttachment(client) == null);
    }

    private TransitGatewayVpcAttachment getTransitGatewayVpcAttachment(Ec2Client client) {
        TransitGatewayVpcAttachment attachment = null;

        try {
            DescribeTransitGatewayVpcAttachmentsResponse response = client.describeTransitGatewayVpcAttachments(r -> r.transitGatewayAttachmentIds(
                getId()));

            List<TransitGatewayVpcAttachment> transitGatewayVpcAttachments = response.transitGatewayVpcAttachments();
            if (!transitGatewayVpcAttachments.isEmpty() && !transitGatewayVpcAttachments.get(0)
                .state()
                .equals(TransitGatewayAttachmentState.DELETED)) {
                attachment = transitGatewayVpcAttachments.get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.awsErrorDetails().errorCode().equals("InvalidTransitGatewayAttachmentID.NotFound")) {
                throw ex;
            }
        }

        return attachment;
    }

    private void copyFrom(TransitGatewayVpcAttachment model, boolean refreshTags) {
        setId(model.transitGatewayAttachmentId());
        setSubnets(model.subnetIds().stream().map(s -> findById(SubnetResource.class, s)).collect(Collectors.toList()));
        setVpc(findById(VpcResource.class, model.vpcId()));
        setTransitGateway(findById(TransitGatewayResource.class, model.transitGatewayId()));
        setState(model.state());

        setOptions(null);
        if (model.options() != null) {
            TransitGatewayVpcAttachmentOptions transitGatewayVpcAttachmentOptions = newSubresource(
                TransitGatewayVpcAttachmentOptions.class);
            transitGatewayVpcAttachmentOptions.copyFrom(model.options());
            setOptions(transitGatewayVpcAttachmentOptions);
        }

        if (refreshTags) {
            refreshTags();
        }
    }

    private void waitForAvailability(TimeUnit durationUnit, Ec2Client client, TimeoutSettings.Action action) {
        Wait.atMost(2, durationUnit)
            .checkEvery(30, TimeUnit.SECONDS)
            .resourceOverrides(this, action)
            .prompt(false)
            .until(() -> {
                TransitGatewayVpcAttachment attachment = getTransitGatewayVpcAttachment(client);
                return attachment != null && attachment.state().equals(TransitGatewayAttachmentState.AVAILABLE);
            });
    }
}
