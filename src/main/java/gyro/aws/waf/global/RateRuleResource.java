package gyro.aws.waf.global;

import gyro.core.resource.Resource;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateRateBasedRuleResponse;
import software.amazon.awssdk.services.waf.model.RateBasedRule;

import java.util.ArrayList;
import java.util.Set;

//@ResourceName("rate-rule")
public class RateRuleResource extends gyro.aws.waf.common.RateRuleResource {
    @Override
    protected RateBasedRule getRule() {
        return getGlobalClient().getRateBasedRule(r -> r.ruleId(getRuleId())).rule();
    }

    @Override
    public void create() {
        WafClient client = getGlobalClient();

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
        WafClient client = getGlobalClient();

        client.updateRateBasedRule(
            r -> r.ruleId(getRuleId())
                .changeToken(client.getChangeToken().changeToken())
                .rateLimit(getRateLimit())
                .updates(new ArrayList<>())
        );
    }

    @Override
    public void delete() {
        WafClient client = getGlobalClient();

        client.deleteRateBasedRule(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .ruleId(getRuleId())
        );
    }
}
