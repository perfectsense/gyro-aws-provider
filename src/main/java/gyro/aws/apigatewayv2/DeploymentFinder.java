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
import software.amazon.awssdk.services.apigatewayv2.model.Deployment;
import software.amazon.awssdk.services.apigatewayv2.model.GetDeploymentsRequest;
import software.amazon.awssdk.services.apigatewayv2.model.GetDeploymentsResponse;

/**
 * Query Deployment.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    deployment: $(external-query aws::api-gateway-deployment {api-id: ""})
 */
@Type("api-gateway-deployment")
public class DeploymentFinder extends ApiGatewayFinder<ApiGatewayV2Client, Deployment, DeploymentResource> {

    private String id;
    private String apiId;

    /**
     * The ID of the deployment.
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
    protected List<Deployment> findAllAws(ApiGatewayV2Client client) {
        List<Deployment> deployments = new ArrayList<>();
        String marker = null;
        GetDeploymentsResponse response;

        for (String api : getApis(client)) {
            do {
                if (ObjectUtils.isBlank(marker)) {
                    response = client.getDeployments(r -> r.apiId(api));
                } else {
                    response = client.getDeployments(GetDeploymentsRequest.builder()
                        .apiId(api).nextToken(marker).build());
                }

                marker = response.nextToken();
                deployments.addAll(response.items());
            } while (!ObjectUtils.isBlank(marker));
        }

        return deployments;
    }

    @Override
    protected List<Deployment> findAws(ApiGatewayV2Client client, Map<String, String> filters) {
        List<Deployment> deployments = new ArrayList<>();

        if (filters.containsKey("api-id")) {
            deployments = client.getDeployments(r -> r.apiId(filters.get("api-id"))).items();

        } else {
            for (String api : getApis(client)) {
                deployments.addAll(client.getDeployments(r -> r.apiId(api)).items());
            }
        }

        if (filters.containsKey("id")) {
            deployments.removeIf(i -> !i.deploymentId().equals(filters.get("id")));
        }

        return deployments;
    }
}
