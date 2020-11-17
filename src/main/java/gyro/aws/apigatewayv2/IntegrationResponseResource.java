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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client;
import software.amazon.awssdk.services.apigatewayv2.model.Api;
import software.amazon.awssdk.services.apigatewayv2.model.ContentHandlingStrategy;
import software.amazon.awssdk.services.apigatewayv2.model.CreateIntegrationResponseResponse;
import software.amazon.awssdk.services.apigatewayv2.model.GetIntegrationResponsesResponse;
import software.amazon.awssdk.services.apigatewayv2.model.Integration;
import software.amazon.awssdk.services.apigatewayv2.model.IntegrationResponse;

/**
 * Create an integration response.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::api-gateway-integration-response example-int-response
 *         api: $(aws::api-gateway example-api-websock)
 *         integration: $(aws::api-gateway-integration example-integration-websock)
 *         integration-response-key: '$default'
 *     end
 */
@Type("api-gateway-integration-response")
public class IntegrationResponseResource extends AwsResource implements Copyable<IntegrationResponse> {

    private ApiResource api;
    private ContentHandlingStrategy contentHandlingStrategy;
    private IntegrationResource integration;
    private String integrationResponseKey;
    private Map<String, String> responseParameters;
    private Map<String, String> responseTemplates;
    private String templateSelectionExpression;

    // Output
    private String id;

    /**
     * The API resource for which to create an integration response.
     */
    @Required
    public ApiResource getApi() {
        return api;
    }

    public void setApi(ApiResource api) {
        this.api = api;
    }

    /**
     * The configuration to handle response payload content type conversions.
     */
    @Updatable
    @ValidStrings({ "CONVERT_TO_BINARY", "CONVERT_TO_TEXT" })
    public ContentHandlingStrategy getContentHandlingStrategy() {
        return contentHandlingStrategy;
    }

    public void setContentHandlingStrategy(ContentHandlingStrategy contentHandlingStrategy) {
        this.contentHandlingStrategy = contentHandlingStrategy;
    }

    /**
     * The integration ID.
     */
    @Required
    public IntegrationResource getIntegration() {
        return integration;
    }

    public void setIntegration(IntegrationResource integration) {
        this.integration = integration;
    }

    /**
     * The integration response key.
     */
    @Required
    @Updatable
    public String getIntegrationResponseKey() {
        return integrationResponseKey;
    }

    public void setIntegrationResponseKey(String integrationResponseKey) {
        this.integrationResponseKey = integrationResponseKey;
    }

    /**
     * The key-value map specifying response parameters that are passed from the method response to the backend.
     * See `Integration subtype reference <https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-develop-integrations-aws-services-reference.html>`_.
     */
    @Updatable
    public Map<String, String> getResponseParameters() {
        if (responseParameters == null) {
            responseParameters = new HashMap<>();
        }

        return responseParameters;
    }

    public void setResponseParameters(Map<String, String> responseParameters) {
        this.responseParameters = responseParameters;
    }

    /**
     * The map of Velocity templates that are applied on the response payload based on the value of the Content-Type header sent by the client.
     */
    @Updatable
    public Map<String, String> getResponseTemplates() {
        if (responseTemplates == null) {
            responseTemplates = new HashMap<>();
        }

        return responseTemplates;
    }

    public void setResponseTemplates(Map<String, String> responseTemplates) {
        this.responseTemplates = responseTemplates;
    }

    /**
     * The template selection expression for the integration response.
     */
    @Updatable
    public String getTemplateSelectionExpression() {
        return templateSelectionExpression;
    }

    public void setTemplateSelectionExpression(String templateSelectionExpression) {
        this.templateSelectionExpression = templateSelectionExpression;
    }

    /**
     * The Id of the integration response.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(IntegrationResponse model) {
        setContentHandlingStrategy(model.contentHandlingStrategy());
        setIntegrationResponseKey(model.integrationResponseKey());
        setResponseParameters(model.hasResponseParameters() ? model.responseParameters() : null);
        setResponseTemplates(model.hasResponseTemplates() ? model.responseTemplates() : null);
        setTemplateSelectionExpression(model.templateSelectionExpression());
        setId(model.integrationResponseId());

        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);
        List<String> apis = client.getApis().items().stream().map(Api::apiId).collect(Collectors.toList());

        apis.stream().filter(a -> {
            List<String> integrations = client.getIntegrations(r -> r.apiId(a)).items().stream().map(
                Integration::integrationId).collect(Collectors.toList());

            String integration = integrations.stream().filter(i -> {
                GetIntegrationResponsesResponse response = client.getIntegrationResponses(r -> r.apiId(a)
                    .integrationId(i));

                return response.hasItems() && response.items()
                    .stream()
                    .filter(r -> r.integrationResponseId().equals(getId()))
                    .findFirst()
                    .orElse(null) != null;
            }).findFirst().orElse(null);

            if (integration != null) {
                setIntegration(findById(IntegrationResource.class, integration));
                return true;
            }

            return false;
        }).findFirst().ifPresent(apiId -> setApi(findById(ApiResource.class, apiId)));
    }

    @Override
    public boolean refresh() {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        IntegrationResponse integrationResponse = getIntegrationResponse(client);

        if (integrationResponse == null) {
            return false;
        }

        copyFrom(integrationResponse);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        CreateIntegrationResponseResponse response = client.createIntegrationResponse(r -> r.apiId(getApi().getId())
            .contentHandlingStrategy(getContentHandlingStrategy())
            .integrationId(getIntegration().getId())
            .integrationResponseKey(getIntegrationResponseKey())
            .responseParameters(getResponseParameters())
            .responseTemplates(getResponseTemplates())
            .templateSelectionExpression(getTemplateSelectionExpression()));

        setId(response.integrationResponseId());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        client.updateIntegrationResponse(r -> r.apiId(getApi().getId())
            .contentHandlingStrategy(getContentHandlingStrategy())
            .integrationId(getIntegration().getId())
            .integrationResponseKey(getIntegrationResponseKey())
            .responseParameters(getResponseParameters())
            .responseTemplates(getResponseTemplates())
            .templateSelectionExpression(getTemplateSelectionExpression())
            .integrationResponseId(getId()));
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        client.deleteIntegrationResponse(r -> r.apiId(getApi().getId())
            .integrationId(getIntegration().getId())
            .integrationResponseId(getId()));
    }

    private IntegrationResponse getIntegrationResponse(ApiGatewayV2Client client) {
        IntegrationResponse integrationResponse = null;

        GetIntegrationResponsesResponse response = client.getIntegrationResponses(r -> r.apiId(getApi().getId())
            .integrationId(getIntegration().getId()));

        if (response.hasItems()) {
            integrationResponse = response.items()
                .stream()
                .filter(i -> i.integrationResponseId().equals(getId()))
                .findFirst()
                .orElse(null);
        }

        return integrationResponse;
    }
}
