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

import java.util.UUID;
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
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.CreatePublicKeyRequest;
import software.amazon.awssdk.services.cloudfront.model.CreatePublicKeyResponse;
import software.amazon.awssdk.services.cloudfront.model.DeletePublicKeyRequest;
import software.amazon.awssdk.services.cloudfront.model.GetPublicKeyRequest;
import software.amazon.awssdk.services.cloudfront.model.GetPublicKeyResponse;
import software.amazon.awssdk.services.cloudfront.model.NoSuchPublicKeyException;
import software.amazon.awssdk.services.cloudfront.model.PublicKey;
import software.amazon.awssdk.services.cloudfront.model.PublicKeyConfig;
import software.amazon.awssdk.services.cloudfront.model.UpdatePublicKeyRequest;
import software.amazon.awssdk.services.cloudfront.model.UpdatePublicKeyResponse;

/**
 * Create a CloudFront public key.
 *
 * A public key is the RSA public key that CloudFront uses to verify signed URLs and signed cookies.
 * It is referenced by a ``aws::cloudfront-key-group``, which is in turn attached to a distribution
 * cache behavior via its ``trusted-key-groups`` field.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::cloudfront-public-key public-key-example
 *        name: "example-public-key"
 *        comment: "example public key"
 *        encoded-key: "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A...\n-----END PUBLIC KEY-----\n"
 *    end
 */
@Type("cloudfront-public-key")
public class CloudFrontPublicKeyResource extends AwsResource implements Copyable<PublicKey> {

    private String name;
    private String encodedKey;
    private String comment;
    private String callerReference;

    // Read-only
    private String id;
    private String eTag;

    /**
     * The name of the public key.
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
     * The public key, in PEM encoded format, that CloudFront uses to verify the signatures of signed URLs and signed cookies.
     */
    @Required
    @Regex(value = "(?s)^-----BEGIN PUBLIC KEY-----.*-----END PUBLIC KEY-----\\s*$", message = "a PEM encoded public key delimited by '-----BEGIN PUBLIC KEY-----' and '-----END PUBLIC KEY-----'")
    public String getEncodedKey() {
        return encodedKey;
    }

    public void setEncodedKey(String encodedKey) {
        this.encodedKey = encodedKey;
    }

    /**
     * A comment to describe the public key.
     */
    @Updatable
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * A unique value that ensures that the request can't be replayed. Generated automatically if not set.
     * This value is immutable once the public key has been created.
     */
    public String getCallerReference() {
        return callerReference;
    }

    public void setCallerReference(String callerReference) {
        this.callerReference = callerReference;
    }

    /**
     * The ID of the public key.
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
     * The current version (ETag value) of the public key.
     */
    @Output
    public String getETag() {
        return eTag;
    }

    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    @Override
    public void copyFrom(PublicKey model) {
        setId(model.id());

        PublicKeyConfig config = model.publicKeyConfig();
        if (config != null) {
            setName(config.name());
            setEncodedKey(config.encodedKey());
            setComment(config.comment());
            setCallerReference(config.callerReference());
        }

        // The ETag is required for update / delete and is only returned by the Get operation,
        // not embedded in the PublicKey model, so it is fetched explicitly here.
        CloudFrontClient client = getCloudFrontClient();
        GetPublicKeyResponse response = getPublicKey(client);
        setETag(response != null ? response.eTag() : null);
    }

    @Override
    public boolean refresh() {
        CloudFrontClient client = getCloudFrontClient();

        GetPublicKeyResponse response = getPublicKey(client);

        if (response == null || response.publicKey() == null) {
            return false;
        }

        copyFrom(response.publicKey());

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        CloudFrontClient client = getCloudFrontClient();

        // Persist the generated caller reference so subsequent updates reuse it (it is immutable).
        if (getCallerReference() == null) {
            setCallerReference(UUID.randomUUID().toString());
        }

        CreatePublicKeyResponse response = client.createPublicKey(CreatePublicKeyRequest.builder()
            .publicKeyConfig(toPublicKeyConfig())
            .build());

        setId(response.publicKey().id());
        setETag(response.eTag());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        CloudFrontClient client = getCloudFrontClient();

        UpdatePublicKeyResponse response = client.updatePublicKey(UpdatePublicKeyRequest.builder()
            .id(getId())
            .publicKeyConfig(toPublicKeyConfig())
            .ifMatch(getETag())
            .build());

        setETag(response.eTag());
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        CloudFrontClient client = getCloudFrontClient();

        client.deletePublicKey(DeletePublicKeyRequest.builder()
            .id(getId())
            .ifMatch(getETag())
            .build());
    }

    private PublicKeyConfig toPublicKeyConfig() {
        return PublicKeyConfig.builder()
            .callerReference(getCallerReference())
            .name(getName())
            .encodedKey(getEncodedKey())
            .comment(getComment())
            .build();
    }

    private CloudFrontClient getCloudFrontClient() {
        return createClient(CloudFrontClient.class, "us-east-1", "https://cloudfront.amazonaws.com");
    }

    private GetPublicKeyResponse getPublicKey(CloudFrontClient client) {
        GetPublicKeyResponse response = null;

        try {
            response = client.getPublicKey(GetPublicKeyRequest.builder().id(getId()).build());
        } catch (NoSuchPublicKeyException ex) {
            // Public key no longer exists.
        }

        return response;
    }
}
