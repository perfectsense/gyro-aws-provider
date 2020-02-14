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
 *         amazonSideAsn: "64513"
 *         enable-dns-support: true
 *         description "new example transit gateway"
 *     end
 */
@Type("transit-gateway")
public class TransitGatewayResource extends Ec2TaggableResource<TransitGateway> implements Copyable<TransitGateway> {

    private Long amazonSideAsn;
    private DnsSupportValue enableDnsSupport;
    private VpnEcmpSupportValue enableVpnEcmpSupport;
    private DefaultRouteTableAssociationValue enableDefaultRouteTableAssoc;
    private DefaultRouteTablePropagationValue enableDefaultRouteTableProp;
    private AutoAcceptSharedAttachmentsValue enableAutoAcceptSharedAttach;
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
    public DnsSupportValue getEnableDnsSupport() {
        return enableDnsSupport;
    }

    public void setEnableDnsSupport(DnsSupportValue enableDnsSupport) {
        this.enableDnsSupport = enableDnsSupport;
    }

    /**
     * Enable Equal Cost Multipath (ECMP) routing support between VPN connections. Valid values ``enable`` or ``disable``. Defaults to ``enable``.
     */
    public VpnEcmpSupportValue getEnableVpnEcmpSupport() {
        return enableVpnEcmpSupport;
    }

    public void setEnableVpnEcmpSupport(VpnEcmpSupportValue enableVpnEcmpSupport) {
        this.enableVpnEcmpSupport = enableVpnEcmpSupport;
    }

    /**
     * Enable to automatically associate transit gateway attachments with the default route table for the transit gateway. Valid values ``enable`` or ``disable``. Defaults to ``enable``.
     */
    public DefaultRouteTableAssociationValue getEnableDefaultRouteTableAssoc() {
        return enableDefaultRouteTableAssoc;
    }

    public void setEnableDefaultRouteTableAssoc(DefaultRouteTableAssociationValue enableDefaultRouteTableAssoc) {
        this.enableDefaultRouteTableAssoc = enableDefaultRouteTableAssoc;
    }

    /**
     * Enable to automatically propagate transit gateway attachments to the default route table for the transit gateway. Valid values ``enable`` or ``disable``. Defaults to ``enable``.
     */
    public DefaultRouteTablePropagationValue getEnableDefaultRouteTableProp() {
        return enableDefaultRouteTableProp;
    }

    public void setEnableDefaultRouteTableProp(DefaultRouteTablePropagationValue enableDefaultRouteTableProp) {
        this.enableDefaultRouteTableProp = enableDefaultRouteTableProp;
    }

    /**
     * Enable to automatically accept cross-account attachments. Valid values ``enable`` or ``disable``. Defaults to ``disable``.
     */
    public AutoAcceptSharedAttachmentsValue getEnableAutoAcceptSharedAttach() {
        return enableAutoAcceptSharedAttach;
    }

    public void setEnableAutoAcceptSharedAttach(AutoAcceptSharedAttachmentsValue enableAutoAcceptSharedAttach) {
        this.enableAutoAcceptSharedAttach = enableAutoAcceptSharedAttach;
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
        setId(model.transitGatewayId());
        setOwnerId(model.ownerId());
        setArn(model.transitGatewayArn());
        setAmazonSideAsn(model.options().amazonSideAsn());
        setDescription(model.description());
        setEnableAutoAcceptSharedAttach(model.options().autoAcceptSharedAttachments());
        setEnableDefaultRouteTableAssoc(model.options().defaultRouteTableAssociation());
        setEnableDefaultRouteTableProp(model.options().defaultRouteTablePropagation());
        setEnableDnsSupport(model.options().dnsSupport());
        setEnableVpnEcmpSupport(model.options().vpnEcmpSupport());
        setAssociationDefaultRouteTableId(model.options().associationDefaultRouteTableId());
        setPropagationDefaultRouteTableId(model.options().propagationDefaultRouteTableId());
        refreshTags();
    }

    @Override
    protected boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);
        DescribeTransitGatewaysResponse response = client.describeTransitGateways(r -> r.transitGatewayIds(Collections.singleton(getId())));
        if (response.transitGateways().isEmpty()) {
            return false;
        }
        copyFrom(response.transitGateways().get(0));
        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        TransitGatewayRequestOptions options = TransitGatewayRequestOptions.builder()
                .amazonSideAsn(getAmazonSideAsn())
                .autoAcceptSharedAttachments(getEnableAutoAcceptSharedAttach())
                .defaultRouteTableAssociation(getEnableDefaultRouteTableAssoc())
                .defaultRouteTablePropagation(getEnableDefaultRouteTableProp())
                .dnsSupport(getEnableDnsSupport())
                .vpnEcmpSupport(getEnableVpnEcmpSupport())
                .build();

        CreateTransitGatewayResponse response = client.createTransitGateway(CreateTransitGatewayRequest.builder()
                .description(getDescription())
                .options(options)
                .build()
        );

        copyFrom(response.transitGateway());

        boolean waitResult = Wait.atMost(3, TimeUnit.MINUTES)
                .checkEvery(10, TimeUnit.SECONDS)
                .prompt(false)
                .until(() -> checkState(TransitGatewayState.AVAILABLE, client));
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);
        client.deleteTransitGateway(DeleteTransitGatewayRequest.builder().transitGatewayId(getId()).build());

        boolean waitResult = Wait.atMost(3, TimeUnit.MINUTES)
                .checkEvery(10, TimeUnit.SECONDS)
                .prompt(false)
                .until(() -> checkState(TransitGatewayState.DELETED, client));
    }

    private boolean checkState(TransitGatewayState state, Ec2Client client) {
        TransitGateway gateway = client.describeTransitGateways(r -> r.transitGatewayIds(Collections.singleton(getId()))).transitGateways().get(0);
        return gateway.state().equals(state);
    }
}
