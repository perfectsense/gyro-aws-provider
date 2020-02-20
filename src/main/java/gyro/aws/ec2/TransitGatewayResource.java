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

import java.util.Collections;
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
import gyro.core.validation.Range;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

/**
 * Creates a transit gateway.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::transit-gateway transit-gateway-example
 *        description: "example transit gateway"
 *        amazon-side-asn: 64512
 *        dns-support: enable
 *        vpn-ecmp-support: enable
 *        default-route-table-association: disable
 *        default-route-table-propagation: enable
 *        auto-accept-shared-attachments: disable
 *
 *        tags: {
 *            Name: "Aws EC2 Transit Gateway Resource Example"
 *        }
 *     end
 */
@Type("transit-gateway")
public class TransitGatewayResource extends Ec2TaggableResource<TransitGateway> implements Copyable<TransitGateway> {

    private Long amazonSideAsn;
    private DnsSupportValue dnsSupport;
    private VpnEcmpSupportValue vpnEcmpSupport;
    private DefaultRouteTableAssociationValue defaultRouteTableAssociation;
    private DefaultRouteTablePropagationValue defaultRouteTablePropagation;
    private AutoAcceptSharedAttachmentsValue autoAcceptSharedAttachments;
    private String description;

    // Read only
    private String id;
    private String arn;
    private String ownerId;
    private String associationDefaultRouteTableId;
    private String propagationDefaultRouteTableId;


    /**
     * A private Autonomous System Number (ASN) for the Amazon side of a BGP session. Valid values belong in between ``64512`` to ``65534`` for a 16-bit ASN or between ``4200000000`` to ``4294967294`` for a 32-bit ASN. Defaults to ``64512``.
     */
    @Range(min=64512, max=65534)
    @Range(min=4200000000L, max=4294967294L)
    public Long getAmazonSideAsn() {
        return amazonSideAsn;
    }

    public void setAmazonSideAsn(Long amazonSideAsn) {
        this.amazonSideAsn = amazonSideAsn;
    }

    /**
     * Enable the VPC to resolve public IPv4 DNS host names to private IPv4 addresses when queried from instances in another VPC attached to the transit gateway. Valid values ``enable`` or ``disable``. Defaults to ``enable``.
     */
    public DnsSupportValue getDnsSupport() {
        return dnsSupport;
    }

    public void setDnsSupport(DnsSupportValue dnsSupport) {
        this.dnsSupport = dnsSupport;
    }

    /**
     * Enable Equal Cost Multipath (ECMP) routing support between VPN connections. Valid values ``enable`` or ``disable``. Defaults to ``enable``.
     */
    public VpnEcmpSupportValue getVpnEcmpSupport() {
        return vpnEcmpSupport;
    }

    public void setVpnEcmpSupport(VpnEcmpSupportValue vpnEcmpSupport) {
        this.vpnEcmpSupport = vpnEcmpSupport;
    }

    /**
     * Enable to automatically associate transit gateway attachments with the default route table for the transit gateway. Valid values ``enable`` or ``disable``. Defaults to ``enable``.
     */
    public DefaultRouteTableAssociationValue getDefaultRouteTableAssociation() {
        return defaultRouteTableAssociation;
    }

    public void setDefaultRouteTableAssociation(DefaultRouteTableAssociationValue defaultRouteTableAssociation) {
        this.defaultRouteTableAssociation = defaultRouteTableAssociation;
    }

    /**
     * Enable to automatically propagate transit gateway attachments to the default route table for the transit gateway. Valid values ``enable`` or ``disable``. Defaults to ``enable``.
     */
    public DefaultRouteTablePropagationValue getDefaultRouteTablePropagation() {
        return defaultRouteTablePropagation;
    }

    public void setDefaultRouteTablePropagation(DefaultRouteTablePropagationValue defaultRouteTablePropagation) {
        this.defaultRouteTablePropagation = defaultRouteTablePropagation;
    }

    /**
     * Enable to automatically accept cross-account attachments. Valid values ``enable`` or ``disable``. Defaults to ``disable``.
     */
    public AutoAcceptSharedAttachmentsValue getAutoAcceptSharedAttachments() {
        return autoAcceptSharedAttachments;
    }

    public void setAutoAcceptSharedAttachments(AutoAcceptSharedAttachmentsValue autoAcceptSharedAttachments) {
        this.autoAcceptSharedAttachments = autoAcceptSharedAttachments;
    }

    /**
     * The description of the transit gateway.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The ID of the transit gateway.
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
     * The Amazon Resource Name (ARN) of the transit gateway.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The ID of the AWS account ID that owns the transit gateway.
     */
    @Output
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * The ID of the default association route table.
     */
    @Output
    public String getAssociationDefaultRouteTableId() {
        return associationDefaultRouteTableId;
    }

    public void setAssociationDefaultRouteTableId(String associationDefaultRouteTableId) {
        this.associationDefaultRouteTableId = associationDefaultRouteTableId;
    }

    /**
     * The ID of the default propagation route table.
     */
    @Output
    public String getPropagationDefaultRouteTableId() {
        return propagationDefaultRouteTableId;
    }

    public void setPropagationDefaultRouteTableId(String propagationDefaultRouteTableId) {
        this.propagationDefaultRouteTableId = propagationDefaultRouteTableId;
    }

    @Override
    protected String getResourceId() {
        return getId();
    }

    @Override
    public void copyFrom(TransitGateway model) {
        copyFrom(model, true);
    }

    @Override
    protected boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);
        TransitGateway gateway = getTransitGateway(client);
        if (gateway == null || gateway.state().equals(TransitGatewayState.DELETING)) {
            return false;
        }
        copyFrom(gateway);

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        TransitGatewayRequestOptions options = TransitGatewayRequestOptions.builder()
                .amazonSideAsn(getAmazonSideAsn())
                .autoAcceptSharedAttachments(getAutoAcceptSharedAttachments())
                .defaultRouteTableAssociation(getDefaultRouteTableAssociation())
                .defaultRouteTablePropagation(getDefaultRouteTablePropagation())
                .dnsSupport(getDnsSupport())
                .vpnEcmpSupport(getVpnEcmpSupport())
                .build();

        CreateTransitGatewayResponse response = client.createTransitGateway(CreateTransitGatewayRequest.builder()
                .description(getDescription())
                .options(options)
                .build()
        );

        // Refreshing the tags before the resource is created (available) would reset the tags to an empty Map since the tags are added after the resource is created (available).
        copyFrom(response.transitGateway(), false);

        Wait.atMost(3, TimeUnit.MINUTES)
                .checkEvery(1, TimeUnit.MINUTES)
                .prompt(false)
                .until(() -> {
                    TransitGateway gateway = getTransitGateway(client);
                    return gateway != null && gateway.state().equals(TransitGatewayState.AVAILABLE);
                });
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);
        client.deleteTransitGateway(DeleteTransitGatewayRequest.builder().transitGatewayId(getId()).build());

        Wait.atMost(3, TimeUnit.MINUTES)
                .checkEvery(1, TimeUnit.MINUTES)
                .prompt(false)
                .until(() -> {
                    return getTransitGateway(client) == null;
                });
    }

    public void acceptTransitGatewayPeeringAttachment(String attachmentId) {
        Ec2Client client = createClient(Ec2Client.class);

        client.acceptTransitGatewayPeeringAttachment(r -> r.transitGatewayAttachmentId(attachmentId));
    }

    private TransitGateway getTransitGateway(Ec2Client client) {
        TransitGateway gateway = null;

        try {
            DescribeTransitGatewaysResponse response = client.describeTransitGateways(r -> r.transitGatewayIds(Collections.singleton(getId())));

            List<TransitGateway> transitGateways = response.transitGateways();
            if (!transitGateways.isEmpty() && !transitGateways.get(0).state().equals(TransitGatewayState.DELETED)) {
                gateway = transitGateways.get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.awsErrorDetails().errorCode().equals("InvalidTransitGatewayID.NotFound")) {
                throw ex;
            }
        }

        return gateway;
    }

    private void copyFrom(TransitGateway model, boolean refreshTags) {
        setId(model.transitGatewayId());
        setOwnerId(model.ownerId());
        setArn(model.transitGatewayArn());
        setAmazonSideAsn(model.options().amazonSideAsn());
        setDescription(model.description());
        setAutoAcceptSharedAttachments(model.options().autoAcceptSharedAttachments());
        setDefaultRouteTableAssociation(model.options().defaultRouteTableAssociation());
        setDefaultRouteTablePropagation(model.options().defaultRouteTablePropagation());
        setDnsSupport(model.options().dnsSupport());
        setVpnEcmpSupport(model.options().vpnEcmpSupport());
        setAssociationDefaultRouteTableId(model.options().associationDefaultRouteTableId());
        setPropagationDefaultRouteTableId(model.options().propagationDefaultRouteTableId());
        if (refreshTags) {
            refreshTags();
        }
    }
}
