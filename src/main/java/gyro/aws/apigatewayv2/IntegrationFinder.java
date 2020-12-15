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

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client;
import software.amazon.awssdk.services.apigatewayv2.model.GetIntegrationsRequest;
import software.amazon.awssdk.services.apigatewayv2.model.GetIntegrationsResponse;
import software.amazon.awssdk.services.apigatewayv2.model.Integration;

/**
 * Query Integration.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    integration: $(external-query aws::api-gateway-integration {api-id: "", id: ""})
 */
@Type("api-gateway-integration")
public class IntegrationFinder extends ApiGatewayFinder<ApiGatewayV2Client, Integration, IntegrationResource> {

    private String id;
    private String apiId;

    /**
     * The ID of the integration.
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

    @Override
    protected List<Integration> findAllAws(ApiGatewayV2Client client) {
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

        return integrations;
    }

    @Override
    protected List<Integration> findAws(ApiGatewayV2Client client, Map<String, String> filters) {
        List<Integration> integrations = new ArrayList<>();

        if (filters.containsKey("api-id")) {
            integrations = client.getIntegrations(r -> r.apiId(filters.get("api-id"))).items();

        } else {
            for (String api : getApis(client)) {
                integrations.addAll(client.getIntegrations(r -> r.apiId(api)).items());
            }
        }

        if (filters.containsKey("id")) {
            integrations.removeIf(i -> !i.integrationId().equals(filters.get("id")));
        }

        return integrations;
    }
}
