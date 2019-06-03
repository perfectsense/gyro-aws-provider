package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.RouteTable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Query route table.
 *
 * .. code-block:: gyro
 *
 *    route-table: $(aws::route-table EXTERNAL/* | route-table-id = '')
 */
@Type("route-table")
public class RouteTableFinder extends AwsFinder<Ec2Client, RouteTable, RouteTableResource> {

    private String associationRouteTableAssociationId;
    private String associationRouteTableId;
    private String associationSubnetId;
    private String associationMain;
    private String ownerId;
    private String routeTableId;
    private String routeDestinationCidrBlock;
    private String routeDestinationIpv6CidrBlock;
    private String routeDestinationPrefixListId;
    private String routeEgressOnlyInternetGatewayId;
    private String routeGatewayId;
    private String routeInstanceId;
    private String routeNatGatewayId;
    private String routeTransitGatewayId;
    private String routeOrigin;
    private String routeState;
    private String routeVpcPeeringConnectionId;
    private Map<String, String> tag;
    private String tagKey;
    private String transitGatewayId;
    private String vpcId;

    /**
     * The ID of an association ID for the route table.
     */
    @Filter("association.route-table-association-id")
    public String getAssociationRouteTableAssociationId() {
        return associationRouteTableAssociationId;
    }

    public void setAssociationRouteTableAssociationId(String associationRouteTableAssociationId) {
        this.associationRouteTableAssociationId = associationRouteTableAssociationId;
    }

    /**
     * The ID of the route table involved in the association.
     */
    @Filter("association.route-table-id")
    public String getAssociationRouteTableId() {
        return associationRouteTableId;
    }

    public void setAssociationRouteTableId(String associationRouteTableId) {
        this.associationRouteTableId = associationRouteTableId;
    }

    /**
     * The ID of the subnet involved in the association.
     */
    @Filter("association.subnet-id")
    public String getAssociationSubnetId() {
        return associationSubnetId;
    }

    public void setAssociationSubnetId(String associationSubnetId) {
        this.associationSubnetId = associationSubnetId;
    }

    /**
     * Indicates whether the route table is the main route table for the VPC . Valid values are `` true `` or `` false``. Route tables that do not have an association ID are not returned in the response.
     */
    @Filter("association.main")
    public String getAssociationMain() {
        return associationMain;
    }

    public void setAssociationMain(String associationMain) {
        this.associationMain = associationMain;
    }

    /**
     * The ID of the AWS account that owns the route table.
     */
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * The ID of the route table.
     */
    public String getRouteTableId() {
        return routeTableId;
    }

    public void setRouteTableId(String routeTableId) {
        this.routeTableId = routeTableId;
    }

    /**
     * The IPv4 CIDR range specified in a route in the table.
     */
    @Filter("route.destination-cidr-block")
    public String getRouteDestinationCidrBlock() {
        return routeDestinationCidrBlock;
    }

    public void setRouteDestinationCidrBlock(String routeDestinationCidrBlock) {
        this.routeDestinationCidrBlock = routeDestinationCidrBlock;
    }

    /**
     * The IPv6 CIDR range specified in a route in the route table.
     */
    @Filter("route.destination-ipv6-cidr-block")
    public String getRouteDestinationIpv6CidrBlock() {
        return routeDestinationIpv6CidrBlock;
    }

    public void setRouteDestinationIpv6CidrBlock(String routeDestinationIpv6CidrBlock) {
        this.routeDestinationIpv6CidrBlock = routeDestinationIpv6CidrBlock;
    }

    /**
     * The ID (prefix) of the AWS service specified in a route in the table.
     */
    @Filter("route.destination-prefix-list-id")
    public String getRouteDestinationPrefixListId() {
        return routeDestinationPrefixListId;
    }

    public void setRouteDestinationPrefixListId(String routeDestinationPrefixListId) {
        this.routeDestinationPrefixListId = routeDestinationPrefixListId;
    }

    /**
     * The ID of an egress-only Internet gateway specified in a route in the route table.
     */
    @Filter("route.egress-only-internet-gateway-id")
    public String getRouteEgressOnlyInternetGatewayId() {
        return routeEgressOnlyInternetGatewayId;
    }

    public void setRouteEgressOnlyInternetGatewayId(String routeEgressOnlyInternetGatewayId) {
        this.routeEgressOnlyInternetGatewayId = routeEgressOnlyInternetGatewayId;
    }

    /**
     * The ID of a gateway specified in a route in the table.
     */
    @Filter("route.gateway-id")
    public String getRouteGatewayId() {
        return routeGatewayId;
    }

    public void setRouteGatewayId(String routeGatewayId) {
        this.routeGatewayId = routeGatewayId;
    }

    /**
     * The ID of an instance specified in a route in the table.
     */
    @Filter("route.instance-id")
    public String getRouteInstanceId() {
        return routeInstanceId;
    }

    public void setRouteInstanceId(String routeInstanceId) {
        this.routeInstanceId = routeInstanceId;
    }

    /**
     * The ID of a NAT gateway.
     */
    @Filter("route.nat-gateway-id")
    public String getRouteNatGatewayId() {
        return routeNatGatewayId;
    }

    public void setRouteNatGatewayId(String routeNatGatewayId) {
        this.routeNatGatewayId = routeNatGatewayId;
    }

    /**
     * The ID of a transit gateway.
     */
    @Filter("route.transit-gateway-id")
    public String getRouteTransitGatewayId() {
        return routeTransitGatewayId;
    }

    public void setRouteTransitGatewayId(String routeTransitGatewayId) {
        this.routeTransitGatewayId = routeTransitGatewayId;
    }

    /**
     * Describes how the route was created. CreateRouteTable indicates that the route was automatically created when the route table was created; CreateRoute indicates that the route was manually added to the route table; EnableVgwRoutePropagation indicates that the route was propagated by route propagation.
     */
    @Filter("route.origin")
    public String getRouteOrigin() {
        return routeOrigin;
    }

    public void setRouteOrigin(String routeOrigin) {
        this.routeOrigin = routeOrigin;
    }

    /**
     * The state of a route in the route table (active | blackhole ). The blackhole state indicates that the route's target isn't available . Valid values are ``for example, the specified gateway isn't attached to the VPC, the specified NAT instance has been terminated, and so on``.
     */
    @Filter("route.state")
    public String getRouteState() {
        return routeState;
    }

    public void setRouteState(String routeState) {
        this.routeState = routeState;
    }

    /**
     * The ID of a VPC peering connection specified in a route in the table.
     */
    @Filter("route.vpc-peering-connection-id")
    public String getRouteVpcPeeringConnectionId() {
        return routeVpcPeeringConnectionId;
    }

    public void setRouteVpcPeeringConnectionId(String routeVpcPeeringConnectionId) {
        this.routeVpcPeeringConnectionId = routeVpcPeeringConnectionId;
    }

    /**
     * The key/value combination of a tag assigned to the resource.
     */
    public Map<String, String> getTag() {
        if (tag == null) {
            tag = new HashMap<>();
        }

        return tag;
    }

    public void setTag(Map<String, String> tag) {
        this.tag = tag;
    }

    /**
     * The key of a tag assigned to the resource. Use this filter to find all resources assigned a tag with a specific key, regardless of the tag value.
     */
    public String getTagKey() {
        return tagKey;
    }

    public void setTagKey(String tagKey) {
        this.tagKey = tagKey;
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
     * The ID of the VPC for the route table.
     */
    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    @Override
    protected List<RouteTable> findAllAws(Ec2Client client) {
        return client.describeRouteTables().routeTables();
    }

    @Override
    protected List<RouteTable> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeRouteTables(r -> r.filters(createFilters(filters))).routeTables();
    }
}