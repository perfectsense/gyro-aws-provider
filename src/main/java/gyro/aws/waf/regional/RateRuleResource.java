package gyro.aws.waf.regional;

import gyro.core.resource.Resource;
import software.amazon.awssdk.services.waf.model.CreateRateBasedRuleResponse;
import software.amazon.awssdk.services.waf.model.RateBasedRule;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.Set;

//@ResourceName("rate-rule")
public class RateRuleResource extends gyro.aws.waf.common.RateRuleResource {
    @Override
    protected RateBasedRule getRule() {
        return getRegionalClient().getRateBasedRule(r -> r.ruleId(getRuleId())).rule();
    }

    @Override
    public void create() {
        WafRegionalClient client = getRegionalClient();

        CreateRateBasedRuleResponse response = client.createRateBasedRule(
            r -> r.name(getName())
                .metricName(getMetricName())
                .changeToken(client.getChangeToken().changeToken())
                .rateKey(getRateKey())
                .rateLimit(getRateLimit())
        );

        setRuleId(response.rule().ruleId());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        WafRegionalClient client = getRegionalClient();

        client.updateRateBasedRule(
            r -> r.ruleId(getRuleId())
                .changeToken(client.getChangeToken().changeToken())
                .rateLimit(getRateLimit())
                .updates(new ArrayList<>())
        );
    }

    @Override
    public void delete() {
        WafRegionalClient client = getRegionalClient();

        client.deleteRateBasedRule(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .ruleId(getRuleId())
        );
    }
}
