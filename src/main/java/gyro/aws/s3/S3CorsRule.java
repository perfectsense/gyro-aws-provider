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

package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.s3.model.CORSRule;

import java.util.List;

public class S3CorsRule extends Diffable implements Copyable<CORSRule> {
    private List<String> allowedHeaders;
    private List<String> allowedMethods;
    private List<String> allowedOrigins;
    private List<String> exposeHeaders;
    private Integer maxAgeSeconds;

    /**
     * List of allowed headers for the rule.
     */
    @Updatable
    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    /**
     * Allowed HTTP methods for the rule.
     */
    @Updatable
    @ValidStrings({"GET", "PUT", "POST", "DELETE", "HEAD"})
    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    /**
     * Allowed origins for the rule that requires bucket access.
     */
    @Updatable
    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * Expose headers for the rule.
     */
    @Updatable
    public List<String> getExposeHeaders() {
        return exposeHeaders;
    }

    public void setExposeHeaders(List<String> exposeHeaders) {
        this.exposeHeaders = exposeHeaders;
    }

    /**
     * Max age in seconds that specifies the cache duration of the response.
     */
    @Updatable
    public Integer getMaxAgeSeconds() {
        return maxAgeSeconds;
    }

    public void setMaxAgeSeconds(Integer maxAgeSeconds) {
        this.maxAgeSeconds = maxAgeSeconds;
    }

    @Override
    public String primaryKey() {
        return getUniqueName();
    }

    @Override
    public void copyFrom(CORSRule corsRule) {
        setAllowedHeaders(corsRule.allowedHeaders());
        setAllowedMethods(corsRule.allowedMethods());
        setAllowedOrigins(corsRule.allowedOrigins());
        setExposeHeaders(corsRule.exposeHeaders());
        setMaxAgeSeconds(corsRule.maxAgeSeconds());
    }

    CORSRule toCorsRule() {
        return CORSRule.builder()
            .allowedHeaders(getAllowedHeaders())
            .allowedMethods(getAllowedMethods())
            .allowedOrigins(getAllowedOrigins())
            .exposeHeaders(getExposeHeaders())
            .maxAgeSeconds(getMaxAgeSeconds())
            .build();
    }

    private String getUniqueName() {
        StringBuilder sb = new StringBuilder();

        sb.append("s3 cors rule - ");

        sb.append("allowed-origins [");
        if (getAllowedOrigins() != null && !getAllowedOrigins().isEmpty()) {
            sb.append(String.join(",", getAllowedOrigins()));
        }
        sb.append("], ");

        sb.append("allowed-methods [");
        if (getAllowedMethods() != null && !getAllowedMethods().isEmpty()) {
            sb.append(String.join(",", getAllowedMethods()));
        }
        sb.append("], ");

        sb.append("allowed-headers [");
        if (getAllowedHeaders() != null && !getAllowedHeaders().isEmpty()) {
            sb.append(String.join(",", getAllowedHeaders()));
        }
        sb.append("], ");

        sb.append("expose-headers [");
        if (getExposeHeaders() != null && !getExposeHeaders().isEmpty()) {
            sb.append(String.join(",", getExposeHeaders()));
        }
        sb.append("], max-age-seconds [")
            .append(getMaxAgeSeconds() != null ? getMaxAgeSeconds() : "").append("]");

        return sb.toString();
    }
}
