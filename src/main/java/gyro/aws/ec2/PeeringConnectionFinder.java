package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.VpcPeeringConnection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Query peering connection.
 *
 * .. code-block:: gyro
 *
 *    peering-connection: $(aws::peering-connection EXTERNAL/* | vpc-peering-connection-id = '')
 */
@Type("peering-connection")
public class PeeringConnectionFinder extends AwsFinder<Ec2Client, VpcPeeringConnection, PeeringConnectionResource> {

    private String accepterVpcInfoCidrBlock;
    private String accepterVpcInfoOwnerId;
    private String accepterVpcInfoVpcId;
    private String expirationTime;
    private String requesterVpcInfoCidrBlock;
    private String requesterVpcInfoOwnerId;
    private String requesterVpcInfoVpcId;
    private String statusCode;
    private String statusMessage;
    private Map<String, String> tag;
    private String tagKey;
    private String vpcPeeringConnectionId;

    /**
     * The IPv4 CIDR block of the accepter VPC.
     */
    @Filter("accepter-vpc-info.cidr-block")
    public String getAccepterVpcInfoCidrBlock() {
        return accepterVpcInfoCidrBlock;
    }

    public void setAccepterVpcInfoCidrBlock(String accepterVpcInfoCidrBlock) {
        this.accepterVpcInfoCidrBlock = accepterVpcInfoCidrBlock;
    }

    /**
     * The AWS account ID of the owner of the accepter VPC.
     */
    @Filter("accepter-vpc-info.owner-id")
    public String getAccepterVpcInfoOwnerId() {
        return accepterVpcInfoOwnerId;
    }

    public void setAccepterVpcInfoOwnerId(String accepterVpcInfoOwnerId) {
        this.accepterVpcInfoOwnerId = accepterVpcInfoOwnerId;
    }

    /**
     * The ID of the accepter VPC.
     */
    @Filter("accepter-vpc-info.vpc-id")
    public String getAccepterVpcInfoVpcId() {
        return accepterVpcInfoVpcId;
    }

    public void setAccepterVpcInfoVpcId(String accepterVpcInfoVpcId) {
        this.accepterVpcInfoVpcId = accepterVpcInfoVpcId;
    }

    /**
     * The expiration date and time for the VPC peering connection.
     */
    public String getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(String expirationTime) {
        this.expirationTime = expirationTime;
    }

    /**
     * The IPv4 CIDR block of the requester's VPC.
     */
    @Filter("requester-vpc-info.cidr-block")
    public String getRequesterVpcInfoCidrBlock() {
        return requesterVpcInfoCidrBlock;
    }

    public void setRequesterVpcInfoCidrBlock(String requesterVpcInfoCidrBlock) {
        this.requesterVpcInfoCidrBlock = requesterVpcInfoCidrBlock;
    }

    /**
     * The AWS account ID of the owner of the requester VPC.
     */
    @Filter("requester-vpc-info.owner-id")
    public String getRequesterVpcInfoOwnerId() {
        return requesterVpcInfoOwnerId;
    }

    public void setRequesterVpcInfoOwnerId(String requesterVpcInfoOwnerId) {
        this.requesterVpcInfoOwnerId = requesterVpcInfoOwnerId;
    }

    /**
     * The ID of the requester VPC.
     */
    @Filter("requester-vpc-info.vpc-id")
    public String getRequesterVpcInfoVpcId() {
        return requesterVpcInfoVpcId;
    }

    public void setRequesterVpcInfoVpcId(String requesterVpcInfoVpcId) {
        this.requesterVpcInfoVpcId = requesterVpcInfoVpcId;
    }

    /**
     * The status of the VPC peering connection . Valid values are ``pending-acceptance `` or `` failed `` or `` expired `` or `` provisioning `` or `` active `` or `` deleting `` or `` deleted `` or `` rejected``.
     */
    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * A message that provides more information about the status of the VPC peering connection, if applicable.
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
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
     * The ID of the VPC peering connection.
     */
    public String getVpcPeeringConnectionId() {
        return vpcPeeringConnectionId;
    }

    public void setVpcPeeringConnectionId(String vpcPeeringConnectionId) {
        this.vpcPeeringConnectionId = vpcPeeringConnectionId;
    }

    @Override
    protected List<VpcPeeringConnection> findAllAws(Ec2Client client) {
        return client.describeVpcPeeringConnections().vpcPeeringConnections();
    }

    @Override
    protected List<VpcPeeringConnection> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeVpcPeeringConnections(r -> r.filters(createFilters(filters))).vpcPeeringConnections();
    }
}