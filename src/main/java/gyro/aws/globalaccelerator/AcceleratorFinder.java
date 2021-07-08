package gyro.aws.globalaccelerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.globalaccelerator.GlobalAcceleratorClient;
import software.amazon.awssdk.services.globalaccelerator.model.Accelerator;
import software.amazon.awssdk.services.globalaccelerator.model.AcceleratorNotFoundException;

/**
 * Query Global Accelerators.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    accelerator: $(external-query aws::global-accelerator { arn: ''})
 */
@Type("global-accelerator")
public class AcceleratorFinder extends AwsFinder<GlobalAcceleratorClient, Accelerator, AcceleratorResource> {

    private String arn;

    /**
     * The arn of the accelerator to look up.
     */
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
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
    protected List<Accelerator> findAllAws(GlobalAcceleratorClient client) {
        return client.listAcceleratorsPaginator().accelerators().stream().collect(Collectors.toList());
    }

    @Override
    protected List<Accelerator> findAws(GlobalAcceleratorClient client, Map<String, String> filters) {
        List<Accelerator> accelerators = new ArrayList<>();

        try {
            accelerators.add(client.describeAccelerator(r -> r.acceleratorArn(filters.get("arn"))).accelerator());
        } catch (AcceleratorNotFoundException ex) {
            // Ignore
        }

        return accelerators;
    }
}
