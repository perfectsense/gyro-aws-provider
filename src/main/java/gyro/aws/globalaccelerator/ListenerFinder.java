package gyro.aws.globalaccelerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.globalaccelerator.GlobalAcceleratorClient;
import software.amazon.awssdk.services.globalaccelerator.model.Listener;

/**
 * Query Global Accelerators Listeners.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    accelerator-listener: $(external-query aws::global-accelerator-listener { accelerator-arn: ''})
 */
@Type("global-accelerator-listener")
public class ListenerFinder extends AwsFinder<GlobalAcceleratorClient, Listener, ListenerResource> {

    private String acceleratorArn;

    /**
     * The arn of the accelerator to look up listeners on.
     */
    public String getAcceleratorArn() {
        return acceleratorArn;
    }

    public void setAcceleratorArn(String acceleratorArn) {
        this.acceleratorArn = acceleratorArn;
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
    protected List<Listener> findAllAws(GlobalAcceleratorClient client) {
        return new ArrayList<>();
    }

    @Override
    protected List<Listener> findAws(GlobalAcceleratorClient client, Map<String, String> filters) {
        return client.listListenersPaginator(r -> r.acceleratorArn(filters.get("accelerator-arn")))
            .listeners()
            .stream()
            .collect(Collectors.toList());
    }
}
