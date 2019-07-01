package gyro.aws.route53;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.GetHealthCheckResponse;
import software.amazon.awssdk.services.route53.model.HealthCheck;
import software.amazon.awssdk.services.route53.model.ListHealthChecksRequest;
import software.amazon.awssdk.services.route53.model.ListHealthChecksResponse;
import software.amazon.awssdk.services.route53.model.NoSuchHealthCheckException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query route53 health check.
 *
 * .. code-block:: gyro
 *
 *    health-check: $(aws::route53-health-check EXTERNAL/* | health-check-id = '')
 */
@Type("route53-health-check")
public class HealthCheckFinder extends AwsFinder<Route53Client, HealthCheck, HealthCheckResource> {
    private String healthCheckId;

    /**
     * The ID of the Health Check.
     */
    public String getHealthCheckId() {
        return healthCheckId;
    }

    public void setHealthCheckId(String healthCheckId) {
        this.healthCheckId = healthCheckId;
    }

    @Override
    protected List<HealthCheck> findAllAws(Route53Client client) {
        List<HealthCheck> healthChecks = new ArrayList<>();

        String marker = null;
        ListHealthChecksResponse response;

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listHealthChecks();
            } else {
                response = client.listHealthChecks(ListHealthChecksRequest.builder().marker(marker).build());
            }

            marker = response.marker();
            healthChecks.addAll(response.healthChecks());

        } while (response.isTruncated());

        return healthChecks;
    }

    @Override
    protected List<HealthCheck> findAws(Route53Client client, Map<String, String> filters) {
        List<HealthCheck> healthChecks = new ArrayList<>();

        if (filters.containsKey("health-check-id") && !ObjectUtils.isBlank(filters.get("health-check-id"))) {
            try {
                GetHealthCheckResponse response = client.getHealthCheck(r -> r.healthCheckId(filters.get("health-check-id")));
                healthChecks.add(response.healthCheck());
            } catch (NoSuchHealthCheckException ignore) {
                //ignore
            }
        }

        return healthChecks;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }
}
