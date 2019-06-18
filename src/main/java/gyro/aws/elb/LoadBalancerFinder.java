package gyro.aws.elb;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.elasticloadbalancing.ElasticLoadBalancingClient;
import software.amazon.awssdk.services.elasticloadbalancing.model.DescribeLoadBalancersResponse;
import software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query for classic load balancers.
 *
 * .. code-block:: gyro
 *
 *      $(aws::load-balancer EXTERNAL/* | name = 'my elb')
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
        return client.describeLoadBalancers(r -> r.loadBalancerNames(filters.get("name"))).loadBalancerDescriptions();
    }

    @Override
    protected List<LoadBalancerDescription> findAllAws(ElasticLoadBalancingClient client) {
        List<LoadBalancerDescription> loadBalancerDescriptions = new ArrayList<>();

        DescribeLoadBalancersResponse response = client.describeLoadBalancers();
        loadBalancerDescriptions.addAll(response.loadBalancerDescriptions());

        while (!ObjectUtils.isBlank(response.nextMarker())) {
            final String marker = response.nextMarker();
            response = client.describeLoadBalancers(r -> r.marker(marker));
            loadBalancerDescriptions.addAll(response.loadBalancerDescriptions());
        }

        return loadBalancerDescriptions;
    }

}
