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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.cloudfront.model.ParametersInCacheKeyAndForwardedToOrigin;

public class CachePolicyKeyParam extends Diffable implements Copyable<ParametersInCacheKeyAndForwardedToOrigin> {

    private CachePolicyCookiesConfig cookiesConfig;
    private CachePolicyHeadersConfig headersConfig;
    private CachePolicyQueryStringsConfig queryStringsConfig;
    private Boolean acceptEncodingBrotli;
    private Boolean acceptEncodingGzip;

    /**
     * The cookies configuration for the cache policy.
     *
     * @subresource gyro.aws.cloudfront.CachePolicyCookiesConfig
     */
    public CachePolicyCookiesConfig getCookiesConfig() {
        return cookiesConfig;
    }

    public void setCookiesConfig(CachePolicyCookiesConfig cookiesConfig) {
        this.cookiesConfig = cookiesConfig;
    }

    /**
     * The headers configuration for the cache policy.
     *
     * @subresource gyro.aws.cloudfront.CachePolicyHeadersConfig
     */
    public CachePolicyHeadersConfig getHeadersConfig() {
        return headersConfig;
    }

    public void setHeadersConfig(CachePolicyHeadersConfig headersConfig) {
        this.headersConfig = headersConfig;
    }

    /**
     * The query strings configuration for the cache policy.
     *
     * @subresource gyro.aws.cloudfront.CachePolicyQueryStringsConfig
     */
    public CachePolicyQueryStringsConfig getQueryStringsConfig() {
        return queryStringsConfig;
    }

    public void setQueryStringsConfig(CachePolicyQueryStringsConfig queryStringsConfig) {
        this.queryStringsConfig = queryStringsConfig;
    }

    /**
     * Enable accept encoding brotli.
     */
    @Required
    @Updatable
    public Boolean getAcceptEncodingBrotli() {
        return acceptEncodingBrotli;
    }

    public void setAcceptEncodingBrotli(Boolean acceptEncodingBrotli) {
        this.acceptEncodingBrotli = acceptEncodingBrotli;
    }

    /**
     * Enable accept encoding gzip.
     */
    @Required
    @Updatable
    public Boolean getAcceptEncodingGzip() {
        return acceptEncodingGzip;
    }

    public void setAcceptEncodingGzip(Boolean acceptEncodingGzip) {
        this.acceptEncodingGzip = acceptEncodingGzip;
    }

    @Override
    public void copyFrom(ParametersInCacheKeyAndForwardedToOrigin model) {
        setCookiesConfig(null);
        if (model.cookiesConfig() != null) {
            CachePolicyCookiesConfig cookiesConfig = newSubresource(CachePolicyCookiesConfig.class);
            cookiesConfig.copyFrom(model.cookiesConfig());
            setCookiesConfig(cookiesConfig);
        }

        setHeadersConfig(null);
        if (model.headersConfig() != null) {
            CachePolicyHeadersConfig headersConfig = newSubresource(CachePolicyHeadersConfig.class);
            headersConfig.copyFrom(model.headersConfig());
            setHeadersConfig(headersConfig);
        }

        setQueryStringsConfig(null);
        if (model.queryStringsConfig() != null) {
            CachePolicyQueryStringsConfig queryStringsConfig = newSubresource(CachePolicyQueryStringsConfig.class);
            queryStringsConfig.copyFrom(model.queryStringsConfig());
            setQueryStringsConfig(queryStringsConfig);
        }

        setAcceptEncodingGzip(model.enableAcceptEncodingGzip());
        setAcceptEncodingBrotli(model.enableAcceptEncodingBrotli());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    ParametersInCacheKeyAndForwardedToOrigin toParametersInCacheKeyAndForwardedToOrigin() {
        return ParametersInCacheKeyAndForwardedToOrigin.builder()
            .cookiesConfig(getCookiesConfig() != null ? getCookiesConfig().toCachePolicyCookiesConfig() : null)
            .headersConfig(getHeadersConfig() != null ? getHeadersConfig().toCachePolicyHeadersConfig() : null)
            .queryStringsConfig(
                getQueryStringsConfig() != null ? getQueryStringsConfig().toCachePolicyQueryStringsConfig() : null)
            .enableAcceptEncodingBrotli(getAcceptEncodingBrotli())
            .enableAcceptEncodingGzip(getAcceptEncodingGzip())
            .build();
    }
}
