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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.TransitGatewayVpcAttachment;

/**
 * Query transit gateway attachment.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    transit-gateway-vpc-attachment: $(external-query aws::transit-gateway-vpc-attachment { tag: { Name: "example-transit-gateway-vpc-attachment" }})
 */
@Type("transit-gateway-vpc-attachment")
public class TransitGatewayVpcAttachmentFinder
    extends Ec2TaggableAwsFinder<Ec2Client, TransitGatewayVpcAttachment, TransitGatewayVpcAttachmentResource> {

    private String state;
    private String transitGatewayId;
    private String transitGatewayAttachmentId;
    private String vpcId;

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
     * The ID of the attachment.
     */
    public String getTransitGatewayAttachmentId() {
        return transitGatewayAttachmentId;
    }

    public void setTransitGatewayAttachmentId(String transitGatewayAttachmentId) {
        this.transitGatewayAttachmentId = transitGatewayAttachmentId;
    }

    /**
     * The ID of the vpc attached to a transit gateway.
     */
    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    @Override
    protected List<TransitGatewayVpcAttachment> findAllAws(Ec2Client client) {
        return client.describeTransitGatewayVpcAttachmentsPaginator()
            .transitGatewayVpcAttachments()
            .stream()
            .collect(Collectors.toList());
    }

    @Override
    protected List<TransitGatewayVpcAttachment> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeTransitGatewayVpcAttachmentsPaginator(r -> r.filters(createFilters(filters)))
            .transitGatewayVpcAttachments()
            .stream()
            .collect(Collectors.toList());
    }
}
