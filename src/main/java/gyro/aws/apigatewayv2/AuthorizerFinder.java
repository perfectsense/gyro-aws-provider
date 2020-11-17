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
import software.amazon.awssdk.services.apigatewayv2.model.Authorizer;

/**
 * Query Authorizer.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    authorizer: $(external-query aws::api-gateway-authorizer {api-id: ""})
 */
@Type("api-gateway-authorizer")
public class AuthorizerFinder extends AwsFinder<ApiGatewayV2Client, Authorizer, AuthorizerResource> {

    private String name;
    private String apiId;

    /**
     * The name of the authorizer.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    protected List<Authorizer> findAllAws(ApiGatewayV2Client client) {
        return getApis(client).stream()
            .map(api -> client.getAuthorizers(r -> r.apiId(api)).items())
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    @Override
    protected List<Authorizer> findAws(ApiGatewayV2Client client, Map<String, String> filters) {
        List<Authorizer> authorizers = new ArrayList<>();

        if (filters.containsKey("api-id")) {
            authorizers = client.getAuthorizers(r -> r.apiId(filters.get("api-id"))).items();

        } else {
            for (String api : getApis(client)) {
                authorizers.addAll(client.getAuthorizers(r -> r.apiId(api)).items());
            }
        }

        if (filters.containsKey("name")) {
            authorizers.removeIf(i -> !i.name().equals(filters.get("name")));
        }

        return authorizers;
    }

    private List<String> getApis(ApiGatewayV2Client client) {
        return client.getApis().items().stream().map(Api::apiId).collect(Collectors.toList());
    }
}
