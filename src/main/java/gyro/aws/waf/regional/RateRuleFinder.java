package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import software.amazon.awssdk.services.waf.model.ListRateBasedRulesRequest;
import software.amazon.awssdk.services.waf.model.ListRateBasedRulesResponse;
import software.amazon.awssdk.services.waf.model.RateBasedRule;
import software.amazon.awssdk.services.waf.model.RuleSummary;
import software.amazon.awssdk.services.waf.model.WafNonexistentItemException;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query rate rule regional.
 *
 * .. code-block:: gyro
 *
 *    rate-rules: $(external-query aws::waf-rate-rule-regional)
 */
@Type("waf-rate-rule-regional")
public class RateRuleFinder extends gyro.aws.waf.common.RateRuleFinder<WafRegionalClient, RateRuleResource> {
    @Override
    protected List<RateBasedRule> findAllAws(WafRegionalClient client) {
        List<RateBasedRule> rules = new ArrayList<>();

        String marker = null;
        ListRateBasedRulesResponse response;
        List<RuleSummary> ruleSummaries = new ArrayList<>();

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listRateBasedRules();
            } else {
                response = client.listRateBasedRules(ListRateBasedRulesRequest.builder().nextMarker(marker).build());
            }

            marker = response.nextMarker();
            ruleSummaries.addAll(response.rules());

        } while (!ObjectUtils.isBlank(marker));

        for (RuleSummary ruleSummary : ruleSummaries) {
            rules.add(client.getRateBasedRule(r -> r.ruleId(ruleSummary.ruleId())).rule());
        }

        return rules;
    }

    @Override
    protected List<RateBasedRule> findAws(WafRegionalClient client, Map<String, String> filters) {
        List<RateBasedRule> rules = new ArrayList<>();

        try {
            rules.add(client.getRateBasedRule(r -> r.ruleId(filters.get("id"))).rule());
        } catch (WafNonexistentItemException ignore) {
            //ignore
        }

        return rules;
    }
}
