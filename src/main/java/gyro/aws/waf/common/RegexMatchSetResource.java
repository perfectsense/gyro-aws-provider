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

import gyro.aws.Copyable;
import software.amazon.awssdk.services.waf.model.PredicateType;
import software.amazon.awssdk.services.waf.model.RegexMatchSet;

public abstract class RegexMatchSetResource extends ConditionResource implements Copyable<RegexMatchSet> {
    @Override
    String getDisplayName() {
        return "waf regex match set";
    }

    @Override
    protected String getType() {
        return PredicateType.REGEX_MATCH.toString();
    }
}
