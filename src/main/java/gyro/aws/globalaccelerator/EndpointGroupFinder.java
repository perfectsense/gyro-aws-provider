package gyro.aws.globalaccelerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.globalaccelerator.GlobalAcceleratorClient;
import software.amazon.awssdk.services.globalaccelerator.model.EndpointGroup;

/**
 * Query Global Accelerators Listener Endpoint Groups.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    accelerator-endpoint-group: $(external-query aws::global-accelerator-endpoint-group { listener-arn: ''})
 */
@Type("global-accelerator-endpoint-group")
public class EndpointGroupFinder extends AwsFinder<GlobalAcceleratorClient, EndpointGroup, EndpointGroupResource> {

    private String listenerArn;

    /**
     * The arn of the accelerator listener to look up endpoint groups on.
     */
    public String getListenerArn() {
        return listenerArn;
    }

    public void setListenerArn(String listenerArn) {
        this.listenerArn = listenerArn;
    }

    @Override
    protected String getRegion() {
        return "us-west-2";
    }

    @Override
    protected String getEndpoint() {
        return "https://globalaccelerator.us-west-2.amazonaws.com";
    }

    @Override
    protected List<EndpointGroup> findAllAws(GlobalAcceleratorClient client) {
        return new ArrayList<>();
    }

    @Override
    protected List<EndpointGroup> findAws(GlobalAcceleratorClient client, Map<String, String> filters) {
        return client.listEndpointGroups(r -> r.listenerArn(filters.get("listener-arn")))
            .endpointGroups()
            .stream()
            .collect(Collectors.toList());
    }
}
