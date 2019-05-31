package gyro.aws.waf.regional;

import gyro.core.Type;
import software.amazon.awssdk.services.waf.model.RateBasedRule;
import software.amazon.awssdk.services.waf.model.RuleSummary;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query rate rule regional.
 *
 * .. code-block:: gyro
 *
 *    rate-rules: $(aws::rate-rule-regional EXTERNAL/* | rule-id = '')
 */
@Type("rate-rule-regional")
public class RateRuleFinder extends gyro.aws.waf.common.RateRuleFinder<WafRegionalClient, RateRuleResource> {
    @Override
    protected List<RateBasedRule> findAllAws(WafRegionalClient client) {
        List<RateBasedRule> rules = new ArrayList<>();
        List<RuleSummary> ruleSummaries = client.listRateBasedRules().rules();

        for (RuleSummary ruleSummary : ruleSummaries) {
            rules.add(client.getRateBasedRule(r -> r.ruleId(ruleSummary.ruleId())).rule());
        }

        return rules;
    }

    @Override
    protected List<RateBasedRule> findAws(WafRegionalClient client, Map<String, String> filters) {
        List<RateBasedRule> rules = new ArrayList<>();

        if (filters.containsKey("rule-id")) {
            rules.add(client.getRateBasedRule(r -> r.ruleId(filters.get("rule-id"))).rule());
        }

        return rules;
    }
}
