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
import software.amazon.awssdk.services.wafv2.model.HeaderMatchPattern;

public class FieldMatchHeaderMatchPatternResource extends Diffable implements Copyable<HeaderMatchPattern> {

    private Boolean all;
    private List<String> excludedHeaders;
    private List<String> includedHeaders;

    /**
     * When set to ``true``, the pattern will match all headers.
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
     * The list of headers to exclude from the pattern.
     */
    @Updatable
    @ConflictsWith({"all", "included-headers"})
    public List<String> getExcludedHeaders() {
        if (excludedHeaders == null) {
            excludedHeaders = new ArrayList<>();
        }

        return excludedHeaders;
    }

    public void setExcludedHeaders(List<String> excludedHeaders) {
        this.excludedHeaders = excludedHeaders;
    }

    /**
     * The list of headers to include in the pattern.
     */
    @Updatable
    @ConflictsWith({"all", "excluded-headers"})
    public List<String> getIncludedHeaders() {
        if (includedHeaders == null) {
            includedHeaders = new ArrayList<>();
        }

        return includedHeaders;
    }

    public void setIncludedHeaders(List<String> includedHeaders) {
        this.includedHeaders = includedHeaders;
    }

    @Override
    public void copyFrom(HeaderMatchPattern model) {
        setAll(model.all() != null);
        setExcludedHeaders(model.excludedHeaders());
        setIncludedHeaders(model.includedHeaders());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    HeaderMatchPattern toHeaderMatchPattern() {
        return HeaderMatchPattern.builder()
            .all(getAll() != null && getAll() ? All.builder().build() : null)
            .excludedHeaders(getExcludedHeaders().isEmpty() ? null : getExcludedHeaders())
            .includedHeaders(getIncludedHeaders().isEmpty() ? null : getIncludedHeaders())
            .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if ((getAll() == null || !getAll())
            && getExcludedHeaders().isEmpty()
            && getIncludedHeaders().isEmpty()) {
            errors.add(new ValidationError(
                this,
                null,
                "One of 'all', 'excluded-headers' or 'included-headers' is required!"));
        }

        return errors;
    }
}
