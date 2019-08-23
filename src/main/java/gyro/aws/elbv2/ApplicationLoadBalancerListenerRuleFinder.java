package gyro.aws.elbv2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;

import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeRulesRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeRulesResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Listener;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query application load balancer listener rules.
 *
 * .. code-block:: gyro
 *
 *    alb-listener-rule: $(external-query aws::application-load-balancer-listener-rule { arn: ''})
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
        if (!filters.containsKey("arn")) {
            throw new IllegalArgumentException("'arn' is required.");
        }

        return client.describeRules(r -> r.ruleArns(filters.get("arn"))).rules();
    }

    @Override
    public List<Rule> findAllAws(ElasticLoadBalancingV2Client client) {
        List<Rule> rules = new ArrayList<>();
        List<String> listnerArns = new ArrayList<>();

        client.describeLoadBalancersPaginator().loadBalancers()
            .stream().map(LoadBalancer::loadBalancerArn)
            .collect(Collectors.toList())
            .forEach(o -> listnerArns.addAll(
                client.describeListenersPaginator(r -> r.loadBalancerArn(o))
                    .listeners().stream()
                    .map(Listener::listenerArn)
                    .collect(Collectors.toList())
            ));

        listnerArns.forEach(o -> rules.addAll(getRulesByListnerArn(client, o)));

        return rules;
    }

    private List<Rule> getRulesByListnerArn(ElasticLoadBalancingV2Client client, String arn) {
        List<Rule> rules = new ArrayList<>();
        String marker = null;
        DescribeRulesResponse response;

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.describeRules(DescribeRulesRequest.builder().listenerArn(arn).build());
            } else {
                response = client.describeRules(DescribeRulesRequest.builder().listenerArn(arn).marker(marker).build());
            }

            marker = response.nextMarker();
            rules.addAll(response.rules());

        } while (!ObjectUtils.isBlank(marker));

        return rules;
    }

}
