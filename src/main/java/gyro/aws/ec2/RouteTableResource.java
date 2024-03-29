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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.TimeoutSettings;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateRouteTableResponse;
import software.amazon.awssdk.services.ec2.model.DescribeRouteTablesResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Route;
import software.amazon.awssdk.services.ec2.model.RouteTable;
import software.amazon.awssdk.services.ec2.model.RouteTableAssociation;

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
@Type("route-table")
public class RouteTableResource extends Ec2TaggableResource<RouteTable> implements Copyable<RouteTable> {

    private VpcResource vpc;
    private Set<SubnetResource> subnets;
    private Set<RouteResource> route;
    private String id;
    private String ownerId;

    /**
     * The VPC to create a Route Table for.
     */
    @Required
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    /**
     * Subnets to associate with this Route Table.
     */
    @Updatable
    public Set<SubnetResource> getSubnets() {
        if (subnets == null) {
            subnets = new HashSet<>();
        }

        return subnets;
    }

    public void setSubnets(Set<SubnetResource> subnets) {
        this.subnets = subnets;
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
        setVpc(!ObjectUtils.isBlank(routeTable.vpcId()) ? findById(VpcResource.class, routeTable.vpcId()) : null);
        setOwnerId(routeTable.ownerId());

        getSubnets().clear();
        for (RouteTableAssociation rta : routeTable.associations()) {
            if (!rta.main()) {
                getSubnets().add(!ObjectUtils.isBlank(rta.subnetId()) ? findById(SubnetResource.class, rta.subnetId()) : null);
            }
        }

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

        CreateRouteTableResponse response = client.createRouteTable(r -> r.vpcId(getVpc().getId()));

        setId(response.routeTable().routeTableId());
        setOwnerId(response.routeTable().ownerId());

        state.save();

        Wait.atMost(10, TimeUnit.SECONDS)
            .checkEvery(2, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.CREATE)
            .until(() -> getRouteTable(client) != null);

        for (String subnetId : getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList())) {
            client.associateRouteTable(r -> r.routeTableId(getId()).subnetId(subnetId));
        }
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource current, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        RouteTableResource currentResource = (RouteTableResource) current;

        List<String> additions = getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList());
        additions.removeAll(currentResource.getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList()));

        Set<String> subtractions = currentResource.getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toSet());
        subtractions.removeAll(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toSet()));

        for (String subnetId : additions) {
            client.associateRouteTable(r -> r.routeTableId(getId()).subnetId(subnetId));
        }

        RouteTable routeTable = getRouteTable(client);

        List<String> routeTableAssociations = routeTable.associations().stream()
            .filter(o -> !o.main() && subtractions.contains(o.subnetId()))
            .map(RouteTableAssociation::routeTableAssociationId)
            .collect(Collectors.toList());

        routeTableAssociations.forEach(o ->  client.disassociateRouteTable(r -> r.associationId(o)));
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        RouteTable routeTable = getRouteTable(client);

        if (routeTable != null) {
            for (RouteTableAssociation rta : routeTable.associations()) {
                client.disassociateRouteTable(r -> r.associationId(rta.routeTableAssociationId()));
            }

            client.deleteRouteTable(r -> r.routeTableId(getId()));
        }
    }

    private RouteTable getRouteTable(Ec2Client client) {
        RouteTable routeTable = null;

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load route table.");
        }

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
