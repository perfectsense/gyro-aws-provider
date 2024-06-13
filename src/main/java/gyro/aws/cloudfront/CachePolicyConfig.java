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

public class CachePolicyConfig
    extends Diffable implements Copyable<software.amazon.awssdk.services.cloudfront.model.CachePolicyConfig> {

    private String comment;
    private Long defaultTtl;
    private String name;
    private Long maxTtl;
    private Long minTtl;
    private CachePolicyKeyParam keyParam;

    /**
     * The comment for the cache policy.
     */
    @Updatable
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * The default time to live for the cache policy.
     */
    @Updatable
    public Long getDefaultTtl() {
        return defaultTtl;
    }

    public void setDefaultTtl(Long defaultTtl) {
        this.defaultTtl = defaultTtl;
    }

    /**
     * The name for the cache policy.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The maximum time to live for the cache policy.
     */
    @Updatable
    public Long getMaxTtl() {
        return maxTtl;
    }

    public void setMaxTtl(Long maxTtl) {
        this.maxTtl = maxTtl;
    }

    /**
     * The minimum time to live for the cache policy.
     */
    @Updatable
    public Long getMinTtl() {
        return minTtl;
    }

    public void setMinTtl(Long minTtl) {
        this.minTtl = minTtl;
    }

    /**
     * The key param for the cache policy.
     *
     * @subresource gyro.aws.cloudfront.CachePolicyKeyParam
     */
    public CachePolicyKeyParam getKeyParam() {
        return keyParam;
    }

    public void setKeyParam(CachePolicyKeyParam keyParam) {
        this.keyParam = keyParam;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.cloudfront.model.CachePolicyConfig model) {
        setComment(model.comment());
        setDefaultTtl(model.defaultTTL());
        setName(model.name());
        setMaxTtl(model.maxTTL());
        setMinTtl(model.minTTL());

        setKeyParam(null);
        if (model.parametersInCacheKeyAndForwardedToOrigin() != null) {
            CachePolicyKeyParam keyParam = newSubresource(CachePolicyKeyParam.class);
            keyParam.copyFrom(model.parametersInCacheKeyAndForwardedToOrigin());
            setKeyParam(keyParam);
        }
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    software.amazon.awssdk.services.cloudfront.model.CachePolicyConfig toCachePolicyConfig() {
        return software.amazon.awssdk.services.cloudfront.model.CachePolicyConfig.builder()
            .comment(getComment())
            .defaultTTL(getDefaultTtl())
            .name(getName())
            .maxTTL(getMaxTtl())
            .minTTL(getMinTtl())
            .parametersInCacheKeyAndForwardedToOrigin(getKeyParam() != null ? getKeyParam().toParametersInCacheKeyAndForwardedToOrigin() : null)
            .build();
    }
}
