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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.CollectionMin;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.CreateKeyGroupRequest;
import software.amazon.awssdk.services.cloudfront.model.CreateKeyGroupResponse;
import software.amazon.awssdk.services.cloudfront.model.DeleteKeyGroupRequest;
import software.amazon.awssdk.services.cloudfront.model.GetKeyGroupRequest;
import software.amazon.awssdk.services.cloudfront.model.GetKeyGroupResponse;
import software.amazon.awssdk.services.cloudfront.model.KeyGroup;
import software.amazon.awssdk.services.cloudfront.model.KeyGroupConfig;
import software.amazon.awssdk.services.cloudfront.model.NoSuchResourceException;
import software.amazon.awssdk.services.cloudfront.model.UpdateKeyGroupRequest;
import software.amazon.awssdk.services.cloudfront.model.UpdateKeyGroupResponse;

/**
 * Create a CloudFront key group.
 *
 * A key group references one or more ``aws::cloudfront-public-key`` resources. It is attached to a
 * distribution cache behavior through the behavior's ``trusted-key-groups`` field, which makes
 * CloudFront require a valid signed URL or signed cookie for every request served by that behavior.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::cloudfront-key-group key-group-example
 *        name: "example-key-group"
 *        comment: "example key group"
 *        items: [
 *            $(aws::cloudfront-public-key public-key-example)
 *        ]
 *    end
 */
@Type("cloudfront-key-group")
public class CloudFrontKeyGroupResource extends AwsResource implements Copyable<KeyGroup> {

    private String name;
    private Set<CloudFrontPublicKeyResource> items;
    private String comment;

    // Read-only
    private String id;
    private String eTag;

    /**
     * The name of the key group.
     */
    @Required
    @Updatable
    @Regex(value = "[A-Za-z0-9_-]{1,128}", message = "a string 1-128 characters long containing only alphanumeric characters, hyphens or underscores")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The public keys that make up this key group. At least one public key is required.
     */
    @Required
    @Updatable
    @CollectionMin(1)
    public Set<CloudFrontPublicKeyResource> getItems() {
        if (items == null) {
            items = new LinkedHashSet<>();
        }

        return items;
    }

    public void setItems(Set<CloudFrontPublicKeyResource> items) {
        this.items = items;
    }

    /**
     * A comment to describe the key group.
     */
    @Updatable
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * The ID of the key group.
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
     * The current version (ETag value) of the key group.
     */
    @Output
    public String getETag() {
        return eTag;
    }

    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    @Override
    public void copyFrom(KeyGroup model) {
        setId(model.id());

        KeyGroupConfig config = model.keyGroupConfig();
        if (config != null) {
            setName(config.name());
            setComment(config.comment());

            getItems().clear();
            if (config.hasItems()) {
                for (String publicKeyId : config.items()) {
                    getItems().add(findById(CloudFrontPublicKeyResource.class, publicKeyId));
                }
            }
        }

        // The ETag is required for update / delete and is only returned by the Get operation,
        // not embedded in the KeyGroup model, so it is fetched explicitly here.
        CloudFrontClient client = getCloudFrontClient();
        GetKeyGroupResponse response = getKeyGroup(client);
        setETag(response != null ? response.eTag() : null);
    }

    @Override
    public boolean refresh() {
        CloudFrontClient client = getCloudFrontClient();

        GetKeyGroupResponse response = getKeyGroup(client);

        if (response == null || response.keyGroup() == null) {
            return false;
        }

        copyFrom(response.keyGroup());

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        CloudFrontClient client = getCloudFrontClient();

        CreateKeyGroupResponse response = client.createKeyGroup(CreateKeyGroupRequest.builder()
            .keyGroupConfig(toKeyGroupConfig())
            .build());

        setId(response.keyGroup().id());
        setETag(response.eTag());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        CloudFrontClient client = getCloudFrontClient();

        UpdateKeyGroupResponse response = client.updateKeyGroup(UpdateKeyGroupRequest.builder()
            .id(getId())
            .keyGroupConfig(toKeyGroupConfig())
            .ifMatch(getETag())
            .build());

        setETag(response.eTag());
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        CloudFrontClient client = getCloudFrontClient();

        client.deleteKeyGroup(DeleteKeyGroupRequest.builder()
            .id(getId())
            .ifMatch(getETag())
            .build());
    }

    private KeyGroupConfig toKeyGroupConfig() {
        return KeyGroupConfig.builder()
            .name(getName())
            .comment(getComment())
            .items(getItems().stream()
                .map(CloudFrontPublicKeyResource::getId)
                .collect(Collectors.toList()))
            .build();
    }

    private CloudFrontClient getCloudFrontClient() {
        return createClient(CloudFrontClient.class, "us-east-1", "https://cloudfront.amazonaws.com");
    }

    private GetKeyGroupResponse getKeyGroup(CloudFrontClient client) {
        GetKeyGroupResponse response = null;

        try {
            response = client.getKeyGroup(GetKeyGroupRequest.builder().id(getId()).build());
        } catch (NoSuchResourceException ex) {
            // Key group no longer exists.
        }

        return response;
    }
}
