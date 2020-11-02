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
import software.amazon.awssdk.services.apigatewayv2.model.CreateRouteResponseResponse;
import software.amazon.awssdk.services.apigatewayv2.model.GetRouteResponsesResponse;
import software.amazon.awssdk.services.apigatewayv2.model.ParameterConstraints;
import software.amazon.awssdk.services.apigatewayv2.model.RouteResponse;

/**
 * Create a route response.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::route-response example-route-websocket
 *         api: $(aws::api example-api-websock)
 *         route: $(aws::route example-route-websocket)
 *         route-response-key: '$default'
 *     end
 */
@Type("route-response")
public class RouteResponseResource extends AwsResource implements Copyable<RouteResponse> {

    private ApiResource api;
    private String modelSelectionExpression;
    private Map<String, String> responseModels;
    private Map<String, Boolean> responseParameters;
    private RouteResource route;
    private String routeResponseKey;

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
    public String getRouteResponseKey() {
        return routeResponseKey;
    }

    public void setRouteResponseKey(String routeResponseKey) {
        this.routeResponseKey = routeResponseKey;
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
     * The response models for the route.
     */
    @Updatable
    public Map<String, String> getResponseModels() {
        if (responseModels == null) {
            responseModels = new HashMap<>();
        }

        return responseModels;
    }

    public void setResponseModels(Map<String, String> responseModels) {
        this.responseModels = responseModels;
    }

    /**
     * The response parameters for the route.
     */
    @Updatable
    public Map<String, Boolean> getResponseParameters() {
        return responseParameters;
    }

    public void setResponseParameters(Map<String, Boolean> responseParameters) {
        this.responseParameters = responseParameters;
    }

    /**
     * The route associated to the response.
     */
    @Required
    public RouteResource getRoute() {
        return route;
    }

    public void setRoute(RouteResource route) {
        this.route = route;
    }

    /**
     * The id for the route response.
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
    public void copyFrom(RouteResponse model) {
        setModelSelectionExpression(model.modelSelectionExpression());
        setResponseModels(model.responseModels());
        setResponseParameters(model.responseParameters().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, r -> r.getValue().required())));
        setRouteResponseKey(model.routeResponseKey());
        setId(model.routeResponseId());
    }

    @Override
    public boolean refresh() {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        RouteResponse routeResponse = getRouteResponse(client);

        if (routeResponse == null) {
            return false;
        }

        copyFrom(routeResponse);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        CreateRouteResponseResponse route = client.createRouteResponse(r -> r.apiId(getApi().getId())
            .modelSelectionExpression(getModelSelectionExpression())
            .responseModels(getResponseModels())
            .routeResponseKey(getRouteResponseKey())
            .routeId(getRoute().getId())
            .responseParameters(getResponseParameters() != null ?
                getResponseParameters().entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        v -> ParameterConstraints.builder().required(v.getValue()).build()))
                : null));

        setId(route.routeResponseId());
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        client.updateRouteResponse(r -> r.apiId(getApi().getId())
            .modelSelectionExpression(getModelSelectionExpression())
            .responseModels(getResponseModels())
            .routeResponseKey(getRouteResponseKey())
            .routeId(getRoute().getId())
            .responseParameters(getResponseParameters() != null ?
                getResponseParameters().entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        v -> ParameterConstraints.builder().required(v.getValue()).build()))
                : null)
            .routeResponseId(getId()));
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        client.deleteRouteResponse(r -> r.apiId(getApi().getId()).routeResponseId(getId()).routeId(getRoute().getId()));
    }

    private RouteResponse getRouteResponse(ApiGatewayV2Client client) {
        RouteResponse routeResponse = null;

        GetRouteResponsesResponse response = client.getRouteResponses(r -> r.apiId(getApi().getId())
            .routeId(getRoute().getId()));

        if (response.hasItems()) {
            routeResponse = response.items()
                .stream()
                .filter(i -> i.routeResponseId().equals(getId()))
                .findFirst()
                .orElse(null);
        }

        return routeResponse;
    }
}
