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
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.wafv2.model.ResponseInspection;
import software.amazon.awssdk.services.wafv2.model.ResponseInspectionStatusCode;

public class ResponseInspectionResource extends Diffable implements Copyable<ResponseInspection> {

    private List<Integer> statusCodeSuccessCodes;
    private List<Integer> statusCodeFailureCodes;
    private ResponseInspectionHeaderResource header;
    private ResponseInspectionBodyContainsResource bodyContains;
    private ResponseInspectionJsonResource json;

    /**
     * HTTP status codes that indicate a successful login or account creation.
     */
    @CollectionMax(10)
    @Updatable
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
    @Updatable
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
     * Criteria for inspecting an HTTP header in the response.
     *
     * @subresource gyro.aws.wafv2.ResponseInspectionHeaderResource
     */
    @Updatable
    public ResponseInspectionHeaderResource getHeader() {
        return header;
    }

    public void setHeader(ResponseInspectionHeaderResource header) {
        this.header = header;
    }

    /**
     * Criteria for inspecting strings in the response body.
     *
     * @subresource gyro.aws.wafv2.ResponseInspectionBodyContainsResource
     */
    @Updatable
    public ResponseInspectionBodyContainsResource getBodyContains() {
        return bodyContains;
    }

    public void setBodyContains(ResponseInspectionBodyContainsResource bodyContains) {
        this.bodyContains = bodyContains;
    }

    /**
     * Criteria for inspecting a value in the response body JSON.
     *
     * @subresource gyro.aws.wafv2.ResponseInspectionJsonResource
     */
    @Updatable
    public ResponseInspectionJsonResource getJson() {
        return json;
    }

    public void setJson(ResponseInspectionJsonResource json) {
        this.json = json;
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

        setHeader(null);
        if (responseInspection.header() != null) {
            ResponseInspectionHeaderResource headerResource = newSubresource(ResponseInspectionHeaderResource.class);
            headerResource.copyFrom(responseInspection.header());
            setHeader(headerResource);
        }

        setBodyContains(null);
        if (responseInspection.bodyContains() != null) {
            ResponseInspectionBodyContainsResource bodyContainsResource =
                newSubresource(ResponseInspectionBodyContainsResource.class);
            bodyContainsResource.copyFrom(responseInspection.bodyContains());
            setBodyContains(bodyContainsResource);
        }

        setJson(null);
        if (responseInspection.json() != null) {
            ResponseInspectionJsonResource jsonResource = newSubresource(ResponseInspectionJsonResource.class);
            jsonResource.copyFrom(responseInspection.json());
            setJson(jsonResource);
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

        if (getHeader() != null) {
            builder.header(getHeader().toResponseInspectionHeader());
        }

        if (getBodyContains() != null) {
            builder.bodyContains(getBodyContains().toResponseInspectionBodyContains());
        }

        if (getJson() != null) {
            builder.json(getJson().toResponseInspectionJson());
        }

        return builder.build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

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

            for (Integer code : getStatusCodeSuccessCodes()) {
                if (code == null || code < 0 || code > 999) {
                    errors.add(new ValidationError(
                        this,
                        "status-code-success-codes",
                        "Each 'status-code-success-codes' entry must be between 0 and 999."));
                }
            }
            for (Integer code : getStatusCodeFailureCodes()) {
                if (code == null || code < 0 || code > 999) {
                    errors.add(new ValidationError(
                        this,
                        "status-code-failure-codes",
                        "Each 'status-code-failure-codes' entry must be between 0 and 999."));
                }
            }
        }

        return errors;
    }
}
