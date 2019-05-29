package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.VpcEndpoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Type("endpoint")
public class EndpointFinder extends AwsFinder<Ec2Client, VpcEndpoint, EndpointResource> {
    private String serviceName;
    private String vpcId;
    private String vpcEndpointId;
    private String vpcEndpointState;
    private Map<String, String> tag;
    private String tagKey;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    public String getVpcEndpointId() {
        return vpcEndpointId;
    }

    public void setVpcEndpointId(String vpcEndpointId) {
        this.vpcEndpointId = vpcEndpointId;
    }

    public String getVpcEndpointState() {
        return vpcEndpointState;
    }

    public void setVpcEndpointState(String vpcEndpointState) {
        this.vpcEndpointState = vpcEndpointState;
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

    @Override
    protected List<VpcEndpoint> findAllAws(Ec2Client client) {
        return client.describeVpcEndpoints().vpcEndpoints();
    }

    @Override
    protected List<VpcEndpoint> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeVpcEndpoints(r -> r.filters(createFilters(filters))).vpcEndpoints();
    }
}
