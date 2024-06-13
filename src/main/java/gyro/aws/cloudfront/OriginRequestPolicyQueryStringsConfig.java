/*
 * Copyright 2024, Brightspot.
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

package gyro.aws.cloudfront;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.cloudfront.model.OriginRequestPolicyQueryStringBehavior;

public class OriginRequestPolicyQueryStringsConfig extends Diffable implements Copyable<software.amazon.awssdk.services.cloudfront.model.OriginRequestPolicyQueryStringsConfig> {

    private OriginRequestPolicyQueryStringBehavior queryStringBehavior;
    private Set<String> queryStrings;

    /**
     * The query string behavior for this origin request policy.
     */
    @Required
    @ValidStrings({"none", "whitelist", "allExcept", "all"})
    public OriginRequestPolicyQueryStringBehavior getQueryStringBehavior() {
        return queryStringBehavior;
    }

    public void setQueryStringBehavior(OriginRequestPolicyQueryStringBehavior queryStringBehavior) {
        this.queryStringBehavior = queryStringBehavior;
    }

    /**
     * The query strings for this origin request policy.
     */
    @Updatable
    public Set<String> getQueryStrings() {
        if (queryStrings == null) {
            queryStrings = new HashSet<>();
        }

        return queryStrings;
    }

    public void setQueryStrings(Set<String> queryStrings) {
        this.queryStrings = queryStrings;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.cloudfront.model.OriginRequestPolicyQueryStringsConfig model) {
        setQueryStringBehavior(model.queryStringBehavior());
        setQueryStrings(null);
        if (model.queryStrings() != null) {
            setQueryStrings(new HashSet<>(model.queryStrings().items()));
        }
    }

    software.amazon.awssdk.services.cloudfront.model.OriginRequestPolicyQueryStringsConfig toOriginRequestPolicyQueryStringsConfig() {
        return software.amazon.awssdk.services.cloudfront.model.OriginRequestPolicyQueryStringsConfig.builder()
            .queryStrings(r -> r.items(getQueryStrings()).quantity(getQueryStrings().size()))
            .queryStringBehavior(getQueryStringBehavior())
            .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getQueryStringBehavior() != null) {
            if ((getQueryStringBehavior() != OriginRequestPolicyQueryStringBehavior.NONE
                || getQueryStringBehavior() != OriginRequestPolicyQueryStringBehavior.ALL)
                && getQueryStrings().isEmpty()) {
                errors.add(new ValidationError(
                    this,
                    null,
                    "'query-strings' is required when 'query-string-behavior' is not 'none' or 'all'."));
            }

            if (getQueryStringBehavior() == OriginRequestPolicyQueryStringBehavior.NONE && !getQueryStrings().isEmpty()) {
                errors.add(new ValidationError(
                    this,
                    null,
                    "'query-strings' should be empty when 'query-string-behavior' is 'none' or 'all'."));
            }
        }

        return errors;
    }
}
