package gyro.aws.elbv2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;

import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeRulesRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeRulesResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        if (!filters.containsKey("arn")) {
            throw new IllegalArgumentException("'arn' is required.");
        }

        return getRulesByLoadBalancerArn(client, Collections.singletonList(filters.get("arn")));
    }

    @Override
    public List<Rule> findAllAws(ElasticLoadBalancingV2Client client) {
        List<String> loadBalancerArns = client.describeLoadBalancersPaginator().loadBalancers()
            .stream().map(LoadBalancer::loadBalancerArn)
            .collect(Collectors.toList());

        return getRulesByLoadBalancerArn(client, loadBalancerArns);
    }

    private List<Rule> getRulesByLoadBalancerArn(ElasticLoadBalancingV2Client client, List<String> arns) {
        List<Rule> rules = new ArrayList<>();
        String marker = null;
        DescribeRulesResponse response;

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.describeRules(DescribeRulesRequest.builder().ruleArns(arns).build());
            } else {
                response = client.describeRules(DescribeRulesRequest.builder().ruleArns(arns).marker(marker).build());
            }

            marker = response.nextMarker();
            rules.addAll(response.rules());

        } while (!ObjectUtils.isBlank(marker));

        return rules;
    }

}
