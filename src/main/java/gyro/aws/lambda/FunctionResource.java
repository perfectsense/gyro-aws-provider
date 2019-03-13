package gyro.aws.lambda;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.core.BeamCore;
import gyro.core.BeamException;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import gyro.lang.Resource;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.CreateFunctionRequest;
import software.amazon.awssdk.services.lambda.model.CreateFunctionResponse;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;
import software.amazon.awssdk.services.lambda.model.GetFunctionResponse;
import software.amazon.awssdk.services.lambda.model.Layer;
import software.amazon.awssdk.services.lambda.model.ListTagsResponse;
import software.amazon.awssdk.services.lambda.model.ResourceNotFoundException;
import software.amazon.awssdk.services.lambda.model.UpdateFunctionCodeRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private Boolean publish;
    private List<String> lambdaLayers;
    private Boolean updateCode;
    private Integer reservedConcurrentExecutions;

    // -- Readonly

    private String functionArn;
    private String functionArnNoVersion;
    private String revisionId;
    private String masterArn;
    private String lastModified;
    private String version;

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getDescription() {
        return description;
    }

    @ResourceDiffProperty(updatable = true)
    public void setDescription(String description) {
        this.description = description;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public void setS3Bucket(String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public String getS3ObjectVersion() {
        return s3ObjectVersion;
    }

    public void setS3ObjectVersion(String s3ObjectVersion) {
        this.s3ObjectVersion = s3ObjectVersion;
    }

    public String getContentZipPath() {
        return contentZipPath;
    }

    public void setContentZipPath(String contentZipPath) {
        this.contentZipPath = contentZipPath;
    }

    @ResourceDiffProperty(updatable = true)
    public String getRoleArn() {
        return roleArn;
    }

    public void setRoleArn(String roleArn) {
        this.roleArn = roleArn;
    }

    @ResourceDiffProperty(updatable = true)
    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    @ResourceDiffProperty(updatable = true)
    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    @ResourceDiffProperty(updatable = true)
    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    @ResourceDiffProperty(updatable = true)
    public Integer getMemorySize() {
        return memorySize;
    }

    public void setMemorySize(Integer memorySize) {
        this.memorySize = memorySize;
    }

    @ResourceDiffProperty(updatable = true)
    public String getTrackingConfig() {
        return trackingConfig;
    }

    public void setTrackingConfig(String trackingConfig) {
        this.trackingConfig = trackingConfig;
    }

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

    @ResourceDiffProperty(updatable = true)
    public String getKmsKeyArn() {
        return kmsKeyArn;
    }

    public void setKmsKeyArn(String kmsKeyArn) {
        this.kmsKeyArn = kmsKeyArn;
    }

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

    @ResourceDiffProperty(updatable = true)
    public List<String> getSecurityGroupIds() {
        if (securityGroupIds == null) {
            securityGroupIds = new ArrayList<>();
        } else {
            Collections.sort(securityGroupIds);
        }

        return securityGroupIds;
    }

    public void setSecurityGroupIds(List<String> securityGroupIds) {
        this.securityGroupIds = securityGroupIds;
    }

    @ResourceDiffProperty(updatable = true)
    public List<String> getSubnetIds() {
        if (subnetIds == null) {
            subnetIds = new ArrayList<>();
        } else {
            Collections.sort(subnetIds);
        }

        return subnetIds;
    }

    public void setSubnetIds(List<String> subnetIds) {
        this.subnetIds = subnetIds;
    }

    public Boolean getPublish() {
        if (publish == null) {
            publish = false;
        }

        return publish;
    }

    public void setPublish(Boolean publish) {
        this.publish = publish;
    }

    @ResourceDiffProperty(updatable = true)
    public List<String> getLambdaLayers() {
        if (lambdaLayers == null) {
            lambdaLayers = new ArrayList<>();
        } else {
            Collections.sort(lambdaLayers);
        }

        return lambdaLayers;
    }

    public void setLambdaLayers(List<String> lambdaLayers) {
        this.lambdaLayers = lambdaLayers;
    }

    public Boolean getUpdateCode() {
        if (updateCode == null) {
            updateCode = false;
        }

        return updateCode;
    }

    public void setUpdateCode(Boolean updateCode) {
        this.updateCode = updateCode;
    }

    @ResourceDiffProperty(updatable = true)
    public Integer getReservedConcurrentExecutions() {
        return reservedConcurrentExecutions;
    }

    public void setReservedConcurrentExecutions(Integer reservedConcurrentExecutions) {
        this.reservedConcurrentExecutions = reservedConcurrentExecutions;
    }

    public String getFunctionArn() {
        return functionArn;
    }

    public void setFunctionArn(String functionArn) {
        this.functionArn = functionArn;
    }

    public String getFunctionArnNoVersion() {
        return functionArnNoVersion;
    }

    public void setFunctionArnNoVersion(String functionArnNoVersion) {
        this.functionArnNoVersion = functionArnNoVersion;
    }

    public String getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(String revisionId) {
        this.revisionId = revisionId;
    }

    public String getMasterArn() {
        return masterArn;
    }

    public void setMasterArn(String masterArn) {
        this.masterArn = masterArn;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean refresh() {
        LambdaClient client = createClient(LambdaClient.class);

        try {

            GetFunctionResponse response = client.getFunction(
                r -> r.functionName(getFunctionName()).qualifier(getVersion())
            );

            setUpdateCode(false);
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
            setFunctionArn(configuration.functionArn());
            setFunctionArnNoVersion(getFunctionArn().replace(":" + getVersion(), ""));
            setLastModified(configuration.lastModified());
            setMasterArn(configuration.masterArn());
            setRevisionId(configuration.revisionId());
            setVersion(configuration.version());
            setUpdateCode(false);

            ListTagsResponse tagResponse = client.listTags(
                r -> r.resource(getFunctionArnNoVersion())
            );

            setTags(tagResponse.tags());

        } catch (ResourceNotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    public void create() {
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

        setFunctionArn(response.functionArn());
        setLastModified(response.lastModified());
        setMasterArn(response.masterArn());
        setRevisionId(response.revisionId());
        setVersion(response.version());
        setUpdateCode(false);

        if (getReservedConcurrentExecutions() != null) {
            try {
                client.putFunctionConcurrency(
                    r -> r.functionName(getFunctionName())
                        .reservedConcurrentExecutions(getReservedConcurrentExecutions())
                );
            } catch (Exception ex) {
                BeamCore.ui().write("\n@|bold,blue Error assigning reserved concurrency executions to lambda function %s. Error - %s|@", getFunctionArn(), ex.getMessage());
            }
        }
    }

    @Override
    public void update(Resource resource, Set<String> set) {
        LambdaClient client = createClient(LambdaClient.class);

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

        if (changeSet.contains("update-code")) {
            UpdateFunctionCodeRequest.Builder builder = UpdateFunctionCodeRequest.builder()
                .functionName(getFunctionName())
                .publish(getPublish())
                .revisionId(getRevisionId());

            if (!ObjectUtils.isBlank(getContentZipPath())) {
                builder = builder.zipFile(getZipFile());
            } else {
                builder = builder.s3Bucket(getS3Bucket()).s3Key(getS3Key()).s3ObjectVersion(getS3ObjectVersion());
            }

            client.updateFunctionCode(builder.build());

            changeSet.remove("update-code");
        }

        if (changeSet.contains("tags")) {
            FunctionResource oldResource = (FunctionResource) resource;
            MapDifference<String, String> mapDifference = Maps.difference(oldResource.getTags(), getTags());

            Map<String, String> deleteTags = mapDifference.entriesOnlyOnLeft();
            if (!deleteTags.isEmpty()) {
                client.untagResource(
                    r -> r.resource(getFunctionArnNoVersion())
                        .tagKeys(deleteTags.keySet())
                );
            }

            Map<String, String> addTags = mapDifference.entriesOnlyOnRight();
            if (!addTags.isEmpty()) {
                client.tagResource(
                    r -> r.resource(getFunctionArnNoVersion()).tags(addTags)
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
            throw new BeamException(String.format("File not found - %s",getContentZipPath()));
        }
    }
}
