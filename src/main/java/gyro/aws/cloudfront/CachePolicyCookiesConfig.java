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
import software.amazon.awssdk.services.cloudfront.model.CachePolicyCookieBehavior;

public class CachePolicyCookiesConfig
    extends Diffable implements Copyable<software.amazon.awssdk.services.cloudfront.model.CachePolicyCookiesConfig> {

    private CachePolicyCookieBehavior cookieBehavior;
    private Set<String> cookies;

    /**
     * The cookie behavior for the cache policy.
     */
    @Required
    @ValidStrings({"none", "whitelist", "allExcept", "all"})
    public CachePolicyCookieBehavior getCookieBehavior() {
        return cookieBehavior;
    }

    public void setCookieBehavior(CachePolicyCookieBehavior cookieBehavior) {
        this.cookieBehavior = cookieBehavior;
    }

    /**
     * The cookies for the cache policy.
     */
    @Updatable
    public Set<String> getCookies() {
        if (cookies == null) {
            cookies = new HashSet<>();
        }

        return cookies;
    }

    public void setCookies(Set<String> cookies) {
        this.cookies = cookies;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.cloudfront.model.CachePolicyCookiesConfig model) {
        setCookieBehavior(model.cookieBehavior());
        setCookies(null);
        if (model.cookies() != null) {
            setCookies(new HashSet<>(model.cookies().items()));
        }
    }

    software.amazon.awssdk.services.cloudfront.model.CachePolicyCookiesConfig toCachePolicyCookiesConfig() {
        return software.amazon.awssdk.services.cloudfront.model.CachePolicyCookiesConfig.builder()
            .cookies(r -> r.items(getCookies()).quantity(getCookies().size()))
            .cookieBehavior(getCookieBehavior())
            .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getCookieBehavior() != null) {
            if ((getCookieBehavior() != CachePolicyCookieBehavior.NONE
                || getCookieBehavior() != CachePolicyCookieBehavior.ALL)
                && getCookies().isEmpty()) {
                errors.add(new ValidationError(
                    this,
                    null,
                    "'cookies' is required when 'cookie-behavior' is not 'none' or 'all'."));
            }

            if (getCookieBehavior() == CachePolicyCookieBehavior.NONE && !getCookies().isEmpty()) {
                errors.add(new ValidationError(
                    this,
                    null,
                    "'cookies' should be empty when 'cookie-behavior' is 'none' or 'all'."));
            }
        }

        return errors;
    }
}
