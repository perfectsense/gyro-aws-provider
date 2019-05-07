package gyro.aws.waf;

import gyro.core.resource.ResourceType;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateRuleResponse;
import software.amazon.awssdk.services.waf.model.GetRuleResponse;
import software.amazon.awssdk.services.waf.model.Rule;

import java.util.Set;

/**
 * Creates a rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::rule rule-example
 *         name: "rule-example"
 *         metric-name: "ruleExample"
 *     end
 */
@ResourceType("rule")
public class RuleResource extends RuleBaseResource {
    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getRuleId())) {
            return false;
        }

        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        GetRuleResponse response = client.getRule(
            r -> r.ruleId(getRuleId())
        );

        Rule rule = response.rule();
        setMetricName(rule.metricName());
        setName(rule.name());
        loadPredicates(rule.predicates());

        return true;
    }

    @Override
    public void create() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        CreateRuleResponse response = client.createRule(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
                .metricName(getMetricName())
        );

        Rule rule = response.rule();
        setRuleId(rule.ruleId());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        client.deleteRule(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .ruleId(getRuleId())
        );
    }

    @Override
    boolean isRateRule() {
        return false;
    }
}
