package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.VpcEndpoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Query vpc endpoint.
 *
 * .. code-block:: gyro
 *
 *    endpoint: $(aws::vpc-endpoint EXTERNAL/* | service-name = '')
 */
@Type("vpc-endpoint")
public class EndpointFinder extends AwsFinder<Ec2Client, VpcEndpoint, EndpointResource> {

    private String serviceName;
    private String vpcId;
    private String vpcEndpointId;
    private String vpcEndpointState;
    private Map<String, String> tag;
    private String tagKey;

    /**
     * The name of the service.
     */
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * The ID of the VPC in which the endpoint resides.
     */
    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    /**
     * The ID of the endpoint.
     */
    public String getVpcEndpointId() {
        return vpcEndpointId;
    }

    public void setVpcEndpointId(String vpcEndpointId) {
        this.vpcEndpointId = vpcEndpointId;
    }

    /**
     * The state of the endpoint. Valid values are ``pending `` or `` available `` or `` deleting `` or `` deleted``.
     */
    public String getVpcEndpointState() {
        return vpcEndpointState;
    }

    public void setVpcEndpointState(String vpcEndpointState) {
        this.vpcEndpointState = vpcEndpointState;
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
    protected List<VpcEndpoint> findAllAws(Ec2Client client) {
        return client.describeVpcEndpoints().vpcEndpoints();
    }

    @Override
    protected List<VpcEndpoint> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeVpcEndpoints(r -> r.filters(createFilters(filters))).vpcEndpoints();
    }
}