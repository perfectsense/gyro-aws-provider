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

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.*;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Creates a VPC route table.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::route-table route-table-example
 *         vpc: $(aws::vpc vpc-example)
 *
 *         route
 *             destination-cidr-block: 0.0.0.0/0
 *             gateway: $(aws::internet-gateway ig-example)
 *         end
 *
 *         tags: {
 *             Name: route-table-example
 *         }
 *     end
 */
@Type("route-table-route")
public class RouteTableRouteResource extends Ec2TaggableResource<RouteTable> implements Copyable<RouteTable> {

    private RouteTableResource routeTable;
    private Set<RouteResource> route;
    private String id;
    private String ownerId;

    /**
     * The Route Table to create a route for.
     */
    @Required
    public RouteTableResource getRouteTable() {
        return routeTable;
    }

    public void setRouteTable(RouteTableResource routeTable) {
        this.routeTable = routeTable;
    }

    /**
     * The routes for the Route Table.
     *
     * @subresource gyro.aws.ec2.RouteResource
     */
    @Updatable
    public Set<RouteResource> getRoute() {
        if (route == null) {
            route = new HashSet<>();
        }

        return route;
    }

    public void setRoute(Set<RouteResource> route) {
        this.route = route;
    }

    /**
     * The ID of the Route Table.
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
     * The owner ID of the Route Table.
     */
    @Output
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    protected String getResourceId() {
        return getId();
    }

    @Override
    public void copyFrom(RouteTable routeTable) {
        setId(routeTable.routeTableId());
        setRouteTable(findById(RouteTableResource.class, routeTable.routeTableId()));
        setOwnerId(routeTable.ownerId());

        getRoute().clear();
        for (Route route : routeTable.routes()) {
            if ("local".equals(route.gatewayId())) {
                continue;
            }

            if (route.destinationPrefixListId() != null) {
                continue;
            }
            RouteResource routeResource = newSubresource(RouteResource.class);
            routeResource.copyFrom(route);
            getRoute().add(routeResource);
        }
        refreshTags();
    }

    @Override
    public boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);
        RouteTable routeTable = getRouteTable(client);
        if (routeTable == null) {
            return false;
        }
        copyFrom(routeTable);
        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);
        getRouteTable(client);
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource current, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);
        getRouteTable(client);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);
        getRouteTable(client);
    }

    private RouteTable getRouteTable(Ec2Client client) {
        RouteTable routeTable = null;
        setId(this.routeTable.getId());
        setOwnerId(this.routeTable.getOwnerId());

        try {
            DescribeRouteTablesResponse response = client.describeRouteTables(r -> r.filters(
                Filter.builder().name("route-table-id").values(getId()).build()
            ));

            if (!response.routeTables().isEmpty()) {
                routeTable = response.routeTables().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return routeTable;
    }
}
