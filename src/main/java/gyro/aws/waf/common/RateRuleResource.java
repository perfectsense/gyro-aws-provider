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

package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.waf.model.RateBasedRule;
import software.amazon.awssdk.services.waf.model.WafRuleType;

public abstract class RateRuleResource extends CommonRuleResource implements Copyable<RateBasedRule> {
    private String rateKey;
    private Long rateLimit;

    /**
     * The rate key based on which the rate would be checked. (Required)
     */
    @Required
    public String getRateKey() {
        return rateKey != null ? rateKey.toUpperCase() : null;
    }

    public void setRateKey(String rateKey) {
        this.rateKey = rateKey;
    }

    /**
     * The rate limit at which the action would be taken. Valid values integer ``2000`` and above. (Required)
     */
    @Required
    @Updatable
    public Long getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(Long rateLimit) {
        this.rateLimit = rateLimit;
    }

    protected abstract RateBasedRule getRule();

    @Override
    public void copyFrom(RateBasedRule rule) {
        setRuleId(rule.ruleId());
        setName(rule.name());
        setMetricName(rule.metricName());
        setRateKey(rule.rateKeyAsString());
        setRateLimit(rule.rateLimit());
        loadPredicates(rule.matchPredicates());
    }

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getRuleId())) {
            return false;
        }

        copyFrom(getRule());

        return true;
    }

    @Override
    public boolean isRateRule() {
        return true;
    }

    @Override
    protected String getType() {
        return WafRuleType.RATE_BASED.name();
    }
}
