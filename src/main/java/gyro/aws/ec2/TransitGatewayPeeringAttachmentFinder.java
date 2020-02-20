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
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeTransitGatewayPeeringAttachmentsRequest;
import software.amazon.awssdk.services.ec2.model.TransitGatewayPeeringAttachment;

import java.util.List;
import java.util.Map;

/**
 * Query transit gateway peering attachment.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    transit-gateway-peering-attachment: $(external-query aws::transit-gateway-peering-attachment { tag: { Name: "example-transit-gateway-peering-attachment" }})
 */
@Type("transit-gateway-peering-attachment")
public class TransitGatewayPeeringAttachmentFinder extends Ec2TaggableAwsFinder<Ec2Client, TransitGatewayPeeringAttachment, TransitGatewayPeeringAttachmentResource>  {

    private String transitGatewayAttachmentId;

    /**
     * The ID of the transit gateway peering attachment
     */
    public String getTransitGatewayAttachmentId() {
        return transitGatewayAttachmentId;
    }

    public void setTransitGatewayAttachmentId(String transitGatewayAttachmentId) {
        this.transitGatewayAttachmentId = transitGatewayAttachmentId;
    }

    @Override
    protected List<TransitGatewayPeeringAttachment> findAllAws(Ec2Client client) {
        return client.describeTransitGatewayPeeringAttachments(DescribeTransitGatewayPeeringAttachmentsRequest.builder().build()).transitGatewayPeeringAttachments();
    }

    @Override
    protected List<TransitGatewayPeeringAttachment> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeTransitGatewayPeeringAttachments(r -> r.filters(createFilters(filters))).transitGatewayPeeringAttachments();
    }
}
