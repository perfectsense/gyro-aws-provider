/*
 * Copyright 2019, Perfect Sense, Inc.
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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.cloudfront.model.CustomErrorResponse;

public class CloudFrontCustomErrorResponse extends Diffable implements Copyable<CustomErrorResponse> {

    private long ttl;
    private Integer errorCode;
    private String responseCode;
    private String responsePagePath;
    private Boolean customizeErrorResponse;

    /**
     * The minimum amount of time to cache this error code.
     */
    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    /**
     * HTTP error code to return a custom response for.
     */
    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Custom HTTP status code to return.
     */
    public String getResponseCode() {
        if (responseCode == null) {
            return "";
        }

        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * Path to a custom error page.
     */
    public String getResponsePagePath() {
        if (responsePagePath == null) {
            responsePagePath = "";
        }

        return responsePagePath;
    }

    public void setResponsePagePath(String responsePagePath) {
        this.responsePagePath = responsePagePath;
    }

    public Boolean getCustomizeErrorResponse() {
        if (customizeErrorResponse == null) {
            customizeErrorResponse = false;
        }

        return customizeErrorResponse;
    }

    public void setCustomizeErrorResponse(Boolean customizeErrorResponse) {
        this.customizeErrorResponse = customizeErrorResponse;
    }

    @Override
    public void copyFrom(CustomErrorResponse errorResponse) {
        setTtl(errorResponse.errorCachingMinTTL());
        setErrorCode(errorResponse.errorCode());
        setResponseCode(errorResponse.responseCode());
        setResponsePagePath(errorResponse.responsePagePath());
    }

    @Override
    public String primaryKey() {
        return getErrorCode() != null ? getErrorCode().toString() : "";
    }

    CustomErrorResponse toCustomErrorResponse() {
        return CustomErrorResponse.builder()
            .errorCachingMinTTL(getTtl())
            .errorCode(getErrorCode())
            .responseCode(getResponseCode())
            .responsePagePath(getResponsePagePath()).build();
    }
}
