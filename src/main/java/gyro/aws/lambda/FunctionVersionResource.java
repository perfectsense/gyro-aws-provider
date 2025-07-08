/*
 * Copyright 2025, Brightspot.
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

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.AddPermissionRequest;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;
import software.amazon.awssdk.services.lambda.model.GetFunctionEventInvokeConfigResponse;
import software.amazon.awssdk.services.lambda.model.GetPolicyResponse;
import software.amazon.awssdk.services.lambda.model.ListVersionsByFunctionResponse;
import software.amazon.awssdk.services.lambda.model.PublishVersionRequest;
import software.amazon.awssdk.services.lambda.model.PublishVersionResponse;
import software.amazon.awssdk.services.lambda.model.RemovePermissionRequest;
import software.amazon.awssdk.services.lambda.model.ResourceNotFoundException;

/**
 * Creates a version of a Lambda Function.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::lambda-version lambda-version-example
 *         function: $(aws::lambda-function lambda-function-example)
 *         description: "Version 1 of lambda-function-example"
 *
 *         permission
 *             statement-id: "AllowGetFunctionFromCloudFront"
 *             action: "lambda:GetFunction"
 *             principal: "edgelambda.amazonaws.com"
 *         end
 *
 *         event-invoke-config
 *             maximum-retry-attempts: 1
 *             maximum-event-age-in-seconds: 600
 *         end
 *     end
 */
@Type("lambda-version")
public class FunctionVersionResource extends AwsResource implements Copyable<FunctionConfiguration> {

    private FunctionResource function;
    private String description;
    private Set<FunctionPermission> permission;
    private FunctionEventInvokeConfig eventInvokeConfig;

    // Read-only
    private String version;
    private String arn;

    /**
     * The Lambda Function to create a version for.
     */
    @Required
    public FunctionResource getFunction() {
        return function;
    }

    public void setFunction(FunctionResource function) {
        this.function = function;
    }

    /**
     * The description for the version.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The set of permissions to be associated with the Lambda Function version.
     *
     * @subresource gyro.aws.lambda.FunctionPermission
     */
    @Updatable
    public Set<FunctionPermission> getPermission() {
        if (permission == null) {
            permission = new HashSet<>();
        }
        return permission;
    }

    public void setPermission(Set<FunctionPermission> permission) {
        this.permission = permission;
    }

    /**
     * The event invoke configuration for the function version.
     *
     * @subresource gyro.aws.lambda.FunctionEventInvokeConfig
     */
    @Updatable
    public FunctionEventInvokeConfig getEventInvokeConfig() {
        return eventInvokeConfig;
    }

    public void setEventInvokeConfig(FunctionEventInvokeConfig eventInvokeConfig) {
        this.eventInvokeConfig = eventInvokeConfig;
    }

    /**
     * The published version.
     */
    @Output
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * The ARN of the published version.
     */
    @Output
    @Id
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public void copyFrom(FunctionConfiguration model) {
        setVersion(model.version());
        setArn(model.functionArn());
        setDescription(model.description());

        LambdaClient client = createClient(LambdaClient.class);

        getPermission().clear();
        try {
            GetPolicyResponse response = client.getPolicy(r -> r.functionName(getArn()));

            if (response.policy() != null) {
                setPolicy(response.policy());
            }

        } catch (ResourceNotFoundException ex) {
            // No policies exist, ignore
        }

        setEventInvokeConfig(null);
        try {
            GetFunctionEventInvokeConfigResponse response = client.getFunctionEventInvokeConfig(r -> r
                .functionName(getArn())
                .qualifier(getVersion()));

            if (response != null) {
                FunctionEventInvokeConfig config = newSubresource(FunctionEventInvokeConfig.class);
                config.copyFrom(response);
                setEventInvokeConfig(config);
            }

        } catch (ResourceNotFoundException ex) {
            // No config exists, ignore
        }
    }

    @Override
    public boolean refresh() {
        LambdaClient client = createClient(LambdaClient.class);

        FunctionConfiguration versionConfig = null;
        try {
            for (ListVersionsByFunctionResponse response : client.listVersionsByFunctionPaginator(
                r -> r.functionName(getFunction().getName()))) {
                for (FunctionConfiguration config : response.versions()) {
                    if (config.version().equals(getVersion())) {
                        versionConfig = config;
                    }
                }
            }
        } catch (ResourceNotFoundException ex) {
            // ignore
        }

        if (versionConfig != null) {
            copyFrom(versionConfig);
            return true;
        }

        return false;
    }

    @Override
    public void create(GyroUI ui, State state) {
        LambdaClient client = createClient(LambdaClient.class);
        int currentMaxVersion = function.getMaxVersion(client);

        PublishVersionResponse response = client.publishVersion(PublishVersionRequest.builder()
            .functionName(getFunctionName())
            .description(getDescription())
            .build());

        if (currentMaxVersion == getVersionAsInt(response.version())) {
            throw new GyroException(String.format(
                "A new version can only be created if the function has been updated since the last version. Please update the function <%s> before creating a new version",
                getFunctionName()));
        }

        setVersion(response.version());
        setArn(response.functionArn());

        state.save();
        getFunction().waitForActiveStatus(client);

        // Add permissions if any
        if (!getPermission().isEmpty()) {
            for (FunctionPermission perm : getPermission()) {
                client.addPermission(perm.toAddPermissionRequest());
            }
        }

        state.save();
        getFunction().waitForActiveStatus(client);

        if (getEventInvokeConfig() != null) {
            client.putFunctionEventInvokeConfig(getEventInvokeConfig().toPutFunctionEventInvokeConfigRequest()
                .functionName(getFunctionName())
                .qualifier(getVersion())
                .build());
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource resource, java.util.Set<String> changedFieldNames) {
        FunctionVersionResource oldResource = (FunctionVersionResource) resource;
        LambdaClient client = createClient(LambdaClient.class);

        if (changedFieldNames.contains("permission")) {
            if (!oldResource.getPermission().isEmpty()) {
                for (FunctionPermission perm : oldResource.getPermission()) {
                    client.removePermission(RemovePermissionRequest.builder()
                        .functionName(getFunctionName())
                        .qualifier(getVersion())
                        .statementId(perm.getStatementId())
                        .build());
                }
            }

            for (FunctionPermission perm : getPermission()) {
                client.addPermission(perm.toAddPermissionRequest());
            }

            state.save();
        }

        if (changedFieldNames.contains("destinationConfig")) {
            if (getEventInvokeConfig() != null) {
                client.updateFunctionEventInvokeConfig(
                    getEventInvokeConfig().toUpdateFunctionEventInvokeConfigRequest()
                        .functionName(getFunctionName())
                        .qualifier(getVersion())
                        .build());

            } else {
                // If event invoke config is removed, delete it
                try {
                    client.deleteFunctionEventInvokeConfig(
                        r -> r.functionName(getFunctionName()).qualifier(getVersion()));
                } catch (ResourceNotFoundException ex) {
                    // Ignore if it doesn't exist
                }
            }
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        LambdaClient client = createClient(LambdaClient.class);
        client.deleteFunction(r -> r.functionName(getFunctionName()).qualifier(getVersion()));
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
                AddPermissionRequest request = FunctionPermission.getAddPermissionRequest(statement);
                if (request != null) {
                    FunctionPermission permission = newSubresource(FunctionPermission.class);
                    permission.copyFrom(request);
                    getPermission().add(permission);
                }
            }

        } catch (Exception e) {
            throw new GyroException("Error parsing function policy", e);
        }
    }

    public String getFunctionName() {
        return getFunction().getName();
    }

    private int getVersionAsInt(String version) {
        if (version == null || version.equals("$LATEST")) {
            return 0;
        }

        try {
            return Integer.parseInt(version);
        } catch (NumberFormatException e) {
            throw new GyroException("Invalid function version: " + version);
        }
    }
}
