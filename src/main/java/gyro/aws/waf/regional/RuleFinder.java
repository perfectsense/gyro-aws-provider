package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import software.amazon.awssdk.services.waf.model.ListRulesRequest;
import software.amazon.awssdk.services.waf.model.ListRulesResponse;
import software.amazon.awssdk.services.waf.model.Rule;
import software.amazon.awssdk.services.waf.model.RuleSummary;
import software.amazon.awssdk.services.waf.model.WafNonexistentItemException;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query rule regional.
 *
 * .. code-block:: gyro
 *
 *    rules: $(aws::waf-rule-regional EXTERNAL/* | rule-id = '')
 */
@Type("waf-rule-regional")
public class RuleFinder extends gyro.aws.waf.common.RuleFinder<WafRegionalClient, RuleResource> {
    @Override
    protected List<Rule> findAllAws(WafRegionalClient client) {
        List<Rule> rules = new ArrayList<>();

        String marker = null;
        ListRulesResponse response;
        List<RuleSummary> ruleSummaries = new ArrayList<>();

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listRules();
            } else {
                response = client.listRules(ListRulesRequest.builder().nextMarker(marker).build());
            }

            marker = response.nextMarker();
            ruleSummaries.addAll(response.rules());

        } while (!ObjectUtils.isBlank(marker));

        for (RuleSummary ruleSummary : ruleSummaries) {
            rules.add(client.getRule(r -> r.ruleId(ruleSummary.ruleId())).rule());
        }

        return rules;
    }

    @Override
    protected List<Rule> findAws(WafRegionalClient client, Map<String, String> filters) {
        List<Rule> rules = new ArrayList<>();

        try {
            rules.add(client.getRule(r -> r.ruleId(filters.get("rule-id"))).rule());
        } catch (WafNonexistentItemException ignore) {
            //ignore
        }

        return rules;
    }
}
