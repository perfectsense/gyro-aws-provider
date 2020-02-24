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
import software.amazon.awssdk.services.ec2.model.TransitGatewayRouteTable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query transit gateway route table.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    transit-gateway-route-table: $(external-query aws::transit-gateway-route-table { state: "available" })
 */
@Type("transit-gateway-route-table")
public class TransitGatewayRouteTableFinder extends Ec2TaggableAwsFinder<Ec2Client, TransitGatewayRouteTable, TransitGatewayRouteTableResource> {

    private String transitGatewayRouteTableId;
    private String transitGatewayId;
    private String defaultAssociationRouteTable;
    private String defaultPropagationRouteTable;
    private String state;

    /**
     * The ID of the transit gateway route table.
     */
    public String getTransitGatewayRouteTableId() {
        return transitGatewayRouteTableId;
    }

    public void setTransitGatewayRouteTableId(String transitGatewayRouteTableId) {
        this.transitGatewayRouteTableId = transitGatewayRouteTableId;
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
     * Whether the route table is the default association route table for a transit gateway.
     */
    public String getDefaultAssociationRouteTable() {
        return defaultAssociationRouteTable;
    }

    public void setDefaultAssociationRouteTable(String defaultAssociationRouteTable) {
        this.defaultAssociationRouteTable = defaultAssociationRouteTable;
    }

    /**
     * Whether the route table is the default propagation route table for a transit gateway.
     */
    public String getDefaultPropagationRouteTable() {
        return defaultPropagationRouteTable;
    }

    public void setDefaultPropagationRouteTable(String defaultPropagationRouteTable) {
        this.defaultPropagationRouteTable = defaultPropagationRouteTable;
    }

    /**
     * The state of the transit gateway route table. Valid values are ``available`` or ``deleted`` or ``deleting`` or ``failed`` or ``modifying`` or ``pendingAcceptance`` or ``pending`` or ``rollingBack`` or ``rejected`` or ``rejecting``.
     */
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    protected List<TransitGatewayRouteTable> findAllAws(Ec2Client client) {
        return client.describeTransitGatewayRouteTablesPaginator().transitGatewayRouteTables().stream().collect(Collectors.toList());
    }

    @Override
    protected List<TransitGatewayRouteTable> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeTransitGatewayRouteTablesPaginator(r -> r.filters(createFilters(filters))).transitGatewayRouteTables().stream().collect(Collectors.toList());
    }
}
