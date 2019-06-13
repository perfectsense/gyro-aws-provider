package gyro.aws.waf.global;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.RateBasedRule;
import software.amazon.awssdk.services.waf.model.RuleSummary;
import software.amazon.awssdk.services.waf.model.WafNonexistentItemException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query rate rule.
 *
 * .. code-block:: gyro
 *
 *    rate-rules: $(aws::rate-rule EXTERNAL/* | rule-id = '')
 */
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

        if (filters.containsKey("rule-id") && !ObjectUtils.isBlank(filters.get("rile-id"))) {
            try {
                rules.add(client.getRateBasedRule(r -> r.ruleId(filters.get("rule-id"))).rule());
            } catch (WafNonexistentItemException ignore) {
                //ignore
            }
        }

        return rules;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }
}
