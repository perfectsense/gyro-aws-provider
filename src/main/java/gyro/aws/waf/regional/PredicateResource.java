package gyro.aws.waf.regional;

import software.amazon.awssdk.services.waf.model.Predicate;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

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
