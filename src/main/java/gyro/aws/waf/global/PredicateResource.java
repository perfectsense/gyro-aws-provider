package gyro.aws.waf.global;

import gyro.aws.waf.common.AbstractRuleResource;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.Predicate;
import software.amazon.awssdk.services.waf.model.WafInvalidOperationException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PredicateResource extends gyro.aws.waf.common.PredicateResource {
    @Override
    protected void savePredicate(Predicate predicate, boolean isDelete) {
        AbstractRuleResource parent = (AbstractRuleResource) parent();

        if (!isDelete) {
            handleMaxPredicate(parent);
        }

        saveDeletePredicate(parent.isRateRule(), predicate, isDelete);
    }

    private void handleMaxPredicate(AbstractRuleResource rule) {
        List<Predicate> predicates;
        Set<PredicateResource> predicateResources;
        if (rule instanceof RuleResource) {
            RuleResource parent = (RuleResource) rule;
            predicates = parent.getRule().predicates();
            predicateResources = parent.getPredicate();
        } else {
            RateRuleResource parent = (RateRuleResource) rule;
            predicates = parent.getRule().matchPredicates();
            predicateResources = parent.getPredicate();
        }

        if (predicates.size() == 10) {
            Set<String> finalConfiguredPredicateIds = predicateResources.stream().map(o -> o.getCondition().getId()).collect(Collectors.toSet());;
            Predicate predicate = predicates.stream().filter(o -> !finalConfiguredPredicateIds.contains(o.dataId())).findFirst().orElse(null);

            saveDeletePredicate(rule.isRateRule(), predicate, true);
        }
    }

    private void saveDeletePredicate(boolean isRateRule, Predicate predicate, boolean isDelete) {
        WafClient client = getGlobalClient();
        if (predicate != null) {
            try {
                if (!isRateRule) {
                    client.updateRule(toUpdateRuleRequest(predicate, isDelete)
                        .changeToken(client.getChangeToken().changeToken())
                        .build()
                    );
                } else {
                    client.updateRateBasedRule(toUpdateRateBasedRuleRequest(predicate, isDelete)
                        .changeToken(client.getChangeToken().changeToken())
                        .build()
                    );
                }
            } catch (WafInvalidOperationException ex) {
                if (!isDelete || !ex.awsErrorDetails().errorCode().equals("WAFInvalidOperationException")) {
                    throw ex;
                }
            }
        }
    }
}
