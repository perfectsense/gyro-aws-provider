package gyro.aws.elb;

import gyro.aws.AwsResource;
import gyro.core.GyroUI;
import gyro.core.resource.Updatable;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import software.amazon.awssdk.services.elasticloadbalancing.ElasticLoadBalancingClient;
import software.amazon.awssdk.services.elasticloadbalancing.model.HealthCheck;

import java.util.Set;

/**
 * Creates a Health Check Resource
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     health-check
 *        healthy-threshold: "2"
 *        interval: "30"
 *        target: "HTTP:80/png"
 *        timeout: "3"
 *        unhealthy-threshold: "2"
 *     end
 */
public class HealthCheckResource extends AwsResource {

    private Integer healthyThreshold;
    private Integer interval;
    private String target;
    private Integer timeout;
    private Integer unhealthyThreshold;

    /**
     * The number of health check successes required to move an instance to the 'Healthy' state.
     */
    @Updatable
    public Integer getHealthyThreshold() {
        return healthyThreshold;
    }

    public void setHealthyThreshold(Integer healthyThreshold) {
        this.healthyThreshold = healthyThreshold;
    }

    /**
     * The interval, in seconds, between health checks of an instance
     */
    @Updatable
    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    /**
     * The instance that is being checked
     */
    @Updatable
    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * The amount of time, in seconds, a lack of response means a failed health check
     */
    @Updatable
    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    /**
     * The number of health check failures required to move an instance to the 'Unhealthy' state.
     */
    @Updatable
    public Integer getUnhealthyThreshold() {
        return unhealthyThreshold;
    }

    public void setUnhealthyThreshold(Integer unhealthyThreshold) {
        this.unhealthyThreshold = unhealthyThreshold;
    }

    public String getLoadBalancer() {
        LoadBalancerResource parent = (LoadBalancerResource) parent();
        if (parent != null) {
            return parent.getLoadBalancerName();
        }

        return null;
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        ElasticLoadBalancingClient client = createClient(ElasticLoadBalancingClient.class);

        client.configureHealthCheck(r ->
                r.loadBalancerName(getLoadBalancer())
                        .healthCheck(toHealthCheck()));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        create(ui, state);
    }

    @Override
    public void delete(GyroUI ui, State state) {}

    private HealthCheck toHealthCheck() {
        HealthCheck healthCheck = HealthCheck.builder()
            .healthyThreshold(getHealthyThreshold())
            .interval(getInterval())
            .target(getTarget())
            .timeout(getTimeout())
            .unhealthyThreshold(getUnhealthyThreshold())
            .build();

        return healthCheck;
    }

}
