package gyro.aws.route53;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.GetHealthCheckResponse;
import software.amazon.awssdk.services.route53.model.HealthCheck;
import software.amazon.awssdk.services.route53.model.NoSuchHealthCheckException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query route53 health check.
 *
 * .. code-block:: gyro
 *
 *    health-check: $(aws::route53-health-check EXTERNAL/* | id = '')
 */
@Type("route53-health-check")
public class HealthCheckFinder extends AwsFinder<Route53Client, HealthCheck, HealthCheckResource> {
    private String id;

    /**
     * The ID of the Health Check.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<HealthCheck> findAllAws(Route53Client client) {
        return client.listHealthChecksPaginator().healthChecks().stream().collect(Collectors.toList());
    }

    @Override
    protected List<HealthCheck> findAws(Route53Client client, Map<String, String> filters) {
        List<HealthCheck> healthChecks = new ArrayList<>();

        try {
            GetHealthCheckResponse response = client.getHealthCheck(r -> r.healthCheckId(filters.get("id")));
            healthChecks.add(response.healthCheck());
        } catch (NoSuchHealthCheckException ignore) {
            //ignore
        }

        return healthChecks;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }
}
