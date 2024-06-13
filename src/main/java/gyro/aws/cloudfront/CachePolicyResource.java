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

import java.util.Set;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.CachePolicy;
import software.amazon.awssdk.services.cloudfront.model.GetCachePolicyResponse;
import software.amazon.awssdk.services.cloudfront.model.NoSuchCachePolicyException;

/**
 * Create a cloudfront cache policy.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::cloudfront-cache-policy cache-policy-example
 *         cache-policy-config
 *             default-ttl: 3600
 *             max-ttl: 86400
 *             min-ttl: 0
 *             name: "cache-policy-example"
 *
 *             key-param
 *                 accept-encoding-brotli: true
 *                 accept-encoding-gzip: true
 *
 *                 headers-config
 *                     header-behavior: "whitelist"
 *                     headers: [
 *                         "example-header1",
 *                         "example-header2"
 *                     ]
 *                 end
 *
 *                 query-strings-config
 *                     query-string-behavior: "whitelist"
 *                     query-strings: [
 *                         "example-query1",
 *                         "example-query2"
 *                     ]
 *                 end
 *
 *                 cookies-config
 *                     cookie-behavior: "whitelist"
 *                     cookies: [
 *                         "example-cookie1",
 *                         "example-cookie2"
 *                     ]
 *                 end
 *             end
 *         end
 *     end
 */
@Type("cloudfront-cache-policy")
public class CachePolicyResource extends AwsResource implements Copyable<CachePolicy> {

    private CachePolicyConfig cachePolicyConfig;
    private String id;

    /**
     * The cache policy configuration.
     *
     * @subresource gyro.aws.cloudfront.CachePolicyConfig
     */
    @Required
    @Updatable
    public CachePolicyConfig getCachePolicyConfig() {
        return cachePolicyConfig;
    }

    public void setCachePolicyConfig(CachePolicyConfig cachePolicyConfig) {
        this.cachePolicyConfig = cachePolicyConfig;
    }

    /**
     * The ID for the cache policy.
     */
    @Output
    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(CachePolicy model) {
        setId(model.id());

        setCachePolicyConfig(null);
        if (model.cachePolicyConfig() != null) {
            CachePolicyConfig config = newSubresource(CachePolicyConfig.class);
            config.copyFrom(model.cachePolicyConfig());
            setCachePolicyConfig(config);
        }
    }

    @Override
    public boolean refresh() {

        CloudFrontClient client = getCloudFrontClient();

        try {
            GetCachePolicyResponse cachePolicy = client.getCachePolicy(r -> r.id(getId()));

            copyFrom(cachePolicy.cachePolicy());

            return true;
        } catch (NoSuchCachePolicyException ex) {
            return false;
        }
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        CloudFrontClient client = getCloudFrontClient();

        CachePolicy cachePolicy = client.createCachePolicy(r -> r.cachePolicyConfig(getCachePolicyConfig().toCachePolicyConfig())).cachePolicy();

        setId(cachePolicy.id());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        CloudFrontClient client = getCloudFrontClient();

        client.updateCachePolicy(r -> r
            .cachePolicyConfig(getCachePolicyConfig().toCachePolicyConfig())
            .id(getId())
            .ifMatch(getCachePolicyEtag(client))
        );
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        CloudFrontClient client = getCloudFrontClient();

        client.deleteCachePolicy(r -> r.id(getId()).ifMatch(getCachePolicyEtag(client)));
    }

    private CloudFrontClient getCloudFrontClient() {
        CloudFrontClient client = createClient(
            CloudFrontClient.class,
            "us-east-1",
            "https://cloudfront.amazonaws.com");

        return client;
    }

    private String getCachePolicyEtag(CloudFrontClient client) {
        String etag = null;

        try {
            GetCachePolicyResponse response = client.getCachePolicy(r -> r.id(getId()));
            etag = response.eTag();
        } catch (NoSuchCachePolicyException ex) {
            // ignore
        }

        return etag;
    }
}
