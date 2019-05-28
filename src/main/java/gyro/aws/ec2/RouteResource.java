package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.core.GyroException;
import gyro.core.Type;
import gyro.core.resource.Resource;
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
public class RouteResource extends AwsResource {

    private RouteTableResource routeTable;
    private String destinationCidrBlock;
    private String destinationIpv6CidrBlock;
    private String destinationPrefixListId;
    private EgressOnlyInternetGatewayResource egressOnlyInternetGateway;
    private InternetGatewayResource gateway;
    private InstanceResource instance;
    private NatGatewayResource natGateway;
    private NetworkInterfaceResource networkInterface;
    private String transitGatewayId;
    private PeeringConnectionResource vpcPeeringConnection;

    /**
     * The route table to add this route to.
     */
    public RouteTableResource getRouteTable() {
        return routeTable;
    }

    public void setRouteTable(RouteTableResource routeTable) {
        this.routeTable = routeTable;
    }

    /**
     * An IPv4 destination CIDR block.
     */
    public String getDestinationCidrBlock() {
        return destinationCidrBlock;
    }

    public void setDestinationCidrBlock(String destinationCidrBlock) {
        this.destinationCidrBlock = destinationCidrBlock;
    }

    /**
     * An IPv6 destination CIDR block.
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
     * Associate an egress only internet gateway. Only valid with IPv6 destination CIDR.
     */
    public EgressOnlyInternetGatewayResource getEgressOnlyInternetGateway() {
        return egressOnlyInternetGateway;
    }

    public void setEgressOnlyInternetGateway(EgressOnlyInternetGatewayResource egressOnlyInternetGateway) {
        this.egressOnlyInternetGateway = egressOnlyInternetGateway;
    }

    /**
     * Associate an internet gateway. Only valid with IPv4 destination CIDR.
     */
    public InternetGatewayResource getGateway() {
        return gateway;
    }

    public void setGateway(InternetGatewayResource gateway) {
        this.gateway = gateway;
    }

    /**
     * Associate a NAT instance running in your VPC.
     */
    public InstanceResource getInstance() {
        return instance;
    }

    public void setInstance(InstanceResource instance) {
        this.instance = instance;
    }

    /**
     * Associate a NAT gateway. Only valid with IPv4 destination CIDR.
     */
    public NatGatewayResource getNatGateway() {
        return natGateway;
    }

    public void setNatGateway(NatGatewayResource natGateway) {
        this.natGateway = natGateway;
    }

    /**
     * Associate a network interface.
     */
    public NetworkInterfaceResource getNetworkInterface() {
        return networkInterface;
    }

    public void setNetworkInterface(NetworkInterfaceResource networkInterface) {
        this.networkInterface = networkInterface;
    }

    /**
     * The ID of a transit gateway.
     */
    public String getTransitGatewayId() {
        return transitGatewayId;
    }

    public void setTransitGatewayId(String transitGatewayId) {
        this.transitGatewayId = transitGatewayId;
    }

    /**
     * Associate VPC peering connection.
     */
    public PeeringConnectionResource getVpcPeeringConnection() {
        return vpcPeeringConnection;
    }

    public void setVpcPeeringConnection(PeeringConnectionResource vpcPeeringConnection) {
        this.vpcPeeringConnection = vpcPeeringConnection;
    }

    @Override
    public boolean refresh() {
        Ec2Client client = createClient(Ec2Client.class);

        Route route = getRoute(client);

        if (route == null) {
            return false;
        }

        setEgressOnlyInternetGateway(findById(EgressOnlyInternetGatewayResource.class, route.egressOnlyInternetGatewayId()));
        setGateway(findById(InternetGatewayResource.class, route.gatewayId()));
        setInstance(findById(InstanceResource.class, route.instanceId()));
        setNatGateway(findById(NatGatewayResource.class, route.natGatewayId()));
        setNetworkInterface(findById(NetworkInterfaceResource.class, route.networkInterfaceId()));
        setTransitGatewayId(route.transitGatewayId());
        setVpcPeeringConnection(findById(PeeringConnectionResource.class, route.vpcPeeringConnectionId()));

        return true;
    }

    @Override
    public void create() {
        Ec2Client client = createClient(Ec2Client.class);

        client.createRoute(r -> r.destinationCidrBlock(getDestinationCidrBlock())
                .destinationIpv6CidrBlock(getDestinationIpv6CidrBlock())
                .gatewayId(getGateway() != null ? getGateway().getInternetGatewayId() : null)
                .instanceId(getInstance() != null ? getInstance().getInstanceId() : null)
                .natGatewayId(getNatGateway() != null ? getNatGateway().getNatGatewayId() : null)
                .networkInterfaceId(getNetworkInterface() != null ? getNetworkInterface().getNetworkInterfaceId() : null)
                .transitGatewayId(getTransitGatewayId())
                .vpcPeeringConnectionId(getVpcPeeringConnection() != null ? getVpcPeeringConnection().getPeeringConnectionId() : null)
                .routeTableId(getRouteTable() != null ? getRouteTable().getRouteTableId() : null)
        );
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {

    }

    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteRoute(r -> r.destinationCidrBlock(getDestinationCidrBlock())
                .destinationIpv6CidrBlock(getDestinationIpv6CidrBlock())
                .routeTableId(getRouteTable() != null ? getRouteTable().getRouteTableId() : null)
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("route ");
        sb.append(getDestinationCidrBlock());

        if (getGateway() != null) {
            sb.append(" through gateway ");
            sb.append(getGateway().getInternetGatewayId());
        } else if (getInstance() != null) {
            sb.append(" through instance ");
            sb.append(getInstance().getInstanceId());
        } else if (getNatGateway() != null) {
            sb.append(" through nat gateway");
            sb.append(getNatGateway().getNatGatewayId());
        } else if (getNetworkInterface() != null) {
            sb.append(" through network interface ");
            sb.append(getNetworkInterface().getNetworkInterfaceId());
        } else if (getTransitGatewayId() != null) {
            sb.append(" through transit gateway ");
            sb.append(getTransitGatewayId());
        } else if (getVpcPeeringConnection() != null) {
            sb.append(" through vpc peering connection ");
            sb.append(getVpcPeeringConnection().getPeeringConnectionId());
        }

        return sb.toString();
    }

    private Route getRoute(Ec2Client client) {
        Route route = null;

        if (getRouteTable() == null) {
            throw new GyroException("route-table is missing, unable to load route.");
        }

        try {
            DescribeRouteTablesResponse response = client.describeRouteTables(r -> r.filters(
                Filter.builder().name("route-table-id").values(getRouteTable().getRouteTableId()).build()
            ));

            for (RouteTable routeTable : response.routeTables()) {
                for (Route r : routeTable.routes()) {
                    if (r.destinationCidrBlock() != null && r.destinationCidrBlock().equals(getDestinationCidrBlock())) {
                        route = r;
                        break;
                    } else if (r.destinationIpv6CidrBlock() != null && r.destinationIpv6CidrBlock().equals(getDestinationCidrBlock())) {
                        route = r;
                        break;
                    } else if (r.destinationPrefixListId() != null && r.destinationPrefixListId().equals(getDestinationPrefixListId())) {
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
