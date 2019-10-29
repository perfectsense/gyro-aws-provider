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
import software.amazon.awssdk.services.waf.model.ListRegexPatternSetsRequest;
import software.amazon.awssdk.services.waf.model.ListRegexPatternSetsResponse;
import software.amazon.awssdk.services.waf.model.RegexPatternSet;
import software.amazon.awssdk.services.waf.model.RegexPatternSetSummary;
import software.amazon.awssdk.services.waf.model.WafNonexistentItemException;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query regex pattern set regional.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    regex-pattern-sets: $(external-query aws::waf-regex-pattern-set-regional)
 */
@Type("waf-regex-pattern-set-regional")
public class RegexPatternSetFinder extends gyro.aws.waf.common.RegexPatternSetFinder<WafRegionalClient, RegexPatternSetResource> {
    @Override
    protected List<RegexPatternSet> findAllAws(WafRegionalClient client) {
        List<RegexPatternSet> regexPatternSets = new ArrayList<>();

        String marker = null;
        ListRegexPatternSetsResponse response;
        List<RegexPatternSetSummary> regexPatternSetSummaries = new ArrayList<>();

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listRegexPatternSets();
            } else {
                response = client.listRegexPatternSets(ListRegexPatternSetsRequest.builder().nextMarker(marker).build());
            }

            marker = response.nextMarker();
            regexPatternSetSummaries.addAll(response.regexPatternSets());

        } while (!ObjectUtils.isBlank(marker));

        for (RegexPatternSetSummary regexPatternSetSummary : regexPatternSetSummaries) {
            regexPatternSets.add(client.getRegexPatternSet(r -> r.regexPatternSetId(regexPatternSetSummary.regexPatternSetId())).regexPatternSet());
        }

        return regexPatternSets;
    }

    @Override
    protected List<RegexPatternSet> findAws(WafRegionalClient client, Map<String, String> filters) {
        List<RegexPatternSet> regexPatternSets = new ArrayList<>();

        try {
            regexPatternSets.add(client.getRegexPatternSet(r -> r.regexPatternSetId(filters.get("id"))).regexPatternSet());
        } catch (WafNonexistentItemException ignore) {
            //ignore
        }

        return regexPatternSets;
    }
}