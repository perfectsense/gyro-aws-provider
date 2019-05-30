package gyro.aws.waf.global;

import gyro.core.Type;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.Rule;
import software.amazon.awssdk.services.waf.model.RuleSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Type("rule")
public class RuleFinder extends gyro.aws.waf.common.RuleFinder<WafClient, RuleResource> {
    @Override
    protected List<Rule> findAllAws(WafClient client) {
        List<Rule> rules = new ArrayList<>();
        List<RuleSummary> ruleSummaries = client.listRules().rules();

        for (RuleSummary ruleSummary : ruleSummaries) {
            rules.add(client.getRule(r -> r.ruleId(ruleSummary.ruleId())).rule());
        }

        return rules;
    }

    @Override
    protected List<Rule> findAws(WafClient client, Map<String, String> filters) {
        List<Rule> rules = new ArrayList<>();

        if (filters.containsKey("rule-id")) {
            rules.add(client.getRule(r -> r.ruleId(filters.get("rule-id"))).rule());
        }

        return rules;
    }
}
