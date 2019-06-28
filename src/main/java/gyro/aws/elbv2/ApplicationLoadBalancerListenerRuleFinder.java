package gyro.aws.elbv2;

import gyro.aws.AwsFinder;
import gyro.core.Type;

import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Listener;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query application load balancer listener rules.
 *
 * .. code-block:: gyro
 *
 *    alb-listener-rule: $(aws::application-load-balancer-listener-rule EXTERNAL/* | arn = '')
 */
@Type("application-load-balancer-listener-rule")
public class ApplicationLoadBalancerListenerRuleFinder extends AwsFinder<ElasticLoadBalancingV2Client, Rule, ApplicationLoadBalancerListenerRuleResource> {

    private String arn;

    /**
     *  The arn of the application listener rule to find.
     */
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public List<Rule> findAws(ElasticLoadBalancingV2Client client, Map<String, String> filters) {
        return client.describeRules(r -> r.ruleArns(filters.get("arn"))).rules();
    }

    @Override
    public List<Rule> findAllAws(ElasticLoadBalancingV2Client client) {
        List<Rule> rules = new ArrayList<>();

        for (LoadBalancer loadBalancer : client.describeLoadBalancers().loadBalancers()) {
            for (Listener listener : client.describeListeners(r -> r.loadBalancerArn(loadBalancer.loadBalancerArn())).listeners()) {
                rules.addAll(client.describeRules(r -> r.listenerArn(listener.listenerArn())).rules());
            }
        }

        return rules;
    }

}
