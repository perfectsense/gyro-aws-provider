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

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Route;

import java.util.Set;

/**
 * Add a route to a route table. `See Route Tables <https://docs.aws.amazon.com/vpc/latest/userguide/VPC_Route_Tables.html/>`_.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     route
 *         destination-cidr-block: 0.0.0.0/0
 *         gateway: $(aws::internet-gateway ig-example)
 *     end
 */
public class RouteResource extends AwsResource implements Copyable<Route> {

    private String destinationCidrBlock;
    private String destinationIpv6CidrBlock;
    private String destinationPrefixListId;
    private EgressOnlyInternetGatewayResource egressGateway;
    private InternetGatewayResource gateway;
    private InstanceResource instance;
    private NatGatewayResource natGateway;
    private NetworkInterfaceResource networkInterface;
    private String transitGatewayId;
    private PeeringConnectionResource vpcPeeringConnection;

    /**
     * An IPv4 destination CIDR block of the Route.
     */
    public String getDestinationCidrBlock() {
        return destinationCidrBlock;
    }

    public void setDestinationCidrBlock(String destinationCidrBlock) {
        this.destinationCidrBlock = destinationCidrBlock;
    }

    /**
     * An IPv6 destination CIDR block of the Route.
     */
    public String getDestinationIpv6CidrBlock() {
        return destinationIpv6CidrBlock;
    }

    public void setDestinationIpv6CidrBlock(String destinationIpv6CidrBlock) {
        this.destinationIpv6CidrBlock = destinationIpv6CidrBlock;
    }

    public String getDestinationPrefixListId() {
        return destinationPrefixListId;
    }

    public void setDestinationPrefixListId(String destinationPrefixListId) {
        this.destinationPrefixListId = destinationPrefixListId;
    }

    /**
     * Associate an Egress Only Internet Gateway to the Route. Only valid with IPv6 destination CIDR.
     */
    public EgressOnlyInternetGatewayResource getEgressGateway() {
        return egressGateway;
    }

    public void setEgressGateway(EgressOnlyInternetGatewayResource egressGateway) {
        this.egressGateway = egressGateway;
    }

    /**
     * Associate an Internet Gateway to the Route. Only valid with IPv4 destination CIDR.
     */
    public InternetGatewayResource getGateway() {
        return gateway;
    }

    public void setGateway(InternetGatewayResource gateway) {
        this.gateway = gateway;
    }

    /**
     * Associate a NAT instance running in your VPC to the Route.
     */
    public InstanceResource getInstance() {
        return instance;
    }

    public void setInstance(InstanceResource instance) {
        this.instance = instance;
    }

    /**
     * Associate a NAT Gateway to the Route. Only valid with IPv4 destination CIDR.
     */
    public NatGatewayResource getNatGateway() {
        return natGateway;
    }

    public void setNatGateway(NatGatewayResource natGateway) {
        this.natGateway = natGateway;
    }

    /**
     * Associate a Network Interface to the Route.
     */
    public NetworkInterfaceResource getNetworkInterface() {
        return networkInterface;
    }

    public void setNetworkInterface(NetworkInterfaceResource networkInterface) {
        this.networkInterface = networkInterface;
    }

    /**
     * The ID of a transit gateway to associate to the Route.
     */
    public String getTransitGatewayId() {
        return transitGatewayId;
    }

    public void setTransitGatewayId(String transitGatewayId) {
        this.transitGatewayId = transitGatewayId;
    }

    /**
     * Associate a VPC Peering Connection to the Route.
     */
    public PeeringConnectionResource getVpcPeeringConnection() {
        return vpcPeeringConnection;
    }

    public void setVpcPeeringConnection(PeeringConnectionResource vpcPeeringConnection) {
        this.vpcPeeringConnection = vpcPeeringConnection;
    }

    @Override
    public void copyFrom(Route route) {
        setEgressGateway(!ObjectUtils.isBlank(route.egressOnlyInternetGatewayId()) ? findById(EgressOnlyInternetGatewayResource.class, route.egressOnlyInternetGatewayId()) : null);
        setGateway(!ObjectUtils.isBlank(route.gatewayId()) ? findById(InternetGatewayResource.class, route.gatewayId()) : null);
        setInstance(!ObjectUtils.isBlank(route.instanceId()) ? findById(InstanceResource.class, route.instanceId()) : null);
        setNatGateway(!ObjectUtils.isBlank(route.natGatewayId()) ? findById(NatGatewayResource.class, route.natGatewayId()) : null);
        setNetworkInterface(!ObjectUtils.isBlank(route.networkInterfaceId()) ? findById(NetworkInterfaceResource.class, route.networkInterfaceId()) : null);
        setTransitGatewayId(route.transitGatewayId());
        setVpcPeeringConnection(!ObjectUtils.isBlank(route.vpcPeeringConnectionId()) ? findById(PeeringConnectionResource.class, route.vpcPeeringConnectionId()) : null);
        setDestinationCidrBlock(route.destinationCidrBlock());
        setDestinationIpv6CidrBlock(route.destinationIpv6CidrBlock());
        setDestinationPrefixListId(route.destinationPrefixListId());
    }

    @Override
    public String primaryKey() {
        String primaryKey = getDestinationCidrBlock();
        if (getDestinationIpv6CidrBlock() != null) {
            primaryKey = getDestinationIpv6CidrBlock();
        }

        String secondaryKey;
        if (getGateway() != null) {
            secondaryKey = getGateway().getId();
        } else if (getInstance() != null) {
            secondaryKey = getInstance().getId();
        } else if (getNatGateway() != null) {
            secondaryKey = getNatGateway().getId();
        } else if (getNetworkInterface() != null) {
            secondaryKey = getNetworkInterface().getId();
        } else if (getTransitGatewayId() != null) {
            secondaryKey = getTransitGatewayId();
        } else if (getVpcPeeringConnection() != null) {
            secondaryKey = getVpcPeeringConnection().getId();
        } else if (getEgressGateway() != null) {
            secondaryKey = getEgressGateway().getId();
        } else {
            throw new GyroException("Invalid route.");
        }

        return String.format("%s to %s", primaryKey, secondaryKey);
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        client.createRoute(r -> r.destinationCidrBlock(getDestinationCidrBlock())
                .destinationIpv6CidrBlock(getDestinationIpv6CidrBlock())
                .gatewayId(getGateway() != null ? getGateway().getId() : null)
                .instanceId(getInstance() != null ? getInstance().getId() : null)
                .natGatewayId(getNatGateway() != null ? getNatGateway().getId() : null)
                .networkInterfaceId(getNetworkInterface() != null ? getNetworkInterface().getId() : null)
                .transitGatewayId(getTransitGatewayId())
                .vpcPeeringConnectionId(getVpcPeeringConnection() != null ? getVpcPeeringConnection().getId() : null)
                .routeTableId(((RouteTableResource) parent()).getId())
                .egressOnlyInternetGatewayId(getEgressGateway() != null ? getEgressGateway().getId() : null)
        );
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {

    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteRoute(r -> r.destinationCidrBlock(getDestinationCidrBlock())
                .destinationIpv6CidrBlock(getDestinationIpv6CidrBlock())
                .routeTableId(((RouteTableResource) parent()).getId())
        );
    }
}
