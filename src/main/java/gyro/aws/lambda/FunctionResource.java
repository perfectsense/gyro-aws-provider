package gyro.aws.lambda;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.aws.ec2.SubnetResource;
import gyro.core.GyroCore;
import gyro.core.GyroException;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
 *         function-name: "testFunction"
 *         handler: "index.handler"
 *         runtime: "nodejs8.10"
 *         role-arn: "arn:aws:iam::242040583208:role/service-role/testFunctionRole"
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
    private String functionName;
    private String description;
    private String s3Bucket;
    private String s3Key;
    private String s3ObjectVersion;
    private String contentZipPath;
    private String roleArn;
    private String runtime;
    private String handler;
    private Integer timeout;
    private Integer memorySize;
    private String trackingConfig;
    private String deadLetterConfigArn;
    private String kmsKeyArn;
    private Map<String, String> environment;
    private Map<String, String> tags;
    private Set<SecurityGroupResource> securityGroups;
    private Set<SubnetResource> subnets;
    private Set<LayerResource> lambdaLayers;
    private Integer reservedConcurrentExecutions;
    private String fileHash;
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
     * The name of the function. (Required)
     */
    @Id
    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    /**
     * The description of the function.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The s3 bucket name where the function code resides. Required if field 'content-zip-path' not set.
     */
    @Updatable
    public String getS3Bucket() {
        return s3Bucket;
    }

    public void setS3Bucket(String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    /**
     * The s3 object key where the function code resides. Required if field 'content-zip-path' not set.
     */
    @Updatable
    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    /**
     * The s3 object version where the function code resides. Required if field 'content-zip-path' not set.
     */
    @Updatable
    public String getS3ObjectVersion() {
        return s3ObjectVersion;
    }

    public void setS3ObjectVersion(String s3ObjectVersion) {
        this.s3ObjectVersion = s3ObjectVersion;
    }

    /**
     * The zip file location where the function code resides. Required if fields 's3-bucket', 's3-key' and 's3-object-version' not set.
     */
    @Updatable
    public String getContentZipPath() {
        return contentZipPath;
    }

    public void setContentZipPath(String contentZipPath) {
        this.contentZipPath = contentZipPath;

        if (!ObjectUtils.isBlank(contentZipPath)) {
            setFileHashFromPath();
        }
    }

    /**
     * The role arn to be associated with this function. (Required)
     */
    @Updatable
    public String getRoleArn() {
        return roleArn;
    }

    public void setRoleArn(String roleArn) {
        this.roleArn = roleArn;
    }

    /**
     * The runtime language for this function. Valid values are ``nodejs`` or ``nodejs4.3`` or ``nodejs6.10`` or ``nodejs8.10`` or ``java8`` or ``python2.7`` or ``python3.6`` or ``python3.7`` or ``dotnetcore1.0`` or ``dotnetcore2.0`` or ``dotnetcore2.1`` or ``nodejs4.3-edge`` or ``go1.x`` or ``ruby2.5`` or ``provided``. (Required)
     */
    @Updatable
    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    /**
     * The name of the method within your code that Lambda calls to execute the function. (Required)
     */
    @Updatable
    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    /**
     * The amount of time that Lambda allows a function to run before stopping it. Defaults to 3. Valid values between ``3`` and ``900``.
     */
    @Updatable
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
     * The amount of memory that the function has access to. Defaults to 128. valid values are multiple of ``64``.
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
     * The tracking mode of the function. Defaults to ``PassThrough``. Valid values are ``PassThrough`` or ``Active``
     */
    @Updatable
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
     * The arn of SQS queue or an SNS topic to be associated with the function.
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
     * The arn of KMS key to be associated with the function.
     */
    @Updatable
    public String getKmsKeyArn() {
        return kmsKeyArn;
    }

    public void setKmsKeyArn(String kmsKeyArn) {
        this.kmsKeyArn = kmsKeyArn;
    }

    /**
     * A map of key value pair acting as variables accessible from the code of with the function.
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
     * The set of tags be associated with the function.
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
     * The set of security group be associated with the function.
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
     * The set of subnet be associated with the function.
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
     * The set of version arns of lambda layers to be associated with the function.
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
     * The number of simultaneous executions to reserve for the function.
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
     * The arn for the lambda function resource including the version.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The arn for the lambda function resource without the version.
     */
    @Output
    public String getArnNoVersion() {
        return arnNoVersion;
    }

    public void setArnNoVersion(String arnNoVersion) {
        this.arnNoVersion = arnNoVersion;
    }

    /**
     * The revision id for the lambda function.
     */
    @Output
    public String getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(String revisionId) {
        this.revisionId = revisionId;
    }

    /**
     * The arn for the master function of the lambda function.
     */
    @Output
    public String getMasterArn() {
        return masterArn;
    }

    public void setMasterArn(String masterArn) {
        this.masterArn = masterArn;
    }

    /**
     * The date and time that the function was last updated.
     */
    @Output
    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * The version of the Lambda function.
     */
    @Output
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Updatable
    public String getFileHash() {
        if (fileHash == null) {
            fileHash = "";
        }

        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public void setCodeHash(String codeHash) {
        this.codeHash = codeHash;
    }

    @Override
    public void copyFrom(FunctionConfiguration configuration) {
        setFunctionName(configuration.functionName());
        setDeadLetterConfigArn(configuration.deadLetterConfig() != null ? configuration.deadLetterConfig().targetArn() : null);
        setDescription(configuration.description());
        setRuntime(configuration.runtimeAsString());
        setRoleArn(configuration.role());
        setHandler(configuration.handler());
        setTimeout(configuration.timeout());
        setMemorySize(configuration.memorySize());
        setTrackingConfig(configuration.tracingConfig() != null ? configuration.tracingConfig().modeAsString() : null);
        setKmsKeyArn(configuration.kmsKeyArn());
        setLambdaLayers(configuration.layers().stream().map(o -> findById(LayerResource.class, o.arn())).collect(Collectors.toSet()));
        setEnvironment(configuration.environment() != null ? configuration.environment().variables() : null);

        setArn(configuration.functionArn());
        setArnNoVersion(getArn().replace("function:" + getFunctionName() + ":" + "$LATEST", "function:" + getFunctionName()));
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

        if (!ObjectUtils.isBlank(getContentZipPath())) {
            setFileHashFromPath();
        }

        setVersions(client);

        GetFunctionResponse response = client.getFunction(
            r -> r.functionName(getFunctionName())
        );

        setReservedConcurrentExecutions(response.concurrency() != null ? response.concurrency().reservedConcurrentExecutions() : null);
    }

    @Override
    public boolean refresh() {
        LambdaClient client = createClient(LambdaClient.class);

        try {
            GetFunctionResponse response = client.getFunction(
                r -> r.functionName(getFunctionName())
            );

            copyFrom(response.configuration());

        } catch (ResourceNotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    public void create() {
        validate();

        LambdaClient client = createClient(LambdaClient.class);

        CreateFunctionRequest.Builder builder = CreateFunctionRequest.builder()
            .functionName(getFunctionName())
            .description(getDescription())
            .runtime(getRuntime())
            .role(getRoleArn())
            .handler(getHandler())
            .timeout(getTimeout())
            .memorySize(getMemorySize())
            .tracingConfig(t -> t.mode(getTrackingConfig()))
            .kmsKeyArn(getKmsKeyArn())
            .tags(getTags())
            .publish(getPublish())
            .layers(getLambdaLayers().stream().map(LayerResource::getVersionArn).collect(Collectors.toSet()));

        if (!ObjectUtils.isBlank(getContentZipPath())) {
            builder = builder.code(c -> c.zipFile(getZipFile()));
        } else {
            builder = builder.code(c -> c.s3Bucket(getS3Bucket()).s3Key(getS3Key()).s3ObjectVersion(getS3ObjectVersion()));
            setFileHash("");
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
                            .map(SecurityGroupResource::getGroupId)
                            .collect(Collectors.toList()))
                    .subnetIds(
                        getSubnets().stream()
                            .map(SubnetResource::getSubnetId)
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
                    r -> r.functionName(getFunctionName())
                        .reservedConcurrentExecutions(getReservedConcurrentExecutions())
                );
            } catch (Exception ex) {
                GyroCore.ui().write("\n@|bold,red Error assigning reserved concurrency executions to lambda function %s. Error - %s|@", getArn(), ex.getMessage());
            }
        }

        setVersions(client);
    }

    @Override
    public void update(Resource resource, Set<String> changedFieldNames) {
        validate();

        LambdaClient client = createClient(LambdaClient.class);

        FunctionResource oldResource = (FunctionResource) resource;

        Set<String> changeSet = new HashSet<>(changedFieldNames);

        if (changeSet.contains("reserved-concurrent-executions")) {
            if (getReservedConcurrentExecutions() != null) {
                client.putFunctionConcurrency(
                    r -> r.functionName(getFunctionName())
                        .reservedConcurrentExecutions(getReservedConcurrentExecutions())
                );
            } else {
                client.deleteFunctionConcurrency(
                    r -> r.functionName(getFunctionName())
                );
            }

            changeSet.remove("reserved-concurrent-executions");
        }

        if (changeSet.contains("s3-bucket") || changeSet.contains("s3-key") || changeSet.contains("s3-object-version")
            || changeSet.contains("content-zip-path") || changeSet.contains("file-hash")) {

            UpdateFunctionCodeRequest.Builder builder = UpdateFunctionCodeRequest.builder()
                .functionName(getFunctionName())
                .publish(getPublish())
                .revisionId(getRevisionId());
            if (!ObjectUtils.isBlank(getS3Bucket())) {
                builder = builder.s3Bucket(getS3Bucket())
                    .s3Key(getS3Key())
                    .s3ObjectVersion(getS3ObjectVersion());
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
                r -> r.functionName(getFunctionName())
                    .description(getDescription())
                    .runtime(getRuntime())
                    .role(getRoleArn())
                    .handler(getHandler())
                    .timeout(getTimeout())
                    .memorySize(getMemorySize())
                    .tracingConfig(t -> t.mode(getTrackingConfig()))
                    .kmsKeyArn(getKmsKeyArn())
                    .layers(getLambdaLayers().stream().map(LayerResource::getVersionArn).collect(Collectors.toSet()))
                    .environment(e -> e.variables(getEnvironment()))
                    .vpcConfig(
                        v -> v.securityGroupIds(
                            getSecurityGroups().stream()
                                .map(SecurityGroupResource::getGroupId)
                                .collect(Collectors.toList()))
                            .subnetIds(
                                getSubnets().stream()
                                    .map(SubnetResource::getSubnetId)
                                    .collect(Collectors.toList()))
                    )
                    .deadLetterConfig(d -> d.targetArn(getDeadLetterConfigArn()))
            );
        }
    }

    @Override
    public void delete() {
        LambdaClient client = createClient(LambdaClient.class);

        client.deleteFunction(
            r -> r.functionName(getFunctionName())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("lambda function");

        if (!ObjectUtils.isBlank(getFunctionName())) {
            sb.append(" - ").append(getFunctionName());
        }

        if (!ObjectUtils.isBlank(getVersion())) {
            sb.append(" version - ").append(getVersion());
        }

        return sb.toString();
    }

    private SdkBytes getZipFile() {
        try (InputStream input = openInput(getContentZipPath())) {
            return SdkBytes.fromInputStream(input);

        } catch (IOException ex) {
            throw new GyroException(String.format("File not found - %s",getContentZipPath()));
        }
    }

    private void setFileHashFromPath() {
        try (InputStream input = openInput(getContentZipPath())) {
            setFileHash(DigestUtils.sha256Hex(input));

        } catch (Exception ignore) {
            // ignore
        }

    }

    private void validate() {
        int s3FieldCount = 0;
        s3FieldCount += !ObjectUtils.isBlank(getS3Bucket()) ? 1 : 0;
        s3FieldCount += !ObjectUtils.isBlank(getS3Key()) ? 1 : 0;
        s3FieldCount += !ObjectUtils.isBlank(getS3ObjectVersion()) ? 1 : 0;

        if (s3FieldCount > 0 && s3FieldCount < 3 ) {
            throw new GyroException("Fields s3-bucket, s3-key and s3-object-version are needed to set together or none.");
        }

        if (s3FieldCount != 0 && !ObjectUtils.isBlank(getContentZipPath())) {
            throw new GyroException("Field content-zip-path cannot be set when Fields s3-bucket, s3-key and s3-object-version are set.");
        }
    }

    private void setVersions(LambdaClient client) {
        ListVersionsByFunctionResponse versionsResponse = client.listVersionsByFunction(
            r -> r.functionName(getFunctionName())
        );

        getVersionMap().clear();

        if (versionsResponse != null && !versionsResponse.versions().isEmpty()) {
            for (FunctionConfiguration functionConfiguration : versionsResponse.versions()) {
                if (!functionConfiguration.version().equals("$LATEST")) {
                    getVersionMap().put(functionConfiguration.version(), functionConfiguration.functionArn());
                }
            }
        }
    }
}
