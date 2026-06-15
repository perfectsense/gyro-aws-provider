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
import software.amazon.awssdk.services.wafv2.model.ResponseInspectionJson;

public class ResponseInspectionJsonResource extends Diffable implements Copyable<ResponseInspectionJson> {

    private String identifier;
    private List<String> successValues;
    private List<String> failureValues;

    /**
     * Identifier for the value to inspect in the JSON body of the response.
     */
    @Required
    @Updatable
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Values for the specified identifier in the response JSON that indicate a successful login or account creation.
     */
    @Required
    @CollectionMax(5)
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
     * Values for the specified identifier in the response JSON that indicate a failed login or account creation.
     */
    @Required
    @CollectionMax(5)
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
    public void copyFrom(ResponseInspectionJson responseInspectionJson) {
        setIdentifier(responseInspectionJson.identifier());

        getSuccessValues().clear();
        if (responseInspectionJson.successValues() != null) {
            setSuccessValues(responseInspectionJson.successValues());
        }

        getFailureValues().clear();
        if (responseInspectionJson.failureValues() != null) {
            setFailureValues(responseInspectionJson.failureValues());
        }
    }

    ResponseInspectionJson toResponseInspectionJson() {
        return ResponseInspectionJson.builder()
            .identifier(getIdentifier())
            .successValues(getSuccessValues())
            .failureValues(getFailureValues())
            .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getIdentifier() != null && getIdentifier().length() > 512) {
            errors.add(new ValidationError(
                this,
                "identifier",
                "'identifier' must not exceed 512 characters in length."));
        }

        for (String v : getSuccessValues()) {
            if (v == null || v.trim().isEmpty()) {
                errors.add(
                    new ValidationError(this, "success-values", "Each 'success-values' entry must be non-empty."));
            } else if (v.length() > 100) {
                errors.add(new ValidationError(this, "success-values",
                    "Each 'success-values' entry must not exceed 100 characters in length."));
            }
        }
        for (String v : getFailureValues()) {
            if (v == null || v.trim().isEmpty()) {
                errors.add(
                    new ValidationError(this, "failure-values", "Each 'failure-values' entry must be non-empty."));
            } else if (v.length() > 100) {
                errors.add(new ValidationError(this, "failure-values",
                    "Each 'failure-values' entry must not exceed 100 characters in length."));
            }
        }

        return errors;
    }
}
