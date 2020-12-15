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
import software.amazon.awssdk.services.apigatewayv2.model.Authorizer;
import software.amazon.awssdk.services.apigatewayv2.model.GetAuthorizersRequest;
import software.amazon.awssdk.services.apigatewayv2.model.GetAuthorizersResponse;

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
public class AuthorizerFinder extends ApiGatewayFinder<ApiGatewayV2Client, Authorizer, AuthorizerResource> {

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
     * The ID of the api.
     */
    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    @Override
    protected List<Authorizer> findAllAws(ApiGatewayV2Client client) {
        List<Authorizer> authorizers = new ArrayList<>();
        String marker = null;
        GetAuthorizersResponse response;

        for (String api : getApis(client)) {
            do {
                if (ObjectUtils.isBlank(marker)) {
                    response = client.getAuthorizers(r -> r.apiId(api));
                } else {
                    response = client.getAuthorizers(GetAuthorizersRequest.builder()
                        .apiId(api).nextToken(marker).build());
                }

                marker = response.nextToken();
                authorizers.addAll(response.items());
            } while (!ObjectUtils.isBlank(marker));
        }

        return authorizers;
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
}
