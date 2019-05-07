package gyro.aws.waf;

import gyro.core.resource.ResourceName;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateRuleResponse;
import software.amazon.awssdk.services.waf.model.GetRuleResponse;
import software.amazon.awssdk.services.waf.model.Rule;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

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
@ResourceName("rule")
public class RuleResource extends RuleBaseResource {
    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getRuleId())) {
            return false;
        }

        GetRuleResponse response;

        if (getRegionalWaf()) {
            response = getRegionalClient().getRule(
                r -> r.ruleId(getRuleId())
            );
        } else {
            response = getGlobalClient().getRule(
                r -> r.ruleId(getRuleId())
            );
        }

        Rule rule = response.rule();
        setMetricName(rule.metricName());
        setName(rule.name());
        loadPredicates(rule.predicates());

        return true;
    }

    @Override
    public void create() {
        CreateRuleResponse response;

        if (getRegionalWaf()) {
            WafRegionalClient client = getRegionalClient();

            response = client.createRule(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .name(getName())
                    .metricName(getMetricName())
            );
        } else {
            WafClient client = getGlobalClient();

            response = client.createRule(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .name(getName())
                    .metricName(getMetricName())
            );
        }

        Rule rule = response.rule();
        setRuleId(rule.ruleId());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        if (getRegionalWaf()) {
            WafRegionalClient client = getRegionalClient();

            client.deleteRule(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .ruleId(getRuleId())
            );
        } else {
            WafClient client = getGlobalClient();

            client.deleteRule(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .ruleId(getRuleId())
            );
        }
    }

    @Override
    boolean isRateRule() {
        return false;
    }
}
