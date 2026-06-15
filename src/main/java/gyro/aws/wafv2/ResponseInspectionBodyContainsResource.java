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
import gyro.core.validation.CollectionMax;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.wafv2.model.ResponseInspectionBodyContains;

public class ResponseInspectionBodyContainsResource extends Diffable implements Copyable<ResponseInspectionBodyContains> {

    private List<String> successStrings;
    private List<String> failureStrings;

    /**
     * Strings in the response body that indicate a successful login or account creation.
     */
    @Required
    @CollectionMax(5)
    @Updatable
    public List<String> getSuccessStrings() {
        if (successStrings == null) {
            successStrings = new ArrayList<>();
        }

        return successStrings;
    }

    public void setSuccessStrings(List<String> successStrings) {
        this.successStrings = successStrings;
    }

    /**
     * Strings in the response body that indicate a failed login or account creation.
     */
    @Required
    @CollectionMax(5)
    @Updatable
    public List<String> getFailureStrings() {
        if (failureStrings == null) {
            failureStrings = new ArrayList<>();
        }

        return failureStrings;
    }

    public void setFailureStrings(List<String> failureStrings) {
        this.failureStrings = failureStrings;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(ResponseInspectionBodyContains responseInspectionBodyContains) {
        getSuccessStrings().clear();
        if (responseInspectionBodyContains.successStrings() != null) {
            setSuccessStrings(responseInspectionBodyContains.successStrings());
        }

        getFailureStrings().clear();
        if (responseInspectionBodyContains.failureStrings() != null) {
            setFailureStrings(responseInspectionBodyContains.failureStrings());
        }
    }

    ResponseInspectionBodyContains toResponseInspectionBodyContains() {
        return ResponseInspectionBodyContains.builder()
            .successStrings(getSuccessStrings())
            .failureStrings(getFailureStrings())
            .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        for (String v : getSuccessStrings()) {
            if (v == null || v.trim().isEmpty()) {
                errors.add(new ValidationError(
                    this,
                    "success-strings",
                    "Each 'success-strings' entry must be non-empty."));
            } else if (v.length() > 100) {
                errors.add(new ValidationError(
                    this,
                    "success-strings",
                    "Each 'success-strings' entry must not exceed 100 characters in length."));
            }
        }

        for (String v : getFailureStrings()) {
            if (v == null || v.trim().isEmpty()) {
                errors.add(new ValidationError(
                    this,
                    "failure-strings",
                    "Each 'failure-strings' entry must be non-empty."));
            } else if (v.length() > 100) {
                errors.add(new ValidationError(
                    this,
                    "failure-strings",
                    "Each 'failure-strings' entry must not exceed 100 characters in length."));
            }
        }

        return errors;
    }
}
