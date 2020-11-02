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
import software.amazon.awssdk.services.apigatewayv2.model.Integration;

/**
 * Query Integration.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    integration: $(external-query aws::integration {api-id: "tf2x92hetc", id: "voi8a4l"})
 */
@Type("integration")
public class IntegrationFinder extends AwsFinder<ApiGatewayV2Client, Integration, IntegrationResource> {

    private String id;
    private String apiId;

    /**
     * The id of the integration.
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
    protected List<Integration> findAllAws(ApiGatewayV2Client client) {
        List<Integration> integrations = new ArrayList<>();
        List<String> apis = getApis(client);

        apis.forEach(a -> integrations.addAll(client.getIntegrations(r -> r.apiId(a)).items()));

        return integrations;
    }

    @Override
    protected List<Integration> findAws(ApiGatewayV2Client client, Map<String, String> filters) {
        List<Integration> integrations = new ArrayList<>();

        if (filters.containsKey("api-id")) {
            integrations = new ArrayList<>(client.getIntegrations(r -> r.apiId(filters.get("api-id"))).items());

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

    private List<String> getApis(ApiGatewayV2Client client) {
        return client.getApis().items().stream().map(Api::apiId).collect(Collectors.toList());
    }
}
