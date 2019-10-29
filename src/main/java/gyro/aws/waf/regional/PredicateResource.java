/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.waf.regional;

import gyro.aws.waf.common.AbstractRuleResource;
import software.amazon.awssdk.services.waf.model.Predicate;
import software.amazon.awssdk.services.waf.model.WafInvalidOperationException;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

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
        WafRegionalClient client = getRegionalClient();
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
