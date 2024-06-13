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
import software.amazon.awssdk.services.cloudfront.model.OriginRequestPolicy;
import software.amazon.awssdk.services.cloudfront.model.GetOriginRequestPolicyResponse;
import software.amazon.awssdk.services.cloudfront.model.NoSuchOriginRequestPolicyException;

/**
 * Create a cloudfront origin request policy.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::cloudfront-origin-request-policy origin-request-policy-example
 *         origin-request-policy-config
 *             default-ttl: 3600
 *             max-ttl: 86400
 *             min-ttl: 0
 *             name: "origin-request-policy-example"
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
@Type("cloudfront-origin-request-policy")
public class OriginRequestPolicyResource extends AwsResource implements Copyable<OriginRequestPolicy> {

    private OriginRequestPolicyConfig originRequestPolicyConfig;
    private String id;

    /**
     * The origin request policy configuration.
     *
     * @subresource gyro.aws.cloudfront.OriginRequestPolicyConfig
     */
    @Required
    @Updatable
    public OriginRequestPolicyConfig getOriginRequestPolicyConfig() {
        return originRequestPolicyConfig;
    }

    public void setOriginRequestPolicyConfig(OriginRequestPolicyConfig originRequestPolicyConfig) {
        this.originRequestPolicyConfig = originRequestPolicyConfig;
    }

    /**
     * The ID for the origin request policy.
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
    public void copyFrom(OriginRequestPolicy model) {
        setId(model.id());

        setOriginRequestPolicyConfig(null);
        if (model.originRequestPolicyConfig() != null) {
            OriginRequestPolicyConfig config = newSubresource(OriginRequestPolicyConfig.class);
            config.copyFrom(model.originRequestPolicyConfig());
            setOriginRequestPolicyConfig(config);
        }
    }

    @Override
    public boolean refresh() {

        CloudFrontClient client = getCloudFrontClient();

        try {
            GetOriginRequestPolicyResponse originRequestPolicy = client.getOriginRequestPolicy(r -> r.id(getId()));

            copyFrom(originRequestPolicy.originRequestPolicy());

            return true;
        } catch (NoSuchOriginRequestPolicyException ex) {
            return false;
        }
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        CloudFrontClient client = getCloudFrontClient();

        OriginRequestPolicy originRequestPolicy = client.createOriginRequestPolicy(r -> r.originRequestPolicyConfig(getOriginRequestPolicyConfig().toOriginRequestPolicyConfig())).originRequestPolicy();

        setId(originRequestPolicy.id());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        CloudFrontClient client = getCloudFrontClient();

        client.updateOriginRequestPolicy(r -> r
            .originRequestPolicyConfig(getOriginRequestPolicyConfig().toOriginRequestPolicyConfig())
            .id(getId())
            .ifMatch(getOriginRequestPolicyEtag(client))
        );
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        CloudFrontClient client = getCloudFrontClient();

        client.deleteOriginRequestPolicy(r -> r.id(getId()).ifMatch(getOriginRequestPolicyEtag(client)));
    }

    private CloudFrontClient getCloudFrontClient() {
        CloudFrontClient client = createClient(
            CloudFrontClient.class,
            "us-east-1",
            "https://cloudfront.amazonaws.com");

        return client;
    }

    private String getOriginRequestPolicyEtag(CloudFrontClient client) {
        String etag = null;

        try {
            GetOriginRequestPolicyResponse response = client.getOriginRequestPolicy(r -> r.id(getId()));
            etag = response.eTag();
        } catch (NoSuchOriginRequestPolicyException ex) {
            // ignore
        }

        return etag;
    }
}
