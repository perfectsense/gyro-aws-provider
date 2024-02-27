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

package gyro.aws.wafv2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.wafv2.model.All;
import software.amazon.awssdk.services.wafv2.model.CookieMatchPattern;

public class FieldMatchCookiePatternResource extends Diffable implements Copyable<CookieMatchPattern> {

    private Boolean all;
    private List<String> excludedCookies;
    private List<String> includedCookies;

    /**
     * When set to ``true``, the pattern will match all cookies.
     */
    @Updatable
    @ConflictsWith({"included-headers", "excluded-headers"})
    public Boolean getAll() {
        return all;
    }

    public void setAll(Boolean all) {
        this.all = all;
    }

    /**
     * The list of cookies to exclude from the pattern.
     */
    @Updatable
    @ConflictsWith({"all", "included-headers"})
    public List<String> getExcludedCookies() {
        if (excludedCookies == null) {
            excludedCookies = new ArrayList<>();
        }

        return excludedCookies;
    }

    public void setExcludedCookies(List<String> excludedCookies) {
        this.excludedCookies = excludedCookies;
    }

    /**
     * The list of cookies to include in the pattern.
     */
    @Updatable
    @ConflictsWith({"all", "excluded-headers"})
    public List<String> getIncludedCookies() {
        if (includedCookies == null) {
            includedCookies = new ArrayList<>();
        }

        return includedCookies;
    }

    public void setIncludedCookies(List<String> includedCookies) {
        this.includedCookies = includedCookies;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(CookieMatchPattern model) {
        setAll(model.all() != null);
        setExcludedCookies(model.excludedCookies());
        setIncludedCookies(model.includedCookies());
    }

    CookieMatchPattern toCookieMatchPattern() {
        return CookieMatchPattern.builder()
            .all(getAll() != null && getAll() ? All.builder().build() : null)
            .excludedCookies(getExcludedCookies())
            .includedCookies(getIncludedCookies())
            .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if ((getAll() == null || !getAll())
            && getExcludedCookies().isEmpty()
            && getIncludedCookies().isEmpty()) {
            errors.add(new ValidationError(
                this,
                null,
                "One of 'all', 'excluded-cookies' or 'included-cookies' is required!"));
        }

        return errors;
    }
}
