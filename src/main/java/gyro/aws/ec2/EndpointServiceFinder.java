package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.ServiceConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Type("endpoint-service")
public class EndpointServiceFinder extends AwsFinder<Ec2Client, ServiceConfiguration, EndpointServiceResource> {
    private String serviceName;
    private String serviceId;
    private String serviceState;
    private Map<String, String> tag;
    private String tagKey;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceState() {
        return serviceState;
    }

    public void setServiceState(String serviceState) {
        this.serviceState = serviceState;
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
    protected List<ServiceConfiguration> findAllAws(Ec2Client client) {
        return client.describeVpcEndpointServiceConfigurations().serviceConfigurations();
    }

    @Override
    protected List<ServiceConfiguration> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeVpcEndpointServiceConfigurations(r -> r.filters(createFilters(filters))).serviceConfigurations();
    }
}
