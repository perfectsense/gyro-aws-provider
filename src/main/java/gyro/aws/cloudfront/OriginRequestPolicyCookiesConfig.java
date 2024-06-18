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
import software.amazon.awssdk.services.cloudfront.model.OriginRequestPolicyCookieBehavior;

public class OriginRequestPolicyCookiesConfig
    extends Diffable
    implements Copyable<software.amazon.awssdk.services.cloudfront.model.OriginRequestPolicyCookiesConfig> {

    private OriginRequestPolicyCookieBehavior cookieBehavior;
    private Set<String> cookies;

    /**
     * The cookie behavior for the origin request policy.
     */
    @Required
    @ValidStrings({"none", "whitelist", "allExcept", "all"})
    public OriginRequestPolicyCookieBehavior getCookieBehavior() {
        return cookieBehavior;
    }

    public void setCookieBehavior(OriginRequestPolicyCookieBehavior cookieBehavior) {
        this.cookieBehavior = cookieBehavior;
    }

    /**
     * The cookies for the origin request policy.
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
    public void copyFrom(software.amazon.awssdk.services.cloudfront.model.OriginRequestPolicyCookiesConfig model) {
        setCookieBehavior(model.cookieBehavior());
        setCookies(null);
        if (model.cookies() != null) {
            setCookies(new HashSet<>(model.cookies().items()));
        }
    }

    software.amazon.awssdk.services.cloudfront.model.OriginRequestPolicyCookiesConfig toOriginRequestPolicyCookiesConfig() {
        software.amazon.awssdk.services.cloudfront.model.OriginRequestPolicyCookiesConfig.Builder builder =
            software.amazon.awssdk.services.cloudfront.model.OriginRequestPolicyCookiesConfig.builder()
                .cookieBehavior(getCookieBehavior());

        if (!getCookies().isEmpty()) {
            builder.cookies(r -> r.items(getCookies()).quantity(getCookies().size()));
        }

        return builder.build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getCookieBehavior() != null) {
            if ((getCookieBehavior() != OriginRequestPolicyCookieBehavior.NONE
                && getCookieBehavior() != OriginRequestPolicyCookieBehavior.ALL)
                && getCookies().isEmpty()) {
                errors.add(new ValidationError(
                    this,
                    null,
                    "'cookies' is required when 'cookie-behavior' is not 'none' or 'all'."));
            }

            if ((getCookieBehavior() == OriginRequestPolicyCookieBehavior.NONE ||
                getCookieBehavior() == OriginRequestPolicyCookieBehavior.ALL) && !getCookies().isEmpty()) {
                errors.add(new ValidationError(
                    this,
                    null,
                    "'cookies' should be empty when 'cookie-behavior' is 'none' or 'all'."));
            }
        }

        return errors;
    }
}
