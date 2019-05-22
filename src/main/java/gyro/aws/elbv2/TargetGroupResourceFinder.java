package gyro.aws.elbv2;

import gyro.aws.AwsResourceFinder;
import gyro.core.resource.ResourceType;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ResourceType("elbv2-target-group")
public class TargetGroupResourceFinder extends AwsResourceFinder<ElasticLoadBalancingV2Client, TargetGroup, TargetGroupResource> {

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

        for (LoadBalancer loadBalancer : client.describeLoadBalancers().loadBalancers()) {
            targetGroups.addAll(client.describeTargetGroups(r -> r.loadBalancerArn(loadBalancer.loadBalancerArn())).targetGroups());
        }

        return targetGroups;
    }
}
