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
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
public class TransitGatewayVpcAttachmentResource extends Ec2TaggableResource<TransitGatewayVpcAttachment> implements Copyable<TransitGatewayVpcAttachment> {

    private List<SubnetResource> subnets;
    private TransitGatewayResource transitGateway;
    private VpcResource vpc;
    private DnsSupportValue dnsSupport;
    private Ipv6SupportValue ipv6Support;

    // Output

    private String id;

    /**
     * List of subnets for the availability zone that the transit gateway uses. Limited to one subnet per availability zone. (Required)
     */
    @Required
    @Updatable
    public List<SubnetResource> getSubnets() {
        return subnets;
    }

    public void setSubnets(List<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    /**
     * The transit gateway for this attachment. (Required)
     */
    @Required
    public TransitGatewayResource getTransitGateway() {
        return transitGateway;
    }

    public void setTransitGateway(TransitGatewayResource transitGateway) {
        this.transitGateway = transitGateway;
    }

    /**
     * The VPC to attach to the transit gateway selected. This VPC should have at least one subnet associated with it. (Required)
     */
    @Required
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    /**
     * Enable DNS resolution for the attachment. Valid values are ``enable`` or ``disable``. Defaults to ``enable``.
     */
    @Updatable
    public DnsSupportValue getDnsSupport() {
        return dnsSupport;
    }

    public void setDnsSupport(DnsSupportValue dnsSupport) {
        this.dnsSupport = dnsSupport;
    }

    /**
     * Enable support for ipv6 block support for the attachment. Valid values are ``enable`` or ``disable``. Defaults to ``disable``.
     */
    public Ipv6SupportValue getIpv6Support() {
        return ipv6Support;
    }

    public void setIpv6Support(Ipv6SupportValue ipv6Support) {
        this.ipv6Support = ipv6Support;
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

    @Override
    protected String getResourceId() {
        return getId();
    }

    @Override
    public void copyFrom(TransitGatewayVpcAttachment model) {copyFrom(model, true); }

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
                .subnetIds(getSubnets().stream().map(s -> s.getId()).collect(Collectors.toList()))
                .transitGatewayId(getTransitGateway().getId())
                .vpcId(getVpc().getId())
                .options(s -> s.dnsSupport(getDnsSupport()).ipv6Support(getIpv6Support())));

        copyFrom(response.transitGatewayVpcAttachment(), false);

        waitForAvailability(2, TimeUnit.MINUTES, 30, TimeUnit.SECONDS, client);
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);
        if (changedProperties.contains("dns-support")) {
            client.modifyTransitGatewayVpcAttachment(r -> r.transitGatewayAttachmentId(getId())
                    .options(s -> s.dnsSupport(getDnsSupport())));
            waitForAvailability(2, TimeUnit.SECONDS, 30, TimeUnit.SECONDS, client);
        }

        if (changedProperties.contains("subnets")) {
            List<String> previousAttachmentSubnetIds = ((TransitGatewayVpcAttachmentResource) config).getSubnets().stream().map(s -> s.getId()).collect(Collectors.toList());
            List<String> currentAttachmentSubnetIds = getSubnets().stream().map(s -> s.getId()).collect(Collectors.toList());
            Set<String> subnetsToAdd = new HashSet<String>(currentAttachmentSubnetIds);
            subnetsToAdd.removeAll(previousAttachmentSubnetIds);
            Set<String> subnetsToRemove = new HashSet<String>(previousAttachmentSubnetIds);
            subnetsToRemove.removeAll(currentAttachmentSubnetIds);

            if (!subnetsToAdd.isEmpty() && !subnetsToRemove.isEmpty()) {
                client.modifyTransitGatewayVpcAttachment(r -> r.transitGatewayAttachmentId(getId())
                        .addSubnetIds(subnetsToAdd).removeSubnetIds(subnetsToRemove));

                waitForAvailability(2, TimeUnit.MINUTES, 30, TimeUnit.SECONDS, client);

            } else if (!subnetsToAdd.isEmpty()) {
                client.modifyTransitGatewayVpcAttachment(r -> r.transitGatewayAttachmentId(getId())
                        .addSubnetIds(subnetsToAdd));

                waitForAvailability(2, TimeUnit.MINUTES, 30, TimeUnit.SECONDS, client);

            } else if (!subnetsToRemove.isEmpty()) {
                client.modifyTransitGatewayVpcAttachment(r -> r.transitGatewayAttachmentId(getId())
                        .removeSubnetIds(subnetsToRemove));

                waitForAvailability(2, TimeUnit.MINUTES, 30, TimeUnit.SECONDS, client);
            }
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);
        client.deleteTransitGatewayVpcAttachment(r -> r.transitGatewayAttachmentId(getId()));

        Wait.atMost(2, TimeUnit.MINUTES)
                .checkEvery(30, TimeUnit.SECONDS)
                .prompt(false)
                .until(() -> getTransitGatewayVpcAttachment(client) == null);
    }

    private TransitGatewayVpcAttachment getTransitGatewayVpcAttachment(Ec2Client client) {
        TransitGatewayVpcAttachment attachment = null;

        try {
            DescribeTransitGatewayVpcAttachmentsResponse response = client.describeTransitGatewayVpcAttachments(r -> r.transitGatewayAttachmentIds(getId()));

            List<TransitGatewayVpcAttachment> transitGatewayVpcAttachments = response.transitGatewayVpcAttachments();
            if (!transitGatewayVpcAttachments.isEmpty() && !transitGatewayVpcAttachments.get(0).state().equals(TransitGatewayAttachmentState.DELETED)) {
                attachment = transitGatewayVpcAttachments.get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.awsErrorDetails().errorCode().equals("InvalidTransitGatewayVpcAttachmentId.NotFound")) {
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
        setDnsSupport(model.options().dnsSupport());
        setIpv6Support(model.options().ipv6Support());
        if (refreshTags) {
            refreshTags();
        }
    }

    private void waitForAvailability(Integer duration, TimeUnit durationUnit, Integer interval, TimeUnit intervalUnit, Ec2Client client) {
        Wait.atMost(duration, durationUnit)
                .checkEvery(interval, intervalUnit)
                .prompt(false)
                .until(() -> {
                    TransitGatewayVpcAttachment attachment = getTransitGatewayVpcAttachment(client);
                    return attachment != null && attachment.state().equals(TransitGatewayAttachmentState.AVAILABLE);
                });
    }
}
