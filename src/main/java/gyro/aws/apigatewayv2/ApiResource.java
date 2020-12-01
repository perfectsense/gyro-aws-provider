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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import gyro.aws.AwsCredentials;
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
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client;
import software.amazon.awssdk.services.apigatewayv2.model.Api;
import software.amazon.awssdk.services.apigatewayv2.model.CreateApiResponse;
import software.amazon.awssdk.services.apigatewayv2.model.GetApisResponse;
import software.amazon.awssdk.services.apigatewayv2.model.ProtocolType;

/**
 * Create an api.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::api-gateway example-api
 *         name: "example-api"
 *         protocol-type: HTTP
 *         description: "example-desc-up"
 *         api-key-selection-expression: '$request.header.x-api-key'
 *         version: "example-version"
 *         disable-execute-api-endpoint: true
 *         route-selection-expression: "${request.method} ${request.path}"
 *
 *         cors-configuration
 *             allow-credentials: true
 *             max-age: 1000
 *
 *             allow-headers: [
 *                 "example-header"
 *             ]
 *
 *             expose-headers: [
 *                 "example-header"
 *             ]
 *         end
 *
 *         tags: {
 *             "example-key": "example-value"
 *         }
 *     end
 */
@Type("api-gateway")
public class ApiResource extends AwsResource implements Copyable<Api> {

    private String apiKeySelectionExpression;
    private ApiCors corsConfiguration;
    private String description;
    private Boolean disableExecuteApiEndpoint;
    private String name;
    private ProtocolType protocolType;
    private String routeSelectionExpression;
    private String version;
    private Map<String, String> tags;

    // Read-only
    private String id;
    private String arn;

    /**
     * The API key selection expression.
     */
    @ValidStrings({ "$request.header.x-api-key", "$context.authorizer.usageIdentifierKey" })
    public String getApiKeySelectionExpression() {
        return apiKeySelectionExpression;
    }

    public void setApiKeySelectionExpression(String apiKeySelectionExpression) {
        this.apiKeySelectionExpression = apiKeySelectionExpression;
    }

    /**
     * A CORS configuration for the Api.
     *
     * @subresource gyro.aws.apigatewayv2.ApiCors
     */
    @Updatable
    public ApiCors getCorsConfiguration() {
        return corsConfiguration;
    }

    public void setCorsConfiguration(ApiCors corsConfiguration) {
        this.corsConfiguration = corsConfiguration;
    }

    /**
     * The description of the API.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * When set to ``true``, clients cannot invoke your API by using the default execute-api endpoint.
     */
    @Updatable
    public Boolean getDisableExecuteApiEndpoint() {
        return disableExecuteApiEndpoint;
    }

    public void setDisableExecuteApiEndpoint(Boolean disableExecuteApiEndpoint) {
        this.disableExecuteApiEndpoint = disableExecuteApiEndpoint;
    }

    /**
     * The name of the API.
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
     * The API protocol.
     */
    @ValidStrings({ "HTTP", "WEBSOCKET" })
    @Required
    public ProtocolType getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(ProtocolType protocolType) {
        this.protocolType = protocolType;
    }

    /**
     * The route selection expression for the API.
     */
    @Updatable
    @ValidStrings("${request.method} ${request.path}")
    public String getRouteSelectionExpression() {
        return routeSelectionExpression;
    }

    public void setRouteSelectionExpression(String routeSelectionExpression) {
        this.routeSelectionExpression = routeSelectionExpression;
    }

    /**
     * The version identifier for the API.
     */
    @Updatable
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * The list of tags for the Api.
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
     * The Id of the Api.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The ARN of the Api.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public void copyFrom(Api model) {
        setApiKeySelectionExpression(model.apiKeySelectionExpression());
        setDescription(model.description());
        setDisableExecuteApiEndpoint(model.disableExecuteApiEndpoint());
        setName(model.name());
        setProtocolType(model.protocolType());
        setRouteSelectionExpression(model.routeSelectionExpression());
        setVersion(model.version());
        setId(model.apiId());
        setArn(getArnFormat());

        if (model.corsConfiguration() != null) {
            ApiCors config = newSubresource(ApiCors.class);
            config.copyFrom(model.corsConfiguration());
            setCorsConfiguration(config);
        }

        getTags().clear();
        if (model.hasTags()) {
            setTags(model.tags());
        }
    }

    @Override
    public boolean refresh() {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        Api api = getApi(client);

        if (api == null) {
            return false;
        }

        copyFrom(api);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        CreateApiResponse response = client.createApi(r -> r.apiKeySelectionExpression(getApiKeySelectionExpression())
            .corsConfiguration(getCorsConfiguration() == null ? null : getCorsConfiguration().toCors())
            .description(getDescription())
            .disableExecuteApiEndpoint(getDisableExecuteApiEndpoint())
            .name(getName())
            .protocolType(getProtocolType())
            .routeSelectionExpression(getRouteSelectionExpression())
            .version(getVersion())
            .tags(getTags()));

        setId(response.apiId());
        setArn(getArnFormat());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        client.updateApi(r -> r.apiId(getId())
            .corsConfiguration(getCorsConfiguration() == null ? null : getCorsConfiguration().toCors())
            .description(getDescription())
            .disableExecuteApiEndpoint(getDisableExecuteApiEndpoint())
            .name(getName())
            .routeSelectionExpression(getRouteSelectionExpression())
            .version(getVersion()));

        if (changedFieldNames.contains("tags")) {
            ApiResource currentResource = (ApiResource) current;

            if (!currentResource.getTags().isEmpty()) {
                client.untagResource(r -> r.resourceArn(getArn())
                    .tagKeys(currentResource.getTags().keySet())
                    .build());
            }

            client.tagResource(r -> r.resourceArn(getArn()).tags(getTags()));
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        client.deleteApi(r -> r.apiId(getId()));
    }

    private Api getApi(ApiGatewayV2Client client) {
        Api api = null;

        GetApisResponse apis = client.getApis();

        if (apis.hasItems()) {
            api = apis.items().stream().filter(i -> i.apiId().equals(getId())).findFirst().orElse(null);
        }

        return api;
    }

    private String getArnFormat() {
        return String.format("arn:aws:apigateway:%s::/apis/%s", credentials(AwsCredentials.class).getRegion(), getId());
    }
}
