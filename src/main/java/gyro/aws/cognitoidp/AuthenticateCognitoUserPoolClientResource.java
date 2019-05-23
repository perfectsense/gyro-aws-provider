package gyro.aws.cognitoidp;

import gyro.aws.AwsResource;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
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
@Type("authenticate-cognito-user-pool-client")
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

    @Updatable
    public Boolean getAllowedOAuthFlowsClient() {
        return allowedOAuthFlowsClient;
    }

    public void setAllowedOAuthFlowsClient(Boolean allowedOAuthFlowsClient) {
        this.allowedOAuthFlowsClient = allowedOAuthFlowsClient;
    }

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

    @Updatable
    public List<String> getCallbackUrls() {
        return callbackUrls;
    }

    public void setCallbackUrls(List<String> callbackUrls) {
        this.callbackUrls = callbackUrls;
    }

    @Updatable
    public String getDefaultRedirectUri() {
        return defaultRedirectUri;
    }

    public void setDefaultRedirectUri(String defaultRedirectUri) {
        this.defaultRedirectUri = defaultRedirectUri;
    }

    @Updatable
    public List<String> getExplicitAuthFlows() {
        return explicitAuthFlows;
    }

    public void setExplicitAuthFlows(List<String> explicitAuthFlows) {
        this.explicitAuthFlows = explicitAuthFlows;
    }

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

    public Boolean getGenerateSecret() {
        return generateSecret;
    }

    public void setGenerateSecret(Boolean generateSecret) {
        this.generateSecret = generateSecret;
    }

    @Updatable
    public List<String> getLogoutUrls() {
        return logoutUrls;
    }

    public void setLogoutUrls(List<String> logoutUrls) {
        this.logoutUrls = logoutUrls;
    }

    @Updatable
    public List<String> getReadAttributes() {
        return readAttributes;
    }

    public void setReadAttributes(List<String> readAttributes) {
        this.readAttributes = readAttributes;
    }

    @Updatable
    public Integer getRefreshTokenValidity() {
        return refreshTokenValidity;
    }

    public void setRefreshTokenValidity(Integer refreshTokenValidity) {
        this.refreshTokenValidity = refreshTokenValidity;
    }

    @Updatable
    public List<String> getSupportedIdentityProviders() {
        return supportedIdentityProviders;
    }

    public void setSupportedIdentityProviders(List<String> supportedIdentityProviders) {
        this.supportedIdentityProviders = supportedIdentityProviders;
    }

    @Output
    public String getUserPoolClientId() {
        return userPoolClientId;
    }

    public void setUserPoolClientId(String userPoolClientId) {
        this.userPoolClientId = userPoolClientId;
    }

    @Updatable
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

    @Updatable
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
    public void update(Resource current, Set<String> changedFieldNames) {
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
