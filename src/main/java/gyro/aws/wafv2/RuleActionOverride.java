/*
 * Copyright 2025, Brightspot.
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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.wafv2.model.AllowAction;
import software.amazon.awssdk.services.wafv2.model.BlockAction;
import software.amazon.awssdk.services.wafv2.model.CaptchaAction;
import software.amazon.awssdk.services.wafv2.model.ChallengeAction;
import software.amazon.awssdk.services.wafv2.model.CountAction;
import software.amazon.awssdk.services.wafv2.model.RuleAction;

public class RuleActionOverride
    extends Diffable implements Copyable<software.amazon.awssdk.services.wafv2.model.RuleActionOverride> {

    private String name;
    private WafDefaultAction.RuleAction actionToUse;
    private RuleActionCustomRequestHandlingResource customRequestHandling;
    private RuleActionCustomResponseResource customResponse;

    /**
     * The name of the rule to override
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The override action to use, in place of the configured action of the rule in the rule group.
     */
    @ValidStrings({"ALLOW", "BLOCK", "COUNT", "CAPTCHA", "CHALLENGE"})
    @Updatable
    public WafDefaultAction.RuleAction getActionToUse() {
        return actionToUse;
    }

    public void setActionToUse(WafDefaultAction.RuleAction actionToUse) {
        this.actionToUse = actionToUse;
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

    @Override
    public String primaryKey() {
        return String.format("Rule '%s', Action: '%s'", getName(), getActionToUse());
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.wafv2.model.RuleActionOverride ruleActionOverride) {
        setName(ruleActionOverride.name());
        setActionToUse(evaluateAction(ruleActionOverride.actionToUse()));
    }

    software.amazon.awssdk.services.wafv2.model.RuleActionOverride toRuleActionOverride() {
        return software.amazon.awssdk.services.wafv2.model.RuleActionOverride.builder()
            .name(getName())
            .actionToUse(toRuleAction())
            .build();
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
                    RuleActionCustomResponseResource customResponse =
                        newSubresource(RuleActionCustomResponseResource.class);
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

    RuleAction toRuleAction() {
        RuleAction.Builder builder = RuleAction.builder();
        if (getActionToUse() == WafDefaultAction.RuleAction.ALLOW) {
            AllowAction.Builder allowActionBuilder = AllowAction.builder();
            if (getCustomRequestHandling() != null) {
                allowActionBuilder = allowActionBuilder
                    .customRequestHandling(getCustomRequestHandling().toCustomRequestHandling());
            }
            builder = builder.allow(allowActionBuilder.build());
        } else if (getActionToUse() == WafDefaultAction.RuleAction.BLOCK) {
            BlockAction.Builder blockActionBuilder = BlockAction.builder();
            if (getCustomResponse() != null) {
                blockActionBuilder = blockActionBuilder
                    .customResponse(getCustomResponse().toCustomResponse());
            }
            builder = builder.block(blockActionBuilder.build());
        } else if (getActionToUse() == WafDefaultAction.RuleAction.COUNT) {
            CountAction.Builder countActionBuilder = CountAction.builder();
            if (getCustomRequestHandling() != null) {
                countActionBuilder = countActionBuilder
                    .customRequestHandling(getCustomRequestHandling().toCustomRequestHandling());
            }
            builder = builder.count(countActionBuilder.build());
        } else if (getActionToUse() == WafDefaultAction.RuleAction.CAPTCHA) {
            CaptchaAction.Builder captchaActionBuilder = CaptchaAction.builder();
            if (getCustomRequestHandling() != null) {
                captchaActionBuilder = captchaActionBuilder
                    .customRequestHandling(getCustomRequestHandling().toCustomRequestHandling());
            }
            builder = builder.captcha(captchaActionBuilder.build());
        } else if (getActionToUse() == WafDefaultAction.RuleAction.CHALLENGE) {
            ChallengeAction.Builder challengeActionBuilder = ChallengeAction.builder();
            if (getCustomRequestHandling() != null) {
                challengeActionBuilder = challengeActionBuilder
                    .customRequestHandling(getCustomRequestHandling().toCustomRequestHandling());
            }
            builder = builder.challenge(challengeActionBuilder.build());
        }

        return builder.build();
    }
}
