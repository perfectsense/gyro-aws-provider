package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeRouteTablesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeRouteTablesResponse;
import software.amazon.awssdk.services.ec2.model.RouteTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private String destinationCidrBlock;
    private String destinationIpv6CidrBlock;
    private String destinationPrefixListId;
    private String egressOnlyInternetGatewayId;
    private String gatewayId;
    private String instanceId;
    private String natGatewayId;
    private String routeTransitGatewayId;
    private String origin;
    private String state;
    private String vpcPeeringConnectionId;
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
    public String getDestinationCidrBlock() {
        return destinationCidrBlock;
    }

    public void setDestinationCidrBlock(String destinationCidrBlock) {
        this.destinationCidrBlock = destinationCidrBlock;
    }

    /**
     * The IPv6 CIDR range specified in a route in the route table.
     */
    @Filter("route.destination-ipv6-cidr-block")
    public String getDestinationIpv6CidrBlock() {
        return destinationIpv6CidrBlock;
    }

    public void setDestinationIpv6CidrBlock(String destinationIpv6CidrBlock) {
        this.destinationIpv6CidrBlock = destinationIpv6CidrBlock;
    }

    /**
     * The ID (prefix) of the AWS service specified in a route in the table.
     */
    @Filter("route.destination-prefix-list-id")
    public String getDestinationPrefixListId() {
        return destinationPrefixListId;
    }

    public void setDestinationPrefixListId(String destinationPrefixListId) {
        this.destinationPrefixListId = destinationPrefixListId;
    }

    /**
     * The ID of an egress-only Internet gateway specified in a route in the route table.
     */
    @Filter("route.egress-only-internet-gateway-id")
    public String getEgressOnlyInternetGatewayId() {
        return egressOnlyInternetGatewayId;
    }

    public void setEgressOnlyInternetGatewayId(String egressOnlyInternetGatewayId) {
        this.egressOnlyInternetGatewayId = egressOnlyInternetGatewayId;
    }

    /**
     * The ID of a gateway specified in a route in the table.
     */
    @Filter("route.gateway-id")
    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    /**
     * The ID of an instance specified in a route in the table.
     */
    @Filter("route.instance-id")
    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * The ID of a NAT gateway.
     */
    @Filter("route.nat-gateway-id")
    public String getNatGatewayId() {
        return natGatewayId;
    }

    public void setNatGatewayId(String natGatewayId) {
        this.natGatewayId = natGatewayId;
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
    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    /**
     * The state of a route in the route table (active | blackhole ). The blackhole state indicates that the route's target isn't available . Valid values are ``for example, the specified gateway isn't attached to the VPC, the specified NAT instance has been terminated, and so on``.
     */
    @Filter("route.state")
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * The ID of a VPC peering connection specified in a route in the table.
     */
    @Filter("route.vpc-peering-connection-id")
    public String getVpcPeeringConnectionId() {
        return vpcPeeringConnectionId;
    }

    public void setVpcPeeringConnectionId(String vpcPeeringConnectionId) {
        this.vpcPeeringConnectionId = vpcPeeringConnectionId;
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
        return client.describeRouteTablesPaginator().routeTables().stream().collect(Collectors.toList());
    }

    @Override
    protected List<RouteTable> findAws(Ec2Client client, Map<String, String> filters) {
        List<RouteTable> securityGroups = new ArrayList<>();

        DescribeRouteTablesRequest.Builder builder = DescribeRouteTablesRequest.builder().filters(createFilters(filters));

        String marker = null;
        DescribeRouteTablesResponse response;

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.describeRouteTables(builder.build());
            } else {
                response = client.describeRouteTables(builder.nextToken(marker).build());
            }

            marker = response.nextToken();
            securityGroups.addAll(response.routeTables());
        } while (!ObjectUtils.isBlank(marker));

        return securityGroups;
    }
}