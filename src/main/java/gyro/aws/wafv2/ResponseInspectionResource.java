/*
 * Copyright 2020, Brightspot.
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
import gyro.core.validation.CollectionMax;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.wafv2.model.ResponseInspection;
import software.amazon.awssdk.services.wafv2.model.ResponseInspectionBodyContains;
import software.amazon.awssdk.services.wafv2.model.ResponseInspectionHeader;
import software.amazon.awssdk.services.wafv2.model.ResponseInspectionJson;
import software.amazon.awssdk.services.wafv2.model.ResponseInspectionStatusCode;

public class ResponseInspectionResource extends Diffable implements Copyable<ResponseInspection> {

    private List<Integer> statusCodeSuccessCodes;
    private List<Integer> statusCodeFailureCodes;
    private String headerName;
    private List<String> headerSuccessValues;
    private List<String> headerFailureValues;
    private List<String> bodyContainsSuccessStrings;
    private List<String> bodyContainsFailureStrings;
    private String jsonIdentifier;
    private List<String> jsonSuccessValues;
    private List<String> jsonFailureValues;

    /**
     * HTTP status codes that indicate a successful login or account creation.
     */
    @CollectionMax(10)
    public List<Integer> getStatusCodeSuccessCodes() {
        if (statusCodeSuccessCodes == null) {
            statusCodeSuccessCodes = new ArrayList<>();
        }
        return statusCodeSuccessCodes;
    }

    public void setStatusCodeSuccessCodes(List<Integer> statusCodeSuccessCodes) {
        this.statusCodeSuccessCodes = statusCodeSuccessCodes;
    }

    /**
     * HTTP status codes that indicate a failed login or account creation.
     */
    @CollectionMax(10)
    public List<Integer> getStatusCodeFailureCodes() {
        if (statusCodeFailureCodes == null) {
            statusCodeFailureCodes = new ArrayList<>();
        }
        return statusCodeFailureCodes;
    }

    public void setStatusCodeFailureCodes(List<Integer> statusCodeFailureCodes) {
        this.statusCodeFailureCodes = statusCodeFailureCodes;
    }

    /**
     * Name of the HTTP header.
     */
    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    /**
     * Header values that indicate success.
     */
    @CollectionMax(3)
    public List<String> getHeaderSuccessValues() {
        if (headerSuccessValues == null) {
            headerSuccessValues = new ArrayList<>();
        }
        return headerSuccessValues;
    }

    public void setHeaderSuccessValues(List<String> headerSuccessValues) {
        this.headerSuccessValues = headerSuccessValues;
    }

    /**
     * Header values that indicate failure.
     */
    @CollectionMax(3)
    public List<String> getHeaderFailureValues() {
        if (headerFailureValues == null) {
            headerFailureValues = new ArrayList<>();
        }
        return headerFailureValues;
    }

    public void setHeaderFailureValues(List<String> headerFailureValues) {
        this.headerFailureValues = headerFailureValues;
    }

    /**
     * Strings in the response body that indicate success.
     */
    @CollectionMax(5)
    public List<String> getBodyContainsSuccessStrings() {
        if (bodyContainsSuccessStrings == null) {
            bodyContainsSuccessStrings = new ArrayList<>();
        }
        return bodyContainsSuccessStrings;
    }

    public void setBodyContainsSuccessStrings(List<String> bodyContainsSuccessStrings) {
        this.bodyContainsSuccessStrings = bodyContainsSuccessStrings;
    }

    /**
     * Strings in the response body that indicate failure.
     */
    @CollectionMax(5)
    public List<String> getBodyContainsFailureStrings() {
        if (bodyContainsFailureStrings == null) {
            bodyContainsFailureStrings = new ArrayList<>();
        }
        return bodyContainsFailureStrings;
    }

    public void setBodyContainsFailureStrings(List<String> bodyContainsFailureStrings) {
        this.bodyContainsFailureStrings = bodyContainsFailureStrings;
    }

    /**
     * The JSON identifier.
     */
    public String getJsonIdentifier() {
        return jsonIdentifier;
    }

    public void setJsonIdentifier(String jsonIdentifier) {
        this.jsonIdentifier = jsonIdentifier;
    }

    /**
     * Values at the JSON identifier that indicate success.
     */
    @CollectionMax(5)
    public List<String> getJsonSuccessValues() {
        if (jsonSuccessValues == null) {
            jsonSuccessValues = new ArrayList<>();
        }
        return jsonSuccessValues;
    }

    public void setJsonSuccessValues(List<String> jsonSuccessValues) {
        this.jsonSuccessValues = jsonSuccessValues;
    }

    /**
     * Values at the JSON identifier that indicate failure.
     */
    @CollectionMax(5)
    public List<String> getJsonFailureValues() {
        if (jsonFailureValues == null) {
            jsonFailureValues = new ArrayList<>();
        }
        return jsonFailureValues;
    }

    public void setJsonFailureValues(List<String> jsonFailureValues) {
        this.jsonFailureValues = jsonFailureValues;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(ResponseInspection responseInspection) {
        getStatusCodeSuccessCodes().clear();
        getStatusCodeFailureCodes().clear();
        if (responseInspection.statusCode() != null) {
            if (responseInspection.statusCode().successCodes() != null) {
                setStatusCodeSuccessCodes(responseInspection.statusCode().successCodes());
            }
            if (responseInspection.statusCode().failureCodes() != null) {
                setStatusCodeFailureCodes(responseInspection.statusCode().failureCodes());
            }
        }

        setHeaderName(null);
        getHeaderSuccessValues().clear();
        getHeaderFailureValues().clear();
        if (responseInspection.header() != null) {
            setHeaderName(responseInspection.header().name());
            if (responseInspection.header().successValues() != null) {
                setHeaderSuccessValues(responseInspection.header().successValues());
            }
            if (responseInspection.header().failureValues() != null) {
                setHeaderFailureValues(responseInspection.header().failureValues());
            }
        }

        getBodyContainsSuccessStrings().clear();
        getBodyContainsFailureStrings().clear();
        if (responseInspection.bodyContains() != null) {
            if (responseInspection.bodyContains().successStrings() != null) {
                setBodyContainsSuccessStrings(responseInspection.bodyContains().successStrings());
            }
            if (responseInspection.bodyContains().failureStrings() != null) {
                setBodyContainsFailureStrings(responseInspection.bodyContains().failureStrings());
            }
        }

        setJsonIdentifier(null);
        getJsonSuccessValues().clear();
        getJsonFailureValues().clear();
        if (responseInspection.json() != null) {
            if (responseInspection.json().identifier() != null) {
                setJsonIdentifier(responseInspection.json().identifier());
            }
            if (responseInspection.json().successValues() != null) {
                setJsonSuccessValues(responseInspection.json().successValues());
            }
            if (responseInspection.json().failureValues() != null) {
                setJsonFailureValues(responseInspection.json().failureValues());
            }
        }
    }

    ResponseInspection toResponseInspection() {
        ResponseInspection.Builder builder = ResponseInspection.builder();

        if (!getStatusCodeSuccessCodes().isEmpty() || !getStatusCodeFailureCodes().isEmpty()) {
            ResponseInspectionStatusCode.Builder statusBuilder = ResponseInspectionStatusCode.builder();

            if (!getStatusCodeSuccessCodes().isEmpty()) {
                statusBuilder.successCodes(getStatusCodeSuccessCodes());
            }
            if (!getStatusCodeFailureCodes().isEmpty()) {
                statusBuilder.failureCodes(getStatusCodeFailureCodes());
            }

            builder.statusCode(statusBuilder.build());
        }

        if (getHeaderName() != null
            || !getHeaderSuccessValues().isEmpty()
            || !getHeaderFailureValues().isEmpty()) {

            ResponseInspectionHeader.Builder headerBuilder = ResponseInspectionHeader.builder();

            if (getHeaderName() != null) {
                headerBuilder.name(getHeaderName());
            }
            if (!getHeaderSuccessValues().isEmpty()) {
                headerBuilder.successValues(getHeaderSuccessValues());
            }
            if (!getHeaderFailureValues().isEmpty()) {
                headerBuilder.failureValues(getHeaderFailureValues());
            }

            builder.header(headerBuilder.build());
        }

        if (!getBodyContainsSuccessStrings().isEmpty() || !getBodyContainsFailureStrings().isEmpty()) {
            ResponseInspectionBodyContains.Builder bodyBuilder = ResponseInspectionBodyContains.builder();

            if (!getBodyContainsSuccessStrings().isEmpty()) {
                bodyBuilder.successStrings(getBodyContainsSuccessStrings());
            }
            if (!getBodyContainsFailureStrings().isEmpty()) {
                bodyBuilder.failureStrings(getBodyContainsFailureStrings());
            }

            builder.bodyContains(bodyBuilder.build());
        }

        if (getJsonIdentifier() != null
            || !getJsonSuccessValues().isEmpty()
            || !getJsonFailureValues().isEmpty()) {

            ResponseInspectionJson.Builder jsonBuilder = ResponseInspectionJson.builder();

            if (getJsonIdentifier() != null) {
                jsonBuilder.identifier(getJsonIdentifier());
            }
            if (!getJsonSuccessValues().isEmpty()) {
                jsonBuilder.successValues(getJsonSuccessValues());
            }
            if (!getJsonFailureValues().isEmpty()) {
                jsonBuilder.failureValues(getJsonFailureValues());
            }

            builder.json(jsonBuilder.build());
        }

        return builder.build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        boolean hasBodyContains =
            !getBodyContainsSuccessStrings().isEmpty() || !getBodyContainsFailureStrings().isEmpty();

        if (hasBodyContains) {
            if (getBodyContainsSuccessStrings().isEmpty()) {
                errors.add(new ValidationError(
                    this,
                    "body-contains-success-strings",
                    "At least one 'body-contains-success-strings' value is required when body inspection is configured."));
            }
            if (getBodyContainsFailureStrings().isEmpty()) {
                errors.add(new ValidationError(
                    this,
                    "body-contains-failure-strings",
                    "At least one 'body-contains-failure-strings' value is required when body inspection is configured."));
            }

            // Each string non-blank and length <= 100
            for (String v : getBodyContainsSuccessStrings()) {
                if (v == null || v.trim().isEmpty()) {
                    errors.add(new ValidationError(
                        this,
                        "body-contains-success-strings",
                        "Each 'body-contains-success-strings' entry must be non-empty."));
                    break;
                }
                if (v.length() > 100) {
                    errors.add(new ValidationError(
                        this,
                        "body-contains-success-strings",
                        "Each 'body-contains-success-strings' entry must not exceed 100 characters in length."));
                    break;
                }
            }
            for (String v : getBodyContainsFailureStrings()) {
                if (v == null || v.trim().isEmpty()) {
                    errors.add(new ValidationError(
                        this,
                        "body-contains-failure-strings",
                        "Each 'body-contains-failure-strings' entry must be non-empty."));
                    break;
                }
                if (v.length() > 100) {
                    errors.add(new ValidationError(
                        this,
                        "body-contains-failure-strings",
                        "Each 'body-contains-failure-strings' entry must not exceed 100 characters in length."));
                    break;
                }
            }
        }

        boolean hasHeader =
            getHeaderName() != null
                || !getHeaderSuccessValues().isEmpty()
                || !getHeaderFailureValues().isEmpty();

        if (hasHeader) {
            if (getHeaderName() == null || getHeaderName().trim().isEmpty()) {
                errors.add(new ValidationError(
                    this,
                    "header-name",
                    "'header-name' is required when header response inspection is configured."));
            } else if (getHeaderName().length() > 200) {
                errors.add(new ValidationError(
                    this,
                    "header-name",
                    "'header-name' must not exceed 200 characters in length."));
            }

            if (getHeaderSuccessValues().isEmpty()) {
                errors.add(new ValidationError(
                    this,
                    "header-success-values",
                    "At least one 'header-success-values' entry is required when header response inspection is configured."));
            }
            if (getHeaderFailureValues().isEmpty()) {
                errors.add(new ValidationError(
                    this,
                    "header-failure-values",
                    "At least one 'header-failure-values' entry is required when header response inspection is configured."));
            }

            // Each header value non-blank and length <= 100
            for (String v : getHeaderSuccessValues()) {
                if (v == null || v.trim().isEmpty()) {
                    errors.add(new ValidationError(
                        this,
                        "header-success-values",
                        "Each 'header-success-values' entry must be non-empty."));
                    break;
                }
                if (v.length() > 100) {
                    errors.add(new ValidationError(
                        this,
                        "header-success-values",
                        "Each 'header-success-values' entry must not exceed 100 characters in length."));
                    break;
                }
            }
            for (String v : getHeaderFailureValues()) {
                if (v == null || v.trim().isEmpty()) {
                    errors.add(new ValidationError(
                        this,
                        "header-failure-values",
                        "Each 'header-failure-values' entry must be non-empty."));
                    break;
                }
                if (v.length() > 100) {
                    errors.add(new ValidationError(
                        this,
                        "header-failure-values",
                        "Each 'header-failure-values' entry must not exceed 100 characters in length."));
                    break;
                }
            }
        }

        boolean hasStatus =
            !getStatusCodeSuccessCodes().isEmpty() || !getStatusCodeFailureCodes().isEmpty();

        if (hasStatus) {
            if (getStatusCodeSuccessCodes().isEmpty()) {
                errors.add(new ValidationError(
                    this,
                    "status-code-success-codes",
                    "At least one 'status-code-success-codes' value is required when status code response inspection is configured."));
            }
            if (getStatusCodeFailureCodes().isEmpty()) {
                errors.add(new ValidationError(
                    this,
                    "status-code-failure-codes",
                    "At least one 'status-code-failure-codes' value is required when status code response inspection is configured."));
            }

            // Each status code must be a between (0–999)
            for (Integer code : getStatusCodeSuccessCodes()) {
                if (code == null || code < 0 || code > 999) {
                    errors.add(new ValidationError(
                        this,
                        "status-code-success-codes",
                        "Each 'status-code-success-codes' entry must be a between (0–999)."));
                    break;
                }
            }
            for (Integer code : getStatusCodeFailureCodes()) {
                if (code == null || code < 0 || code > 999) {
                    errors.add(new ValidationError(
                        this,
                        "status-code-failure-codes",
                        "Each 'status-code-failure-codes' entry must be a between (0–999)."));
                    break;
                }
            }
        }

        boolean hasJson =
            getJsonIdentifier() != null
                || !getJsonSuccessValues().isEmpty()
                || !getJsonFailureValues().isEmpty();

        if (hasJson) {
            if (getJsonIdentifier() == null || getJsonIdentifier().trim().isEmpty()) {
                errors.add(new ValidationError(
                    this,
                    "json-identifier",
                    "'json-identifier' is required when JSON response inspection is configured."));
            } else if (getJsonIdentifier().length() > 512) {
                errors.add(new ValidationError(
                    this,
                    "json-identifier",
                    "'json-identifier' must not exceed 512 characters in length."));
            }

            if (getJsonSuccessValues().isEmpty()) {
                errors.add(new ValidationError(
                    this,
                    "json-success-values",
                    "At least one 'json-success-values' entry is required when JSON response inspection is configured."));
            }
            if (getJsonFailureValues().isEmpty()) {
                errors.add(new ValidationError(
                    this,
                    "json-failure-values",
                    "At least one 'json-failure-values' entry is required when JSON response inspection is configured."));
            }

            // Each value non-blank and length <= 100
            for (String v : getJsonSuccessValues()) {
                if (v == null || v.trim().isEmpty()) {
                    errors.add(new ValidationError(
                        this,
                        "json-success-values",
                        "Each 'json-success-values' entry must be non-empty."));
                    break;
                }
                if (v.length() > 100) {
                    errors.add(new ValidationError(
                        this,
                        "json-success-values",
                        "Each 'json-success-values' entry must not exceed 100 characters in length."));
                    break;
                }
            }
            for (String v : getJsonFailureValues()) {
                if (v == null || v.trim().isEmpty()) {
                    errors.add(new ValidationError(
                        this,
                        "json-failure-values",
                        "Each 'json-failure-values' entry must be non-empty."));
                    break;
                }
                if (v.length() > 100) {
                    errors.add(new ValidationError(
                        this,
                        "json-failure-values",
                        "Each 'json-failure-values' entry must not exceed 100 characters in length."));
                    break;
                }
            }
        }

        return errors;
    }
}
