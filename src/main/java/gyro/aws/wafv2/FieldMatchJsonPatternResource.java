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
import software.amazon.awssdk.services.wafv2.model.JsonMatchPattern;

public class FieldMatchJsonPatternResource extends Diffable implements Copyable<JsonMatchPattern> {

    private Boolean all;
    private List<String> includePaths;

    /**
     * When set to ``true``, the pattern will match all paths.
     */
    @Updatable
    @ConflictsWith({"included-paths"})
    public Boolean getAll() {
        return all;
    }

    public void setAll(Boolean all) {
        this.all = all;
    }

   /**
    * The list of paths to include in the pattern.
    */
    @Updatable
    @ConflictsWith({"all"})
    public List<String> getIncludePaths() {
        if (includePaths == null) {
            includePaths = new ArrayList<>();
        }

        return includePaths;
    }

    public void setIncludePaths(List<String> includePaths) {
        this.includePaths = includePaths;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(JsonMatchPattern model) {
        setAll(model.all() != null);
        setIncludePaths(model.includedPaths());
    }

    JsonMatchPattern toJsonMatchPattern() {
        return JsonMatchPattern.builder()
            .all(getAll() != null && getAll() ? All.builder().build() : null)
            .includedPaths(getIncludePaths())
            .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if ((getAll() == null || !getAll()) && getIncludePaths().isEmpty()) {
            errors.add(new ValidationError(
                this,
                null,
                "One of 'all' or 'include-paths' is required!"));
        }

        return errors;
    }
}
