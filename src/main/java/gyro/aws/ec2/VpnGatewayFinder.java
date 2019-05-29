package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.VpnGateway;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Type("vpn-gateway")
public class VpnGatewayFinder extends AwsFinder<Ec2Client, VpnGateway, VpnGatewayResource> {
    private String amazonSideAsn;
    private String attachmentState;
    private String attachmentVpcId;
    private String availabilityZone;
    private String state;
    private Map<String, String> tag;
    private String tagKey;
    private String type;
    private String vpnGatewayId;

    public String getAmazonSideAsn() {
        return amazonSideAsn;
    }

    public void setAmazonSideAsn(String amazonSideAsn) {
        this.amazonSideAsn = amazonSideAsn;
    }

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

    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVpnGatewayId() {
        return vpnGatewayId;
    }

    public void setVpnGatewayId(String vpnGatewayId) {
        this.vpnGatewayId = vpnGatewayId;
    }

    @Override
    protected List<VpnGateway> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeVpnGateways(r -> r.filters(createFilters(filters))).vpnGateways();
    }

    @Override
    protected List<VpnGateway> findAllAws(Ec2Client client) {
        return client.describeVpnGateways().vpnGateways();
    }
}
