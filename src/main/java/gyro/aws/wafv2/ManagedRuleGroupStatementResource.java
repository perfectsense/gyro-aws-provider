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
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.wafv2.model.ExcludedRule;
import software.amazon.awssdk.services.wafv2.model.ManagedRuleGroupStatement;

public class ManagedRuleGroupStatementResource extends WafDiffable implements Copyable<ManagedRuleGroupStatement> {

    private Set<String> excludedRules;
    private String name;
    private String vendorName;

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
     * The name of the managed rule group. (Required)
     */
    @Required
    @Updatable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The vendor name of the managed rule group. (Required)
     */
    @Required
    @Updatable
    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
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

        setName(managedRuleGroupStatement.name());
        setVendorName(managedRuleGroupStatement.vendorName());
        setHashCode(managedRuleGroupStatement.hashCode());
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

        return builder.build();
    }
}
