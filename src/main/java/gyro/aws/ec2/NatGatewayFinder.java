package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.NatGateway;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Query nat gateway.
 *
 * .. code-block:: gyro
 *
 *    nat-gateway: $(aws::nat-gateway EXTERNAL/* | nat-gateway-id = '')
 */
@Type("nat-gateway")
public class NatGatewayFinder extends AwsFinder<Ec2Client, NatGateway, NatGatewayResource> {

    private String natGatewayId;
    private String state;
    private String subnetId;
    private Map<String, String> tag;
    private String tagKey;
    private String vpcId;

    /**
     * The ID of the NAT gateway.
     */
    public String getNatGatewayId() {
        return natGatewayId;
    }

    public void setNatGatewayId(String natGatewayId) {
        this.natGatewayId = natGatewayId;
    }

    /**
     * The state of the NAT gateway . Valid values are ``pending `` or `` failed `` or `` available `` or `` deleting `` or `` deleted``.
     */
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * The ID of the subnet in which the NAT gateway resides.
     */
    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
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
     * The ID of the VPC in which the NAT gateway resides.
     */
    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    @Override
    protected List<NatGateway> findAllAws(Ec2Client client) {
        return client.describeNatGateways().natGateways();
    }

    @Override
    protected List<NatGateway> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeNatGateways(r -> r.filter(createFilters(filters))).natGateways();
    }
}