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

import gyro.aws.waf.common.CommonRegexPatternSet;
import gyro.core.Type;
import software.amazon.awssdk.services.waf.model.CreateRegexPatternSetResponse;
import software.amazon.awssdk.services.waf.model.RegexMatchSet;
import software.amazon.awssdk.services.waf.model.RegexMatchSetSummary;
import software.amazon.awssdk.services.waf.model.RegexPatternSet;
import software.amazon.awssdk.services.waf.model.UpdateRegexPatternSetRequest;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.List;
import java.util.Set;

/**
 * Creates a regex pattern set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::waf-regex-pattern-set-regional regex-pattern-set-example
 *         name: "regex-pattern-set-example"
 *
 *         patterns: [
 *             "pattern1",
 *             "pattern2"
 *         ]
 *     end
 */
@Type("waf-regex-pattern-set-regional")
public class RegexPatternSetResource extends CommonRegexPatternSet {
    @Override
    protected void doCreate() {
        WafRegionalClient client = getRegionalClient();

        CreateRegexPatternSetResponse response = client.createRegexPatternSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setRegexPatternSetId(response.regexPatternSet().regexPatternSetId());
    }

    @Override
    protected void savePatterns(Set<String> oldPatterns, Set<String> newPatterns) {
        WafRegionalClient client = getRegionalClient();

        UpdateRegexPatternSetRequest regexPatternSetRequest = toUpdateRegexPatternSetRequest(oldPatterns, newPatterns)
            .changeToken(client.getChangeToken().changeToken())
            .build();


        if (!regexPatternSetRequest.updates().isEmpty()) {
            client.updateRegexPatternSet(regexPatternSetRequest);
        }
    }

    @Override
    protected void deleteRegexPatternSet() {
        WafRegionalClient client = getRegionalClient();

        client.deleteRegexPatternSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .regexPatternSetId(getRegexPatternSetId())
        );
    }

    @Override
    protected List<RegexMatchSetSummary> getRegexMatchSetSummaries() {
        return getRegionalClient().listRegexMatchSets().regexMatchSets();
    }

    @Override
    protected RegexMatchSet getRegexMatchSet(String regexMatchSetId) {
        return getGlobalClient().getRegexMatchSet(
            r -> r.regexMatchSetId(regexMatchSetId)
        ).regexMatchSet();
    }

    @Override
    protected RegexPatternSet getRegexPatternSet() {
        return getRegionalClient().getRegexPatternSet(
            r -> r.regexPatternSetId(getRegexPatternSetId())
        ).regexPatternSet();
    }
}
