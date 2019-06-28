package gyro.aws.elbv2;

import gyro.aws.AwsFinder;
import gyro.core.Type;

import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup;
import software.amazon.awssdk.services.elasticloadbalancingv2.paginators.DescribeLoadBalancersIterable;
import software.amazon.awssdk.services.elasticloadbalancingv2.paginators.DescribeTargetGroupsIterable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query target groups.
 *
 * .. code-block:: gyro
 *
 *    target-group: $(aws::load-balancer-target-group EXTERNAL/* | arn = '')
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
        return client.describeTargetGroups(r -> r.targetGroupArns(filters.get("arn"))).targetGroups();
    }

    @Override
    public List<TargetGroup> findAllAws(ElasticLoadBalancingV2Client client) {
        List<TargetGroup> targetGroups = new ArrayList<>();

        List<LoadBalancer> loadBalancers = new ArrayList<>();
        DescribeLoadBalancersIterable iterable = client.describeLoadBalancersPaginator();
        iterable.stream().forEach(r -> loadBalancers.addAll(r.loadBalancers()));

        for (LoadBalancer loadBalancer : loadBalancers) {
            DescribeTargetGroupsIterable targetGroupsIterable = client.describeTargetGroupsPaginator(r -> r.loadBalancerArn(loadBalancer.loadBalancerArn()));
            targetGroupsIterable.stream().forEach(r -> targetGroups.addAll(r.targetGroups()));
        }

        return targetGroups;
    }
}
