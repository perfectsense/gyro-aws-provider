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
import java.util.Set;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.RoleResource;
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
import software.amazon.awssdk.services.apigatewayv2.model.Authorizer;
import software.amazon.awssdk.services.apigatewayv2.model.AuthorizerType;
import software.amazon.awssdk.services.apigatewayv2.model.CreateAuthorizerResponse;
import software.amazon.awssdk.services.apigatewayv2.model.GetAuthorizersResponse;

/**
 * Create an authorizer.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::api-gateway-authorizer example-authorizer
 *         api: $(aws::api example-api)
 *         name: "example-authorizer"
 *         authorizer-type: JWT
 *
 *         identity-sources: [
 *             '$request.header.Authorization'
 *         ]
 *
 *         jwt-configuration
 *             audiences: [
 *                 "example-audience"
 *             ]
 *
 *             issuer: "https://cognito-idp.us-east-2.amazonaws.com/us-east-2_pQJThFAx4"
 *         end
 *     end
 */
@Type("api-gateway-authorizer")
public class AuthorizerResource extends AwsResource implements Copyable<Authorizer> {

    private ApiResource api;
    private RoleResource authorizerCredentials;
    private String authorizerPayloadFormatVersion;
    private Integer authorizerResultTtlInSeconds;
    private AuthorizerType authorizerType;
    private String authorizerUri;
    private Boolean enableSimpleResponses;
    private List<String> identitySources;
    private ApiJwtConfiguration jwtConfiguration;
    private String name;

    // Output
    private String id;

    /**
     * The API for which to create an authorizer.
     */
    @Required
    public ApiResource getApi() {
        return api;
    }

    public void setApi(ApiResource api) {
        this.api = api;
    }

    /**
     * The required credentials as an IAM role for API Gateway to invoke the authorizer.
     */
    @Updatable
    public RoleResource getAuthorizerCredentials() {
        return authorizerCredentials;
    }

    public void setAuthorizerCredentials(RoleResource authorizerCredentials) {
        this.authorizerCredentials = authorizerCredentials;
    }

    /**
     * The format of the payload sent to an HTTP API Lambda authorizer.
     */
    @Updatable
    public String getAuthorizerPayloadFormatVersion() {
        return authorizerPayloadFormatVersion;
    }

    public void setAuthorizerPayloadFormatVersion(String authorizerPayloadFormatVersion) {
        this.authorizerPayloadFormatVersion = authorizerPayloadFormatVersion;
    }

    /**
     * The time to live (TTL) for cached authorizer results, in seconds.
     */
    @Updatable
    public Integer getAuthorizerResultTtlInSeconds() {
        return authorizerResultTtlInSeconds;
    }

    public void setAuthorizerResultTtlInSeconds(Integer authorizerResultTtlInSeconds) {
        this.authorizerResultTtlInSeconds = authorizerResultTtlInSeconds;
    }

    /**
     * The authorizer type.
     */
    @Updatable
    @Required
    @ValidStrings({ "REQUEST", "JWT" })
    public AuthorizerType getAuthorizerType() {
        return authorizerType;
    }

    public void setAuthorizerType(AuthorizerType authorizerType) {
        this.authorizerType = authorizerType;
    }

    /**
     * The authorizer's Uniform Resource Identifier (URI).
     */
    @Updatable
    public String getAuthorizerUri() {
        return authorizerUri;
    }

    public void setAuthorizerUri(String authorizerUri) {
        this.authorizerUri = authorizerUri;
    }

    /**
     * When set to ``true``, the authorizer returns a response in a simple format.
     */
    @Updatable
    public Boolean getEnableSimpleResponses() {
        return enableSimpleResponses;
    }

    public void setEnableSimpleResponses(Boolean enableSimpleResponses) {
        this.enableSimpleResponses = enableSimpleResponses;
    }

    /**
     * The identity source for which authorization is requested.
     */
    @Updatable
    @Required
    public List<String> getIdentitySources() {
        if (identitySources == null) {
            identitySources = new ArrayList<>();
        }

        return identitySources;
    }

    public void setIdentitySources(List<String> identitySources) {
        this.identitySources = identitySources;
    }

    /**
     * The configuration of the JSON Web Token (JWT) authorizer.
     */
    @Updatable
    public ApiJwtConfiguration getJwtConfiguration() {
        return jwtConfiguration;
    }

    public void setJwtConfiguration(ApiJwtConfiguration jwtConfiguration) {
        this.jwtConfiguration = jwtConfiguration;
    }

    /**
     * The name of the authorizer. (Required)
     */
    @Updatable
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The id of the authorizer.
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
    public void copyFrom(Authorizer model) {
        setAuthorizerCredentials(findById(RoleResource.class, model.authorizerCredentialsArn()));
        setAuthorizerPayloadFormatVersion(model.authorizerPayloadFormatVersion());
        setAuthorizerResultTtlInSeconds(model.authorizerResultTtlInSeconds());
        setAuthorizerType(model.authorizerType());
        setAuthorizerUri(model.authorizerUri());
        setEnableSimpleResponses(model.enableSimpleResponses());
        setIdentitySources(model.hasIdentitySource() ? model.identitySource() : null);
        setName(model.name());
        setId(model.authorizerId());

        if (model.jwtConfiguration() != null) {
            ApiJwtConfiguration config = newSubresource(ApiJwtConfiguration.class);
            config.copyFrom(model.jwtConfiguration());
            setJwtConfiguration(config);
        }
    }

    @Override
    public boolean refresh() {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        Authorizer authorizer = getAuthorizer(client);

        if (authorizer == null) {
            return false;
        }

        copyFrom(authorizer);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        CreateAuthorizerResponse authorizer = client.createAuthorizer(r -> r.apiId(getApi().getId())
            .authorizerCredentialsArn(getAuthorizerCredentials() != null ? getAuthorizerCredentials().getArn() : null)
            .authorizerPayloadFormatVersion(getAuthorizerPayloadFormatVersion())
            .authorizerResultTtlInSeconds(getAuthorizerResultTtlInSeconds())
            .authorizerType(getAuthorizerType())
            .authorizerUri(getAuthorizerUri())
            .enableSimpleResponses(getEnableSimpleResponses())
            .identitySource(getIdentitySources())
            .jwtConfiguration(getJwtConfiguration() != null ? getJwtConfiguration().toJWTConfiguration() : null)
            .name(getName()));

        setId(authorizer.authorizerId());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        client.updateAuthorizer(r -> r.apiId(getApi().getId())
            .authorizerCredentialsArn(getAuthorizerCredentials() != null ? getAuthorizerCredentials().getArn() : null)
            .authorizerId(getId())
            .authorizerPayloadFormatVersion(getAuthorizerPayloadFormatVersion())
            .authorizerResultTtlInSeconds(getAuthorizerResultTtlInSeconds())
            .authorizerType(getAuthorizerType())
            .authorizerUri(getAuthorizerUri())
            .enableSimpleResponses(getEnableSimpleResponses())
            .identitySource(getIdentitySources())
            .jwtConfiguration(getJwtConfiguration() != null ? getJwtConfiguration().toJWTConfiguration() : null)
            .name(getName()));
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        client.deleteAuthorizer(r -> r.apiId(getApi().getId()).authorizerId(getId()));
    }

    private Authorizer getAuthorizer(ApiGatewayV2Client client) {
        Authorizer authorizer = null;

        GetAuthorizersResponse authorizers = client.getAuthorizers(r -> r.apiId(getApi().getId()));

        if (authorizers.hasItems()) {
            authorizer = authorizers.items()
                .stream()
                .filter(i -> i.authorizerId().equals(getId()))
                .findFirst()
                .orElse(null);
        }

        return authorizer;
    }
}
