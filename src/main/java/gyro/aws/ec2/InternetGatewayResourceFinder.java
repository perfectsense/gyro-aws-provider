package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.InternetGateway;

import java.util.List;
import java.util.Map;

@Type("internet-gateway")
public class InternetGatewayResourceFinder extends AwsFinder<Ec2Client, InternetGateway, InternetGatewayResource> {
    private String attachmentState;
    private String attachmentVpcId;
    private String internetGatewayId;
    private String ownerId;
    private Map<String, String> tag;
    private String tagKey;

    @Filter("attachment.state")
    public String getAttachmentState() {
        return attachmentState;
    }

    public void setAttachmentState(String attachmentState) {
        this.attachmentState = attachmentState;
    }

    @Filter("attachment.vpc-id")
    public String getAttachmentVpcId() {
        return attachmentVpcId;
    }

    public void setAttachmentVpcId(String attachmentVpcId) {
        this.attachmentVpcId = attachmentVpcId;
    }

    public String getInternetGatewayId() {
        return internetGatewayId;
    }

    public void setInternetGatewayId(String internetGatewayId) {
        this.internetGatewayId = internetGatewayId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
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

    @Override
    protected List<InternetGateway> findAllAws(Ec2Client client) {
        return client.describeInternetGateways().internetGateways();
    }

    @Override
    protected List<InternetGateway> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeInternetGateways(r -> r.filters(createFilters(filters))).internetGateways();
    }
}
