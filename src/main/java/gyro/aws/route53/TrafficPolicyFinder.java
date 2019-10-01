package gyro.aws.route53;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.GetTrafficPolicyResponse;
import software.amazon.awssdk.services.route53.model.ListTrafficPoliciesRequest;
import software.amazon.awssdk.services.route53.model.ListTrafficPoliciesResponse;
import software.amazon.awssdk.services.route53.model.NoSuchTrafficPolicyException;
import software.amazon.awssdk.services.route53.model.TrafficPolicy;
import software.amazon.awssdk.services.route53.model.TrafficPolicySummary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query traffic policy.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    traffic-policy: $(external-query aws::route53-traffic-policy { id: '', version: ''})
 */
@Type("route53-traffic-policy")
public class TrafficPolicyFinder extends AwsFinder<Route53Client, TrafficPolicy, TrafficPolicyResource> {
    private String id;
    private String version;

    /**
     * The ID of the traffic policy.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The version of the traffic policy.
     */
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    protected List<TrafficPolicy> findAllAws(Route53Client client) {
        List<TrafficPolicy> trafficPolicies = new ArrayList<>();
        Map<String, Integer> trafficPolicyMap = new HashMap<>();
        ListTrafficPoliciesResponse response = null;

        do {
            if (response == null) {
                response = client.listTrafficPolicies();
            } else {
                response = client.listTrafficPolicies(
                    ListTrafficPoliciesRequest.builder()
                        .trafficPolicyIdMarker(response.trafficPolicyIdMarker())
                        .build()
                );
            }

            trafficPolicyMap.putAll(response.trafficPolicySummaries().stream().collect(Collectors.toMap(TrafficPolicySummary::id, TrafficPolicySummary::latestVersion)));

        } while (response.isTruncated());

        for (String key : trafficPolicyMap.keySet()) {
            trafficPolicies.add(client.getTrafficPolicy(r -> r.id(key).version(trafficPolicyMap.get(key))).trafficPolicy());
        }

        return trafficPolicies;
    }

    @Override
    protected List<TrafficPolicy> findAws(Route53Client client, Map<String, String> filters) {
        List<TrafficPolicy> trafficPolicies = new ArrayList<>();

        if (!filters.containsKey("id") || !filters.containsKey("version")) {
            throw new IllegalArgumentException("Both 'id' and 'version' are needed.");
        }

        if (!isValidVersion(filters.get("version"))) {
            throw new IllegalArgumentException("'version' needs to be a valid integer.");
        }

        try {
            GetTrafficPolicyResponse response = client.getTrafficPolicy(r -> r.id(filters.get("id")).version(Integer.parseInt(filters.get("version"))));

            trafficPolicies.add(response.trafficPolicy());

        } catch (NoSuchTrafficPolicyException ignore) {
            // ignore
        }

        return trafficPolicies;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }

    private boolean isValidVersion(String version) {
        try {
            if (version == null) {
                return false;
            }

            int i = Integer.parseInt(version);
            return i >= 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
