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
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateTransitGatewayMulticastDomainResponse;
import software.amazon.awssdk.services.ec2.model.DescribeTransitGatewayMulticastDomainsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.TransitGatewayAttachmentState;
import software.amazon.awssdk.services.ec2.model.TransitGatewayMulticastDomain;
import software.amazon.awssdk.services.ec2.model.TransitGatewayMulticastDomainAssociation;
import software.amazon.awssdk.services.ec2.model.TransitGatewayMulticastDomainState;
import software.amazon.awssdk.services.ec2.model.TransitGatewayMulticastGroup;

/**
 * Creates a transit gateway multicast domain.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::transit-gateway-multicast-domain transit-gateway-multicast-domain-example
 *         transit-gateway: $(aws::transit-gateway example-transit-gateway)
 *
 *         association
 *             vpc-attachment: $(aws::transit-gateway-vpc-attachment transit-gateway-vpc-attachment-example)
 *             subnet: $(aws::subnet example-subnet-1)
 *         end
 *
 *         group-member
 *             group-ip-address: "224.0.0.0"
 *             network-interface: $(aws::network-interface example-network-interface)
 *         end
 *     end
 */
@Type("transit-gateway-multicast-domain")
public class TransitGatewayMulticastDomainResource extends Ec2TaggableResource<TransitGatewayMulticastDomain>
    implements Copyable<TransitGatewayMulticastDomain> {

    private TransitGatewayResource transitGateway;
    private List<TransitGatewayMulticastDomainAssociationResource> association;
    private List<TransitGatewayMulticastDomainGroupMemberResource> groupMember;
    private List<TransitGatewayMulticastDomainGroupSourceResource> groupSource;

    // Output

    private String id;

    /**
     * The transit gateway that processes the multicast traffic. (Required)
     */
    @Required
    public TransitGatewayResource getTransitGateway() {
        return transitGateway;
    }

    public void setTransitGateway(TransitGatewayResource transitGateway) {
        this.transitGateway = transitGateway;
    }

    /**
     * The attachments and subnets to associate with the multicast domain.
     *
     * @subresource gyro.aws.ec2.TransitGatewayMulticastDomainAssociationResource
     */
    public List<TransitGatewayMulticastDomainAssociationResource> getAssociation() {
        if (association == null) {
            association = new ArrayList<TransitGatewayMulticastDomainAssociationResource>();
        }
        return association;
    }

    public void setAssociation(List<TransitGatewayMulticastDomainAssociationResource> association) {
        this.association = association;
    }

    /**
     * The network interfaces that receive the multicast traffic.
     *
     * @subresource gyro.aws.ec2.TransitGatewayMulticastDomainGroupMemberResource
     */
    public List<TransitGatewayMulticastDomainGroupMemberResource> getGroupMember() {
        if (groupMember == null) {
            groupMember = new ArrayList<TransitGatewayMulticastDomainGroupMemberResource>();
        }
        return groupMember;
    }

    public void setGroupMember(List<TransitGatewayMulticastDomainGroupMemberResource> groupMember) {
        this.groupMember = groupMember;
    }

    /**
     * The network interfaces that send the multicast traffic.
     *
     * @subresource gyro.aws.ec2.TransitGatewayMulticastDomainGroupSourceResource
     */
    public List<TransitGatewayMulticastDomainGroupSourceResource> getGroupSource() {
        if (groupSource == null) {
            groupSource = new ArrayList<TransitGatewayMulticastDomainGroupSourceResource>();
        }
        return groupSource;
    }

    public void setGroupSource(List<TransitGatewayMulticastDomainGroupSourceResource> groupSource) {
        this.groupSource = groupSource;
    }

    /**
     * The ID of the multicast domain.
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
    public void copyFrom(TransitGatewayMulticastDomain model) {
        setTransitGateway(findById(TransitGatewayResource.class, model.transitGatewayId()));
        setId(model.transitGatewayMulticastDomainId());

        Ec2Client client = createClient(Ec2Client.class);

        List<TransitGatewayMulticastDomainAssociation> associations = client.getTransitGatewayMulticastDomainAssociations(
            r -> r.transitGatewayMulticastDomainId(getId())).multicastDomainAssociations();
        getAssociation().clear();
        for (TransitGatewayMulticastDomainAssociation a : associations) {
            TransitGatewayMulticastDomainAssociationResource associationResource = newSubresource(
                TransitGatewayMulticastDomainAssociationResource.class);
            associationResource.copyFrom(a);

            getAssociation().add(associationResource);
        }

        List<TransitGatewayMulticastGroup> groupMembers = client.searchTransitGatewayMulticastGroups(r -> r.transitGatewayMulticastDomainId(
            getId()).filters(Filter.builder().name("is-group-member").values("true").build())).multicastGroups();
        getGroupMember().clear();
        for (TransitGatewayMulticastGroup g : groupMembers) {
            TransitGatewayMulticastDomainGroupMemberResource groupMemberResource = newSubresource(
                TransitGatewayMulticastDomainGroupMemberResource.class);
            groupMemberResource.copyFrom(g);

            getGroupMember().add(groupMemberResource);
        }

        List<TransitGatewayMulticastGroup> groupSources = client.searchTransitGatewayMulticastGroups(r -> r.transitGatewayMulticastDomainId(
            getId()).filters(Filter.builder().name("is-group-source").values("true").build())).multicastGroups();
        getGroupSource().clear();
        for (TransitGatewayMulticastGroup g : groupSources) {
            TransitGatewayMulticastDomainGroupSourceResource groupSourceResource = newSubresource(
                TransitGatewayMulticastDomainGroupSourceResource.class);
            groupSourceResource.copyFrom(g);

            getGroupSource().add(groupSourceResource);
        }

        refreshTags();
    }

    @Override
    protected boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        TransitGatewayMulticastDomain domain = getTransitGatewayMulticastDomain(client);

        if (domain == null) {
            return false;
        }

        copyFrom(domain);

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        CreateTransitGatewayMulticastDomainResponse response = client.createTransitGatewayMulticastDomain(r -> r.transitGatewayId(
            getTransitGateway().getId()));
        setId(response.transitGatewayMulticastDomain().transitGatewayMulticastDomainId());

        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(30, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> {
                TransitGatewayMulticastDomain domain = getTransitGatewayMulticastDomain(client);
                return domain != null && domain.state().equals(TransitGatewayMulticastDomainState.AVAILABLE);
            });
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteTransitGatewayMulticastDomain(r -> r.transitGatewayMulticastDomainId(getId()));

        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(30, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> getTransitGatewayMulticastDomain(client) == null);
    }

    private TransitGatewayMulticastDomain getTransitGatewayMulticastDomain(Ec2Client client) {
        TransitGatewayMulticastDomain domain = null;

        try {
            DescribeTransitGatewayMulticastDomainsResponse response = client.describeTransitGatewayMulticastDomains(r -> r
                .transitGatewayMulticastDomainIds(getId()));

            List<TransitGatewayMulticastDomain> transitGatewayMulticastDomains = response.transitGatewayMulticastDomains();
            if (!transitGatewayMulticastDomains.isEmpty() && !transitGatewayMulticastDomains.get(0)
                .state()
                .equals(TransitGatewayAttachmentState.DELETED)) {
                domain = transitGatewayMulticastDomains.get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.awsErrorDetails().errorCode().equals("InvalidTransitGatewayMulticastDomainId.NotFound")) {
                throw ex;
            }
        }

        return domain;
    }
}
