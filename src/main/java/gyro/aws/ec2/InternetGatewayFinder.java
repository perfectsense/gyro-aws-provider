package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.InternetGateway;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query internet gateway.
 *
 * .. code-block:: gyro
 *
 *    internet-gateway: $(external-query aws::internet-gateway { internet-gateway-id: ''})
 */
@Type("internet-gateway")
public class InternetGatewayFinder extends Ec2TaggableAwsFinder<Ec2Client, InternetGateway, InternetGatewayResource> {

    private String attachmentState;
    private String attachmentVpcId;
    private String internetGatewayId;
    private String ownerId;
    private Map<String, String> tag;
    private String tagKey;

    /**
     * The current state of the attachment between the gateway and the VPC ( available). Present only if a VPC is attached.
     */
    @Filter("attachment.state")
    public String getAttachmentState() {
        return attachmentState;
    }

    public void setAttachmentState(String attachmentState) {
        this.attachmentState = attachmentState;
    }

    /**
     * The ID of an attached VPC.
     */
    @Filter("attachment.vpc-id")
    public String getAttachmentVpcId() {
        return attachmentVpcId;
    }

    public void setAttachmentVpcId(String attachmentVpcId) {
        this.attachmentVpcId = attachmentVpcId;
    }

    /**
     * The ID of the Internet gateway.
     */
    public String getInternetGatewayId() {
        return internetGatewayId;
    }

    public void setInternetGatewayId(String internetGatewayId) {
        this.internetGatewayId = internetGatewayId;
    }

    /**
     * The ID of the AWS account that owns the internet gateway.
     */
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
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

    @Override
    protected List<InternetGateway> findAllAws(Ec2Client client) {
        return client.describeInternetGatewaysPaginator().internetGateways().stream().collect(Collectors.toList());
    }

    @Override
    protected List<InternetGateway> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeInternetGatewaysPaginator(r -> r.filters(createFilters(filters))).internetGateways().stream().collect(Collectors.toList());
    }
}