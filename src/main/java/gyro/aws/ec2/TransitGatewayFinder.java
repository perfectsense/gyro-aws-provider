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

import gyro.core.Type;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.TransitGateway;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query transit gateways.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    transit-gateway: $(external-query aws::transit-gateway { tag: { Name: "example-transit-gateway" }})
 */
@Type("transit-gateway")
public class TransitGatewayFinder extends Ec2TaggableAwsFinder<Ec2Client, TransitGateway, TransitGatewayResource> {

    private String ownerId;
    private String state;
    private String transitGatewayId;
    private String propagationDefaultRouteTableId;
    private String amazonSideAsn;
    private String associationDefaultRouteTableId;
    private String autoAcceptSharedAttachments;
    private String defaultRouteTableAssociation;
    private String defaultRouteTablePropagation;
    private String dnsSupport;
    private String vpnEcmpSupport;

    /**
     * The ID of the AWS account that owns the transit gateway.
     */
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * The state of the attachment. Valid values are ``available``, ``deleted``, ``deleting``, ``failed``, ``modifying``, ``pendingAcceptance``, ``pending``, ``rollingBack``, ``rejected`` or ``rejecting``.
     */
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * The ID of the transit gateway.
     */
    public String getTransitGatewayId() {
        return transitGatewayId;
    }

    public void setTransitGatewayId(String transitGatewayId) {
        this.transitGatewayId = transitGatewayId;
    }

    /**
     * The ID of the default propagation route table.
     */
    @Filter("options.propagation-default-route-table-id")
    public String getPropagationDefaultRouteTableId() {
        return propagationDefaultRouteTableId;
    }

    public void setPropagationDefaultRouteTableId(String propagationDefaultRouteTableId) {
        this.propagationDefaultRouteTableId = propagationDefaultRouteTableId;
    }

    /**
     * The private ASN for the Amazon side of a BGP session.
     */
    @Filter("options.amazon-side-asn")
    public String getAmazonSideAsn() {
        return amazonSideAsn;
    }

    public void setAmazonSideAsn(String amazonSideAsn) {
        this.amazonSideAsn = amazonSideAsn;
    }

    /**
     * The ID of the default association route table.
     */
    @Filter("options.association-default-route-table-id ")
    public String getAssociationDefaultRouteTableId() {
        return associationDefaultRouteTableId;
    }

    public void setAssociationDefaultRouteTableId(String associationDefaultRouteTableId) {
        this.associationDefaultRouteTableId = associationDefaultRouteTableId;
    }

    /**
     * Whether automatic acceptance of attachment requests is enabled. Valid values are ``enable`` or ``disable``.
     */
    @Filter("options.auto-accept-shared-attachments ")
    public String getAutoAcceptSharedAttachments() {
        return autoAcceptSharedAttachments;
    }

    public void setAutoAcceptSharedAttachments(String autoAcceptSharedAttachments) {
        this.autoAcceptSharedAttachments = autoAcceptSharedAttachments;
    }

    /**
     * Whether resource attachments are automatically associated with the default association route table. Valid values are ``enable`` or ``disable``.
     */
    @Filter("options.default-route-table-association")
    public String getDefaultRouteTableAssociation() {
        return defaultRouteTableAssociation;
    }

    public void setDefaultRouteTableAssociation(String defaultRouteTableAssociation) {
        this.defaultRouteTableAssociation = defaultRouteTableAssociation;
    }

    /**
     * Whether resource attachments automatically propagate routes to the default propagation route table. Valid values are ``enable`` or ``disable``.
     */
    @Filter("options.default-route-table-propagation")
    public String getDefaultRouteTablePropagation() {
        return defaultRouteTablePropagation;
    }

    public void setDefaultRouteTablePropagation(String defaultRouteTablePropagation) {
        this.defaultRouteTablePropagation = defaultRouteTablePropagation;
    }

    /**
     * Whether DNS support is enabled. Valid values are ``enable`` or ``disable``.
     */
    @Filter("options.dns-support")
    public String getDnsSupport() {
        return dnsSupport;
    }

    public void setDnsSupport(String dnsSupport) {
        this.dnsSupport = dnsSupport;
    }

    /**
     * Whether Equal Cost Multipath Protocol support is enabled. Valid values are ``enable`` or ``disable``.
     */
    @Filter("options.vpn-ecmp-support")
    public String getVpnEcmpSupport() {
        return vpnEcmpSupport;
    }

    public void setVpnEcmpSupport(String vpnEcmpSupport) {
        this.vpnEcmpSupport = vpnEcmpSupport;
    }

    @Override
    protected List<TransitGateway> findAllAws(Ec2Client client) {
        return client.describeTransitGatewaysPaginator().transitGateways().stream().collect(Collectors.toList());
    }

    @Override
    protected List<TransitGateway> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeTransitGatewaysPaginator(r -> r.filters(createFilters(filters))).transitGateways().stream().collect(Collectors.toList());
    }
}
