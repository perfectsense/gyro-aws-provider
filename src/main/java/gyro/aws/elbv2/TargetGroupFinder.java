package gyro.aws.elbv2;

import gyro.aws.AwsFinder;
import gyro.core.Type;

import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query target groups.
 *
 * .. code-block:: gyro
 *
 *    target-group: $(external-query aws::load-balancer-target-group { arn: ''})
 */
@Type("load-balancer-target-group")
public class TargetGroupFinder extends AwsFinder<ElasticLoadBalancingV2Client, TargetGroup, TargetGroupResource> {

    private String arn;

    /**
     *  The arn of the target group to find.
     */
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public List<TargetGroup> findAws(ElasticLoadBalancingV2Client client, Map<String, String> filters) {
        if (!filters.containsKey("arn")) {
            throw new IllegalArgumentException("'arn' is required.");
        }

        return client.describeTargetGroups(r -> r.targetGroupArns(filters.get("arn"))).targetGroups();
    }

    @Override
    public List<TargetGroup> findAllAws(ElasticLoadBalancingV2Client client) {
        return client.describeLoadBalancersPaginator().loadBalancers()
            .stream()
            .map(LoadBalancer::loadBalancerArn)
            .map(oo -> client.describeTargetGroupsPaginator().targetGroups().stream().collect(Collectors.toList()))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }
}
