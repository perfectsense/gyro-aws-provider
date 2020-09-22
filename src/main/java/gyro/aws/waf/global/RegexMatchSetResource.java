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

package gyro.aws.waf.global;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateRegexMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetRegexMatchSetResponse;
import software.amazon.awssdk.services.waf.model.RegexMatchSet;
import software.amazon.awssdk.services.waf.model.RegexMatchTuple;

import java.util.HashSet;
import java.util.Set;

/**
 * Creates a global regex match set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::waf-regex-pattern-set regex-pattern-set-match-set-example
 *         name: "regex-pattern-set-match-set-example"
 *
 *         patterns: [
 *             "pattern1",
 *             "pattern2"
 *         ]
 *     end
 *
 *     aws::waf-regex-match-set regex-match-set-example
 *         name: "regex-match-set-example"
 *         regex-match-tuple
 *             field-to-match
 *                 type: "METHOD"
 *             end
 *             text-transformation: "NONE"
 *             regex-pattern-set: $(aws::regex-pattern-set regex-pattern-set-match-set-example)
 *         end
 *     end
 */
@Type("waf-regex-match-set")
public class RegexMatchSetResource extends gyro.aws.waf.common.RegexMatchSetResource {
    private Set<RegexMatchTupleResource> regexMatchTuple;

    /**
     * List of regex match tuple data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.global.RegexMatchTupleResource
     */
    @Required
    @Updatable
    public Set<RegexMatchTupleResource> getRegexMatchTuple() {
        if (regexMatchTuple == null) {
            regexMatchTuple = new HashSet<>();
        }

        return regexMatchTuple;
    }

    public void setRegexMatchTuple(Set<RegexMatchTupleResource> regexMatchTuple) {
        this.regexMatchTuple = regexMatchTuple;

        if (regexMatchTuple.size() > 1) {
            throw new GyroException("Regex Match Tuple limit exception. Max 1 per Regex Match Set.");
        }
    }

    @Override
    public void copyFrom(RegexMatchSet regexMatchSet) {
        setId(regexMatchSet.regexMatchSetId());
        setName(regexMatchSet.name());

        getRegexMatchTuple().clear();

        for (RegexMatchTuple regexMatchTuple : regexMatchSet.regexMatchTuples()) {
            RegexMatchTupleResource regexMatchTupleResource = newSubresource(RegexMatchTupleResource.class);
            regexMatchTupleResource.copyFrom(regexMatchTuple);
            getRegexMatchTuple().add(regexMatchTupleResource);
        }
    }

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getId())) {
            return false;
        }

        GetRegexMatchSetResponse response = getGlobalClient().getRegexMatchSet(
                r -> r.regexMatchSetId(getId())
            );

        this.copyFrom(response.regexMatchSet());

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        WafClient client = getGlobalClient();

        CreateRegexMatchSetResponse response = client.createRegexMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setId(response.regexMatchSet().regexMatchSetId());
    }

    @Override
    public void delete(GyroUI ui, State state) {
        WafClient client = getGlobalClient();

        client.deleteRegexMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .regexMatchSetId(getId())
        );
    }

    RegexMatchSet getRegexMatchSet(WafClient client) {
        return client.getRegexMatchSet(r -> r.regexMatchSetId(getId())).regexMatchSet();
    }
}
