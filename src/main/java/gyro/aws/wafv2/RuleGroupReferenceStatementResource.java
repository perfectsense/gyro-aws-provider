/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws.wafv2;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.wafv2.model.ExcludedRule;
import software.amazon.awssdk.services.wafv2.model.RuleGroupReferenceStatement;

public class RuleGroupReferenceStatementResource extends Diffable implements Copyable<RuleGroupReferenceStatement> {

    private RuleGroupResource ruleGroup;
    private Set<String> excludedRules;

    /**
     * A rule group resource to reference with. (Required)
     */
    @Required
    public RuleGroupResource getRuleGroup() {
        return ruleGroup;
    }

    public void setRuleGroup(RuleGroupResource ruleGroup) {
        this.ruleGroup = ruleGroup;
    }

    /**
     * A set of rule names to be excluded that are part of the referenced rule group resource.
     */
    @Updatable
    public Set<String> getExcludedRules() {
        if (excludedRules == null) {
            excludedRules = new HashSet<>();
        }

        return excludedRules;
    }

    public void setExcludedRules(Set<String> excludedRules) {
        this.excludedRules = excludedRules;
    }

    @Override
    public String primaryKey() {
        return String.format("referencing rule - '%s'", getRuleGroup().getArn());
    }

    @Override
    public void copyFrom(RuleGroupReferenceStatement ruleGroupReferenceStatement) {
        getExcludedRules().clear();
        if (ruleGroupReferenceStatement.excludedRules() != null) {
            setExcludedRules(ruleGroupReferenceStatement.excludedRules()
                .stream()
                .map(ExcludedRule::name)
                .collect(Collectors.toSet()));
        }

        setRuleGroup(findById(RuleGroupResource.class, ruleGroupReferenceStatement.arn()));
    }

    RuleGroupReferenceStatement toRuleGroupReferenceStatement() {
        RuleGroupReferenceStatement.Builder builder = RuleGroupReferenceStatement.builder()
            .arn(getRuleGroup().getArn());

        if (!getExcludedRules().isEmpty()) {
            builder = builder.excludedRules(getExcludedRules().stream()
                .map(o -> ExcludedRule.builder().name(o).build())
                .collect(Collectors.toList()));
        }

        return builder.build();
    }
}
