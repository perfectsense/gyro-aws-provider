package gyro.aws.waf.regional;

import gyro.core.Type;
import software.amazon.awssdk.services.waf.model.Rule;
import software.amazon.awssdk.services.waf.model.RuleSummary;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query rule regional.
 *
 * .. code-block:: gyro
 *
 *    rules: $(aws::rule-regional EXTERNAL/* | rule-id = '')
 */
@Type("rule-regional")
public class RuleFinder extends gyro.aws.waf.common.RuleFinder<WafRegionalClient, RuleResource> {
    @Override
    protected List<Rule> findAllAws(WafRegionalClient client) {
        List<Rule> rules = new ArrayList<>();
        List<RuleSummary> ruleSummaries = client.listRules().rules();

        for (RuleSummary ruleSummary : ruleSummaries) {
            rules.add(client.getRule(r -> r.ruleId(ruleSummary.ruleId())).rule());
        }

        return rules;
    }

    @Override
    protected List<Rule> findAws(WafRegionalClient client, Map<String, String> filters) {
        List<Rule> rules = new ArrayList<>();

        if (filters.containsKey("rule-id")) {
            rules.add(client.getRule(r -> r.ruleId(filters.get("rule-id"))).rule());
        }

        return rules;
    }
}
