package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.RouteTable;

import java.util.List;
import java.util.Map;

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

    @Filter("association.route-table-association-id")
    public String getAssociationRouteTableAssociationId() {
        return associationRouteTableAssociationId;
    }

    public void setAssociationRouteTableAssociationId(String associationRouteTableAssociationId) {
        this.associationRouteTableAssociationId = associationRouteTableAssociationId;
    }

    @Filter("association.route-table-id")
    public String getAssociationRouteTableId() {
        return associationRouteTableId;
    }

    public void setAssociationRouteTableId(String associationRouteTableId) {
        this.associationRouteTableId = associationRouteTableId;
    }

    @Filter("association.subnet-id")
    public String getAssociationSubnetId() {
        return associationSubnetId;
    }

    public void setAssociationSubnetId(String associationSubnetId) {
        this.associationSubnetId = associationSubnetId;
    }

    @Filter("association.main")
    public String getAssociationMain() {
        return associationMain;
    }

    public void setAssociationMain(String associationMain) {
        this.associationMain = associationMain;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getRouteTableId() {
        return routeTableId;
    }

    public void setRouteTableId(String routeTableId) {
        this.routeTableId = routeTableId;
    }

    @Filter("route.destination-cidr-block")
    public String getRouteDestinationCidrBlock() {
        return routeDestinationCidrBlock;
    }

    public void setRouteDestinationCidrBlock(String routeDestinationCidrBlock) {
        this.routeDestinationCidrBlock = routeDestinationCidrBlock;
    }

    @Filter("route.destination-ipv6-cidr-block")
    public String getRouteDestinationIpv6CidrBlock() {
        return routeDestinationIpv6CidrBlock;
    }

    public void setRouteDestinationIpv6CidrBlock(String routeDestinationIpv6CidrBlock) {
        this.routeDestinationIpv6CidrBlock = routeDestinationIpv6CidrBlock;
    }

    @Filter("route.destination-prefix-list-id")
    public String getRouteDestinationPrefixListId() {
        return routeDestinationPrefixListId;
    }

    public void setRouteDestinationPrefixListId(String routeDestinationPrefixListId) {
        this.routeDestinationPrefixListId = routeDestinationPrefixListId;
    }

    @Filter("route.egress-only-internet-gateway-id")
    public String getRouteEgressOnlyInternetGatewayId() {
        return routeEgressOnlyInternetGatewayId;
    }

    public void setRouteEgressOnlyInternetGatewayId(String routeEgressOnlyInternetGatewayId) {
        this.routeEgressOnlyInternetGatewayId = routeEgressOnlyInternetGatewayId;
    }

    @Filter("route.gateway-id")
    public String getRouteGatewayId() {
        return routeGatewayId;
    }

    public void setRouteGatewayId(String routeGatewayId) {
        this.routeGatewayId = routeGatewayId;
    }

    @Filter("route.instance-id")
    public String getRouteInstanceId() {
        return routeInstanceId;
    }

    public void setRouteInstanceId(String routeInstanceId) {
        this.routeInstanceId = routeInstanceId;
    }

    @Filter("route.nat-gateway-id")
    public String getRouteNatGatewayId() {
        return routeNatGatewayId;
    }

    public void setRouteNatGatewayId(String routeNatGatewayId) {
        this.routeNatGatewayId = routeNatGatewayId;
    }

    @Filter("route.transit-gateway-id")
    public String getRouteTransitGatewayId() {
        return routeTransitGatewayId;
    }

    public void setRouteTransitGatewayId(String routeTransitGatewayId) {
        this.routeTransitGatewayId = routeTransitGatewayId;
    }

    @Filter("route.origin")
    public String getRouteOrigin() {
        return routeOrigin;
    }

    public void setRouteOrigin(String routeOrigin) {
        this.routeOrigin = routeOrigin;
    }

    @Filter("route.state")
    public String getRouteState() {
        return routeState;
    }

    public void setRouteState(String routeState) {
        this.routeState = routeState;
    }

    @Filter("route.vpc-peering-connection-id")
    public String getRouteVpcPeeringConnectionId() {
        return routeVpcPeeringConnectionId;
    }

    public void setRouteVpcPeeringConnectionId(String routeVpcPeeringConnectionId) {
        this.routeVpcPeeringConnectionId = routeVpcPeeringConnectionId;
    }

    public Map<String, String> getTag() {
        return tag;
    }

    public void setTag(Map<String, String> tag) {
        this.tag = tag;
    }

    public String getTagKey() {
        return tagKey;
    }

    public void setTagKey(String tagKey) {
        this.tagKey = tagKey;
    }

    public String getTransitGatewayId() {
        return transitGatewayId;
    }

    public void setTransitGatewayId(String transitGatewayId) {
        this.transitGatewayId = transitGatewayId;
    }

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
