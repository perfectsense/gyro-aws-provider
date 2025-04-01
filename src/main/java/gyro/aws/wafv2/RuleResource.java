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
import java.util.HashSet;
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
import software.amazon.awssdk.services.wafv2.model.CaptchaAction;
import software.amazon.awssdk.services.wafv2.model.ChallengeAction;
import software.amazon.awssdk.services.wafv2.model.CountAction;
import software.amazon.awssdk.services.wafv2.model.Label;
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
    private RuleActionCustomRequestHandlingResource customRequestHandling;
    private RuleActionCaptchaConfig captchaConfig;
    private RuleActionChallengeConfig challengeConfig;
    private RuleActionCustomResponseResource customResponse;
    private Set<String> actionLabels;

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
    @ValidStrings({"ALLOW", "BLOCK", "COUNT", "CAPTCHA", "CHALLENGE"})
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
     * @subresource gyro.aws.wafv2.StatementResource
     */
    @Updatable
    public StatementResource getStatement() {
        return statement;
    }

    public void setStatement(StatementResource statement) {
        this.statement = statement;
    }

    /**
     * The custom request handling configuration for the rule.
     *
     * @subresource gyro.aws.wafv2.RuleActionCustomRequestHandlingResource
     */
    @Updatable
    public RuleActionCustomRequestHandlingResource getCustomRequestHandling() {
        return customRequestHandling;
    }

    public void setCustomRequestHandling(RuleActionCustomRequestHandlingResource customRequestHandling) {
        this.customRequestHandling = customRequestHandling;
    }

    /**
     * The captcha configuration for the rule.
     *
     * @subresource gyro.aws.wafv2.RuleActionCaptchaConfig
     */
    @Updatable
    public RuleActionCaptchaConfig getCaptchaConfig() {
        return captchaConfig;
    }

    public void setCaptchaConfig(RuleActionCaptchaConfig captchaConfig) {
        this.captchaConfig = captchaConfig;
    }

    /**
     * The challenge configuration for the rule.
     *
     * @subresource gyro.aws.wafv2.RuleActionChallengeConfig
     */
    @Updatable
    public RuleActionChallengeConfig getChallengeConfig() {
        return challengeConfig;
    }

    public void setChallengeConfig(RuleActionChallengeConfig challengeConfig) {
        this.challengeConfig = challengeConfig;
    }

    /**
     * The custom response configuration for the rule.
     *
     * @subresource gyro.aws.wafv2.RuleActionCustomResponseResource
     */
    @Updatable
    public RuleActionCustomResponseResource getCustomResponse() {
        return customResponse;
    }

    public void setCustomResponse(RuleActionCustomResponseResource customResponse) {
        this.customResponse = customResponse;
    }

    /**
     * The action labels for the rule. The string containing the label name and optional prefix and namespaces.
     */
    public Set<String> getActionLabels() {
        if (actionLabels == null) {
            actionLabels = new HashSet<>();
        }

        return actionLabels;
    }

    public void setActionLabels(Set<String> actionLabels) {
        this.actionLabels = actionLabels;
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

        setCaptchaConfig(null);
        if (rule.captchaConfig() != null) {
            RuleActionCaptchaConfig captchaConfig = newSubresource(RuleActionCaptchaConfig.class);
            captchaConfig.copyFrom(rule.captchaConfig());
            setCaptchaConfig(captchaConfig);
        }

        setChallengeConfig(null);
        if (rule.challengeConfig() != null) {
            RuleActionChallengeConfig challengeConfig = newSubresource(RuleActionChallengeConfig.class);
            challengeConfig.copyFrom(rule.challengeConfig());
            setChallengeConfig(challengeConfig);
        }

        setActionLabels(null);
        if (rule.ruleLabels() != null) {
            setActionLabels(rule.ruleLabels().stream()
                .map(Label::name).collect(Collectors.toSet()));
        }
    }

    private WafDefaultAction.RuleAction evaluateAction(RuleAction ruleAction) {
        WafDefaultAction.RuleAction action = null;
        setCustomRequestHandling(null);

        if (ruleAction != null) {

            if (ruleAction.allow() != null) {
                action = WafDefaultAction.RuleAction.ALLOW;

                if (ruleAction.allow().customRequestHandling() != null) {
                    RuleActionCustomRequestHandlingResource customRequestHandling = newSubresource(
                        RuleActionCustomRequestHandlingResource.class);
                    customRequestHandling.copyFrom(ruleAction.allow().customRequestHandling());
                    setCustomRequestHandling(customRequestHandling);
                }
            }

            if (ruleAction.count() != null) {
                action = WafDefaultAction.RuleAction.COUNT;

                if (ruleAction.count().customRequestHandling() != null) {
                    RuleActionCustomRequestHandlingResource customRequestHandling = newSubresource(
                        RuleActionCustomRequestHandlingResource.class);
                    customRequestHandling.copyFrom(ruleAction.count().customRequestHandling());
                    setCustomRequestHandling(customRequestHandling);
                }
            }

            if (ruleAction.block() != null) {
                action = WafDefaultAction.RuleAction.BLOCK;

                if (ruleAction.block().customResponse() != null) {
                    RuleActionCustomResponseResource customResponse = newSubresource(RuleActionCustomResponseResource.class);
                    customResponse.copyFrom(ruleAction.block().customResponse());
                    setCustomResponse(customResponse);
                }
            }

            if (ruleAction.captcha() != null) {
                action = WafDefaultAction.RuleAction.CAPTCHA;

                if (ruleAction.captcha().customRequestHandling() != null) {
                    RuleActionCustomRequestHandlingResource customRequestHandling = newSubresource(
                        RuleActionCustomRequestHandlingResource.class);
                    customRequestHandling.copyFrom(ruleAction.captcha().customRequestHandling());
                    setCustomRequestHandling(customRequestHandling);
                }
            }

            if (ruleAction.challenge() != null) {
                action = WafDefaultAction.RuleAction.CHALLENGE;

                if (ruleAction.challenge().customRequestHandling() != null) {
                    RuleActionCustomRequestHandlingResource customRequestHandling = newSubresource(
                        RuleActionCustomRequestHandlingResource.class);
                    customRequestHandling.copyFrom(ruleAction.challenge().customRequestHandling());
                    setCustomRequestHandling(customRequestHandling);
                }
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

        if (getCaptchaConfig() != null) {
            builder = builder.captchaConfig(getCaptchaConfig().toCaptchaConfig());
        }

        if (getChallengeConfig() != null) {
            builder = builder.challengeConfig(getChallengeConfig().toChallengeConfig());
        }

        if (getActionLabels() != null) {
            builder = builder.ruleLabels(getActionLabels().stream()
                .map(l -> Label.builder().name(l).build())
                .collect(Collectors.toList()));
        }

        return builder.build();
    }

    RuleAction toRuleAction() {
        RuleAction.Builder builder = RuleAction.builder();
        if (getAction() == WafDefaultAction.RuleAction.ALLOW) {
            AllowAction.Builder allowActionBuilder = AllowAction.builder();
            if (getCustomRequestHandling() != null) {
                allowActionBuilder = allowActionBuilder
                    .customRequestHandling(getCustomRequestHandling().toCustomRequestHandling());
            }
            builder = builder.allow(allowActionBuilder.build());
        } else if (getAction() == WafDefaultAction.RuleAction.BLOCK) {
            BlockAction.Builder blockActionBuilder = BlockAction.builder();
            if (getCustomResponse() != null) {
                blockActionBuilder = blockActionBuilder
                    .customResponse(getCustomResponse().toCustomResponse());
            }
            builder = builder.block(blockActionBuilder.build());
        } else if (getAction() == WafDefaultAction.RuleAction.COUNT) {
            CountAction.Builder countActionBuilder = CountAction.builder();
            if (getCustomRequestHandling() != null) {
                countActionBuilder = countActionBuilder
                    .customRequestHandling(getCustomRequestHandling().toCustomRequestHandling());
            }
            builder = builder.count(countActionBuilder.build());
        } else if (getAction() == WafDefaultAction.RuleAction.CAPTCHA) {
            CaptchaAction.Builder captchaActionBuilder = CaptchaAction.builder();
            if (getCustomRequestHandling() != null) {
                captchaActionBuilder = captchaActionBuilder
                    .customRequestHandling(getCustomRequestHandling().toCustomRequestHandling());
            }
            builder = builder.captcha(captchaActionBuilder.build());
        } else if (getAction() == WafDefaultAction.RuleAction.CHALLENGE) {
            ChallengeAction.Builder challengeActionBuilder = ChallengeAction.builder();
            if (getCustomRequestHandling() != null) {
                challengeActionBuilder = challengeActionBuilder
                    .customRequestHandling(getCustomRequestHandling().toCustomRequestHandling());
            }
            builder = builder.challenge(challengeActionBuilder.build());
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

        if (getCustomRequestHandling() != null
            && (getAction() == null
            || getAction() == WafDefaultAction.RuleAction.BLOCK)) {
            errors.add(new ValidationError(
                this,
                "custom-request-handling",
                "'custom-request-handling' can only be set when 'action' is set to 'CAPTCHA', 'CHALLENGE', 'COUNT' or 'ALLOW'."));
        }

        if (getCustomResponse() != null
            && (getAction() == null
            || getAction() != WafDefaultAction.RuleAction.BLOCK)) {
            errors.add(new ValidationError(
                this,
                "custom-response",
                "'custom-response' can only be set when 'action' is set to 'BLOCK'."));
        }

        if (getCaptchaConfig() != null
            && (getAction() == null
            || getAction() != WafDefaultAction.RuleAction.CAPTCHA)) {
            errors.add(new ValidationError(
                this,
                "captcha-config",
                "'captcha-config' can only be set when 'action' is set to 'CAPTCHA'."));
        }

        if (getChallengeConfig() != null
            && (getAction() == null
            || getAction() != WafDefaultAction.RuleAction.CHALLENGE)) {
            errors.add(new ValidationError(
                this,
                "challenge-config",
                "'challenge-config' can only be set when 'action' is set to 'CHALLENGE'."));
        }

        return errors;
    }
}
