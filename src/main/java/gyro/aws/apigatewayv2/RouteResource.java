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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client;
import software.amazon.awssdk.services.apigatewayv2.model.Api;
import software.amazon.awssdk.services.apigatewayv2.model.AuthorizationType;
import software.amazon.awssdk.services.apigatewayv2.model.CreateRouteResponse;
import software.amazon.awssdk.services.apigatewayv2.model.GetRoutesResponse;
import software.amazon.awssdk.services.apigatewayv2.model.ParameterConstraints;
import software.amazon.awssdk.services.apigatewayv2.model.Route;

/**
 * Create a route.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::api-gateway-route example-route
 *         api: $(aws::api example-api)
 *         route-key: 'ANY /api/example/route'
 *         authorizer: $(aws::api-gateway-authorizer example-authorizer)
 *         authorization-type: JWT
 *         authorization-scopes: [ "example-scope" ]
 *     end
 */
@Type("api-gateway-route")
public class RouteResource extends AwsResource implements Copyable<Route> {

    private ApiResource api;
    private String routeKey;
    private Boolean apiKeyRequired;
    private List<String> authorizationScopes;
    private AuthorizationType authorizationType;
    private AuthorizerResource authorizer;
    private String modelSelectionExpression;
    private String operationName;
    private Map<String, String> requestModels;
    private Map<String, Boolean> requestParameters;
    private String routeResponseSelectionExpression;
    private String target;

    // Output
    private String id;

    /**
     * The API resource for which to create the route.
     */
    @Required
    public ApiResource getApi() {
        return api;
    }

    public void setApi(ApiResource api) {
        this.api = api;
    }

    /**
     * The route key for the route.
     */
    @Required
    @Updatable
    public String getRouteKey() {
        return routeKey;
    }

    public void setRouteKey(String routeKey) {
        this.routeKey = routeKey;
    }

    /**
     * When set to ``true``, an API key is required for the route.
     */
    @Updatable
    public Boolean getApiKeyRequired() {
        return apiKeyRequired;
    }

    public void setApiKeyRequired(Boolean apiKeyRequired) {
        this.apiKeyRequired = apiKeyRequired;
    }

    /**
     * The authorization scopes supported by this route.
     */
    @Updatable
    public List<String> getAuthorizationScopes() {
        if (authorizationScopes == null) {
            authorizationScopes = new ArrayList<>();
        }

        return authorizationScopes;
    }

    public void setAuthorizationScopes(List<String> authorizationScopes) {
        this.authorizationScopes = authorizationScopes;
    }

    /**
     * The authorization type for the route.
     */
    @Updatable
    @ValidStrings({ "NONE", "AWS_IAM", "CUSTOM", "JWT" })
    public AuthorizationType getAuthorizationType() {
        return authorizationType;
    }

    public void setAuthorizationType(AuthorizationType authorizationType) {
        this.authorizationType = authorizationType;
    }

    /**
     * The Authorizer resource to be associated with this route.
     */
    @Updatable
    public AuthorizerResource getAuthorizer() {
        return authorizer;
    }

    public void setAuthorizer(AuthorizerResource authorizer) {
        this.authorizer = authorizer;
    }

    /**
     * The model selection expression for the route.
     */
    @Updatable
    public String getModelSelectionExpression() {
        return modelSelectionExpression;
    }

    public void setModelSelectionExpression(String modelSelectionExpression) {
        this.modelSelectionExpression = modelSelectionExpression;
    }

    /**
     * The operation name for the route.
     */
    @Updatable
    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    /**
     * The request models for the route.
     */
    @Updatable
    public Map<String, String> getRequestModels() {
        if (requestModels == null) {
            requestModels = new HashMap<>();
        }

        return requestModels;
    }

    public void setRequestModels(Map<String, String> requestModels) {
        this.requestModels = requestModels;
    }

    /**
     * The request parameters for the route.
     */
    @Updatable
    public Map<String, Boolean> getRequestParameters() {
        if (requestParameters == null) {
            requestParameters = new HashMap<>();
        }

        return requestParameters;
    }

    public void setRequestParameters(Map<String, Boolean> requestParameters) {
        this.requestParameters = requestParameters;
    }

    /**
     * The route response selection expression for the route.
     */
    @Updatable
    public String getRouteResponseSelectionExpression() {
        return routeResponseSelectionExpression;
    }

    public void setRouteResponseSelectionExpression(String routeResponseSelectionExpression) {
        this.routeResponseSelectionExpression = routeResponseSelectionExpression;
    }

    /**
     * The target for the route.
     */
    @Updatable
    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * The id for the route.
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
    public void copyFrom(Route model) {
        setRouteKey(model.routeKey());
        setApiKeyRequired(model.apiKeyRequired());
        setAuthorizationScopes(model.authorizationScopes());
        setAuthorizationType(model.authorizationType());
        setAuthorizer(findById(AuthorizerResource.class, model.authorizerId()));
        setModelSelectionExpression(model.modelSelectionExpression());
        setOperationName(model.operationName());
        setRequestModels(model.requestModels());
        setRequestParameters(model.requestParameters().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, r -> r.getValue().required())));
        setRouteResponseSelectionExpression(model.routeResponseSelectionExpression());
        setTarget(model.target());
        setId(model.routeId());

        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);
        List<String> apis = client.getApis().items().stream().map(Api::apiId).collect(Collectors.toList());

        apis.stream().filter(a -> {
            GetRoutesResponse response = client.getRoutes(r -> r.apiId(a));

            return response.hasItems() &&
                response.items()
                    .stream()
                    .filter(i -> i.routeId().equals(getId()))
                    .findFirst()
                    .orElse(null) != null;
        }).findFirst().ifPresent(apiId -> setApi(findById(ApiResource.class, apiId)));
    }

    @Override
    public boolean refresh() {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        Route route = getRoute(client);

        if (route == null) {
            return false;
        }

        copyFrom(route);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        CreateRouteResponse route = client.createRoute(r -> r.apiId(getApi().getId())
            .apiKeyRequired(getApiKeyRequired())
            .authorizationScopes(getAuthorizationScopes())
            .authorizationType(getAuthorizationType())
            .authorizerId(getAuthorizer() != null ? getAuthorizer().getId() : null)
            .modelSelectionExpression(getModelSelectionExpression())
            .operationName(getOperationName())
            .requestModels(getRequestModels())
            .routeKey(getRouteKey())
            .routeResponseSelectionExpression(getRouteResponseSelectionExpression())
            .target(getTarget())
            .requestParameters(getRequestParameters().entrySet()
                .stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    v -> ParameterConstraints.builder().required(v.getValue()).build()))));

        setId(route.routeId());
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        client.updateRoute(r -> r.apiId(getApi().getId())
            .apiKeyRequired(getApiKeyRequired())
            .authorizationScopes(getAuthorizationScopes())
            .authorizationType(getAuthorizationType())
            .authorizerId(getAuthorizer() != null ? getAuthorizer().getId() : null)
            .modelSelectionExpression(getModelSelectionExpression())
            .operationName(getOperationName())
            .requestModels(getRequestModels())
            .routeKey(getRouteKey())
            .routeResponseSelectionExpression(getRouteResponseSelectionExpression())
            .target(getTarget())
            .requestParameters(getRequestParameters().entrySet()
                .stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    v -> ParameterConstraints.builder().required(v.getValue()).build())))
            .routeId(getId()));
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        client.deleteRoute(r -> r.apiId(getApi().getId()).routeId(getId()));
    }

    private Route getRoute(ApiGatewayV2Client client) {
        Route route = null;

        GetRoutesResponse response = client.getRoutes(r -> r.apiId(getApi().getId()));

        if (response.hasItems()) {
            route = response.items()
                .stream()
                .filter(i -> i.routeId().equals(getId()))
                .findFirst()
                .orElse(null);
        }

        return route;
    }
}
