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
import software.amazon.awssdk.services.ec2.model.TransitGatewayMulticastDomainAssociation;

import java.util.Set;

/**
 * Creates a transit gateway multicast domain association.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *        association
 *           vpc-attachment: $(aws::transit-gateway-vpc-attachment transit-gateway-vpc-attachment-example)
 *           subnet: $(aws::subnet example-subnet-1)
 *        end
 */
public class TransitGatewayMulticastDomainAssociationResource extends AwsResource implements Copyable<TransitGatewayMulticastDomainAssociation> {

    private TransitGatewayVpcAttachmentResource vpcAttachment;
    private SubnetResource subnet;

    /**
     * The Vpc attachment that needs to be associated to the multicast domain. (Required)
     */
    @Required
    public TransitGatewayVpcAttachmentResource getVpcAttachment() {
        return vpcAttachment;
    }

    public void setVpcAttachment(TransitGatewayVpcAttachmentResource vpcAttachment) {
        this.vpcAttachment = vpcAttachment;
    }

    /**
     * The list of subnets that need to be included in the domain. (Required)
     */
    @Required
    public SubnetResource getSubnet() {
        return subnet;
    }

    public void setSubnet(SubnetResource subnet) {
        this.subnet = subnet;
    }

    @Override
    public void copyFrom(TransitGatewayMulticastDomainAssociation model) {
        setVpcAttachment(findById(TransitGatewayVpcAttachmentResource.class, model.transitGatewayAttachmentId()));
        setSubnet(findById(SubnetResource.class, model.subnet().subnetId()));
    }

    @Override
    public String primaryKey() {
        return String.format("%s in %s", subnet.getId(), vpcAttachment.getId());
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);

        client.associateTransitGatewayMulticastDomain(r -> r.transitGatewayAttachmentId(getVpcAttachment().getId())
                .transitGatewayMulticastDomainId(((TransitGatewayMulticastDomainResource) parent()).getId())
                .subnetIds(getSubnet().getId()));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);

        client.disassociateTransitGatewayMulticastDomain(r -> r.transitGatewayAttachmentId(getVpcAttachment().getId())
                .transitGatewayMulticastDomainId(((TransitGatewayMulticastDomainResource) parent()).getId())
                .subnetIds(getSubnet().getId()));
    }
}
