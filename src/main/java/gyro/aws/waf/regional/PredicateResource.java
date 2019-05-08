package gyro.aws.waf.regional;

import software.amazon.awssdk.services.waf.model.Predicate;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

//@ResourceName(parent = "rule", value = "predicate-regular")
//@ResourceName(parent = "rate-rule", value = "predicate-rate-based")
public class PredicateResource extends gyro.aws.waf.common.PredicateResource {
    @Override
    protected void savePredicate(Predicate predicate, boolean isDelete) {
        WafRegionalClient client = getRegionalClient();

        if (!getRateRule()) {
            client.updateRule(getUpdateRuleRequest(predicate, isDelete)
                .changeToken(client.getChangeToken().changeToken())
                .build()
            );
        } else {
            client.updateRateBasedRule(getUpdateRateBasedRuleRequest(predicate, isDelete)
                .changeToken(client.getChangeToken().changeToken())
                .build()
            );
        }
    }
}
