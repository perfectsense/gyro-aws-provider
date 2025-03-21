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

package gyro.aws.lambda;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.AddLayerVersionPermissionRequest;
import software.amazon.awssdk.services.lambda.model.GetLayerVersionPolicyResponse;
import software.amazon.awssdk.services.lambda.model.GetLayerVersionResponse;
import software.amazon.awssdk.services.lambda.model.PublishLayerVersionRequest;
import software.amazon.awssdk.services.lambda.model.PublishLayerVersionResponse;
import software.amazon.awssdk.services.lambda.model.ResourceNotFoundException;
import software.amazon.awssdk.services.lambda.model.Runtime;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a lambda layer.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::lambda-layer lambda-layer-example
 *         name: "lambda-layer-example"
 *         description: "lambda-layer-example-desc"
 *         content-zip-path: "example-function.zip"
 *         compatible-runtimes: [
 *             "nodejs20.x"
 *         ]
 *     end
 */
@Type("lambda-layer")
public class LayerResource extends AwsResource implements Copyable<GetLayerVersionResponse> {
    private String name;
    private String description;
    private String licenseInfo;
    private Set<String> compatibleRuntimes;
    private String s3Bucket;
    private String s3Key;
    private String s3ObjectVersion;
    private String contentZipPath;
    private Set<LayerPermission> permission;

    // -- Readonly

    private String arn;
    private String versionArn;
    private Long version;
    private String createdDate;

    /**
     * The name of the Lambda Layer.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The description of the Lambda Layer.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The software license for the Lambda Layer.
     */
    public String getLicenseInfo() {
        return licenseInfo;
    }

    public void setLicenseInfo(String licenseInfo) {
        this.licenseInfo = licenseInfo;
    }

    /**
     * The list of runtime language for the function using this Lambda Layer.  See `Supported Runtimes for Lambda <https://docs.aws.amazon.com/lambda/latest/dg/lambda-runtimes.html>`_.
     */
    @Required
    public Set<String> getCompatibleRuntimes() {
        if (compatibleRuntimes == null) {
            compatibleRuntimes = new HashSet<>();
        }

        return compatibleRuntimes;
    }

    public void setCompatibleRuntimes(Set<String> compatibleRuntimes) {
        this.compatibleRuntimes = compatibleRuntimes;
    }

    /**
     * The S3 Bucket name where the code for the function using this Lambda Layer resides. Required if field 'content-zip-path' not set.
     */
    public String getS3Bucket() {
        return s3Bucket;
    }

    public void setS3Bucket(String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    /**
     * The S3 object key where the code for the function using this Lambda Layer resides. Required if field 'content-zip-path' not set.
     */
    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    /**
     * The S3 object version where the code for the function using this Lambda Layer resides. Required if field 'content-zip-path' not set.
     */
    public String getS3ObjectVersion() {
        return s3ObjectVersion;
    }

    public void setS3ObjectVersion(String s3ObjectVersion) {
        this.s3ObjectVersion = s3ObjectVersion;
    }

    /**
     * The zip file location where the code for the function using this Lambda Layer resides. Required if fields 's3-bucket', 's3-key' and 's3-object-version' not set.
     */
    public String getContentZipPath() {
        return contentZipPath;
    }

    public void setContentZipPath(String contentZipPath) {
        this.contentZipPath = contentZipPath;
    }

    /**
     * The list of permissions for the Lambda Layer.
     *
     * @subresource gyro.aws.lambda.LayerPermission
     */
    @Updatable
    public Set<LayerPermission> getPermission() {
        if (permission == null) {
            permission = new HashSet<>();
        }

        return permission;
    }

    public void setPermission(Set<LayerPermission> permission) {
        this.permission = permission;
    }

    /**
     * The ARN of the Lambda Layer.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The ARN of the Lambda Layer version specific.
     */
    @Id
    @Output
    public String getVersionArn() {
        return versionArn;
    }

    public void setVersionArn(String versionArn) {
        this.versionArn = versionArn;
    }

    /**
     * The version of the Lambda Layer.
     */
    @Output
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * The date that the Lambda Layer version was created.
     */
    @Output
    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public void copyFrom(GetLayerVersionResponse response) {
        setVersion(response.version());
        setCompatibleRuntimes(new HashSet<>(response.compatibleRuntimesAsStrings()));
        setCreatedDate(response.createdDate());
        setDescription(response.description());
        setArn(response.layerArn());
        setVersionArn(response.layerVersionArn());
        setLicenseInfo(response.licenseInfo());

        // Set permissions
        getPermission().clear();
        LambdaClient client = createClient(LambdaClient.class);
        try {
            GetLayerVersionPolicyResponse policy = client.getLayerVersionPolicy(r -> r.layerName(getName())
                .versionNumber(getVersion()));

            setPolicy(policy.policy());
        } catch (ResourceNotFoundException ex) {
            // Ignore
        }
    }

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getName()) || getVersion() == null) {
            throw new GyroException("name and/or version is missing, unable to load lambda layer.");
        }

        LambdaClient client = createClient(LambdaClient.class);

        try {
            GetLayerVersionResponse response = client.getLayerVersion(
                r -> r.layerName(getName())
                    .versionNumber(getVersion())
            );

            copyFrom(response);
        } catch (ResourceNotFoundException ex) {
            return false;
        }


        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        LambdaClient client = createClient(LambdaClient.class);

        PublishLayerVersionRequest.Builder builder = PublishLayerVersionRequest.builder()
            .compatibleRuntimes(getCompatibleRuntimes().stream().map(Runtime::fromValue).collect(Collectors.toList()))
            .layerName(getName())
            .description(getDescription())
            .licenseInfo(getLicenseInfo());

        if (!ObjectUtils.isBlank(getContentZipPath())) {
            builder = builder.content(c -> c.zipFile(getZipFile()));

        } else {
            builder = builder.content(c -> c.s3Bucket(getS3Bucket())
                .s3Key(getS3Key())
                .s3ObjectVersion(getS3ObjectVersion())
            );
        }

        PublishLayerVersionResponse response = client.publishLayerVersion(builder.build());

        setArn(response.layerArn());
        setVersionArn(response.layerVersionArn());
        setVersion(response.version());
        setCreatedDate(response.createdDate());
    }

    @Override
    public void update(GyroUI ui, State state, Resource resource, Set<String> changedFieldNames) {
        LambdaClient client = createClient(LambdaClient.class);
        LayerResource oldResource = (LayerResource) resource;

        if (changedFieldNames.contains("permission")) {
            if (!oldResource.getPermission().isEmpty()) {
                for (LayerPermission permission : oldResource.getPermission()) {
                    client.removeLayerVersionPermission(r -> r.layerName(getName())
                        .versionNumber(getVersion())
                        .statementId(permission.getStatementId())
                    );
                }
            }

            for (LayerPermission permission : getPermission()) {
                client.addLayerVersionPermission(permission.toAddLayerVersionPermissionRequest());
            }
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        LambdaClient client = createClient(LambdaClient.class);

        client.deleteLayerVersion(
            r -> r.layerName(getName())
                .versionNumber(getVersion())
        );
    }

    private SdkBytes getZipFile() {
        try (InputStream input = openInput(getContentZipPath())) {
            return SdkBytes.fromInputStream(input);

        } catch (IOException ex) {
            throw new GyroException(String.format("File not found - %s",getContentZipPath()));
        }
    }

    private void setPolicy(String jsonPolicy) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode policyNode = objectMapper.readTree(jsonPolicy);
            JsonNode statements = policyNode.get("Statement");

            if (statements == null || !statements.isArray()) {
                throw new IllegalArgumentException("Invalid policy format. Needs at least one statement.");
            }

            for (JsonNode statement : statements) {
                AddLayerVersionPermissionRequest request = LayerPermission.getAddLayerPermissionRequest(statement);
                LayerPermission permission = newSubresource(LayerPermission.class);
                permission.copyFrom(request);
                getPermission().add(permission);
            }

        } catch (Exception e) {
            throw new GyroException("Error parsing layer policy.", e);
        }
    }

}
