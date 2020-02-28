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
import java.util.List;
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
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateTransitGatewayRouteTableResponse;
import software.amazon.awssdk.services.ec2.model.DescribeTransitGatewayRouteTablesResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.TransitGatewayRoute;
import software.amazon.awssdk.services.ec2.model.TransitGatewayRouteTable;
import software.amazon.awssdk.services.ec2.model.TransitGatewayRouteTableAssociation;
import software.amazon.awssdk.services.ec2.model.TransitGatewayRouteTablePropagation;
import software.amazon.awssdk.services.ec2.model.TransitGatewayRouteTableState;

@Type("transit-gateway-route-table")
public class TransitGatewayRouteTableResource extends Ec2TaggableResource<TransitGatewayRouteTable>
    implements Copyable<TransitGatewayRouteTable> {

    private TransitGatewayResource transitGateway;
    private List<TransitGatewayRouteTableAssociationResource> association;
    private List<TransitGatewayRouteTablePropagationResource> propagation;
    private List<TransitGatewayRouteResource> route;

    // Output

    private String id;

    /**
     * The transit gateway of the route table. (Required)
     */
    @Required
    public TransitGatewayResource getTransitGateway() {
        return transitGateway;
    }

    public void setTransitGateway(TransitGatewayResource transitGateway) {
        this.transitGateway = transitGateway;
    }

    /**
     * The attachments to associate with the route table.
     *
     * @subresource gyro.aws.ec2.TransitGatewayRouteTableAssociationResource
     */
    public List<TransitGatewayRouteTableAssociationResource> getAssociation() {
        return association;
    }

    public void setAssociation(List<TransitGatewayRouteTableAssociationResource> association) {
        this.association = association;
    }

    /**
     * The attachments to propagate with the route table.
     *
     * @subresource gyro.aws.ec2.TransitGatewayRouteTablePropagationResource
     */
    public List<TransitGatewayRouteTablePropagationResource> getPropagation() {
        return propagation;
    }

    public void setPropagation(List<TransitGatewayRouteTablePropagationResource> propagation) {
        this.propagation = propagation;
    }

    /**
     * The static routes or blackholes for the route table.
     *
     * @subresource gyro.aws.ec2.TransitGatewayRouteResource
     */
    public List<TransitGatewayRouteResource> getRoute() {
        return route;
    }

    public void setRoute(List<TransitGatewayRouteResource> route) {
        this.route = route;
    }

    /**
     * The ID of the route table.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected String getResourceId() {
        return getId();
    }

    @Override
    public void copyFrom(TransitGatewayRouteTable model) {
        setId(model.transitGatewayRouteTableId());
        setTransitGateway(findById(TransitGatewayResource.class, model.transitGatewayId()));

        Ec2Client client = createClient(Ec2Client.class);

        if (getAssociation() != null) {
            List<TransitGatewayRouteTableAssociation> associations = client.getTransitGatewayRouteTableAssociations(r -> r
                .transitGatewayRouteTableId(getId())).associations();
            getAssociation().clear();
            for (TransitGatewayRouteTableAssociation a : associations) {
                TransitGatewayRouteTableAssociationResource associationResource = newSubresource(
                    TransitGatewayRouteTableAssociationResource.class);
                associationResource.copyFrom(a);

                getAssociation().add(associationResource);
            }
        }

        if (getPropagation() != null) {
            List<TransitGatewayRouteTablePropagation> propagations = client.getTransitGatewayRouteTablePropagations(r -> r
                .transitGatewayRouteTableId(getId())).transitGatewayRouteTablePropagations();
            getPropagation().clear();
            for (TransitGatewayRouteTablePropagation p : propagations) {
                TransitGatewayRouteTablePropagationResource propagationResource = newSubresource(
                    TransitGatewayRouteTablePropagationResource.class);
                propagationResource.copyFrom(p);

                getPropagation().add(propagationResource);
            }
        }

        if (getRoute() != null) {
            List<TransitGatewayRoute> routes = client.searchTransitGatewayRoutes(r -> r.transitGatewayRouteTableId(getId())
                .filters(Collections.singleton(Filter.builder().name("type").values("static").build()))).routes();
            getRoute().clear();
            for (TransitGatewayRoute r : routes) {
                TransitGatewayRouteResource routeResource = newSubresource(TransitGatewayRouteResource.class);
                routeResource.copyFrom(r);

                getRoute().add(routeResource);
            }
        }

        refreshTags();
    }

    @Override
    protected boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        TransitGatewayRouteTable routeTable = getTransitGatewayRouteTable(client);

        if (routeTable == null) {
            return false;
        }

        copyFrom(routeTable);

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        CreateTransitGatewayRouteTableResponse response = client.createTransitGatewayRouteTable(r -> r.transitGatewayId(
            getTransitGateway().getId()));

        setId(response.transitGatewayRouteTable().transitGatewayRouteTableId());

        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(30, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> {
                TransitGatewayRouteTable routeTable = getTransitGatewayRouteTable(client);
                return routeTable != null && routeTable.state().equals(TransitGatewayRouteTableState.AVAILABLE);
            });
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);
        client.deleteTransitGatewayRouteTable(r -> r.transitGatewayRouteTableId(getId()));

        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(30, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> getTransitGateway() == null);
    }

    private TransitGatewayRouteTable getTransitGatewayRouteTable(Ec2Client client) {
        TransitGatewayRouteTable routeTable = null;

        try {
            DescribeTransitGatewayRouteTablesResponse response = client.describeTransitGatewayRouteTables(r -> r.transitGatewayRouteTableIds(
                Collections.singleton(getId())));

            List<TransitGatewayRouteTable> transitGatewayRouteTables = response.transitGatewayRouteTables();
            if (!transitGatewayRouteTables.isEmpty() && !transitGatewayRouteTables.get(0)
                .state()
                .equals(TransitGatewayRouteTableState.DELETED)) {
                routeTable = transitGatewayRouteTables.get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.awsErrorDetails().errorCode().equals("InvalidTransitGatewayRouteTableID.NotFound")) {
                throw ex;
            }
        }

        return routeTable;
    }
}
