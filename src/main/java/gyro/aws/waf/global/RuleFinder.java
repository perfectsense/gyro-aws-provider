package gyro.aws.waf.global;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ListRulesRequest;
import software.amazon.awssdk.services.waf.model.ListRulesResponse;
import software.amazon.awssdk.services.waf.model.Rule;
import software.amazon.awssdk.services.waf.model.RuleSummary;
import software.amazon.awssdk.services.waf.model.WafNonexistentItemException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    rules: $(external-query aws::waf-rule)
 */
@Type("waf-rule")
public class RuleFinder extends gyro.aws.waf.common.RuleFinder<WafClient, RuleResource> {
    @Override
    protected List<Rule> findAllAws(WafClient client) {
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
    protected List<Rule> findAws(WafClient client, Map<String, String> filters) {
        List<Rule> rules = new ArrayList<>();

        try {
            rules.add(client.getRule(r -> r.ruleId(filters.get("id"))).rule());
        } catch (WafNonexistentItemException ignore) {
            //ignore
        }

        return rules;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }
}
