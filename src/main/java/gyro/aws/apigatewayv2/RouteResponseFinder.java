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

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client;
import software.amazon.awssdk.services.apigatewayv2.model.GetIntegrationsRequest;
import software.amazon.awssdk.services.apigatewayv2.model.GetIntegrationsResponse;
import software.amazon.awssdk.services.apigatewayv2.model.GetRouteResponsesRequest;
import software.amazon.awssdk.services.apigatewayv2.model.GetRouteResponsesResponse;
import software.amazon.awssdk.services.apigatewayv2.model.Integration;
import software.amazon.awssdk.services.apigatewayv2.model.NotFoundException;
import software.amazon.awssdk.services.apigatewayv2.model.RouteResponse;

/**
 * Query Route Response.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    route-response: $(external-query aws::api-gateway-route-response {api-id: "", route-id: "", id: ""})
 */
@Type("api-gateway-route-response")
public class RouteResponseFinder extends ApiGatewayFinder<ApiGatewayV2Client, RouteResponse, RouteResponseResource> {

    private String id;
    private String apiId;
    private String routeId;

    /**
     * The ID of the route response.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The ID of the api.
     */
    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    /**
     * The ID of the route.
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
        String marker = null;
        GetRouteResponsesResponse response;

        for (String api : getApis(client)) {
            for (String route : getRoutes(client, api)) {
                do {
                    if (marker == null) {
                        response = client.getRouteResponses(r -> r.routeId(route).apiId(api));
                    } else {
                        response = client.getRouteResponses(GetRouteResponsesRequest.builder()
                            .nextToken(marker).apiId(api).routeId(route).build());
                    }

                    marker = response.nextToken();
                    routeResponses.addAll(response.items());
                } while (!ObjectUtils.isBlank(marker));
            }
        }

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

    private List<String> getRoutes(ApiGatewayV2Client client, String apiId) {
        List<Integration> integrations = new ArrayList<>();
        String marker = null;
        GetIntegrationsResponse response;

        for (String api : getApis(client)) {
            do {
                if (ObjectUtils.isBlank(marker)) {
                    response = client.getIntegrations(r -> r.apiId(api));
                } else {
                    response = client.getIntegrations(GetIntegrationsRequest.builder()
                        .apiId(api).nextToken(marker).build());
                }

                marker = response.nextToken();
                integrations.addAll(response.items());
            } while (!ObjectUtils.isBlank(marker));
        }

        return integrations.stream().map(Integration::integrationId).collect(Collectors.toList());
    }
}
