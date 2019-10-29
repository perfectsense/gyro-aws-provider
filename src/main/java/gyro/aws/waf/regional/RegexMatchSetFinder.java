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
import software.amazon.awssdk.services.waf.model.ListRegexMatchSetsRequest;
import software.amazon.awssdk.services.waf.model.ListRegexMatchSetsResponse;
import software.amazon.awssdk.services.waf.model.RegexMatchSet;
import software.amazon.awssdk.services.waf.model.RegexMatchSetSummary;
import software.amazon.awssdk.services.waf.model.WafNonexistentItemException;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query regex match set regional.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    regex-match-sets: $(external-query aws::waf-regex-match-set-regional)
 */
@Type("waf-regex-match-set-regional")
public class RegexMatchSetFinder extends gyro.aws.waf.common.RegexMatchSetFinder<WafRegionalClient, RegexMatchSetResource> {
    @Override
    protected List<RegexMatchSet> findAllAws(WafRegionalClient client) {
        List<RegexMatchSet> regexMatchSets = new ArrayList<>();

        String marker = null;
        ListRegexMatchSetsResponse response;
        List<RegexMatchSetSummary> regexMatchSetSummaries = new ArrayList<>();

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listRegexMatchSets();
            } else {
                response = client.listRegexMatchSets(ListRegexMatchSetsRequest.builder().nextMarker(marker).build());
            }

            marker = response.nextMarker();
            regexMatchSetSummaries.addAll(response.regexMatchSets());

        } while (!ObjectUtils.isBlank(marker));

        for (RegexMatchSetSummary regexMatchSetSummary : regexMatchSetSummaries) {
            regexMatchSets.add(client.getRegexMatchSet(r -> r.regexMatchSetId(regexMatchSetSummary.regexMatchSetId())).regexMatchSet());
        }

        return regexMatchSets;
    }

    @Override
    protected List<RegexMatchSet> findAws(WafRegionalClient client, Map<String, String> filters) {
        List<RegexMatchSet> regexMatchSets = new ArrayList<>();

        try {
            regexMatchSets.add(client.getRegexMatchSet(r -> r.regexMatchSetId(filters.get("id"))).regexMatchSet());
        } catch (WafNonexistentItemException ignore) {
            //ignore
        }

        return regexMatchSets;
    }
}