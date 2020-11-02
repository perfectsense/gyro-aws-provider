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
import java.util.Map;
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
import gyro.core.validation.Range;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client;
import software.amazon.awssdk.services.apigatewayv2.model.ConnectionType;
import software.amazon.awssdk.services.apigatewayv2.model.ContentHandlingStrategy;
import software.amazon.awssdk.services.apigatewayv2.model.CreateIntegrationResponse;
import software.amazon.awssdk.services.apigatewayv2.model.GetIntegrationsResponse;
import software.amazon.awssdk.services.apigatewayv2.model.Integration;
import software.amazon.awssdk.services.apigatewayv2.model.IntegrationType;
import software.amazon.awssdk.services.apigatewayv2.model.PassthroughBehavior;

/**
 * Create an integration.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::integration example-integration
 *         api: $(aws::api example-api)
 *         integration-type: HTTP_PROXY
 *         timeout-in-millis: 30000
 *         integration-uri: 'https://example-domain.com'
 *         integration-method: ANY
 *         payload-format-version: "1.0"
 *     end
 */
@Type("integration")
public class IntegrationResource extends AwsResource implements Copyable<Integration> {

    private ApiResource api;
    private VpcLinkResource connection;
    private ConnectionType connectionType;
    private ContentHandlingStrategy contentHandlingStrategy;
    private String credentialsArn;
    private String description;
    private String integrationMethod;
    private String integrationSubtype;
    private IntegrationType integrationType;
    private String integrationUri;
    private PassthroughBehavior passthroughBehavior;
    private String payloadFormatVersion;
    private Map<String, String> requestParameters;
    private Map<String, String> requestTemplates;
    private String templateSelectionExpression;
    private Integer timeoutInMillis;
    private ApiTlsConfig tlsConfig;

    // Output
    private String id;

    /**
     * The API resource for which to create an integration.
     */
    @Required
    public ApiResource getApi() {
        return api;
    }

    public void setApi(ApiResource api) {
        this.api = api;
    }

    /**
     * The VPC link connection for a private integration.
     */
    @Updatable
    public VpcLinkResource getConnection() {
        return connection;
    }

    public void setConnection(VpcLinkResource connection) {
        this.connection = connection;
    }

    /**
     * The type of the network connection to the integration endpoint.
     */
    @Updatable
    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    /**
     * The configuration to handle response payload content type conversions.
     */
    @Updatable
    public ContentHandlingStrategy getContentHandlingStrategy() {
        return contentHandlingStrategy;
    }

    public void setContentHandlingStrategy(ContentHandlingStrategy contentHandlingStrategy) {
        this.contentHandlingStrategy = contentHandlingStrategy;
    }

    /**
     * The credentials required for the integration.
     */
    @Updatable
    public String getCredentialsArn() {
        return credentialsArn;
    }

    public void setCredentialsArn(String credentialsArn) {
        this.credentialsArn = credentialsArn;
    }

    /**
     * The description of the integration.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The integration's HTTP method type.
     */
    @Updatable
    public String getIntegrationMethod() {
        return integrationMethod;
    }

    public void setIntegrationMethod(String integrationMethod) {
        this.integrationMethod = integrationMethod;
    }

    /**
     * The AWS service action to invoke. See `Integration subtype reference <https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-develop-integrations-aws-services-reference.html>`_.
     */
    public String getIntegrationSubtype() {
        return integrationSubtype;
    }

    public void setIntegrationSubtype(String integrationSubtype) {
        this.integrationSubtype = integrationSubtype;
    }

    /**
     * The integration type of an integration.
     */
    @Required
    public IntegrationType getIntegrationType() {
        return integrationType;
    }

    public void setIntegrationType(IntegrationType integrationType) {
        this.integrationType = integrationType;
    }

    /**
     * The url endpoint for the integration.
     */
    @Updatable
    public String getIntegrationUri() {
        return integrationUri;
    }

    public void setIntegrationUri(String integrationUri) {
        this.integrationUri = integrationUri;
    }

    /**
     * The pass-through behavior for incoming requests.
     */
    @Updatable
    public PassthroughBehavior getPassthroughBehavior() {
        return passthroughBehavior;
    }

    public void setPassthroughBehavior(PassthroughBehavior passthroughBehavior) {
        this.passthroughBehavior = passthroughBehavior;
    }

    /**
     * The format of the payload sent to an integration.
     */
    @Updatable
    public String getPayloadFormatVersion() {
        return payloadFormatVersion;
    }

    public void setPayloadFormatVersion(String payloadFormatVersion) {
        this.payloadFormatVersion = payloadFormatVersion;
    }

    /**
     * The key-value map specifying request parameters that are passed from the method request to the backend. See `Integration subtype reference <https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-develop-integrations-aws-services-reference.html>`_.
     */
    @Updatable
    public Map<String, String> getRequestParameters() {
        if (requestParameters == null) {
            requestParameters = new HashMap<>();
        }

        return requestParameters;
    }

    public void setRequestParameters(Map<String, String> requestParameters) {
        this.requestParameters = requestParameters;
    }

    /**
     * The map of Velocity templates that are applied on the request payload based on the value of the Content-Type header sent by the client.
     */
    @Updatable
    public Map<String, String> getRequestTemplates() {
        if (requestTemplates == null) {
            requestTemplates = new HashMap<>();
        }

        return requestTemplates;
    }

    public void setRequestTemplates(Map<String, String> requestTemplates) {
        this.requestTemplates = requestTemplates;
    }

    /**
     * The template selection expression for the integration.
     */
    @Updatable
    public String getTemplateSelectionExpression() {
        return templateSelectionExpression;
    }

    public void setTemplateSelectionExpression(String templateSelectionExpression) {
        this.templateSelectionExpression = templateSelectionExpression;
    }

    /**
     * Custom timeout in milliseconds for the apis. Valid values are between ``50`` and ``30000``.
     */
    @Range(min = 50, max = 30000)
    @Updatable
    public Integer getTimeoutInMillis() {
        return timeoutInMillis;
    }

    public void setTimeoutInMillis(Integer timeoutInMillis) {
        this.timeoutInMillis = timeoutInMillis;
    }

    /**
     * The TLS configuration for a private integration.
     */
    @Updatable
    public ApiTlsConfig getTlsConfig() {
        return tlsConfig;
    }

    public void setTlsConfig(ApiTlsConfig tlsConfig) {
        this.tlsConfig = tlsConfig;
    }

    /**
     * The Id of the integration.
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
    public void copyFrom(Integration model) {
        setConnection(findById(VpcLinkResource.class, model.connectionId()));
        setConnectionType(model.connectionType());
        setContentHandlingStrategy(model.contentHandlingStrategy());
        setCredentialsArn(model.credentialsArn());
        setDescription(model.description());
        setIntegrationMethod(model.integrationMethod());
        setIntegrationSubtype(model.integrationSubtype());
        setIntegrationType(model.integrationType());
        setIntegrationUri(model.integrationUri());
        setPassthroughBehavior(model.passthroughBehavior());
        setPayloadFormatVersion(model.payloadFormatVersion());
        setRequestParameters(model.hasRequestParameters() ? model.requestParameters() : null);
        setRequestTemplates(model.hasRequestTemplates() ? model.requestTemplates() : null);
        setTemplateSelectionExpression(model.templateSelectionExpression());
        setTimeoutInMillis(model.timeoutInMillis());
        setId(model.integrationId());

        if (model.tlsConfig() != null) {
            ApiTlsConfig config = newSubresource(ApiTlsConfig.class);
            config.copyFrom(model.tlsConfig());
            setTlsConfig(config);
        }
    }

    @Override
    public boolean refresh() {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        Integration integration = getIntegration(client);

        if (integration == null) {
            return false;
        }

        copyFrom(integration);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        CreateIntegrationResponse integration = client.createIntegration(r -> r.apiId(getApi().getId())
            .connectionId(getConnection() != null ? getConnection().getId() : null)
            .connectionType(getConnectionType())
            .contentHandlingStrategy(getContentHandlingStrategy())
            .credentialsArn(getCredentialsArn())
            .description(getDescription())
            .integrationMethod(getIntegrationMethod())
            .integrationUri(getIntegrationUri())
            .integrationType(getIntegrationType())
            .passthroughBehavior(getPassthroughBehavior())
            .payloadFormatVersion(getPayloadFormatVersion())
            .requestParameters(getRequestParameters())
            .requestTemplates(getRequestTemplates())
            .timeoutInMillis(getTimeoutInMillis())
            .templateSelectionExpression(getTemplateSelectionExpression())
            .tlsConfig(getTlsConfig() != null ? getTlsConfig().toTlsConfigInput() : null));

        setId(integration.integrationId());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        client.updateIntegration(r -> r.apiId(getApi().getId())
            .connectionId(getConnection() != null ? getConnection().getId() : null)
            .connectionType(getConnectionType())
            .contentHandlingStrategy(getContentHandlingStrategy())
            .credentialsArn(getCredentialsArn())
            .description(getDescription())
            .integrationMethod(getIntegrationMethod())
            .integrationSubtype(getIntegrationSubtype())
            .integrationType(getIntegrationType())
            .integrationUri(getIntegrationUri())
            .passthroughBehavior(getPassthroughBehavior())
            .payloadFormatVersion(getPayloadFormatVersion())
            .requestParameters(getRequestParameters())
            .requestTemplates(getRequestTemplates())
            .timeoutInMillis(getTimeoutInMillis())
            .templateSelectionExpression(getTemplateSelectionExpression())
            .tlsConfig(getTlsConfig() != null ? getTlsConfig().toTlsConfigInput() : null)
            .integrationId(getId()));
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        client.deleteIntegration(r -> r.apiId(getApi().getId()).integrationId(getId()));
    }

    private Integration getIntegration(ApiGatewayV2Client client) {
        Integration integration = null;

        GetIntegrationsResponse response = client.getIntegrations(r -> r.apiId(getApi().getId()));

        if (response.hasItems()) {
            integration = response.items()
                .stream()
                .filter(i -> i.integrationId().equals(getId()))
                .findFirst()
                .orElse(null);
        }

        return integration;
    }
}
