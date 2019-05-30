package gyro.aws.waf.global;

import gyro.core.Type;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.RateBasedRule;
import software.amazon.awssdk.services.waf.model.RuleSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Type("rate-rule")
public class RateRuleFinder extends gyro.aws.waf.common.RateRuleFinder<WafClient, RateRuleResource> {
    @Override
    protected List<RateBasedRule> findAllAws(WafClient client) {
        List<RateBasedRule> rules = new ArrayList<>();
        List<RuleSummary> ruleSummaries = client.listRateBasedRules().rules();

        for (RuleSummary ruleSummary : ruleSummaries) {
            rules.add(client.getRateBasedRule(r -> r.ruleId(ruleSummary.ruleId())).rule());
        }

        return rules;
    }

    @Override
    protected List<RateBasedRule> findAws(WafClient client, Map<String, String> filters) {
        List<RateBasedRule> rules = new ArrayList<>();

        if (filters.containsKey("rule-id")) {
            rules.add(client.getRateBasedRule(r -> r.ruleId(filters.get("rule-id"))).rule());
        }

        return rules;
    }
}
