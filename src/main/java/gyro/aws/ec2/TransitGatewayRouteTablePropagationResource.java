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
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.TransitGatewayRouteTablePropagation;

import java.util.Set;

/**
 * Propagate an attachment to a transit gateway route table.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *      propagation
 *          vpc-attachment: $(aws::transit-gateway-vpc-attachment vpc-attachment-example)
 *      end
 */
public class TransitGatewayRouteTablePropagationResource extends AwsResource implements Copyable<TransitGatewayRouteTablePropagation> {

    private TransitGatewayVpcAttachmentResource vpcAttachment;

    /**
     * The attachment to propagate to the route table. (Required)
     */
    @Required
    public TransitGatewayVpcAttachmentResource getVpcAttachment() {
        return vpcAttachment;
    }

    public void setVpcAttachment(TransitGatewayVpcAttachmentResource vpcAttachment) {
        this.vpcAttachment = vpcAttachment;
    }

    @Override
    public void copyFrom(TransitGatewayRouteTablePropagation model) {
        setVpcAttachment(findById(TransitGatewayVpcAttachmentResource.class, model.transitGatewayAttachmentId()));
    }

    @Override
    public String primaryKey() {
        String primaryKey = getVpcAttachment().getId();
        return primaryKey;
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);

        client.enableTransitGatewayRouteTablePropagation(r -> r.transitGatewayRouteTableId(((TransitGatewayRouteTableResource) parent()).getId()).transitGatewayAttachmentId(getVpcAttachment().getId()));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);

        client.disableTransitGatewayRouteTablePropagation(r -> r.transitGatewayRouteTableId(((TransitGatewayRouteTableResource) parent()).getId()).transitGatewayAttachmentId(getVpcAttachment().getId()));
    }
}
