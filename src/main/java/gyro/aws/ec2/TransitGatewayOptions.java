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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Range;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.ec2.model.AutoAcceptSharedAttachmentsValue;
import software.amazon.awssdk.services.ec2.model.DefaultRouteTableAssociationValue;
import software.amazon.awssdk.services.ec2.model.DefaultRouteTablePropagationValue;
import software.amazon.awssdk.services.ec2.model.DnsSupportValue;
import software.amazon.awssdk.services.ec2.model.MulticastSupportValue;
import software.amazon.awssdk.services.ec2.model.TransitGatewayRequestOptions;
import software.amazon.awssdk.services.ec2.model.VpnEcmpSupportValue;

public class TransitGatewayOptions
    extends Diffable implements Copyable<software.amazon.awssdk.services.ec2.model.TransitGatewayOptions> {

    private Long amazonSideAsn;
    private DnsSupportValue dnsSupport;
    private VpnEcmpSupportValue vpnEcmpSupport;
    private DefaultRouteTableAssociationValue defaultRouteTableAssociation;
    private DefaultRouteTablePropagationValue defaultRouteTablePropagation;
    private AutoAcceptSharedAttachmentsValue autoAcceptSharedAttachments;
    private MulticastSupportValue multicastSupport;

    /**
     * A private Autonomous System Number (ASN) for the Amazon side of a BGP session. Defaults to ``64512``.
     *
     * @no-doc Range
     */
    @Range(min = 64512, max = 65534)
    @Range(min = 4200000000L, max = 4294967294L)
    public Long getAmazonSideAsn() {
        return amazonSideAsn;
    }

    public void setAmazonSideAsn(Long amazonSideAsn) {
        this.amazonSideAsn = amazonSideAsn;
    }

    /**
     * Enable the VPC to resolve public IPv4 DNS host names to private IPv4 addresses when queried from instances in another VPC attached to the transit gateway. Defaults to ``enable``.
     */
    @ValidStrings({"enable", "disable"})
    public DnsSupportValue getDnsSupport() {
        return dnsSupport;
    }

    public void setDnsSupport(DnsSupportValue dnsSupport) {
        this.dnsSupport = dnsSupport;
    }

    /**
     * Enable Equal Cost Multipath (ECMP) routing support between VPN connections. Defaults to ``enable``.
     */
    @ValidStrings({"enable", "disable"})
    public VpnEcmpSupportValue getVpnEcmpSupport() {
        return vpnEcmpSupport;
    }

    public void setVpnEcmpSupport(VpnEcmpSupportValue vpnEcmpSupport) {
        this.vpnEcmpSupport = vpnEcmpSupport;
    }

    /**
     * Enable to automatically associate transit gateway attachments with the default route table for the transit gateway. Defaults to ``enable``.
     */
    @ValidStrings({"enable", "disable"})
    public DefaultRouteTableAssociationValue getDefaultRouteTableAssociation() {
        return defaultRouteTableAssociation;
    }

    public void setDefaultRouteTableAssociation(DefaultRouteTableAssociationValue defaultRouteTableAssociation) {
        this.defaultRouteTableAssociation = defaultRouteTableAssociation;
    }

    /**
     * Enable to automatically propagate transit gateway attachments to the default route table for the transit gateway. Defaults to ``enable``.
     */
    @ValidStrings({"enable", "disable"})
    public DefaultRouteTablePropagationValue getDefaultRouteTablePropagation() {
        return defaultRouteTablePropagation;
    }

    public void setDefaultRouteTablePropagation(DefaultRouteTablePropagationValue defaultRouteTablePropagation) {
        this.defaultRouteTablePropagation = defaultRouteTablePropagation;
    }

    /**
     * Enable to automatically accept cross-account attachments. Defaults to ``disable``.
     */
    @ValidStrings({"enable", "disable"})
    public AutoAcceptSharedAttachmentsValue getAutoAcceptSharedAttachments() {
        return autoAcceptSharedAttachments;
    }

    public void setAutoAcceptSharedAttachments(AutoAcceptSharedAttachmentsValue autoAcceptSharedAttachments) {
        this.autoAcceptSharedAttachments = autoAcceptSharedAttachments;
    }

    /**
     * Enable to support Multicast communication protocol. Defaults to ``disable``.
     */
    @ValidStrings({"enable", "disable"})
    public MulticastSupportValue getMulticastSupport() {
        return multicastSupport;
    }

    public void setMulticastSupport(MulticastSupportValue multicastSupport) {
        this.multicastSupport = multicastSupport;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.ec2.model.TransitGatewayOptions model) {
        setAmazonSideAsn(model.amazonSideAsn());
        setAutoAcceptSharedAttachments(model.autoAcceptSharedAttachments());
        setDefaultRouteTableAssociation(model.defaultRouteTableAssociation());
        setDefaultRouteTablePropagation(model.defaultRouteTablePropagation());
        setMulticastSupport(model.multicastSupport());
        setDnsSupport(model.dnsSupport());
        setVpnEcmpSupport(model.vpnEcmpSupport());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    TransitGatewayRequestOptions transitGatewayRequestOptions() {
        return TransitGatewayRequestOptions.builder()
            .amazonSideAsn(getAmazonSideAsn())
            .autoAcceptSharedAttachments(getAutoAcceptSharedAttachments())
            .defaultRouteTableAssociation(getDefaultRouteTableAssociation())
            .defaultRouteTablePropagation(getDefaultRouteTablePropagation())
            .dnsSupport(getDnsSupport())
            .vpnEcmpSupport(getVpnEcmpSupport())
            .multicastSupport(getMulticastSupport())
            .build();
    }
}
