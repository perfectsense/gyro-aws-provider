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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.wafv2.model.AllowAction;
import software.amazon.awssdk.services.wafv2.model.BlockAction;
import software.amazon.awssdk.services.wafv2.model.CountAction;
import software.amazon.awssdk.services.wafv2.model.NoneAction;
import software.amazon.awssdk.services.wafv2.model.OverrideAction;
import software.amazon.awssdk.services.wafv2.model.Rule;
import software.amazon.awssdk.services.wafv2.model.RuleAction;

public class RuleResource extends Diffable implements Copyable<Rule> {

    private String name;
    private Integer priority;
    private VisibilityConfigResource visibilityConfig;
    private WafDefaultAction.RuleAction action;
    private WafDefaultAction.OverrideAction overrideAction;
    private StatementResource statement;

    /**
     * The name of the rule.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The priority of the rule. The priority assigned needs to be ordered in increasing order starting from 0.
     */
    @Required
    @Updatable
    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * The visibility configuration for the rule.
     */
    @Required
    public VisibilityConfigResource getVisibilityConfig() {
        return visibilityConfig;
    }

    public void setVisibilityConfig(VisibilityConfigResource visibilityConfig) {
        this.visibilityConfig = visibilityConfig;
    }

    /**
     * The action to perform if the rule passes.
     */
    @Updatable
    @ValidStrings({"ALLOW", "BLOCK", "COUNT"})
    @ConflictsWith("override-action")
    public WafDefaultAction.RuleAction getAction() {
        return action;
    }

    public void setAction(WafDefaultAction.RuleAction action) {
        this.action = action;
    }

    /**
     * The override action to perform if the rule passes.
     */
    @Updatable
    @ValidStrings({"NONE", "COUNT"})
    @ConflictsWith("action")
    public WafDefaultAction.OverrideAction getOverrideAction() {
        return overrideAction;
    }

    public void setOverrideAction(WafDefaultAction.OverrideAction overrideAction) {
        this.overrideAction = overrideAction;
    }

    /**
     * The statement configuration having the individual conditions.
     *
     * @subresourse gyro.aws.wafv2.StatementResource
     */
    @Updatable
    public StatementResource getStatement() {
        return statement;
    }

    public void setStatement(StatementResource statement) {
        this.statement = statement;
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    @Override
    public void copyFrom(Rule rule) {
        setAction(evaluateAction(rule.action()));
        setOverrideAction(evaluateAction(rule.overrideAction()));
        setName(rule.name());
        setPriority(rule.priority());

        setStatement(null);
        if (rule.statement() != null) {
            StatementResource statement = newSubresource(StatementResource.class);
            statement.copyFrom(rule.statement());
            setStatement(statement);
        }

        VisibilityConfigResource visibilityConfig = newSubresource(VisibilityConfigResource.class);
        visibilityConfig.copyFrom(rule.visibilityConfig());
        setVisibilityConfig(visibilityConfig);
    }

    private WafDefaultAction.RuleAction evaluateAction(RuleAction ruleAction) {
        WafDefaultAction.RuleAction action = null;

        if (ruleAction != null) {
            action = WafDefaultAction.RuleAction.ALLOW;

            if (ruleAction.count() != null) {
                action = WafDefaultAction.RuleAction.COUNT;
            }

            if (ruleAction.block() != null) {
                action = WafDefaultAction.RuleAction.BLOCK;
            }
        }

        return action;
    }

    private WafDefaultAction.OverrideAction evaluateAction(OverrideAction overrideAction) {
        WafDefaultAction.OverrideAction action = null;

        if (overrideAction != null) {
            if (overrideAction.count() != null) {
                action = WafDefaultAction.OverrideAction.COUNT;
            } else {
                action = WafDefaultAction.OverrideAction.NONE;
            }
        }

        return action;
    }

    Rule toRule() {
        Rule.Builder builder = Rule.builder()
            .name(getName())
            .priority(getPriority())
            .visibilityConfig(getVisibilityConfig().toVisibilityConfig());

        if (getAction() != null) {
            builder = builder.action(toRuleAction());
        }

        if (getOverrideAction() != null) {
            builder = builder.overrideAction(toOverrideAction());
        }

        if (getStatement() != null) {
            builder = builder.statement(getStatement().toStatement());
        }

        return builder.build();
    }

    RuleAction toRuleAction() {
        RuleAction.Builder builder = RuleAction.builder();
        if (getAction() == WafDefaultAction.RuleAction.ALLOW) {
            builder = builder.allow(AllowAction.builder().build());
        } else if (getAction() == WafDefaultAction.RuleAction.BLOCK) {
            builder = builder.block(BlockAction.builder().build());
        } else if (getAction() == WafDefaultAction.RuleAction.COUNT) {
            builder = builder.count(CountAction.builder().build());
        }

        return builder.build();
    }

    OverrideAction toOverrideAction() {
        OverrideAction.Builder builder = OverrideAction.builder();

        if (getOverrideAction() == WafDefaultAction.OverrideAction.COUNT) {
            builder = builder.count(CountAction.builder().build());
        } else if (getOverrideAction() == WafDefaultAction.OverrideAction.NONE) {
            builder = builder.none(NoneAction.builder().build());
        }

        return builder.build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getStatement() != null) {
            if (getStatement().isRuleGroupReferenceStatement() && getOverrideAction() == null) {
                errors.add(new ValidationError(
                    this,
                    "statement",
                    "rule group reference statements requires the 'override-action' to be set for the rule."));
            } else if (!getStatement().isRuleGroupReferenceStatement() && getAction() == null) {
                errors.add(new ValidationError(
                    this,
                    "statement",
                    "non rule group reference statements requires the 'action' to be set for the rule."));
            }
        }

        return errors;
    }

    static boolean invalidPriority(Set<RuleResource> rules) {
        List<Integer> priorityList = rules.stream()
            .sorted(Comparator.comparing(RuleResource::getPriority))
            .map(RuleResource::getPriority)
            .collect(Collectors.toList());

        boolean invalidPriority = false;
        int start = 0;

        for (int priority : priorityList) {
            if (priority != start) {
                invalidPriority = true;
            }
            start++;
        }

        return invalidPriority;
    }
}
