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

package gyro.aws.cloudfront;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.cloudfront.model.CachePolicyHeaderBehavior;

public class CachePolicyHeadersConfig
    extends Diffable implements Copyable<software.amazon.awssdk.services.cloudfront.model.CachePolicyHeadersConfig> {

    private CachePolicyHeaderBehavior headerBehavior;
    private Set<String> headers;

    /**
     * The header behavior for the cache policy.
     */
    @Required
    @ValidStrings({ "none", "whitelist" })
    public CachePolicyHeaderBehavior getHeaderBehavior() {
        return headerBehavior;
    }

    public void setHeaderBehavior(CachePolicyHeaderBehavior headerBehavior) {
        this.headerBehavior = headerBehavior;
    }

    /**
     * The headers for the cache policy.
     */
    @Updatable
    public Set<String> getHeaders() {
        if (headers == null) {
            headers = new HashSet<>();
        }

        return headers;
    }

    public void setHeaders(Set<String> headers) {
        this.headers = headers;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.cloudfront.model.CachePolicyHeadersConfig model) {
        setHeaderBehavior(model.headerBehavior());
        setHeaders(null);
        if (model.headers() != null) {
            setHeaders(new HashSet<>(model.headers().items()));
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    software.amazon.awssdk.services.cloudfront.model.CachePolicyHeadersConfig toCachePolicyHeadersConfig() {
        return software.amazon.awssdk.services.cloudfront.model.CachePolicyHeadersConfig.builder()
            .headers(r -> r.items(getHeaders()).quantity(getHeaders().size()))
            .headerBehavior(getHeaderBehavior())
            .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getHeaderBehavior() != null) {
            if (getHeaderBehavior() != CachePolicyHeaderBehavior.NONE && getHeaders().isEmpty()) {
                errors.add(new ValidationError(
                    this,
                    null,
                    "'headers' is required when 'header-behavior' is not 'none'."));
            }

            if (getHeaderBehavior() == CachePolicyHeaderBehavior.NONE && !getHeaders().isEmpty()) {
                errors.add(new ValidationError(
                    this,
                    null,
                    "'headers' should be empty when 'headers-behavior' is 'none'."));
            }
        }

        return errors;
    }
}
