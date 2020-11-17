/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.apigatewayv2;

import java.util.List;
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
import gyro.core.validation.Required;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client;
import software.amazon.awssdk.services.apigatewayv2.model.Api;
import software.amazon.awssdk.services.apigatewayv2.model.CreateModelResponse;
import software.amazon.awssdk.services.apigatewayv2.model.GetModelsResponse;
import software.amazon.awssdk.services.apigatewayv2.model.Model;

/**
 * Create a model.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::api-gateway-model example-model
 *         api: $(aws::api-gateway example-api-websock)
 *         content-type: "application/json"
 *         description: "example-desc"
 *         name: "exampleModel"
 *         schema: '{"type": "object","properties": {"id": {"type": "string"}}}'
 *     end
 */
@Type("api-gateway-model")
public class ModelResource extends AwsResource implements Copyable<Model> {

    private ApiResource api;
    private String contentType;
    private String description;
    private String name;
    private String schema;

    // Output
    private String id;

    /**
     * The API identifier.
     */
    @Required
    public ApiResource getApi() {
        return api;
    }

    public void setApi(ApiResource api) {
        this.api = api;
    }

    /**
     * The content-type for the model.
     */
    @Updatable
    @Required
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * The description of the model.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The name of the model.
     */
    @Required
    @Updatable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The schema for the model.
     */
    @Required
    @Updatable
    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * The id of the model.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(Model model) {
        setContentType(model.contentType());
        setDescription(model.description());
        setName(model.name());
        setSchema(model.schema());
        setId(model.modelId());

        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);
        List<String> apis = client.getApis().items().stream().map(Api::apiId).collect(Collectors.toList());

        apis.stream().filter(a -> {
            GetModelsResponse models = client.getModels(r -> r.apiId(a));

            return models.hasItems() &&
                models.items()
                    .stream()
                    .filter(i -> i.modelId().equals(getId()))
                    .findFirst()
                    .orElse(null) != null;
        }).findFirst().ifPresent(apiId -> setApi(findById(ApiResource.class, apiId)));
    }

    @Override
    public boolean refresh() {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        Model model = getModel(client);

        if (model == null) {
            return false;
        }

        copyFrom(model);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        CreateModelResponse response = client.createModel(r -> r.apiId(getApi().getId())
            .contentType(getContentType())
            .description(getDescription())
            .name(getName())
            .schema(getSchema()));

        setId(response.modelId());
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        client.updateModel(r -> r.apiId(getApi().getId())
            .contentType(getContentType())
            .description(getDescription())
            .name(getName())
            .schema(getSchema())
            .modelId(getId()));
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        client.deleteModel(r -> r.apiId(getApi().getId()).modelId(getId()));
    }

    private Model getModel(ApiGatewayV2Client client) {
        Model model = null;

        GetModelsResponse models = client.getModels(r -> r.apiId(getApi().getId()));

        if (models.hasItems()) {
            model = models.items()
                .stream()
                .filter(i -> i.modelId().equals(getId()))
                .findFirst()
                .orElse(null);
        }

        return model;
    }
}
