package gyro.aws.route53;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.route53.Route53Client;
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

    public String getTrafficPolicyInstanceId() {
        return trafficPolicyInstanceId;
    }

    public void setTrafficPolicyInstanceId(String trafficPolicyInstanceId) {
        this.trafficPolicyInstanceId = trafficPolicyInstanceId;
    }

    @Override
    protected List<TrafficPolicyInstance> findAllAws(Route53Client client) {
        return client.listTrafficPolicyInstances().trafficPolicyInstances();
    }

    @Override
    protected List<TrafficPolicyInstance> findAws(Route53Client client, Map<String, String> filters) {
        List<TrafficPolicyInstance> trafficPolicyInstances = new ArrayList<>();

        if (filters.containsKey("traffic-policy-instance-id")) {
            try {
                trafficPolicyInstances.add(client.getTrafficPolicyInstance(r -> r.id(filters.get("traffic-policy-instance-id"))).trafficPolicyInstance());
            } catch (NoSuchTrafficPolicyInstanceException ignore) {
                // ignore
            }
        }

        return trafficPolicyInstances;
    }
}
