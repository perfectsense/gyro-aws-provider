package gyro.aws.elbv2;

import gyro.aws.AwsResourceFinder;
import gyro.core.resource.ResourceType;

import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;

import java.util.List;
import java.util.Map;

@ResourceType("elbv2-load-balancer")
public class LoadBalancerResourceFinder extends AwsResourceFinder<ElasticLoadBalancingV2Client, LoadBalancer, LoadBalancerResource> {

    private String arn;
    private String name;

    /**
     *  The arn of the load balancer to find.
     */
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     *  The name of the load balancer to find.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<LoadBalancer> findAws(ElasticLoadBalancingV2Client client, Map<String, String> filters) {
        if (getArn() != null) {
            return client.describeLoadBalancers(r -> r.loadBalancerArns(filters.get("arn"))).loadBalancers();
        }

        return client.describeLoadBalancers(r -> r.names(filters.get("load-balancer-name"))).loadBalancers();
    }

    @Override
    public List<LoadBalancer> findAllAws(ElasticLoadBalancingV2Client client) {
        return client.describeLoadBalancers().loadBalancers();
    }
}
