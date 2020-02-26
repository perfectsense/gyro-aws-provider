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
import software.amazon.awssdk.services.ec2.model.DescribeTransitGatewayMulticastDomainsRequest;
import software.amazon.awssdk.services.ec2.model.TransitGatewayMulticastDomain;

import java.util.List;
import java.util.Map;


/**
 * Query transit gateway multicast domains.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    transit-gateway-multicast-domain: $(external-query aws::transit-gateway-multicast-domain { tag: { Name: "example-transit-gateway-multicast-domain" }})
 */
@Type("transit-gateway-multicast-domain")
public class TransitGatewayMulticastDomainFinder extends Ec2TaggableAwsFinder<Ec2Client, TransitGatewayMulticastDomain, TransitGatewayMulticastDomainResource> {

    private String state;
    private String transitGatewayId;
    private String transitGatewayMulticastDomainId;

    /**
     * The state of the multicast domain. Valid values are ``pending`` or ``available`` or ``deleting`` or ``deleted``.
     */
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * The ID of the transit gateway that processes the multicast traffic.
     */
    public String getTransitGatewayId() {
        return transitGatewayId;
    }

    public void setTransitGatewayId(String transitGatewayId) {
        this.transitGatewayId = transitGatewayId;
    }

    /**
     * The ID of the multicast domain.
     */
    public String getTransitGatewayMulticastDomainId() {
        return transitGatewayMulticastDomainId;
    }

    public void setTransitGatewayMulticastDomainId(String transitGatewayMulticastDomainId) {
        this.transitGatewayMulticastDomainId = transitGatewayMulticastDomainId;
    }

    @Override
    protected List<TransitGatewayMulticastDomain> findAllAws(Ec2Client client) {
        return client.describeTransitGatewayMulticastDomains(DescribeTransitGatewayMulticastDomainsRequest.builder().build()).transitGatewayMulticastDomains();
    }

    @Override
    protected List<TransitGatewayMulticastDomain> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeTransitGatewayMulticastDomains(r -> r.filters(createFilters(filters))).transitGatewayMulticastDomains();
    }
}
