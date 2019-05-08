package gyro.aws.waf.regional;

import software.amazon.awssdk.services.waf.model.CreateRuleResponse;
import software.amazon.awssdk.services.waf.model.Rule;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

//@ResourceName("rule")
public class RuleResource extends gyro.aws.waf.common.RuleResource {
    @Override
    protected Rule getRule() {
        return getGlobalClient().getRule(
            r -> r.ruleId(getRuleId())
        ).rule();
    }

    @Override
    public void create() {
        WafRegionalClient client = getRegionalClient();

        CreateRuleResponse response = client.createRule(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
                .metricName(getMetricName())
        );

        setRuleId(response.rule().ruleId());
    }

    @Override
    public void delete() {
        WafRegionalClient client = getRegionalClient();

        client.deleteRule(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .ruleId(getRuleId())
        );
    }
}
