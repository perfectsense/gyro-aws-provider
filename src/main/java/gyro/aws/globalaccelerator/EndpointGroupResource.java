package gyro.aws.globalaccelerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Range;
import gyro.core.validation.ValidNumbers;
import software.amazon.awssdk.services.globalaccelerator.GlobalAcceleratorClient;
import software.amazon.awssdk.services.globalaccelerator.model.CreateEndpointGroupResponse;
import software.amazon.awssdk.services.globalaccelerator.model.DescribeEndpointGroupResponse;
import software.amazon.awssdk.services.globalaccelerator.model.EndpointConfiguration;
import software.amazon.awssdk.services.globalaccelerator.model.EndpointDescription;
import software.amazon.awssdk.services.globalaccelerator.model.EndpointGroup;
import software.amazon.awssdk.services.globalaccelerator.model.EndpointGroupNotFoundException;
import software.amazon.awssdk.services.globalaccelerator.model.HealthCheckProtocol;
import software.amazon.awssdk.services.globalaccelerator.model.PortOverride;

/**
 * Create a global accelerator.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::global-accelerator-endpoint-group accelerator
 *         listener: $(aws::global-accelerator-listener accelerator)
 *
 *         endpoint-group-region: us-east-1
 *         endpoint-configuration
 *             client-ip-preservation-enabled: true
 *             endpoint-id: arn:aws:elasticloadbalancing:us-east-1:111111111111:loadbalancer/app/qa/e8222a13d93ea86f
 *             weight: 1.0
 *         end
 *
 *         health-check-path: "/_ping"
 *         health-check-interval-seconds: 10
 *         health-check-protocol: HTTP
 *         health-check-port: 443
 *     end
 */
@Type("global-accelerator-endpoint-group")
public class EndpointGroupResource extends AwsResource implements Copyable<EndpointGroup> {

    private ListenerResource listener;
    private List<EndpointGroupConfiguration> endpointConfiguration;
    private List<EndpointGroupPortOverride> portOverride;
    private String endpointGroupRegion;
    private Integer healthCheckIntervalSeconds;
    private String healthCheckPath;
    private Integer healthCheckPort;
    private HealthCheckProtocol healthCheckProtocol;
    private Integer thresholdCount;
    private Float trafficDialPercentage;

    // -- Output fields
    private String arn;

    @Override
    public boolean refresh() {
        GlobalAcceleratorClient client =
            createClient(GlobalAcceleratorClient.class, "us-west-2", "https://globalaccelerator.us-west-2.amazonaws.com");

        try {
            DescribeEndpointGroupResponse response = client.describeEndpointGroup(r -> r.endpointGroupArn(getArn()));
            copyFrom(response.endpointGroup());
        } catch (EndpointGroupNotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        GlobalAcceleratorClient client =
            createClient(GlobalAcceleratorClient.class, "us-west-2", "https://globalaccelerator.us-west-2.amazonaws.com");

        CreateEndpointGroupResponse response = client.createEndpointGroup(r -> r
            .listenerArn(getListener().getArn())
            .endpointGroupRegion(getEndpointGroupRegion())
            .healthCheckIntervalSeconds(getHealthCheckIntervalSeconds())
            .healthCheckPath(getHealthCheckPath())
            .healthCheckPort(getHealthCheckPort())
            .healthCheckProtocol(getHealthCheckProtocol())
            .healthCheckIntervalSeconds(getHealthCheckIntervalSeconds())
            .trafficDialPercentage(getTrafficDialPercentage())
            .thresholdCount(getThresholdCount())
            .endpointConfigurations(endpointGroupConfigurations())
            .portOverrides(endpointPortOverrides())
        );

        setArn(response.endpointGroup().endpointGroupArn());
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        GlobalAcceleratorClient client =
            createClient(GlobalAcceleratorClient.class, "us-west-2", "https://globalaccelerator.us-west-2.amazonaws.com");

        client.updateEndpointGroup(r -> r
            .endpointGroupArn(getArn())
            .healthCheckIntervalSeconds(getHealthCheckIntervalSeconds())
            .healthCheckPath(getHealthCheckPath())
            .healthCheckPort(getHealthCheckPort())
            .healthCheckProtocol(getHealthCheckProtocol())
            .healthCheckIntervalSeconds(getHealthCheckIntervalSeconds())
            .trafficDialPercentage(getTrafficDialPercentage())
            .thresholdCount(getThresholdCount())
            .endpointConfigurations(endpointGroupConfigurations())
            .portOverrides(endpointPortOverrides())
        );
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        GlobalAcceleratorClient client =
            createClient(GlobalAcceleratorClient.class, "us-west-2", "https://globalaccelerator.us-west-2.amazonaws.com");

        client.deleteEndpointGroup(r -> r.endpointGroupArn(getArn()));
    }

    /**
     * The listener to configure the endpoint(s) on.
     */
    public ListenerResource getListener() {
        return listener;
    }

    public void setListener(ListenerResource listener) {
        this.listener = listener;
    }

    /**
     * The endpoint configurations.
     *
     * @subresource gyro.aws.globalaccelerator.EndpointGroupConfiguration
     */
    @Updatable
    public List<EndpointGroupConfiguration> getEndpointConfiguration() {
        if (endpointConfiguration == null) {
            endpointConfiguration = new ArrayList<>();
        }

        return endpointConfiguration;
    }

    public void setEndpointConfiguration(List<EndpointGroupConfiguration> endpointConfiguration) {
        this.endpointConfiguration = endpointConfiguration;
    }

    /**
     * Route traffic to an alternate port on the endpoint.
     *
     * @subresource gyro.aws.globalaccelerator.EndpointGroupPortOverride
     */
    @Updatable
    public List<EndpointGroupPortOverride> getPortOverride() {
        if (portOverride == null) {
            portOverride = new ArrayList<>();
        }

        return portOverride;
    }

    public void setPortOverride(List<EndpointGroupPortOverride> portOverride) {
        this.portOverride = portOverride;
    }

    /**
     * The region the endpoint is located in.
     */
    public String getEndpointGroupRegion() {
        return endpointGroupRegion;
    }

    public void setEndpointGroupRegion(String endpointGroupRegion) {
        this.endpointGroupRegion = endpointGroupRegion;
    }

    /**
     * The interval between health checks. Must be either ``10`` or ``30``.
     */
    @Updatable
    @ValidNumbers({10, 30})
    public Integer getHealthCheckIntervalSeconds() {
        return healthCheckIntervalSeconds;
    }

    public void setHealthCheckIntervalSeconds(Integer healthCheckIntervalSeconds) {
        this.healthCheckIntervalSeconds = healthCheckIntervalSeconds;
    }

    /**
     * For HTTP/HTTPs protocol, the path to check.
     */
    @Updatable
    public String getHealthCheckPath() {
        return healthCheckPath;
    }

    public void setHealthCheckPath(String healthCheckPath) {
        this.healthCheckPath = healthCheckPath;
    }

    /**
     * The port to check health on.
     */
    @Updatable
    public Integer getHealthCheckPort() {
        return healthCheckPort;
    }

    public void setHealthCheckPort(Integer healthCheckPort) {
        this.healthCheckPort = healthCheckPort;
    }

    /**
     * The protocol to use to check the health of endpoints.
     */
    @Updatable
    public HealthCheckProtocol getHealthCheckProtocol() {
        return healthCheckProtocol;
    }

    public void setHealthCheckProtocol(HealthCheckProtocol healthCheckProtocol) {
        this.healthCheckProtocol = healthCheckProtocol;
    }

    /**
     * The number of successful (or failed) health checks to trigger a change in health of the endpoint.
     */
    @Updatable
    public Integer getThresholdCount() {
        return thresholdCount;
    }

    public void setThresholdCount(Integer thresholdCount) {
        this.thresholdCount = thresholdCount;
    }

    /**
     * The percentage of traffic to send to this endpoint.
     */
    @Updatable
    @Range(min = 0.0, max = 1.0)
    public Float getTrafficDialPercentage() {
        return trafficDialPercentage;
    }

    public void setTrafficDialPercentage(Float trafficDialPercentage) {
        this.trafficDialPercentage = trafficDialPercentage;
    }

    /**
     * The endpoint group arn.
     */
    @Id
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public void copyFrom(EndpointGroup endpointGroup) {
        setArn(endpointGroup.endpointGroupArn());
        setEndpointGroupRegion(endpointGroup.endpointGroupRegion());
        setHealthCheckPath(endpointGroup.healthCheckPath());
        setHealthCheckIntervalSeconds(endpointGroup.healthCheckIntervalSeconds());
        setHealthCheckPort(endpointGroup.healthCheckPort());
        setHealthCheckIntervalSeconds(endpointGroup.healthCheckIntervalSeconds());
        setThresholdCount(endpointGroup.thresholdCount());
        setTrafficDialPercentage(endpointGroup.trafficDialPercentage());

        getEndpointConfiguration().clear();
        for (EndpointDescription description : endpointGroup.endpointDescriptions()) {
            EndpointGroupConfiguration configuration = newSubresource(EndpointGroupConfiguration.class);
            configuration.copyFrom(description);

            getEndpointConfiguration().add(configuration);
        }
    }

    List<EndpointConfiguration> endpointGroupConfigurations() {
        return getEndpointConfiguration().stream()
            .map(EndpointGroupConfiguration::endpointConfiguration)
            .collect(Collectors.toList());
    }

    List<PortOverride> endpointPortOverrides() {
        return getPortOverride().stream()
            .map(EndpointGroupPortOverride::portOverride)
            .collect(Collectors.toList());
    }
}
