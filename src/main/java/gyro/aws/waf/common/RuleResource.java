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
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import software.amazon.awssdk.services.waf.model.Rule;
import software.amazon.awssdk.services.waf.model.WafRuleType;

import java.util.Set;

public abstract class RuleResource extends CommonRuleResource implements Copyable<Rule> {
    protected abstract Rule getRule();

    @Override
    public void copyFrom(Rule rule) {
        setRuleId(rule.ruleId());
        setMetricName(rule.metricName());
        setName(rule.name());
        loadPredicates(rule.predicates());
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
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {

    }

    @Override
    public boolean isRateRule() {
        return false;
    }

    @Override
    protected String getType() {
        return WafRuleType.REGULAR.name();
    }
}
