package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.VpcPeeringConnection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Filter("accepter-vpc-info.cidr-block")
    public String getAccepterVpcInfoCidrBlock() {
        return accepterVpcInfoCidrBlock;
    }

    public void setAccepterVpcInfoCidrBlock(String accepterVpcInfoCidrBlock) {
        this.accepterVpcInfoCidrBlock = accepterVpcInfoCidrBlock;
    }

    @Filter("accepter-vpc-info.owner-id")
    public String getAccepterVpcInfoOwnerId() {
        return accepterVpcInfoOwnerId;
    }

    public void setAccepterVpcInfoOwnerId(String accepterVpcInfoOwnerId) {
        this.accepterVpcInfoOwnerId = accepterVpcInfoOwnerId;
    }

    @Filter("accepter-vpc-info.vpc-id")
    public String getAccepterVpcInfoVpcId() {
        return accepterVpcInfoVpcId;
    }

    public void setAccepterVpcInfoVpcId(String accepterVpcInfoVpcId) {
        this.accepterVpcInfoVpcId = accepterVpcInfoVpcId;
    }

    public String getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(String expirationTime) {
        this.expirationTime = expirationTime;
    }

    @Filter("requester-vpc-info.cidr-block")
    public String getRequesterVpcInfoCidrBlock() {
        return requesterVpcInfoCidrBlock;
    }

    public void setRequesterVpcInfoCidrBlock(String requesterVpcInfoCidrBlock) {
        this.requesterVpcInfoCidrBlock = requesterVpcInfoCidrBlock;
    }

    @Filter("requester-vpc-info.owner-id")
    public String getRequesterVpcInfoOwnerId() {
        return requesterVpcInfoOwnerId;
    }

    public void setRequesterVpcInfoOwnerId(String requesterVpcInfoOwnerId) {
        this.requesterVpcInfoOwnerId = requesterVpcInfoOwnerId;
    }

    @Filter("requester-vpc-info.vpc-id")
    public String getRequesterVpcInfoVpcId() {
        return requesterVpcInfoVpcId;
    }

    public void setRequesterVpcInfoVpcId(String requesterVpcInfoVpcId) {
        this.requesterVpcInfoVpcId = requesterVpcInfoVpcId;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Map<String, String> getTag() {
        if (tag == null) {
            tag = new HashMap<>();
        }

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
