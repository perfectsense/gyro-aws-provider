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

import java.util.Set;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client;
import software.amazon.awssdk.services.apigatewayv2.model.ApiMapping;
import software.amazon.awssdk.services.apigatewayv2.model.CreateApiMappingResponse;
import software.amazon.awssdk.services.apigatewayv2.model.GetApiMappingsResponse;

/**
 * Create an api mapping.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::api-mapping example-mapping
 *         api: $(aws::api-gateway example-api)
 *         domain-name: "vpn.ops-test.psdops.com"
 *         stage: $(aws::api-gateway-stage example-stage)
 *         api-mapping-key: "example-key"
 *     end
 */
@Type("api-mapping")
public class ApiMappingResource extends AwsResource implements Copyable<ApiMapping> {

    private ApiResource api;
    private String apiMappingKey;
    private DomainNameResource domainName;
    private StageResource stage;

    // Output
    private String id;

    /**
     * The API identifier.
     */
    @Required
    public ApiResource getApi() {
        return api;
    }

    public void setApi(ApiResource api) {
        this.api = api;
    }

    /**
     * The API mapping key.
     */
    @Updatable
    public String getApiMappingKey() {
        return apiMappingKey;
    }

    public void setApiMappingKey(String apiMappingKey) {
        this.apiMappingKey = apiMappingKey;
    }

    /**
     * The domain name.
     */
    @Updatable
    public DomainNameResource getDomainName() {
        return domainName;
    }

    public void setDomainName(DomainNameResource domainName) {
        this.domainName = domainName;
    }

    /**
     * The API stage.
     */
    @Updatable
    public StageResource getStage() {
        return stage;
    }

    public void setStage(StageResource stage) {
        this.stage = stage;
    }

    /**
     * The id of the api mapping.
     */
    @Output
    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(ApiMapping model) {
        setApi(findById(ApiResource.class, model.apiId()));
        setApiMappingKey(model.apiMappingKey());
        setStage(findById(StageResource.class, model.stage()));
        setId(model.apiMappingId());
    }

    @Override
    public boolean refresh() {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        ApiMapping mapping = getApiMapping(client);

        if (mapping == null) {
            return false;
        }

        copyFrom(mapping);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        CreateApiMappingResponse apiMapping = client.createApiMapping(r -> r.apiId(getApi().getId())
            .apiMappingKey(getApiMappingKey())
            .domainName(getDomainName().getName())
            .stage(getStage().getName()));

        setId(apiMapping.apiMappingId());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        client.updateApiMapping(r -> r.apiId(getApi().getId())
            .apiMappingId(getId())
            .apiMappingKey(getApiMappingKey())
            .domainName(getDomainName().getName())
            .stage(getStage().getName()));
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        client.deleteApiMapping(r -> r.apiMappingId(getId()).domainName(getDomainName().getName()));
    }

    private ApiMapping getApiMapping(ApiGatewayV2Client client) {
        ApiMapping apiMapping = null;

        GetApiMappingsResponse mappings = client.getApiMappings(r -> r.domainName(getDomainName().getName()));

        if (mappings.hasItems()) {
            apiMapping = mappings.items()
                .stream()
                .filter(i -> i.apiMappingId().equals(getId()))
                .findFirst()
                .orElse(null);
        }

        return apiMapping;
    }
}