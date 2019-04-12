package gyro.aws.lambda;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.core.GyroCore;
import gyro.core.GyroException;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import gyro.core.diff.ResourceOutput;
import gyro.lang.Resource;
import org.apache.commons.codec.digest.DigestUtils;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.CreateFunctionRequest;
import software.amazon.awssdk.services.lambda.model.CreateFunctionResponse;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;
import software.amazon.awssdk.services.lambda.model.GetFunctionResponse;
import software.amazon.awssdk.services.lambda.model.Layer;
import software.amazon.awssdk.services.lambda.model.ListTagsResponse;
import software.amazon.awssdk.services.lambda.model.ListVersionsByFunctionResponse;
import software.amazon.awssdk.services.lambda.model.ResourceNotFoundException;
import software.amazon.awssdk.services.lambda.model.UpdateFunctionCodeRequest;
import software.amazon.awssdk.services.lambda.model.UpdateFunctionCodeResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
 *             $(aws::lambda-layer lambda-layer-for-function-example-1 | version-arn)
 *         ]
 *     end
 */
@ResourceName("lambda-function")
public class FunctionResource extends AwsResource {
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
    private List<String> securityGroupIds;
    private List<String> subnetIds;
    private List<String> lambdaLayers;
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
    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    /**
     * The description of the function.
     */
    @ResourceDiffProperty(updatable = true)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The s3 bucket name where the function code resides. Required if field 'content-zip-path' not set.
     */
    @ResourceDiffProperty(updatable = true)
    public String getS3Bucket() {
        return s3Bucket;
    }

    public void setS3Bucket(String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    /**
     * The s3 object key where the function code resides. Required if field 'content-zip-path' not set.
     */
    @ResourceDiffProperty(updatable = true)
    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    /**
     * The s3 object version where the function code resides. Required if field 'content-zip-path' not set.
     */
    @ResourceDiffProperty(updatable = true)
    public String getS3ObjectVersion() {
        return s3ObjectVersion;
    }

    public void setS3ObjectVersion(String s3ObjectVersion) {
        this.s3ObjectVersion = s3ObjectVersion;
    }

    /**
     * The zip file location where the function code resides. Required if fields 's3-bucket', 's3-key' and 's3-object-version' not set.
     */
    @ResourceDiffProperty(updatable = true)
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
    @ResourceDiffProperty(updatable = true)
    public String getRoleArn() {
        return roleArn;
    }

    public void setRoleArn(String roleArn) {
        this.roleArn = roleArn;
    }

    /**
     * The runtime language for this function. Valid values are ``nodejs`` or ``nodejs4.3`` or ``nodejs6.10`` or ``nodejs8.10`` or ``java8`` or ``python2.7`` or ``python3.6`` or ``python3.7`` or ``dotnetcore1.0`` or ``dotnetcore2.0`` or ``dotnetcore2.1`` or ``nodejs4.3-edge`` or ``go1.x`` or ``ruby2.5`` or ``provided``. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    /**
     * The name of the method within your code that Lambda calls to execute the function. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    /**
     * The amount of time that Lambda allows a function to run before stopping it. Defaults to 3. Valid values between ``3`` and ``900``.
     */
    @ResourceDiffProperty(updatable = true)
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
    @ResourceDiffProperty(updatable = true)
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
    @ResourceDiffProperty(updatable = true)
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
    @ResourceDiffProperty(updatable = true)
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
    @ResourceDiffProperty(updatable = true)
    public String getKmsKeyArn() {
        return kmsKeyArn;
    }

    public void setKmsKeyArn(String kmsKeyArn) {
        this.kmsKeyArn = kmsKeyArn;
    }

    /**
     * A map of key value pair acting as variables accessible from the code of with the function.
     */
    @ResourceDiffProperty(updatable = true)
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
    @ResourceDiffProperty(updatable = true)
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
    @ResourceDiffProperty(updatable = true)
    public List<String> getSecurityGroupIds() {
        if (securityGroupIds == null) {
            securityGroupIds = new ArrayList<>();
        } else {
            if (!securityGroupIds.contains(null)) {
                Collections.sort(securityGroupIds);
            }
        }

        return securityGroupIds;
    }

    public void setSecurityGroupIds(List<String> securityGroupIds) {
        this.securityGroupIds = securityGroupIds;
    }

    /**
     * The set of subnet be associated with the function.
     */
    @ResourceDiffProperty(updatable = true)
    public List<String> getSubnetIds() {
        if (subnetIds == null) {
            subnetIds = new ArrayList<>();
        } else {
            if (!subnetIds.contains(null)) {
                Collections.sort(subnetIds);
            }
        }

        return subnetIds;
    }

    public void setSubnetIds(List<String> subnetIds) {
        this.subnetIds = subnetIds;
    }

    /**
     * The set of version arns of lambda layers to be associated with the function.
     */
    @ResourceDiffProperty(updatable = true)
    public List<String> getLambdaLayers() {
        if (lambdaLayers == null) {
            lambdaLayers = new ArrayList<>();
        } else {
            if (!lambdaLayers.contains(null)) {
                Collections.sort(lambdaLayers);
            }
        }

        return lambdaLayers;
    }

    public void setLambdaLayers(List<String> lambdaLayers) {
        this.lambdaLayers = lambdaLayers;
    }

    /**
     * The number of simultaneous executions to reserve for the function.
     */
    @ResourceDiffProperty(updatable = true)
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
    @ResourceDiffProperty(updatable = true)
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
    @ResourceOutput
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The arn for the lambda function resource without the version.
     */
    @ResourceOutput
    public String getArnNoVersion() {
        return arnNoVersion;
    }

    public void setArnNoVersion(String arnNoVersion) {
        this.arnNoVersion = arnNoVersion;
    }

    /**
     * The revision id for the lambda function.
     */
    @ResourceOutput
    public String getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(String revisionId) {
        this.revisionId = revisionId;
    }

    /**
     * The arn for the master function of the lambda function.
     */
    @ResourceOutput
    public String getMasterArn() {
        return masterArn;
    }

    public void setMasterArn(String masterArn) {
        this.masterArn = masterArn;
    }

    /**
     * The date and time that the function was last updated.
     */
    @ResourceOutput
    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * The version of the Lambda function.
     */
    @ResourceOutput
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @ResourceDiffProperty(updatable = true)
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
    public boolean refresh() {
        LambdaClient client = createClient(LambdaClient.class);

        try {
            GetFunctionResponse response = client.getFunction(
                r -> r.functionName(getFunctionName())
            );

            setReservedConcurrentExecutions(response.concurrency() != null ? response.concurrency().reservedConcurrentExecutions() : null);

            FunctionConfiguration configuration = response.configuration();
            setDeadLetterConfigArn(configuration.deadLetterConfig() != null ? configuration.deadLetterConfig().targetArn() : null);
            setDescription(configuration.description());
            setRuntime(configuration.runtimeAsString());
            setRoleArn(configuration.role());
            setHandler(configuration.handler());
            setTimeout(configuration.timeout());
            setMemorySize(configuration.memorySize());
            setTrackingConfig(configuration.tracingConfig() != null ? configuration.tracingConfig().modeAsString() : null);
            setKmsKeyArn(configuration.kmsKeyArn());
            setLambdaLayers(configuration.layers().stream().map(Layer::arn).collect(Collectors.toList()));
            setEnvironment(configuration.environment() != null ? configuration.environment().variables() : null);
            setSecurityGroupIds(configuration.vpcConfig() != null ? new ArrayList<>(configuration.vpcConfig().securityGroupIds()) : null);
            setSubnetIds(configuration.vpcConfig() != null ? new ArrayList<>(configuration.vpcConfig().subnetIds()) : null);
            setArn(configuration.functionArn());
            setArnNoVersion(getArn().replace("function:" + getFunctionName() + ":" + "$LATEST", "function:" + getFunctionName()));
            setLastModified(configuration.lastModified());
            setMasterArn(configuration.masterArn());
            setRevisionId(configuration.revisionId());
            setVersion(configuration.version());
            setCodeHash(configuration.codeSha256());

            ListTagsResponse tagResponse = client.listTags(
                r -> r.resource(getArnNoVersion())
            );

            setTags(tagResponse.tags());

            if (!ObjectUtils.isBlank(getContentZipPath())) {
                setFileHashFromPath();
            }

            setVersions(client);

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
            .layers(getLambdaLayers());

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

        if (!getSecurityGroupIds().isEmpty() && !getSubnetIds().isEmpty()) {
            builder = builder.vpcConfig(v -> v.securityGroupIds(getSecurityGroupIds()).subnetIds(getSubnetIds()));
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
    public void update(Resource resource, Set<String> set) {
        validate();

        LambdaClient client = createClient(LambdaClient.class);

        FunctionResource oldResource = (FunctionResource) resource;

        Set<String> changeSet = new HashSet<>(set);

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
                    .layers(getLambdaLayers())
                    .environment(e -> e.variables(getEnvironment()))
                    .vpcConfig(v -> v.securityGroupIds(getSecurityGroupIds()).subnetIds(getSubnetIds()))
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
        try {
            String dir = scope().getFileScope().getFile().substring(0, scope().getFileScope().getFile().lastIndexOf(File.separator));
            return SdkBytes.fromByteArray(Files.readAllBytes(Paths.get(dir + File.separator + getContentZipPath())));
        } catch (IOException ex) {
            throw new GyroException(String.format("File not found - %s",getContentZipPath()));
        }
    }

    private void setFileHashFromPath() {
        try {
            String dir = scope().getFileScope().getFile().substring(0, scope().getFileScope().getFile().lastIndexOf(File.separator));
            setFileHash(DigestUtils.sha256Hex(Files.readAllBytes(Paths.get(dir + File.separator + getContentZipPath()))));
        } catch (IOException ex) {
            throw new GyroException(String.format("File not found - %s",getContentZipPath()));
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
