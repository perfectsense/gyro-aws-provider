/*
 * Copyright 2022, Brightspot.
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
import software.amazon.awssdk.services.cloudfront.model.CreateOriginAccessControlRequest;
import software.amazon.awssdk.services.cloudfront.model.CreateOriginAccessControlResponse;
import software.amazon.awssdk.services.cloudfront.model.DeleteOriginAccessControlRequest;
import software.amazon.awssdk.services.cloudfront.model.GetOriginAccessControlRequest;
import software.amazon.awssdk.services.cloudfront.model.GetOriginAccessControlResponse;
import software.amazon.awssdk.services.cloudfront.model.OriginAccessControl;
import software.amazon.awssdk.services.cloudfront.model.UpdateOriginAccessControlRequest;
import software.amazon.awssdk.services.cloudfront.model.UpdateOriginAccessControlResponse;

/**
 * Create a Origin Access Control.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::origin-access-control origin-access-control-example
 *        config
 *            name: "example-oac"
 *            origin-type: S3
 *            description: "example description"
 *            signing-behavior: NEVER
 *            signing-protocol: SIGV4
 *        end
 *    end
 */
@Type("origin-access-control")
public class OriginAccessControlResource extends AwsResource implements Copyable<OriginAccessControl> {

    private OriginAccessControlConfig config;

    // Read-only
    private String id;
    private String eTag;

    /**
     * The configuration for the Origin Access Control (OAC).
     *
     * @subresource gyro.aws.cloudfront.OriginAccessControlConfig
     */
    @Updatable
    @Required
    public OriginAccessControlConfig getConfig() {
        return config;
    }

    public void setConfig(OriginAccessControlConfig config) {
        this.config = config;
    }

    /**
     * The ID of the Origin Access Control (OAC).
     */
    @Output
    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The current version (ETag value) of the Origin Access Control (OAC).
     */
    @Output
    public String getETag() {
        return eTag;
    }

    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    @Override
    public void copyFrom(OriginAccessControl model) {
        setId(model.id());

        setConfig(null);
        if (model.originAccessControlConfig() != null) {
            OriginAccessControlConfig accessControlConfig = new OriginAccessControlConfig();
            accessControlConfig.copyFrom(model.originAccessControlConfig());
            setConfig(accessControlConfig);
        }

        // set eTag
        CloudFrontClient client = getCloudFrontClient();
        GetOriginAccessControlResponse accessControl =
            client.getOriginAccessControl(GetOriginAccessControlRequest.builder().id(getId()).build());
        setETag(accessControl.eTag());
    }

    @Override
    public boolean refresh() {
        CloudFrontClient client = getCloudFrontClient();

        GetOriginAccessControlResponse accessControl =
            client.getOriginAccessControl(GetOriginAccessControlRequest.builder().id(getId()).build());

        if (accessControl == null) {
            return false;
        }

        copyFrom(accessControl.originAccessControl());

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        CloudFrontClient client = getCloudFrontClient();

        CreateOriginAccessControlResponse response = client.createOriginAccessControl(CreateOriginAccessControlRequest
            .builder().originAccessControlConfig(getConfig().toOriginAccessControlConfig()).build());

        setId(response.originAccessControl().id());
        setETag(response.eTag());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        CloudFrontClient client = getCloudFrontClient();

        UpdateOriginAccessControlResponse response =
            client.updateOriginAccessControl(UpdateOriginAccessControlRequest
                .builder().id(getId()).originAccessControlConfig(getConfig().toOriginAccessControlConfig())
                .ifMatch(getETag()).build());

        setETag(response.eTag());
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        CloudFrontClient client = getCloudFrontClient();

        client.deleteOriginAccessControl(
            DeleteOriginAccessControlRequest.builder().ifMatch(getETag()).id(getId()).build());
    }

    private CloudFrontClient getCloudFrontClient() {
        return createClient(CloudFrontClient.class, "us-east-1", "https://cloudfront.amazonaws.com");
    }
}
