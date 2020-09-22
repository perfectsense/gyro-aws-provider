/*
 * Copyright 2019, Perfect Sense, Inc.
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

package gyro.aws.cognitoidp;

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
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolClientResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DescribeUserPoolClientResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolClientType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::cognito-user-pool-client client
 *         client-name: "clientname"
 *         user-pool: $(aws::cognito-user-pool cognito)
 *     end
 */
@Type("cognito-user-pool-client")
public class UserPoolClientResource extends AwsResource implements Copyable<UserPoolClientType> {

    private Boolean allowedOAuthFlowsClient;
    private List<String> allowedOAuthFlows;
    private List<String> allowedOAuthScopes;
    private List<String> callbackUrls;
    private String defaultRedirectUri;
    private List<String> explicitAuthFlows;
    private Boolean generateSecret;
    private String id;
    private List<String> logoutUrls;
    private String name;
    private List<String> readAttributes;
    private Integer refreshTokenValidity;
    private String secret;
    private List<String> supportedIdentityProviders;
    private UserPoolResource userPool;
    private List<String> writeAttributes;

    /**
     *  Sets to true if client is allowed to follow OAuth protocol when interacting with Cognito user pools. (Optional)
     */
    @Updatable
    public Boolean getAllowedOAuthFlowsClient() {
        return allowedOAuthFlowsClient;
    }

    public void setAllowedOAuthFlowsClient(Boolean allowedOAuthFlowsClient) {
        this.allowedOAuthFlowsClient = allowedOAuthFlowsClient;
    }

    /**
     *  Sets the OAuth flows. Valid values are ``code`` and ``token``. (Optional)
     */
    @Updatable
    public List<String> getAllowedOAuthFlows() {
        if (allowedOAuthFlows == null) {
            allowedOAuthFlows = new ArrayList<>();
        }

        return allowedOAuthFlows;
    }

    public void setAllowedOAuthFlows(List<String> allowedOAuthFlows) {
        this.allowedOAuthFlows = allowedOAuthFlows;
    }

    /**
     *  The list of allowed redirect URLs for the identity providers. (Optional)
     */
    @Updatable
    public List<String> getCallbackUrls() {
        if (callbackUrls == null) {
            callbackUrls = new ArrayList<>();
        }

        return callbackUrls;
    }

    public void setCallbackUrls(List<String> callbackUrls) {
        this.callbackUrls = callbackUrls;
    }

    /**
     *  The default redirect URI. (Optional)
     */
    @Updatable
    public String getDefaultRedirectUri() {
        return defaultRedirectUri;
    }

    public void setDefaultRedirectUri(String defaultRedirectUri) {
        this.defaultRedirectUri = defaultRedirectUri;
    }

    /**
     *  The explicit authentication flows. (Optional)
     */
    @Updatable
    public List<String> getExplicitAuthFlows() {
        if (explicitAuthFlows == null) {
            explicitAuthFlows = new ArrayList<>();
        }

        return explicitAuthFlows;
    }

    public void setExplicitAuthFlows(List<String> explicitAuthFlows) {
        this.explicitAuthFlows = explicitAuthFlows;
    }

    /**
     *  The list of allowed OAuth scopes. (Optional)
     */
    @Updatable
    public List<String> getAllowedOAuthScopes() {
        if (allowedOAuthScopes == null) {
            allowedOAuthScopes = new ArrayList<>();
        }

        return allowedOAuthScopes;
    }

    public void setAllowedOAuthScopes(List<String> allowedOAuthScopes) {
        this.allowedOAuthScopes = allowedOAuthScopes;
    }

    /**
     *  Specified whether you want to generate a secret for the client. (Optional)
     */
    public Boolean getGenerateSecret() {
        return generateSecret;
    }

    public void setGenerateSecret(Boolean generateSecret) {
        this.generateSecret = generateSecret;
    }

    /**
     *  The id of the user pool client.
     */
    @Output
    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     *  The list of logout URLs for the identity providers. (Optional)
     */
    @Updatable
    public List<String> getLogoutUrls() {
        if (logoutUrls == null) {
            logoutUrls = new ArrayList<>();
        }

        return logoutUrls;
    }

    public void setLogoutUrls(List<String> logoutUrls) {
        this.logoutUrls = logoutUrls;
    }

    /**
     *  The name of the client. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *  The read attributes. (Optional)
     */
    @Updatable
    public List<String> getReadAttributes() {
        if (readAttributes == null) {
            readAttributes = new ArrayList<>();
        }

        return readAttributes;
    }

    public void setReadAttributes(List<String> readAttributes) {
        this.readAttributes = readAttributes;
    }

    /**
     *  The time limit after which the refresh token cannot be used because it is no longer valid. (Optional)
     */
    @Updatable
    public Integer getRefreshTokenValidity() {
        return refreshTokenValidity;
    }

    public void setRefreshTokenValidity(Integer refreshTokenValidity) {
        this.refreshTokenValidity = refreshTokenValidity;
    }

    @Output
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     *  The list of provider names for support identity providers. (Optional)
     */
    @Updatable
    public List<String> getSupportedIdentityProviders() {
        if (supportedIdentityProviders == null) {
            supportedIdentityProviders = new ArrayList<>();
        }

        return supportedIdentityProviders;
    }

    public void setSupportedIdentityProviders(List<String> supportedIdentityProviders) {
        this.supportedIdentityProviders = supportedIdentityProviders;
    }

    /**
     *  The user pool resource where the client will be created. (Optional)
     */
    public UserPoolResource getUserPool() {
        return userPool;
    }

    public void setUserPool(UserPoolResource userPool) {
        this.userPool = userPool;
    }

    /**
     *  The user pool attributes the app client can write to. (Optional)
     */
    @Updatable
    public List<String> getWriteAttributes() {
        if (writeAttributes == null) {
            writeAttributes = new ArrayList<>();
        }

        return writeAttributes;
    }

    public void setWriteAttributes(List<String> writeAttributes) {
        this.writeAttributes = writeAttributes;
    }

    @Override
    public void copyFrom(UserPoolClientType model) {
        setId(model.clientId());
        setAllowedOAuthFlowsClient(model.allowedOAuthFlowsUserPoolClient());

        getAllowedOAuthFlows().clear();
        model.allowedOAuthFlowsAsStrings().forEach(allowedOAuthFlow -> getAllowedOAuthFlows().add(allowedOAuthFlow));

        getAllowedOAuthScopes().clear();
        model.allowedOAuthScopes().forEach(allowedOAuthScope -> getAllowedOAuthScopes().add(allowedOAuthScope));

        getCallbackUrls().clear();
        model.callbackURLs().forEach(callbackUrl -> getCallbackUrls().add(callbackUrl));

        setDefaultRedirectUri(model.defaultRedirectURI());

        getExplicitAuthFlows().clear();
        model.explicitAuthFlowsAsStrings().forEach(explicitAuthFlow -> getExplicitAuthFlows().add(explicitAuthFlow));

        getLogoutUrls().clear();
        model.logoutURLs().forEach(logoutUrl -> getLogoutUrls().add(logoutUrl));

        getReadAttributes().clear();
        model.readAttributes().forEach(readAttribute -> getReadAttributes().add(readAttribute));

        setRefreshTokenValidity(model.refreshTokenValidity());

        getSupportedIdentityProviders().clear();
        model.supportedIdentityProviders().forEach(ip -> getSupportedIdentityProviders().add(ip));

        setUserPool(findById(UserPoolResource.class, model.userPoolId()));

        getWriteAttributes().clear();
        model.writeAttributes().forEach(writeAttribute -> getWriteAttributes().add(writeAttribute));
    }

    @Override
    public boolean refresh() {
        try {
            CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);

            DescribeUserPoolClientResponse response = client.describeUserPoolClient(r -> r.clientId(getId())
                                                                                            .userPoolId(getUserPool().getId()));
            this.copyFrom(response.userPoolClient());

            return true;
        } catch (CognitoIdentityProviderException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    @Override
    public void create(GyroUI ui, State state) {
        CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);
        CreateUserPoolClientResponse response = client.createUserPoolClient(r ->
                r.allowedOAuthFlowsUserPoolClient(getAllowedOAuthFlowsClient())
                .allowedOAuthFlowsWithStrings(getAllowedOAuthFlows())
                .allowedOAuthScopes(getAllowedOAuthScopes())
                .callbackURLs(getCallbackUrls())
                .clientName(getName())
                .defaultRedirectURI(getDefaultRedirectUri())
                .explicitAuthFlowsWithStrings(getExplicitAuthFlows())
                .generateSecret(getGenerateSecret())
                .logoutURLs(getLogoutUrls())
                .readAttributes(getReadAttributes())
                .refreshTokenValidity(getRefreshTokenValidity())
                .supportedIdentityProviders(getSupportedIdentityProviders())
                .userPoolId(getUserPool().getId())
                .writeAttributes(getWriteAttributes()));

        setId(response.userPoolClient().clientId());
        setSecret(response.userPoolClient().clientSecret());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);
        client.updateUserPoolClient(r -> r.allowedOAuthFlowsUserPoolClient(getAllowedOAuthFlowsClient())
                .allowedOAuthFlowsWithStrings(getAllowedOAuthFlows())
                .allowedOAuthScopes(getAllowedOAuthScopes())
                .callbackURLs(getCallbackUrls())
                .clientId(getId())
                .clientName(getName())
                .defaultRedirectURI(getDefaultRedirectUri())
                .explicitAuthFlowsWithStrings(getExplicitAuthFlows())
                .logoutURLs(getLogoutUrls())
                .readAttributes(getReadAttributes())
                .refreshTokenValidity(getRefreshTokenValidity())
                .supportedIdentityProviders(getSupportedIdentityProviders())
                .userPoolId(getUserPool().getId())
                .writeAttributes(getWriteAttributes())
        );
    }

    @Override
    public void delete(GyroUI ui, State state) {
        CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);
        client.deleteUserPoolClient(r -> r.clientId(getId())
                                            .userPoolId(getUserPool().getId()));
    }

}
