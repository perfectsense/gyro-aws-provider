package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.NatGateway;

import java.util.List;
import java.util.Map;

@Type("nat-gateway")
public class NatGatewayResourceFinder extends AwsFinder<Ec2Client, NatGateway, NatGatewayResource> {
    private String natGatewayId;
    private String state;
    private String subnetId;
    private Map<String, String> tag;
    private String tagKey;
    private String vpcId;

    public String getNatGatewayId() {
        return natGatewayId;
    }

    public void setNatGatewayId(String natGatewayId) {
        this.natGatewayId = natGatewayId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
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
