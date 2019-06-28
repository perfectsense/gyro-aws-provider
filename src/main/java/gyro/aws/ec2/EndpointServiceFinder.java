package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeVpcEndpointServiceConfigurationsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeVpcEndpointServiceConfigurationsResponse;
import software.amazon.awssdk.services.ec2.model.ServiceConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Query vpc endpoint service.
 *
 * .. code-block:: gyro
 *
 *    endpoint-service: $(aws::vpc-endpoint-service EXTERNAL/* | service-name = '')
 */
@Type("vpc-endpoint-service")
public class EndpointServiceFinder extends AwsFinder<Ec2Client, ServiceConfiguration, EndpointServiceResource> {

    private String serviceName;
    private String serviceId;
    private String serviceState;
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
     * The ID of the service.
     */
    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * The state of the service . Valid values are ``Pending `` or `` Available `` or `` Deleting `` or `` Deleted `` or `` Failed``.
     */
    public String getServiceState() {
        return serviceState;
    }

    public void setServiceState(String serviceState) {
        this.serviceState = serviceState;
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
    protected List<ServiceConfiguration> findAllAws(Ec2Client client) {
        return getServiceConfigurations(client, null);
    }

    @Override
    protected List<ServiceConfiguration> findAws(Ec2Client client, Map<String, String> filters) {
        return getServiceConfigurations(client, filters);
    }

    private List<ServiceConfiguration> getServiceConfigurations(Ec2Client client, Map<String, String> filters) {
        List<ServiceConfiguration> serviceConfigurations = new ArrayList<>();

        DescribeVpcEndpointServiceConfigurationsRequest.Builder builder = DescribeVpcEndpointServiceConfigurationsRequest.builder();

        if (filters != null) {
            builder = builder.filters(createFilters(filters));
        }

        String marker = null;
        DescribeVpcEndpointServiceConfigurationsResponse response;

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.describeVpcEndpointServiceConfigurations(builder.build());
            } else {
                response = client.describeVpcEndpointServiceConfigurations(builder.nextToken(marker).build());
            }

            marker = response.nextToken();
            serviceConfigurations.addAll(response.serviceConfigurations());
        } while (!ObjectUtils.isBlank(marker));

        return serviceConfigurations;
    }
}