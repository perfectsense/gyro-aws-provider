package gyro.aws.waf.global;

import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.Predicate;

//@ResourceName(parent = "rule", value = "predicate-regular")
//@ResourceName(parent = "rate-rule", value = "predicate-rate-based")
public class PredicateResource extends gyro.aws.waf.common.PredicateResource {
    @Override
    protected void savePredicate(Predicate predicate, boolean isDelete) {
        WafClient client = getGlobalClient();
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
