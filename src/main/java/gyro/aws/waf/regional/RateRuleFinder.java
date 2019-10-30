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

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import software.amazon.awssdk.services.waf.model.ListRateBasedRulesRequest;
import software.amazon.awssdk.services.waf.model.ListRateBasedRulesResponse;
import software.amazon.awssdk.services.waf.model.RateBasedRule;
import software.amazon.awssdk.services.waf.model.RuleSummary;
import software.amazon.awssdk.services.waf.model.WafNonexistentItemException;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query rate rule regional.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    rate-rules: $(external-query aws::waf-rate-rule-regional)
 */
@Type("waf-rate-rule-regional")
public class RateRuleFinder extends gyro.aws.waf.common.RateRuleFinder<WafRegionalClient, RateRuleResource> {
    @Override
    protected List<RateBasedRule> findAllAws(WafRegionalClient client) {
        List<RateBasedRule> rules = new ArrayList<>();

        String marker = null;
        ListRateBasedRulesResponse response;
        List<RuleSummary> ruleSummaries = new ArrayList<>();

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listRateBasedRules();
            } else {
                response = client.listRateBasedRules(ListRateBasedRulesRequest.builder().nextMarker(marker).build());
            }

            marker = response.nextMarker();
            ruleSummaries.addAll(response.rules());

        } while (!ObjectUtils.isBlank(marker));

        for (RuleSummary ruleSummary : ruleSummaries) {
            rules.add(client.getRateBasedRule(r -> r.ruleId(ruleSummary.ruleId())).rule());
        }

        return rules;
    }

    @Override
    protected List<RateBasedRule> findAws(WafRegionalClient client, Map<String, String> filters) {
        List<RateBasedRule> rules = new ArrayList<>();

        try {
            rules.add(client.getRateBasedRule(r -> r.ruleId(filters.get("id"))).rule());
        } catch (WafNonexistentItemException ignore) {
            //ignore
        }

        return rules;
    }
}
