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

import java.util.HashMap;
import java.util.Map;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.wafv2.model.CustomHTTPHeader;
import software.amazon.awssdk.services.wafv2.model.CustomResponse;

public class RuleActionCustomResponseResource extends Diffable implements Copyable<CustomResponse> {

    private String customResponseBodyKey;
    private Integer responseCode;
    private Map<String, String> responseHeaders;

    /**
     * The custom response body key.
     */
    @Updatable
    public String getCustomResponseBodyKey() {
        return customResponseBodyKey;
    }

    public void setCustomResponseBodyKey(String customResponseBodyKey) {
        this.customResponseBodyKey = customResponseBodyKey;
    }

    /**
     * The HTTP status code to define custom set of rules.
     */
    @Required
    @Updatable
    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * A list of custom response headers.
     */
    @Updatable
    public Map<String, String> getResponseHeaders() {
        if (responseHeaders == null) {
            responseHeaders = new HashMap<>();
        }

        return responseHeaders;
    }

    public void setResponseHeaders(Map<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    @Override
    public void copyFrom(CustomResponse model) {
        setCustomResponseBodyKey(model.customResponseBodyKey());
        setResponseCode(model.responseCode());
        setResponseHeaders(null);
        if (model.responseHeaders() != null) {
            model.responseHeaders().forEach(h -> getResponseHeaders().put(h.name(), h.value()));
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    CustomResponse toCustomResponse() {
        return CustomResponse.builder()
            .customResponseBodyKey(getCustomResponseBodyKey())
            .responseCode(getResponseCode())
            .responseHeaders(getResponseHeaders().isEmpty() ? null
                : getResponseHeaders().entrySet().stream()
                .map(e -> CustomHTTPHeader.builder()
                    .name(e.getKey())
                    .value(e.getValue())
                    .build())
                .collect(java.util.stream.Collectors.toList()))
            .build();
    }
}
