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
import software.amazon.awssdk.services.apigatewayv2.model.GetStagesRequest;
import software.amazon.awssdk.services.apigatewayv2.model.GetStagesResponse;
import software.amazon.awssdk.services.apigatewayv2.model.Stage;

/**
 * Query Stage.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    stage: $(external-query aws::api-gateway-stage {api-id: ""})
 */
@Type("api-gateway-stage")
public class StageFinder extends ApiGatewayFinder<ApiGatewayV2Client, Stage, StageResource> {

    private String name;
    private String apiId;

    /**
     * The name of the stage.
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
    protected List<Stage> findAllAws(ApiGatewayV2Client client) {
        List<Stage> stages = new ArrayList<>();
        String marker = null;
        GetStagesResponse response;

        for (String api : getApis(client)) {
            do {
                if (ObjectUtils.isBlank(marker)) {
                    response = client.getStages(r -> r.apiId(api));
                } else {
                    response = client.getStages(GetStagesRequest.builder().apiId(api).nextToken(marker).build());
                }

                marker = response.nextToken();
                stages.addAll(response.items());
            } while (!ObjectUtils.isBlank(marker));
        }

        return stages;
    }

    @Override
    protected List<Stage> findAws(ApiGatewayV2Client client, Map<String, String> filters) {
        List<Stage> stages = new ArrayList<>();

        if (filters.containsKey("api-id")) {
            stages = client.getStages(r -> r.apiId(filters.get("api-id"))).items();

        } else {
            for (String api : getApis(client)) {
                stages.addAll(client.getStages(r -> r.apiId(api)).items());
            }
        }

        if (filters.containsKey("name")) {
            stages.removeIf(i -> !i.stageName().equals(filters.get("name")));
        }

        return stages;
    }
}
