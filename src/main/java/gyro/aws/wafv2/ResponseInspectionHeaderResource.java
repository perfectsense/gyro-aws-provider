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
import software.amazon.awssdk.services.wafv2.model.ResponseInspectionHeader;

public class ResponseInspectionHeaderResource extends Diffable implements Copyable<ResponseInspectionHeader> {

    private String name;
    private List<String> successValues;
    private List<String> failureValues;

    /**
     * Name of the HTTP header to inspect in the response.
     */
    @Required
    @Updatable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Header values that indicate a successful login or account creation response.
     */
    @Required
    @CollectionMax(3)
    @Updatable
    public List<String> getSuccessValues() {
        if (successValues == null) {
            successValues = new ArrayList<>();
        }

        return successValues;
    }

    public void setSuccessValues(List<String> successValues) {
        this.successValues = successValues;
    }

    /**
     * Header values that indicate a failed login or account creation response.
     */
    @Required
    @CollectionMax(3)
    @Updatable
    public List<String> getFailureValues() {
        if (failureValues == null) {
            failureValues = new ArrayList<>();
        }

        return failureValues;
    }

    public void setFailureValues(List<String> failureValues) {
        this.failureValues = failureValues;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(ResponseInspectionHeader responseInspectionHeader) {
        setName(responseInspectionHeader.name());

        getSuccessValues().clear();
        if (responseInspectionHeader.successValues() != null) {
            setSuccessValues(responseInspectionHeader.successValues());
        }

        getFailureValues().clear();
        if (responseInspectionHeader.failureValues() != null) {
            setFailureValues(responseInspectionHeader.failureValues());
        }
    }

    ResponseInspectionHeader toResponseInspectionHeader() {
        return ResponseInspectionHeader.builder()
            .name(getName())
            .successValues(getSuccessValues())
            .failureValues(getFailureValues())
            .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getName() != null && getName().length() > 200) {
            errors.add(new ValidationError(
                this,
                "name",
                "'name' must not exceed 200 characters in length."));
        }

        for (String v : getSuccessValues()) {
            if (v == null || v.trim().isEmpty()) {
                errors.add(new ValidationError(
                    this,
                    "success-values",
                    "Each 'success-values' entry must be non-empty."));

            } else if (v.length() > 100) {
                errors.add(new ValidationError(
                    this,
                    "success-values",
                    "Each 'success-values' entry must not exceed 100 characters in length."));
            }
        }

        for (String v : getFailureValues()) {
            if (v == null || v.trim().isEmpty()) {
                errors.add(new ValidationError(
                    this,
                    "failure-values",
                    "Each 'failure-values' entry must be non-empty."));

            } else if (v.length() > 100) {
                errors.add(new ValidationError(
                    this,
                    "failure-values",
                    "Each 'failure-values' entry must not exceed 100 characters in length."));
            }
        }

        return errors;
    }
}
