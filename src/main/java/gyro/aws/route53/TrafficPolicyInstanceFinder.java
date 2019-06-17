package gyro.aws.route53;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.ListTrafficPolicyInstancesRequest;
import software.amazon.awssdk.services.route53.model.ListTrafficPolicyInstancesResponse;
import software.amazon.awssdk.services.route53.model.NoSuchTrafficPolicyInstanceException;
import software.amazon.awssdk.services.route53.model.TrafficPolicyInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query traffic policy instance.
 *
 * .. code-block:: gyro
 *
 *    traffic-policy-instance: $(aws::traffic-policy-instance EXTERNAL/* | traffic-policy-instance-id = '')
 */
@Type("traffic-policy-instance")
public class TrafficPolicyInstanceFinder extends AwsFinder<Route53Client, TrafficPolicyInstance, TrafficPolicyInstanceResource> {
    private String trafficPolicyInstanceId;

    /**
     * The ID of the traffic policy instance.
     */
    public String getTrafficPolicyInstanceId() {
        return trafficPolicyInstanceId;
    }

    public void setTrafficPolicyInstanceId(String trafficPolicyInstanceId) {
        this.trafficPolicyInstanceId = trafficPolicyInstanceId;
    }

    @Override
    protected List<TrafficPolicyInstance> findAllAws(Route53Client client) {
        List<TrafficPolicyInstance> trafficPolicyInstances = new ArrayList<>();

        String marker = null;
        ListTrafficPolicyInstancesResponse response;

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listTrafficPolicyInstances();
            } else {
                response = client.listTrafficPolicyInstances(ListTrafficPolicyInstancesRequest.builder().trafficPolicyInstanceNameMarker(marker).build());
            }

            marker = response.trafficPolicyInstanceNameMarker();
            trafficPolicyInstances.addAll(response.trafficPolicyInstances());
        } while (response.isTruncated());

        return trafficPolicyInstances;
    }

    @Override
    protected List<TrafficPolicyInstance> findAws(Route53Client client, Map<String, String> filters) {
        List<TrafficPolicyInstance> trafficPolicyInstances = new ArrayList<>();

        if (filters.containsKey("traffic-policy-instance-id") && !ObjectUtils.isBlank(filters.get("traffic-policy-instance-id"))) {
            try {
                trafficPolicyInstances.add(client.getTrafficPolicyInstance(r -> r.id(filters.get("traffic-policy-instance-id"))).trafficPolicyInstance());
            } catch (NoSuchTrafficPolicyInstanceException ignore) {
                // ignore
            }
        }

        return trafficPolicyInstances;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }
}
