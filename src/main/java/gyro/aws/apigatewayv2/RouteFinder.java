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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client;
import software.amazon.awssdk.services.apigatewayv2.model.Api;
import software.amazon.awssdk.services.apigatewayv2.model.Route;

/**
 * Query Route.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    route: $(external-query aws::api-gateway-route {api-id: ""})
 */
@Type("api-gateway-route")
public class RouteFinder extends AwsFinder<ApiGatewayV2Client, Route, RouteResource> {

    private String id;
    private String apiId;

    /**
     * The id of the route.
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

    @Override
    protected List<Route> findAllAws(ApiGatewayV2Client client) {
        List<Route> routes = new ArrayList<>();
        List<String> apis = getApis(client);

        apis.forEach(a -> routes.addAll(client.getRoutes(r -> r.apiId(a)).items()));

        return routes;
    }

    @Override
    protected List<Route> findAws(ApiGatewayV2Client client, Map<String, String> filters) {
        List<Route> routes = new ArrayList<>();

        if (!filters.containsKey("api-id")) {
            routes = new ArrayList<>(client.getRoutes(r -> r.apiId(filters.get("api-id"))).items());

        } else {
            for (String api : getApis(client)) {
                routes.addAll(client.getRoutes(r -> r.apiId(api)).items());
            }
        }

        if (filters.containsKey("id")) {
            routes.removeIf(i -> !i.routeId().equals(filters.get("id")));
        }

        return routes;
    }

    private List<String> getApis(ApiGatewayV2Client client) {
        return client.getApis().items().stream().map(Api::apiId).collect(Collectors.toList());
    }
}
