package gyro.aws.waf.regional;

import gyro.aws.Copyable;
import gyro.aws.waf.common.ConditionResource;
import gyro.aws.waf.common.AbstractRuleResource;
import software.amazon.awssdk.services.waf.model.Predicate;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

public class PredicateResource extends gyro.aws.waf.common.PredicateResource {
    @Override
    protected void savePredicate(Predicate predicate, boolean isDelete) {
        AbstractRuleResource parent = (AbstractRuleResource) parent();

        WafRegionalClient client = getRegionalClient();
        if (!parent.isRateRule()) {
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
