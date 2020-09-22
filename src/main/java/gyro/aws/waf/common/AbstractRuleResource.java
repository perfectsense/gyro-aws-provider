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

import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.waf.model.Predicate;

import java.util.List;

public abstract class AbstractRuleResource extends AbstractWafResource {
    private String name;
    private String metricName;
    private String ruleId;

    /**
     * The name of the rule. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The metric name of the rule. Can only contain letters and numbers. (Required)
     */
    @Required
    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    @Id
    @Output
    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public abstract boolean isRateRule();

    protected abstract void loadPredicates(List<Predicate> predicates);

}

