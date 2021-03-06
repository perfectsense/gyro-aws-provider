/*
 * Copyright 2019, Perfect Sense, Inc.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gyro.core.Type;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.VpnGateway;

/**
 * Query vpn gateway.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    vpn-gateway: $(external-query aws::vpn-gateway { vpn-gateway-id: ''})
 */
@Type("vpn-gateway")
public class VpnGatewayFinder extends Ec2TaggableAwsFinder<Ec2Client, VpnGateway, VpnGatewayResource> {

    private String amazonSideAsn;
    private String attachmentState;
    private String attachmentVpcId;
    private String availabilityZone;
    private String state;
    private Map<String, String> tag;
    private String tagKey;
    private String type;
    private String vpnGatewayId;

    /**
     * The Autonomous System Number (ASN) for the Amazon side of the gateway.
     */
    public String getAmazonSideAsn() {
        return amazonSideAsn;
    }

    public void setAmazonSideAsn(String amazonSideAsn) {
        this.amazonSideAsn = amazonSideAsn;
    }

    /**
     * The current state of the attachment between the gateway and the VPC . Valid values are ``attaching`` or ``attached`` or ``detaching`` or ``detached``.
     */
    @Filter("attachment.state")
    public String getAttachmentState() {
        return attachmentState;
    }

    public void setAttachmentState(String attachmentState) {
        this.attachmentState = attachmentState;
    }

    /**
     * The ID of an attached VPC.
     */
    @Filter("attachment.vpc-id")
    public String getAttachmentVpcId() {
        return attachmentVpcId;
    }

    public void setAttachmentVpcId(String attachmentVpcId) {
        this.attachmentVpcId = attachmentVpcId;
    }

    /**
     * The Availability Zone for the virtual private gateway (if applicable).
     */
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * The state of the virtual private gateway . Valid values are ``pending`` or ``available`` or ``deleting`` or ``deleted``.
     */
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * The key/value combination of a tag assigned to the resource.
     */
    public Map<String, String> getTag() {
        if (tag == null) {
            tag = new HashMap<>();
        }

        return tag;
    }

    public void setTag(Map<String, String> tag) {
        this.tag = tag;
    }

    /**
     * The key of a tag assigned to the resource. Use this filter to find all resources assigned a tag with a specific key, regardless of the tag value.
     */
    public String getTagKey() {
        return tagKey;
    }

    public void setTagKey(String tagKey) {
        this.tagKey = tagKey;
    }

    /**
     * The type of virtual private gateway. Currently the only supported type is ``ipsec.1``.
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The ID of the virtual private gateway.
     */
    public String getVpnGatewayId() {
        return vpnGatewayId;
    }

    public void setVpnGatewayId(String vpnGatewayId) {
        this.vpnGatewayId = vpnGatewayId;
    }

    @Override
    protected List<VpnGateway> findAllAws(Ec2Client client) {
        return client.describeVpnGateways().vpnGateways();
    }

    @Override
    protected List<VpnGateway> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeVpnGateways(r -> r.filters(createFilters(filters))).vpnGateways();
    }
}
