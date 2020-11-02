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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client;
import software.amazon.awssdk.services.apigatewayv2.model.Api;
import software.amazon.awssdk.services.apigatewayv2.model.NotFoundException;
import software.amazon.awssdk.services.apigatewayv2.model.Route;
import software.amazon.awssdk.services.apigatewayv2.model.RouteResponse;

/**
 * Query Route Response.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    route-response: $(external-query aws::route-response {api-id: "qoeqbv9k5l", route-id: "a9ov3hm", id: "lucem9"})
 */
@Type("route-response")
public class RouteResponseFinder extends AwsFinder<ApiGatewayV2Client, RouteResponse, RouteResponseResource> {

    private String id;
    private String apiId;
    private String routeId;

    /**
     * The id of the route response.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The id of the api.
     */
    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    /**
     * The id of the route
     */
    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    @Override
    protected List<RouteResponse> findAllAws(ApiGatewayV2Client client) {
        List<RouteResponse> routeResponses = new ArrayList<>();
        List<String> apis = getApis(client);

        apis.forEach(a -> {
            List<String> routes = getRoutes(client, a);
            routes.forEach(i -> routeResponses.addAll(client.getRouteResponses(r -> r.routeId(i)
                .apiId(a))
                .items()));
        });

        return routeResponses;
    }

    @Override
    protected List<RouteResponse> findAws(ApiGatewayV2Client client, Map<String, String> filters) {
        List<RouteResponse> routeResponses = new ArrayList<>();
        List<String> apis = filters.containsKey("api-id")
            ? Collections.singletonList(filters.get("api-id"))
            : getApis(client);

        apis.forEach(a -> {
            List<String> routes = filters.containsKey("route-id")
                ? Collections.singletonList(filters.get("route-id"))
                : getRoutes(client, a);

            try {
                routes.forEach(i -> routeResponses.addAll(client.getRouteResponses(r -> r.routeId(i)
                    .apiId(a))
                    .items()));

            } catch (NotFoundException ignore) {
                //ignore
            }
        });

        if (filters.containsKey("id")) {
            routeResponses.removeIf(r -> !r.routeResponseId()
                .equals(filters.get("id")));
        }

        return routeResponses;
    }

    private List<String> getApis(ApiGatewayV2Client client) {
        return client.getApis().items().stream().map(Api::apiId).collect(Collectors.toList());
    }

    private List<String> getRoutes(ApiGatewayV2Client client, String apiId) {
        return client.getRoutes(r -> r.apiId(apiId))
            .items()
            .stream()
            .map(Route::routeId)
            .collect(Collectors.toList());
    }
}
