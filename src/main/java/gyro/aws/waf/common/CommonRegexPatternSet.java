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

import gyro.core.Type;
import software.amazon.awssdk.services.waf.model.RegexMatchSet;
import software.amazon.awssdk.services.waf.model.RegexMatchSetSummary;
import software.amazon.awssdk.services.waf.model.RegexPatternSet;

import java.util.List;
import java.util.Set;

@Type("common-pattern")
public class CommonRegexPatternSet extends RegexPatternSetResource {
    @Override
    protected void doCreate() {

    }

    @Override
    protected void savePatterns(Set<String> oldPatterns, Set<String> newPatterns) {

    }

    @Override
    protected void deleteRegexPatternSet() {

    }

    @Override
    protected List<RegexMatchSetSummary> getRegexMatchSetSummaries() {
        return null;
    }

    @Override
    protected RegexMatchSet getRegexMatchSet(String regexMatchSetId) {
        return null;
    }

    @Override
    protected RegexPatternSet getRegexPatternSet() {
        return null;
    }
}
