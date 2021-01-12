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
import software.amazon.awssdk.services.apigatewayv2.model.GetModelsRequest;
import software.amazon.awssdk.services.apigatewayv2.model.GetModelsResponse;
import software.amazon.awssdk.services.apigatewayv2.model.Model;

/**
 * Query Model.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    model: $(external-query aws::api-gateway-model {api-id: "", name: "example"})
 */
@Type("api-gateway-model")
public class ModelFinder extends ApiGatewayFinder<ApiGatewayV2Client, Model, ModelResource> {

    private String name;
    private String apiId;

    /**
     * The name of the model.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    protected List<Model> findAllAws(ApiGatewayV2Client client) {
        List<Model> models = new ArrayList<>();
        String marker = null;
        GetModelsResponse response;

        for (String api : getApis(client)) {
            do {
                if (ObjectUtils.isBlank(marker)) {
                    response = client.getModels(r -> r.apiId(api));
                } else {
                    response = client.getModels(GetModelsRequest.builder()
                        .apiId(api).nextToken(marker).build());
                }

                marker = response.nextToken();
                models.addAll(response.items());
            } while (!ObjectUtils.isBlank(marker));
        }

        return models;
    }

    @Override
    protected List<Model> findAws(ApiGatewayV2Client client, Map<String, String> filters) {
        List<Model> models = new ArrayList<>();

        if (filters.containsKey("api-id")) {
            models = client.getModels(r -> r.apiId(filters.get("api-id"))).items();

        } else {
            for (String api : getApis(client)) {
                models.addAll(client.getModels(r -> r.apiId(api)).items());
            }
        }

        if (filters.containsKey("name")) {
            models.removeIf(i -> !i.name().equals(filters.get("name")));
        }

        return models;
    }
}
