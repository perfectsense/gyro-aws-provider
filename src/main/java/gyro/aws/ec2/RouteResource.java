package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import org.apache.commons.lang.NotImplementedException;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeRouteTablesResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Route;
import software.amazon.awssdk.services.ec2.model.RouteTable;

import java.util.Set;

/**
 * Add a route to a route table. `See Route Tables <https://docs.aws.amazon.com/vpc/latest/userguide/VPC_Route_Tables.html/>`_.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::route route-example
 *         destination-cidr-block: 0.0.0.0/0
 *         route-table: $(aws::route-table route-table-example)
 *         gateway: $(aws::internet-gateway ig-example)
 *     end
 */
@Type("route")
public class RouteResource extends AwsResource implements Copyable<Route> {

    private RouteTableResource routeTable;
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
     * The Route Table to add this Route to.
     */
    public RouteTableResource getRouteTable() {
        return routeTable;
    }

    public void setRouteTable(RouteTableResource routeTable) {
        this.routeTable = routeTable;
    }

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
    @Updatable
    public EgressOnlyInternetGatewayResource getEgressGateway() {
        return egressGateway;
    }

    public void setEgressGateway(EgressOnlyInternetGatewayResource egressGateway) {
        this.egressGateway = egressGateway;
    }

    /**
     * Associate an Internet Gateway to the Route. Only valid with IPv4 destination CIDR.
     */
    @Updatable
    public InternetGatewayResource getGateway() {
        return gateway;
    }

    public void setGateway(InternetGatewayResource gateway) {
        this.gateway = gateway;
    }

    /**
     * Associate a NAT instance running in your VPC to the Route.
     */
    @Updatable
    public InstanceResource getInstance() {
        return instance;
    }

    public void setInstance(InstanceResource instance) {
        this.instance = instance;
    }

    /**
     * Associate a NAT Gateway to the Route. Only valid with IPv4 destination CIDR.
     */
    @Updatable
    public NatGatewayResource getNatGateway() {
        return natGateway;
    }

    public void setNatGateway(NatGatewayResource natGateway) {
        this.natGateway = natGateway;
    }

    /**
     * Associate a Network Interface to the Route.
     */
    @Updatable
    public NetworkInterfaceResource getNetworkInterface() {
        return networkInterface;
    }

    public void setNetworkInterface(NetworkInterfaceResource networkInterface) {
        this.networkInterface = networkInterface;
    }

    /**
     * The ID of a transit gateway to associate to the Route.
     */
    @Updatable
    public String getTransitGatewayId() {
        return transitGatewayId;
    }

    public void setTransitGatewayId(String transitGatewayId) {
        this.transitGatewayId = transitGatewayId;
    }

    /**
     * Associate a VPC Peering Connection to the Route.
     */
    @Updatable
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
    public boolean refresh() {
        throw new NotImplementedException();
    }

    @Override
    public void create(GyroUI ui, State state) {
        throw new NotImplementedException();
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        throw new NotImplementedException();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        throw new NotImplementedException();
    }

    private Route getRoute(Ec2Client client) {
        Route route = null;

        if (getRouteTable() == null) {
            throw new GyroException("route-table is missing, unable to load route.");
        }

        try {
            DescribeRouteTablesResponse response = client.describeRouteTables(r -> r.filters(
                Filter.builder().name("route-table-id").values(getRouteTable().getId()).build()
            ));

            for (RouteTable routeTable : response.routeTables()) {
                for (Route r : routeTable.routes()) {
                    if ((r.destinationCidrBlock() != null && r.destinationCidrBlock().equals(getDestinationCidrBlock()))
                        || (r.destinationIpv6CidrBlock() != null && r.destinationIpv6CidrBlock().equals(getDestinationCidrBlock()))
                        || (r.destinationPrefixListId() != null && r.destinationPrefixListId().equals(getDestinationPrefixListId()))) {
                        route = r;
                        break;
                    }
                }
            }
        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return route;
    }
}
