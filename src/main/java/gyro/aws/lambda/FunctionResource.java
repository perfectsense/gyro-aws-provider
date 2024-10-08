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

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.aws.ec2.SubnetResource;
import gyro.aws.iam.RoleResource;
import gyro.aws.kms.KmsKeyResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Range;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import org.apache.commons.codec.digest.DigestUtils;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.CreateFunctionRequest;
import software.amazon.awssdk.services.lambda.model.CreateFunctionResponse;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;
import software.amazon.awssdk.services.lambda.model.GetFunctionResponse;
import software.amazon.awssdk.services.lambda.model.ListTagsResponse;
import software.amazon.awssdk.services.lambda.model.ListVersionsByFunctionResponse;
import software.amazon.awssdk.services.lambda.model.ResourceNotFoundException;
import software.amazon.awssdk.services.lambda.model.UpdateFunctionCodeRequest;
import software.amazon.awssdk.services.lambda.model.UpdateFunctionCodeResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a lambda function.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::lambda-function lambda-function-example
 *         name: "testFunction"
 *         handler: "index.handler"
 *         runtime: "nodejs8.10"
 *         role: "arn:aws:iam::242040583208:role/service-role/testFunctionRole"
 *         content-zip-path: "example-function.zip"
 *
 *         tags: {
 *             Name: "lambda-function-example"
 *         }
 *
 *         lambda-layers: [
 *             $(aws::lambda-layer lambda-layer-for-function-example-1)
 *         ]
 *     end
 */
@Type("lambda-function")
public class FunctionResource extends AwsResource implements Copyable<FunctionConfiguration> {
    private String name;
    private String description;
    private String s3Bucket;
    private String s3Key;
    private String s3ObjectVersion;
    private String contentZipPath;
    private RoleResource role;
    private String runtime;
    private String handler;
    private Integer timeout;
    private Integer memorySize;
    private String trackingConfig;
    private String deadLetterConfigArn;
    private KmsKeyResource kmsKey;
    private Map<String, String> environment;
    private Map<String, String> tags;
    private Set<SecurityGroupResource> securityGroups;
    private Set<SubnetResource> subnets;
    private Set<LayerResource> lambdaLayers;
    private Integer reservedConcurrentExecutions;
    private Map<String, String> versionMap;
    private Boolean publish;

    // -- Readonly

    private String arn;
    private String arnNoVersion;
    private String revisionId;
    private String masterArn;
    private String lastModified;
    private String version;
    private String codeHash;

    /**
     * The name of the Lambda Function.
     */
    @Id
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The description of the Lambda Function.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The S3 bucket name where the Lambda Function code resides. Required if field 'content-zip-path' not set.
     */
    @Updatable
    public String getS3Bucket() {
        return s3Bucket;
    }

    public void setS3Bucket(String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    /**
     * The S3 object key where the Lambda Function code resides. Required if field 'content-zip-path' not set.
     */
    @Updatable
    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    /**
     * The S3 object version where the Lambda Function code resides. Required if field 'content-zip-path' not set.
     */
    @Updatable
    public String getS3ObjectVersion() {
        return s3ObjectVersion;
    }

    public void setS3ObjectVersion(String s3ObjectVersion) {
        this.s3ObjectVersion = s3ObjectVersion;
    }

    /**
     * The zip file location where the Lambda Function code resides. Required if fields 's3-bucket', 's3-key' and 's3-object-version' not set.
     */
    @Updatable
    public String getContentZipPath() {
        if (!ObjectUtils.isBlank(contentZipPath) && !contentZipPath.startsWith("Path:")) {
            return String.format("Path: %s, Hash: %s", contentZipPath, getFileHashFromPath());
        } else {
            return contentZipPath;
        }
    }

    private String getContentZipPathRaw() {
        return contentZipPath;
    }

    public void setContentZipPath(String contentZipPath) {
        this.contentZipPath = contentZipPath;
    }

    /**
     * The IAM Role to be associated with this Lambda Function.
     */
    @Required
    @Updatable
    public RoleResource getRole() {
        return role;
    }

    public void setRole(RoleResource role) {
        this.role = role;
    }

    /**
     * The runtime language for this Lambda Function.
     */
    @Required
    @Updatable
    @ValidStrings({"nodejs20.x", "nodejs18.x", "python3.12", "python3.11", "python3.10", "python3.9", "python3.8", "java21", "java17", "java11", "java8.al2", "dotnet8", "dotnet6", "ruby3.3", "ruby3.2", "provided.al2023", "provided.al2"})
    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    /**
     * The name of the method within your code that Lambda calls to execute the Lambda Function.
     */
    @Required
    @Updatable
    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    /**
     * The amount of time that Lambda allows a Lambda Function to run before stopping it. Defaults to ``3``.
     */
    @Updatable
    @Range(min = 3, max = 900)
    public Integer getTimeout() {
        if (timeout == null) {
            timeout = 3;
        }

        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    /**
     * The amount of memory that the Lambda Function has access to. Defaults to ``128``.
     */
    @Updatable
    public Integer getMemorySize() {
        if (memorySize == null) {
            memorySize = 128;
        }

        return memorySize;
    }

    public void setMemorySize(Integer memorySize) {
        this.memorySize = memorySize;
    }

    /**
     * The tracking mode of the Lambda Function. Defaults to ``PassThrough``. Valid values are ``PassThrough`` or ``Active``
     */
    @Updatable
    @ValidStrings({"PassThrough", "Active"})
    public String getTrackingConfig() {
        if (trackingConfig == null) {
            trackingConfig = "PassThrough";
        }

        return trackingConfig;
    }

    public void setTrackingConfig(String trackingConfig) {
        this.trackingConfig = trackingConfig;
    }

    /**
     * The arn of SQS queue or an SNS topic to be associated with the Lambda Function.
     */
    @Updatable
    public String getDeadLetterConfigArn() {
        if (deadLetterConfigArn == null) {
            deadLetterConfigArn = "";
        }

        return deadLetterConfigArn;
    }

    public void setDeadLetterConfigArn(String deadLetterConfigArn) {
        this.deadLetterConfigArn = deadLetterConfigArn;
    }

    /**
     * The KMS key to be associated with the Lambda Function.
     */
    @Updatable
    public KmsKeyResource getKmsKey() {
        return kmsKey;
    }

    public void setKmsKey(KmsKeyResource kmsKey) {
        this.kmsKey = kmsKey;
    }

    /**
     * A map of key value pair acting as variables accessible from the code of with the Lambda Function.
     */
    @Updatable
    public Map<String, String> getEnvironment() {
        if (environment == null) {
            environment = new HashMap<>();
        }

        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    /**
     * The set of tags to be associated with the Lambda Function.
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The set of security group be associated with the Lambda Function.
     */
    @Updatable
    public Set<SecurityGroupResource> getSecurityGroups() {
        if (securityGroups == null) {
            securityGroups = new HashSet<>();
        }

        return securityGroups;
    }

    public void setSecurityGroups(Set<SecurityGroupResource> securityGroups) {
        this.securityGroups = securityGroups;
    }

    /**
     * The set of subnet be associated with the Lambda Function.
     */
    @Updatable
    public Set<SubnetResource> getSubnets() {
        if (subnets == null) {
            subnets = new HashSet<>();
        }

        return subnets;
    }

    public void setSubnets(Set<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    /**
     * The set of version arns of Lambda Layers to be associated with the Lambda Function.
     */
    @Updatable
    public Set<LayerResource> getLambdaLayers() {
        if (lambdaLayers == null) {
            lambdaLayers = new HashSet<>();
        }

        return lambdaLayers;
    }

    public void setLambdaLayers(Set<LayerResource> lambdaLayers) {
        this.lambdaLayers = lambdaLayers;
    }

    /**
     * The number of simultaneous executions to reserve for the Lambda Function.
     */
    @Updatable
    public Integer getReservedConcurrentExecutions() {
        return reservedConcurrentExecutions;
    }

    public void setReservedConcurrentExecutions(Integer reservedConcurrentExecutions) {
        this.reservedConcurrentExecutions = reservedConcurrentExecutions;
    }

    /**
     * A Map of versions and corresponding arns.
     */
    public Map<String, String> getVersionMap() {
        if (versionMap == null) {
            versionMap = new HashMap<>();
        }
        return versionMap;
    }

    public void setVersionMap(Map<String, String> versionMap) {
        this.versionMap = versionMap;
    }

    /**
     * A flag that states to publish the code or not.
     */
    @Updatable
    public Boolean getPublish() {
        if (publish == null) {
            publish = false;
        }

        return publish;
    }

    public void setPublish(Boolean publish) {
        this.publish = publish;
    }

    /**
     * The arn for the lambda Lambda Function resource including the version.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The arn for the lambda Lambda Function resource without the version.
     */
    @Output
    public String getArnNoVersion() {
        return arnNoVersion;
    }

    public void setArnNoVersion(String arnNoVersion) {
        this.arnNoVersion = arnNoVersion;
    }

    /**
     * The revision ID for the Lambda Function.
     */
    @Output
    public String getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(String revisionId) {
        this.revisionId = revisionId;
    }

    /**
     * The arn for the master function of the Lambda Function.
     */
    @Output
    public String getMasterArn() {
        return masterArn;
    }

    public void setMasterArn(String masterArn) {
        this.masterArn = masterArn;
    }

    /**
     * The date and time that the Lambda Function was last updated.
     */
    @Output
    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * The version of the Lambda Function.
     */
    @Output
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public void setCodeHash(String codeHash) {
        this.codeHash = codeHash;
    }

    @Override
    public void copyFrom(FunctionConfiguration configuration) {
        setName(configuration.functionName());
        setDeadLetterConfigArn(configuration.deadLetterConfig() != null ? configuration.deadLetterConfig().targetArn() : null);
        setDescription(configuration.description());
        setRuntime(configuration.runtimeAsString());
        setRole(!ObjectUtils.isBlank(configuration.role()) ? findById(RoleResource.class, configuration.role()) : null );
        setHandler(configuration.handler());
        setTimeout(configuration.timeout());
        setMemorySize(configuration.memorySize());
        setTrackingConfig(configuration.tracingConfig() != null ? configuration.tracingConfig().modeAsString() : null);
        setKmsKey(!ObjectUtils.isBlank(configuration.kmsKeyArn()) ? findById(KmsKeyResource.class, configuration.kmsKeyArn()) : null);
        setLambdaLayers(configuration.layers().stream().map(o -> findById(LayerResource.class, o.arn())).collect(Collectors.toSet()));
        setEnvironment(configuration.environment() != null ? configuration.environment().variables() : null);

        setArn(configuration.functionArn());
        setArnNoVersion(getArn().replace("function:" + getName() + ":" + "$LATEST", "function:" + getName()));
        setLastModified(configuration.lastModified());
        setMasterArn(configuration.masterArn());
        setRevisionId(configuration.revisionId());
        setVersion(configuration.version());
        setCodeHash(configuration.codeSha256());

        setSecurityGroups(
            configuration.vpcConfig() != null ?
                configuration.vpcConfig()
                    .securityGroupIds().stream()
                    .map(o -> findById(SecurityGroupResource.class, o))
                    .collect(Collectors.toSet())
                : null);
        setSubnets(
            configuration.vpcConfig() != null ?
                configuration.vpcConfig()
                    .subnetIds().stream()
                    .map(o -> findById(SubnetResource.class, o))
                    .collect(Collectors.toSet())
                : null);

        LambdaClient client = createClient(LambdaClient.class);

        ListTagsResponse tagResponse = client.listTags(
            r -> r.resource(getArnNoVersion())
        );

        setTags(tagResponse.tags());

        setVersions(client);

        GetFunctionResponse response = client.getFunction(
            r -> r.functionName(getName())
        );

        setReservedConcurrentExecutions(response.concurrency() != null ? response.concurrency().reservedConcurrentExecutions() : null);
    }

    @Override
    public boolean refresh() {
        LambdaClient client = createClient(LambdaClient.class);

        try {
            GetFunctionResponse response = client.getFunction(
                r -> r.functionName(getName())
            );

            copyFrom(response.configuration());

        } catch (ResourceNotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        validate();

        LambdaClient client = createClient(LambdaClient.class);

        CreateFunctionRequest.Builder builder = CreateFunctionRequest.builder()
            .functionName(getName())
            .description(getDescription())
            .runtime(getRuntime())
            .role(getRole().getArn())
            .handler(getHandler())
            .timeout(getTimeout())
            .memorySize(getMemorySize())
            .tracingConfig(t -> t.mode(getTrackingConfig()))
            .kmsKeyArn(getKmsKey() != null ? getKmsKey().getArn() : null)
            .tags(getTags())
            .publish(getPublish())
            .layers(getLambdaLayers().stream().map(LayerResource::getVersionArn).collect(Collectors.toSet()));

        if (!ObjectUtils.isBlank(getContentZipPathRaw())) {
            builder = builder.code(c -> c.zipFile(getZipFile()));
        } else {
            if (!ObjectUtils.isBlank(getS3ObjectVersion())) {
                builder = builder.code(c -> c.s3Bucket(getS3Bucket()).s3Key(getS3Key()).s3ObjectVersion(getS3ObjectVersion()));
            } else {
                builder = builder.code(c -> c.s3Bucket(getS3Bucket()).s3Key(getS3Key()));
            }
        }

        if (!ObjectUtils.isBlank(getDeadLetterConfigArn())) {
            builder = builder.deadLetterConfig(d -> d.targetArn(getDeadLetterConfigArn()));
        }

        if (!getEnvironment().isEmpty()) {
            builder = builder.environment(e -> e.variables(getEnvironment()));
        }

        if (!getSecurityGroups().isEmpty() && !getSubnets().isEmpty()) {
            builder = builder.vpcConfig(
                v -> v.securityGroupIds(
                    getSecurityGroups().stream()
                        .map(SecurityGroupResource::getId)
                        .collect(Collectors.toList()))
                    .subnetIds(
                        getSubnets().stream()
                            .map(SubnetResource::getId)
                            .collect(Collectors.toList()))
            );
        }

        CreateFunctionResponse response = client.createFunction(builder.build());

        setArn(response.functionArn());
        setLastModified(response.lastModified());
        setMasterArn(response.masterArn());
        setRevisionId(response.revisionId());
        setVersion(response.version());
        setCodeHash(response.codeSha256());

        if (getReservedConcurrentExecutions() != null) {
            try {
                client.putFunctionConcurrency(
                    r -> r.functionName(getName())
                        .reservedConcurrentExecutions(getReservedConcurrentExecutions())
                );
            } catch (Exception ex) {
                ui.write("\n@|bold,red Error assigning reserved concurrency executions to lambda function %s. Error - %s|@", getArn(), ex.getMessage());
            }
        }

        setVersions(client);
    }

    @Override
    public void update(GyroUI ui, State state, Resource resource, Set<String> changedFieldNames) {
        validate();

        LambdaClient client = createClient(LambdaClient.class);

        FunctionResource oldResource = (FunctionResource) resource;

        Set<String> changeSet = new HashSet<>(changedFieldNames);

        if (changeSet.contains("reserved-concurrent-executions")) {
            if (getReservedConcurrentExecutions() != null) {
                client.putFunctionConcurrency(
                    r -> r.functionName(getName())
                        .reservedConcurrentExecutions(getReservedConcurrentExecutions())
                );
            } else {
                client.deleteFunctionConcurrency(
                    r -> r.functionName(getName())
                );
            }

            changeSet.remove("reserved-concurrent-executions");
        }

        if (changeSet.contains("s3-bucket") || changeSet.contains("s3-key") || changeSet.contains("s3-object-version")
            || changeSet.contains("content-zip-path") || changeSet.contains("file-hash")) {

            UpdateFunctionCodeRequest.Builder builder = UpdateFunctionCodeRequest.builder()
                .functionName(getName())
                .publish(getPublish())
                .revisionId(getRevisionId());
            if (!ObjectUtils.isBlank(getS3Bucket())) {
                if (!ObjectUtils.isBlank(getS3ObjectVersion())) {
                    builder = builder.s3Bucket(getS3Bucket())
                        .s3Key(getS3Key())
                        .s3ObjectVersion(getS3ObjectVersion());
                } else {
                    builder = builder.s3Bucket(getS3Bucket())
                        .s3Key(getS3Key());
                }
            } else {
                builder = builder.zipFile(getZipFile());
            }

            UpdateFunctionCodeResponse response = client.updateFunctionCode(builder.build());

            setCodeHash(response.codeSha256());

            setVersions(client);

            changeSet.removeAll(Arrays.asList("s3-bucket","s3-key","s3-object-version","content-zip-path", "file-hash"));
        }

        if (changeSet.contains("tags")) {

            if (!oldResource.getTags().isEmpty()) {
                client.untagResource(
                    r -> r.resource(getArnNoVersion())
                        .tagKeys(oldResource.getTags().keySet())
                );
            }

            if (!getTags().isEmpty()) {
                client.tagResource(
                    r -> r.resource(getArnNoVersion()).tags(getTags())
                );
            }

            changeSet.remove("tags");
        }

        if (!changeSet.isEmpty()) {
            client.updateFunctionConfiguration(
                r -> r.functionName(getName())
                    .description(getDescription())
                    .runtime(getRuntime())
                    .role(getRole().getArn())
                    .handler(getHandler())
                    .timeout(getTimeout())
                    .memorySize(getMemorySize())
                    .tracingConfig(t -> t.mode(getTrackingConfig()))
                    .kmsKeyArn(getKmsKey() != null ? getKmsKey().getArn() : null)
                    .layers(getLambdaLayers().stream().map(LayerResource::getVersionArn).collect(Collectors.toSet()))
                    .environment(e -> e.variables(getEnvironment()))
                    .vpcConfig(
                        v -> v.securityGroupIds(
                            getSecurityGroups().stream()
                                .map(SecurityGroupResource::getId)
                                .collect(Collectors.toList()))
                            .subnetIds(
                                getSubnets().stream()
                                    .map(SubnetResource::getId)
                                    .collect(Collectors.toList()))
                    )
                    .deadLetterConfig(d -> d.targetArn(getDeadLetterConfigArn()))
            );
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        LambdaClient client = createClient(LambdaClient.class);

        client.deleteFunction(
            r -> r.functionName(getName())
        );
    }

    private SdkBytes getZipFile() {
        try (InputStream input = openInput(getContentZipPathRaw())) {
            return SdkBytes.fromInputStream(input);

        } catch (IOException ex) {
            throw new GyroException(String.format("File not found - %s",getContentZipPathRaw()));
        }
    }

    private String getFileHashFromPath() {
        String hash = "";
        try (InputStream input = openInput(getContentZipPathRaw())) {
            hash = DigestUtils.sha256Hex(input);

        } catch (Exception ignore) {
            // ignore
        }

        return hash;
    }

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();
        int s3FieldCount = 0;
        s3FieldCount += !ObjectUtils.isBlank(getS3Bucket()) ? 1 : 0;
        s3FieldCount += !ObjectUtils.isBlank(getS3Key()) ? 1 : 0;

        if (s3FieldCount == 1 ) {
            errors.add(new ValidationError(this, null, "Fields s3-bucket and s3-key are needed to set together or none."));
        }

        if (s3FieldCount != 0 && !ObjectUtils.isBlank(getContentZipPathRaw())) {
            errors.add(new ValidationError(this, null, "Field content-zip-path cannot be set when Fields s3-bucket and s3-key are set."));
        }

        return errors;
    }

    private void setVersions(LambdaClient client) {
        ListVersionsByFunctionResponse versionsResponse = client.listVersionsByFunction(
            r -> r.functionName(getName())
        );

        getVersionMap().clear();

        if (versionsResponse != null && !versionsResponse.versions().isEmpty()) {
            for (FunctionConfiguration functionConfiguration : versionsResponse.versions()) {
                if (!functionConfiguration.version().equals("$LATEST")) {
                    getVersionMap().put("v" + functionConfiguration.version(), functionConfiguration.functionArn());
                }
            }
        }
    }
}
