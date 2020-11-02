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
import software.amazon.awssdk.services.apigatewayv2.model.Integration;
import software.amazon.awssdk.services.apigatewayv2.model.IntegrationResponse;
import software.amazon.awssdk.services.apigatewayv2.model.NotFoundException;

/**
 * Query Integration Response.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    integration-response: $(external-query aws::integration-response {id: "6j49jc5"})
 */
@Type("integration-response")
public class IntegrationResponseFinder
    extends AwsFinder<ApiGatewayV2Client, IntegrationResponse, IntegrationResponseResource> {

    private String id;
    private String apiId;
    private String integrationId;

    /**
     * The id of the integration response.
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
     * The id of the integration
     */
    public String getIntegrationId() {
        return integrationId;
    }

    public void setIntegrationId(String integrationId) {
        this.integrationId = integrationId;
    }

    @Override
    protected List<IntegrationResponse> findAllAws(ApiGatewayV2Client client) {
        List<IntegrationResponse> integrationResponses = new ArrayList<>();
        List<String> apis = getApis(client);

        apis.forEach(a -> {
            List<String> integrations = getIntegrations(client, a);
            integrations.forEach(i -> integrationResponses.addAll(client.getIntegrationResponses(r -> r.integrationId(i)
                .apiId(a))
                .items()));
        });

        return integrationResponses;
    }

    @Override
    protected List<IntegrationResponse> findAws(ApiGatewayV2Client client, Map<String, String> filters) {
        List<IntegrationResponse> integrationResponses = new ArrayList<>();
        List<String> apis = filters.containsKey("api-id")
            ? Collections.singletonList(filters.get("api-id"))
            : getApis(client);

        apis.forEach(a -> {
            List<String> integrations = filters.containsKey("integration-id")
                ? Collections.singletonList(filters.get("integration-id"))
                : getIntegrations(client, a);

            try {
                integrations.forEach(i -> integrationResponses.addAll(client.getIntegrationResponses(r ->
                    r.integrationId(i).apiId(a)).items()));

            } catch (
                NotFoundException ignore) {
                //ignore
            }
        });

        if (filters.containsKey("id")) {
            integrationResponses.removeIf(r -> !r.integrationResponseId()
                .equals(filters.get("id")));
        }

        return integrationResponses;
    }

    private List<String> getApis(ApiGatewayV2Client client) {
        return client.getApis().items().stream().map(Api::apiId).collect(Collectors.toList());
    }

    private List<String> getIntegrations(ApiGatewayV2Client client, String apiId) {
        return client.getIntegrations(r -> r.apiId(apiId))
            .items()
            .stream()
            .map(Integration::integrationId)
            .collect(Collectors.toList());
    }
}
