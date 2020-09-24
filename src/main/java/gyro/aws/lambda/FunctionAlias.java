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
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Range;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.CreateAliasRequest;
import software.amazon.awssdk.services.lambda.model.CreateAliasResponse;
import software.amazon.awssdk.services.lambda.model.GetAliasResponse;
import software.amazon.awssdk.services.lambda.model.ResourceNotFoundException;
import software.amazon.awssdk.services.lambda.model.UpdateAliasRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Creates an function alias.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::lambda-alias lambda-alias-example
 *         name: "lambda-alias-example"
 *         function: $(aws::lambda-function lambda-function-event-source-mapping-example)
 *         function-version: "10"
 *         description: "lambda-alias-example"
 *         additional-version: "9"
 *         weight: 0.4
 *     end
 */
@Type("lambda-alias")
public class FunctionAlias extends AwsResource implements Copyable<GetAliasResponse> {
    private String name;
    private FunctionResource function;
    private String functionVersion;
    private String description;
    private String additionalVersion;
    private Double weight;

    private String arn;
    private String revisionId;

    /**
     * Name of the Lambda Alias.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Lambda Function for the Lambda Alias.
     */
    @Required
    public FunctionResource getFunction() {
        return function;
    }

    public void setFunction(FunctionResource function) {
        this.function = function;
    }

    /**
     * The Lambda Function version for the Lambda Alias.
     */
    @Required
    @Updatable
    public String getFunctionVersion() {
        return functionVersion;
    }

    public void setFunctionVersion(String functionVersion) {
        this.functionVersion = functionVersion;
    }

    /**
     * The description for the Lambda Alias.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Secondary Lambda Function versions for the Lambda Alias.
     */
    @Updatable
    public String getAdditionalVersion() {
        return additionalVersion;
    }

    public void setAdditionalVersion(String additionalVersion) {
        this.additionalVersion = additionalVersion;
    }

    /**
     * The weight to switch between the secondary version. Required if additional version set. Valid values are between ``0.0`` to ``1.0``
     */
    @Updatable
    @Range(min = 0.0, max = 1.0)
    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    /**
     * The arn of the Lambda Alias.
     */
    @Id
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The revision ID of the Lambda Alias.
     */
    @Output
    public String getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(String revisionId) {
        this.revisionId = revisionId;
    }

    @Override
    public void copyFrom(GetAliasResponse response) {
        setName(response.name());
        setArn(response.aliasArn());
        setDescription(response.description());
        setFunctionVersion(response.functionVersion());
        setRevisionId(response.revisionId());
        if (response.routingConfig() != null && response.routingConfig().additionalVersionWeights().isEmpty()) {
            setAdditionalVersion(response.routingConfig().additionalVersionWeights().keySet().iterator().next());
            setWeight(response.routingConfig().additionalVersionWeights().get(getAdditionalVersion()));
        }
    }

    @Override
    public boolean refresh() {
        LambdaClient client = createClient(LambdaClient.class);

        try {
            GetAliasResponse response = client.getAlias(
                r -> r.name(getName())
                    .functionName(getFunction().getName())
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

        CreateAliasRequest.Builder builder = CreateAliasRequest.builder()
            .name(getName())
            .description(getDescription())
            .functionName(getFunction().getName())
            .functionVersion(getFunctionVersion());

        if (!ObjectUtils.isBlank(getAdditionalVersion())) {
            Map<String, Double> map = new HashMap<>();
            map.put(getAdditionalVersion(), getWeight());
            builder = builder.routingConfig(
                r -> r.additionalVersionWeights(map)
            );
        }

        CreateAliasResponse response = client.createAlias(builder.build());

        setArn(response.aliasArn());
        setRevisionId(response.revisionId());
    }

    @Override
    public void update(GyroUI ui, State state, Resource resource, Set<String> changedFieldNames) {
        LambdaClient client = createClient(LambdaClient.class);

        UpdateAliasRequest.Builder builder = UpdateAliasRequest.builder()
            .revisionId(getRevisionId())
            .name(getName())
            .description(getDescription())
            .functionName(getFunction().getName())
            .functionVersion(getFunctionVersion());

        if (!ObjectUtils.isBlank(getAdditionalVersion())) {
            Map<String, Double> map = new HashMap<>();
            map.put(getAdditionalVersion(), getWeight());
            builder = builder.routingConfig(
                r -> r.additionalVersionWeights(map)
            );
        }

        client.updateAlias(builder.build());
    }

    @Override
    public void delete(GyroUI ui, State state) {
        LambdaClient client = createClient(LambdaClient.class);

        client.deleteAlias(
            r -> r.name(getName())
                .functionName(getFunction().getName())
        );
    }
}
