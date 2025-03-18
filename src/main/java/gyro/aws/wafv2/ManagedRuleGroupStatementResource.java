/*
 * Copyright 2020, Brightspot.
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
import software.amazon.awssdk.services.wafv2.model.ManagedRuleGroupStatement;

public class ManagedRuleGroupStatementResource extends Diffable implements Copyable<ManagedRuleGroupStatement> {

    private Set<String> excludedRules;
    private String name;
    private String vendorName;
    private Set<RuleActionOverride> ruleActionOverrides;

    /**
     * A set of rule names to be excluded that are part of the associated managed rule group.
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

    /**
     * The name of the managed rule group.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The vendor name of the managed rule group.
     */
    @Required
    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    /**
     * A set of rule action overrides to apply to the associated managed rule group.
     */
    public Set<RuleActionOverride> getRuleActionOverrides() {
        if (ruleActionOverrides == null) {
            ruleActionOverrides = new HashSet<>();
        }

        return ruleActionOverrides;
    }

    public void setRuleActionOverrides(Set<RuleActionOverride> ruleActionOverrides) {
        this.ruleActionOverrides = ruleActionOverrides;
    }

    @Override
    public String primaryKey() {
        return String.format("with name - '%s' and vendor - '%s'", getName(), getVendorName());
    }

    @Override
    public void copyFrom(ManagedRuleGroupStatement managedRuleGroupStatement) {
        getExcludedRules().clear();
        if (managedRuleGroupStatement.excludedRules() != null) {
            setExcludedRules(managedRuleGroupStatement.excludedRules()
                .stream()
                .map(ExcludedRule::name)
                .collect(Collectors.toSet()));
        }

        getRuleActionOverrides().clear();
        if (managedRuleGroupStatement.ruleActionOverrides() != null) {
            setRuleActionOverrides(managedRuleGroupStatement.ruleActionOverrides()
                .stream()
                .map(o -> {
                    RuleActionOverride ruleActionOverride = newSubresource(RuleActionOverride.class);
                    ruleActionOverride.copyFrom(o);
                    return ruleActionOverride;
                })
                .collect(Collectors.toSet()));
        }

        setName(managedRuleGroupStatement.name());
        setVendorName(managedRuleGroupStatement.vendorName());
    }

    ManagedRuleGroupStatement toManagedRuleGroupStatement() {
        ManagedRuleGroupStatement.Builder builder = ManagedRuleGroupStatement.builder()
            .name(getName())
            .vendorName(getVendorName());

        if (!getExcludedRules().isEmpty()) {
            builder = builder.excludedRules(getExcludedRules().stream()
                .map(o -> ExcludedRule.builder().name(o).build())
                .collect(Collectors.toList()));
        }

        if (!getRuleActionOverrides().isEmpty()) {
            builder = builder.ruleActionOverrides(getRuleActionOverrides().stream()
                .map(RuleActionOverride::toRuleActionOverride)
                .collect(Collectors.toList()));
        }

        return builder.build();
    }
}
