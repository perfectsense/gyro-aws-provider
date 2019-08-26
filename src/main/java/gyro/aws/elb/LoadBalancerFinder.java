package gyro.aws.elb;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.elasticloadbalancing.ElasticLoadBalancingClient;
import software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerDescription;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query for classic load balancers.
 *
 * .. code-block:: gyro
 *
 *    load-balancer: $(external-query aws::load-balancer { name: 'my elb'})
 */
@Type("load-balancer")
public class LoadBalancerFinder extends AwsFinder<ElasticLoadBalancingClient, LoadBalancerDescription, LoadBalancerResource> {

    private String name;

    /**
     * The name of the load balancer to find.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<LoadBalancerDescription> findAws(ElasticLoadBalancingClient client, Map<String, String> filters) {
        if (!filters.containsKey("name")) {
            throw new IllegalArgumentException("'name' is required.");
        }

        return client.describeLoadBalancers(r -> r.loadBalancerNames(filters.get("name"))).loadBalancerDescriptions();
    }

    @Override
    protected List<LoadBalancerDescription> findAllAws(ElasticLoadBalancingClient client) {
        return client.describeLoadBalancersPaginator().loadBalancerDescriptions().stream().collect(Collectors.toList());
    }

}
