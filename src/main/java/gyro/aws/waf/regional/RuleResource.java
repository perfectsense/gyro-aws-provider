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

import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.waf.model.CreateRuleResponse;
import software.amazon.awssdk.services.waf.model.Predicate;
import software.amazon.awssdk.services.waf.model.Rule;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Creates a regional rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::waf-rule-regional rule-example
 *         name: "rule-example"
 *         metric-name: "ruleExample"
 *
 *         predicate
 *             condition: $(aws::regex-match-set-regional regex-match-set-example-waf)
 *         end
 *     end
 */
@Type("waf-rule-regional")
public class RuleResource extends gyro.aws.waf.common.RuleResource {
    private Set<PredicateResource> predicate;

    /**
     * A set of predicates specifying the connection between rule and conditions.
     *
     * @subresource gyro.aws.waf.regional.PredicateResource
     */
    @Updatable
    public Set<PredicateResource> getPredicate() {
        if (predicate == null) {
            predicate = new HashSet<>();
        }

        return predicate;
    }

    public void setPredicate(Set<PredicateResource> predicate) {
        this.predicate = predicate;

        if (predicate.size() > 10) {
            throw new GyroException("Predicate limit exception. Max 10.");
        }
    }

    @Override
    protected Rule getRule() {
        return getRegionalClient().getRule(
            r -> r.ruleId(getRuleId())
        ).rule();
    }

    @Override
    public void create(GyroUI ui, State state) {
        WafRegionalClient client = getRegionalClient();

        CreateRuleResponse response = client.createRule(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
                .metricName(getMetricName())
        );

        setRuleId(response.rule().ruleId());
    }

    @Override
    public void delete(GyroUI ui, State state) {
        WafRegionalClient client = getRegionalClient();

        client.deleteRule(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .ruleId(getRuleId())
        );
    }

    @Override
    protected void loadPredicates(List<Predicate> predicates) {
        getPredicate().clear();

        for (Predicate predicate: predicates) {
            PredicateResource predicateResource = newSubresource(PredicateResource.class);
            predicateResource.copyFrom(predicate);
            getPredicate().add(predicateResource);
        }
    }
}
