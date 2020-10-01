/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.wafv2;

import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.CollectionMax;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.CreateRegexPatternSetResponse;
import software.amazon.awssdk.services.wafv2.model.GetRegexPatternSetResponse;
import software.amazon.awssdk.services.wafv2.model.Regex;
import software.amazon.awssdk.services.wafv2.model.RegexPatternSet;
import software.amazon.awssdk.services.wafv2.model.WafNonexistentItemException;

/**
 * Creates a regex pattern set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::wafv2-regex-pattern-set regex-pattern-set-example
 *         name: "regex-pattern-set-example"
 *         description: "regex-pattern-set-example-desc"
 *         scope: "REGIONAL"
 *         regular-expressions: [
 *             "regular-expression"
 *         ]
 *     end
 */
@Type("wafv2-regex-pattern-set")
public class RegexPatternSetResource extends WafTaggableResource implements Copyable<RegexPatternSet> {

    private String name;
    private String description;
    private Set<String> regularExpressions;
    private String id;
    private String arn;

    /**
     * The name of the regex pattern set. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The description of the regex pattern set.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The set of regular expressions to which the condition is going to be checked. (Required)
     */
    @Required
    @Updatable
    @CollectionMax(10)
    public Set<String> getRegularExpressions() {
        return regularExpressions;
    }

    public void setRegularExpressions(Set<String> regularExpressions) {
        this.regularExpressions = regularExpressions;
    }

    /**
     * The id of the regex pattern set.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The arn of the regex pattern set.
     */
    @Id
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public void copyFrom(RegexPatternSet regexPatternSet) {
        setArn(regexPatternSet.arn());
        setDescription(regexPatternSet.description());
        setId(regexPatternSet.id());
        setName(regexPatternSet.name());
        setRegularExpressions(regexPatternSet.regularExpressionList()
            .stream()
            .map(Regex::regexString)
            .collect(Collectors.toSet()));
    }

    @Override
    protected String getResourceArn() {
        return getArn();
    }

    @Override
    protected boolean doRefresh() {
        Wafv2Client client = createClient(Wafv2Client.class);

        GetRegexPatternSetResponse response = getRegexPatternSet(client);

        if (response != null) {
            copyFrom(response.regexPatternSet());

            return true;
        }

        return false;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        Wafv2Client client = createClient(Wafv2Client.class);

        CreateRegexPatternSetResponse response = client.createRegexPatternSet(
            r -> r.name(getName())
                .description(getDescription())
                .scope(getScope())
                .regularExpressionList(getRegularExpressions().stream()
                    .map(o -> Regex.builder().regexString(o).build())
                    .collect(Collectors.toList()))
        );

        setArn(response.summary().arn());
        setId(response.summary().id());
    }

    @Override
    protected void doUpdate(
        GyroUI ui, State state, Resource config, Set<String> changedProperties) {
        Wafv2Client client = createClient(Wafv2Client.class);

        client.updateRegexPatternSet(
            r -> r.id(getId())
                .name(getName())
                .scope(getScope())
                .lockToken(lockToken(client))
                .description(getDescription())
                .regularExpressionList(getRegularExpressions().stream()
                    .map(o -> Regex.builder().regexString(o).build())
                    .collect(Collectors.toList()))
        );
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Wafv2Client client = createClient(Wafv2Client.class);

        client.deleteRegexPatternSet(r -> r.id(getId()).name(getName()).scope(getScope()).lockToken(lockToken(client)));
    }

    private GetRegexPatternSetResponse getRegexPatternSet(Wafv2Client client) {
        try {
            return client.getRegexPatternSet(r -> r.id(getId()).name(getName()).scope(getScope()));
        } catch (WafNonexistentItemException ex) {
            return null;
        }
    }

    private String lockToken(Wafv2Client client) {
        String token = null;
        GetRegexPatternSetResponse response = getRegexPatternSet(client);

        if (response != null) {
            token = response.lockToken();
        }

        return token;
    }
}
