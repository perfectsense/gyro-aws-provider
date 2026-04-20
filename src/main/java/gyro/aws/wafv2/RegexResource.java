/*
 * Copyright 2025, Brightspot.
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
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.wafv2.model.Regex;

public class RegexResource extends Diffable implements Copyable<Regex> {

    private String regexString;

    /**
     * The regular expression pattern string.
     */
    @Updatable
    public String getRegexString() {
        return regexString;
    }

    public void setRegexString(String regexString) {
        this.regexString = regexString;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(Regex regex) {
        setRegexString(regex.regexString());
    }

    Regex toRegex() {
        return Regex.builder()
            .regexString(getRegexString())
            .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getRegexString() != null && getRegexString().isEmpty()) {
            errors.add(new ValidationError(this, null, "The param 'regex-string' must be at least 1 character long."));
        }
        if (getRegexString() != null && getRegexString().length() > 512) {
            errors.add(new ValidationError(this, null, "The param 'regex-string' must not exceed 512 characters in length."));
        }

        return errors;
    }
}
