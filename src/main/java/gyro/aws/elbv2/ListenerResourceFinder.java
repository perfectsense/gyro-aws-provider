package gyro.aws.elbv2;

import gyro.aws.AwsResourceFinder;
import gyro.core.resource.ResourceType;

import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Listener;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ResourceType("elbv2-listener")
public class ListenerResourceFinder extends AwsResourceFinder<ElasticLoadBalancingV2Client, Listener, ListenerResource> {

    private String arn;

    /**
     *  The arn of the listener to find.
     */
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public List<Listener> findAws(ElasticLoadBalancingV2Client client, Map<String, String> filters) {
        return client.describeListeners(r -> r.listenerArns(filters.get("arn"))).listeners();
    }

    @Override
    public List<Listener> findAllAws(ElasticLoadBalancingV2Client client) {
        List<Listener> listeners = new ArrayList<>();

        for (LoadBalancer loadBalancer : client.describeLoadBalancers().loadBalancers()) {
            listeners.addAll(client.describeListeners(r -> r.loadBalancerArn(loadBalancer.loadBalancerArn())).listeners());
        }

        return listeners;
    }
}
