package gyro.aws.waf.global;

import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.Predicate;

public class PredicateResource extends gyro.aws.waf.common.PredicateResource {
    public PredicateResource() {

    }

    public PredicateResource(Predicate predicate, boolean isRateRule) {
        setDataId(predicate.dataId());
        setNegated(predicate.negated());
        setType(predicate.typeAsString());
        setRateRule(isRateRule);
    }

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
