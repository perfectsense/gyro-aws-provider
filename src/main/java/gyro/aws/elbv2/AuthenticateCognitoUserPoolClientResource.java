package gyro.aws.elbv2;

import gyro.aws.AwsResource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceOutput;
import gyro.core.resource.Resource;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolClientResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DescribeUserPoolClientResponse;

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
 *     aws::authenticate-cognito-user-pool-client client
 *         client-name: "clientname"
 *         user-pool-id: $(aws::authenticate-cognito-user-pool cognito | user-pool-id)
 *     end
 */
@ResourceType("authenticate-cognito-user-pool-client")
public class AuthenticateCognitoUserPoolClientResource extends AwsResource {

    private Boolean allowedOAuthFlowsClient;
    private List<String> allowedOAuthFlows;
    private List<String> allowedOAuthScopes;
    private List<String> callbackUrls;
    private String defaultRedirectUri;
    private List<String> explicitAuthFlows;
    private Boolean generateSecret;
    private List<String> logoutUrls;
    private List<String> readAttributes;
    private Integer refreshTokenValidity;
    private List<String> supportedIdentityProviders;
    private String userPoolClientId;
    private String userPoolClientName;
    private String userPoolClientSecret;
    private String userPoolId;
    private List<String> writeAttributes;

    @ResourceDiffProperty(updatable = true)
    public Boolean getAllowedOAuthFlowsClient() {
        return allowedOAuthFlowsClient;
    }

    public void setAllowedOAuthFlowsClient(Boolean allowedOAuthFlowsClient) {
        this.allowedOAuthFlowsClient = allowedOAuthFlowsClient;
    }

    @ResourceDiffProperty(updatable = true)
    public List<String> getAllowedOAuthFlows() {
        if (allowedOAuthFlows == null) {
            allowedOAuthFlows = new ArrayList<>();
        }

        return allowedOAuthFlows;
    }

    public void setAllowedOAuthFlows(List<String> allowedOAuthFlows) {
        this.allowedOAuthFlows = allowedOAuthFlows;
    }

    @ResourceDiffProperty(updatable = true)
    public List<String> getCallbackUrls() {
        return callbackUrls;
    }

    public void setCallbackUrls(List<String> callbackUrls) {
        this.callbackUrls = callbackUrls;
    }

    @ResourceDiffProperty(updatable = true)
    public String getDefaultRedirectUri() {
        return defaultRedirectUri;
    }

    public void setDefaultRedirectUri(String defaultRedirectUri) {
        this.defaultRedirectUri = defaultRedirectUri;
    }

    @ResourceDiffProperty(updatable = true)
    public List<String> getExplicitAuthFlows() {
        return explicitAuthFlows;
    }

    public void setExplicitAuthFlows(List<String> explicitAuthFlows) {
        this.explicitAuthFlows = explicitAuthFlows;
    }

    @ResourceDiffProperty(updatable = true)
    public List<String> getAllowedOAuthScopes() {
        if (allowedOAuthScopes == null) {
            allowedOAuthScopes = new ArrayList<>();
        }

        return allowedOAuthScopes;
    }

    public void setAllowedOAuthScopes(List<String> allowedOAuthScopes) {
        this.allowedOAuthScopes = allowedOAuthScopes;
    }

    public Boolean getGenerateSecret() {
        return generateSecret;
    }

    public void setGenerateSecret(Boolean generateSecret) {
        this.generateSecret = generateSecret;
    }

    @ResourceDiffProperty(updatable = true)
    public List<String> getLogoutUrls() {
        return logoutUrls;
    }

    public void setLogoutUrls(List<String> logoutUrls) {
        this.logoutUrls = logoutUrls;
    }

    @ResourceDiffProperty(updatable = true)
    public List<String> getReadAttributes() {
        return readAttributes;
    }

    public void setReadAttributes(List<String> readAttributes) {
        this.readAttributes = readAttributes;
    }

    @ResourceDiffProperty(updatable = true)
    public Integer getRefreshTokenValidity() {
        return refreshTokenValidity;
    }

    public void setRefreshTokenValidity(Integer refreshTokenValidity) {
        this.refreshTokenValidity = refreshTokenValidity;
    }

    @ResourceDiffProperty(updatable = true)
    public List<String> getSupportedIdentityProviders() {
        return supportedIdentityProviders;
    }

    public void setSupportedIdentityProviders(List<String> supportedIdentityProviders) {
        this.supportedIdentityProviders = supportedIdentityProviders;
    }

    @ResourceOutput
    public String getUserPoolClientId() {
        return userPoolClientId;
    }

    public void setUserPoolClientId(String userPoolClientId) {
        this.userPoolClientId = userPoolClientId;
    }

    @ResourceDiffProperty(updatable = true)
    public String getUserPoolClientName() {
        return userPoolClientName;
    }

    public void setUserPoolClientName(String userPoolClientName) {
        this.userPoolClientName = userPoolClientName;
    }

    public String getUserPoolClientSecret() {
        return userPoolClientSecret;
    }

    public void setUserPoolClientSecret(String userPoolClientSecret) {
        this.userPoolClientSecret = userPoolClientSecret;
    }

    public String getUserPoolId() {
        return userPoolId;
    }

    public void setUserPoolId(String userPoolId) {
        this.userPoolId = userPoolId;
    }

    @ResourceDiffProperty(updatable = true)
    public List<String> getWriteAttributes() {
        return writeAttributes;
    }

    public void setWriteAttributes(List<String> writeAttributes) {
        this.writeAttributes = writeAttributes;
    }

    @Override
    public boolean refresh() {
        try {
            CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);

            DescribeUserPoolClientResponse response = client.describeUserPoolClient(r -> r.clientId(getUserPoolClientId())
                                                                                            .userPoolId(getUserPoolId()));
            setUserPoolClientId(response.userPoolClient().clientId());
            setUserPoolId(response.userPoolClient().userPoolId());
            setUserPoolClientSecret(response.userPoolClient().clientSecret());

            return true;
        } catch (CognitoIdentityProviderException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    @Override
    public void create() {
        CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);
        CreateUserPoolClientResponse response = client.createUserPoolClient(r ->
                r.allowedOAuthFlowsUserPoolClient(getAllowedOAuthFlowsClient())
                .allowedOAuthFlowsWithStrings(getAllowedOAuthFlows())
                .allowedOAuthScopes(getAllowedOAuthScopes())
                .callbackURLs(getCallbackUrls())
                .clientName(getUserPoolClientName())
                .defaultRedirectURI(getDefaultRedirectUri())
                .explicitAuthFlowsWithStrings(getExplicitAuthFlows())
                .generateSecret(getGenerateSecret())
                .logoutURLs(getLogoutUrls())
                .readAttributes(getReadAttributes())
                .refreshTokenValidity(getRefreshTokenValidity())
                .supportedIdentityProviders(getSupportedIdentityProviders())
                .userPoolId(getUserPoolId())
                .writeAttributes(getWriteAttributes()));

        setUserPoolClientId(response.userPoolClient().clientId());
        setUserPoolClientSecret(response.userPoolClient().clientSecret());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);
        client.updateUserPoolClient(r -> r.allowedOAuthFlowsUserPoolClient(getAllowedOAuthFlowsClient())
                .allowedOAuthFlowsWithStrings(getAllowedOAuthFlows())
                .allowedOAuthScopes(getAllowedOAuthScopes())
                .callbackURLs(getCallbackUrls())
                .clientName(getUserPoolClientName())
                .defaultRedirectURI(getDefaultRedirectUri())
                .explicitAuthFlowsWithStrings(getExplicitAuthFlows())
                .logoutURLs(getLogoutUrls())
                .readAttributes(getReadAttributes())
                .refreshTokenValidity(getRefreshTokenValidity())
                .supportedIdentityProviders(getSupportedIdentityProviders())
                .userPoolId(getUserPoolId())
                .writeAttributes(getWriteAttributes()));
    }

    @Override
    public void delete() {
        CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);
        client.deleteUserPoolClient(r -> r.clientId(getUserPoolClientId())
                                            .userPoolId(getUserPoolId()));
    }

    @Override
    public String toDisplayString() {
        return "user pool client " + getUserPoolClientName();
    }
}
