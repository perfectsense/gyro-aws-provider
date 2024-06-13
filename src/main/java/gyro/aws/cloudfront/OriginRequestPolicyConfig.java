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

public class OriginRequestPolicyConfig
    extends Diffable implements Copyable<software.amazon.awssdk.services.cloudfront.model.OriginRequestPolicyConfig> {

    private String comment;
    private String name;
    private OriginRequestPolicyCookiesConfig cookiesConfig;
    private OriginRequestPolicyHeadersConfig headersConfig;
    private OriginRequestPolicyQueryStringsConfig queryStringsConfig;

    /**
     * The cookies configuration for the origin request policy.
     *
     * @subresource gyro.aws.cloudfront.OriginRequestPolicyCookiesConfig
     */
    public OriginRequestPolicyCookiesConfig getCookiesConfig() {
        return cookiesConfig;
    }

    public void setCookiesConfig(OriginRequestPolicyCookiesConfig cookiesConfig) {
        this.cookiesConfig = cookiesConfig;
    }

    /**
     * The headers configuration for the origin request policy.
     *
     * @subresource gyro.aws.cloudfront.OriginRequestPolicyHeadersConfig
     */
    public OriginRequestPolicyHeadersConfig getHeadersConfig() {
        return headersConfig;
    }

    public void setHeadersConfig(OriginRequestPolicyHeadersConfig headersConfig) {
        this.headersConfig = headersConfig;
    }

    /**
     * The query strings configuration for the origin request policy.
     *
     * @subresource gyro.aws.cloudfront.OriginRequestPolicyQueryStringsConfig
     */
    public OriginRequestPolicyQueryStringsConfig getQueryStringsConfig() {
        return queryStringsConfig;
    }

    public void setQueryStringsConfig(OriginRequestPolicyQueryStringsConfig queryStringsConfig) {
        this.queryStringsConfig = queryStringsConfig;
    }

    /**
     * The comment for the origin request policy.
     */
    @Updatable
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * The name for the origin request policy.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.cloudfront.model.OriginRequestPolicyConfig model) {
        setComment(model.comment());
        setName(model.name());

        setCookiesConfig(null);
        if (model.cookiesConfig() != null) {
            OriginRequestPolicyCookiesConfig cookiesConfig = newSubresource(OriginRequestPolicyCookiesConfig.class);
            cookiesConfig.copyFrom(model.cookiesConfig());
            setCookiesConfig(cookiesConfig);
        }

        setHeadersConfig(null);
        if (model.headersConfig() != null) {
            OriginRequestPolicyHeadersConfig headersConfig = newSubresource(OriginRequestPolicyHeadersConfig.class);
            headersConfig.copyFrom(model.headersConfig());
            setHeadersConfig(headersConfig);
        }

        setQueryStringsConfig(null);
        if (model.queryStringsConfig() != null) {
            OriginRequestPolicyQueryStringsConfig queryStringsConfig = newSubresource(OriginRequestPolicyQueryStringsConfig.class);
            queryStringsConfig.copyFrom(model.queryStringsConfig());
            setQueryStringsConfig(queryStringsConfig);
        }
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    software.amazon.awssdk.services.cloudfront.model.OriginRequestPolicyConfig toOriginRequestPolicyConfig() {
        return software.amazon.awssdk.services.cloudfront.model.OriginRequestPolicyConfig.builder()
            .comment(getComment())
            .name(getName())
            .cookiesConfig(getCookiesConfig() != null ? getCookiesConfig().toOriginRequestPolicyCookiesConfig() : null)
            .headersConfig(getHeadersConfig() != null ? getHeadersConfig().toOriginRequestPolicyHeadersConfig() : null)
            .queryStringsConfig(
                getQueryStringsConfig() != null ? getQueryStringsConfig().toOriginRequestPolicyQueryStringsConfig() : null)
            .build();
    }
}
