package gyro.aws.elbv2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.AuthenticateOidcActionConfig;

import java.util.Map;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     action
 *         type: "authenticate-oidc"
 *
 *         authenticate-oidc-action
 *             authorization-endpoint: "https://example.com/authorization_endpoint"
 *             client-id: "client_id"
 *             client-secret: "client_secret"
 *             issuer: "https://example.com"
 *             token-endpoint: "https://example.com/token_endpoint"
 *             user-info-endpoint: "https://example.com/user_info_endpoint"
 *         end
 *     end
 */
public class AuthenticateOidcAction extends Diffable implements Copyable<AuthenticateOidcActionConfig> {

    private Map<String, String> extraParams;
    private String authorizationEndpoint;
    private String clientId;
    private String clientSecret;
    private String issuer;
    private String onAuthenticatedRequest;
    private String scope;
    private String sessionCookieName;
    private Long sessionTimeout;
    private String tokenEndpoint;
    private String userInfoEndpoint;

    @Updatable
    public Map<String, String> getExtraParams() {
        return extraParams;
    }

    public void setExtraParams(Map<String, String> extraParams) {
        this.extraParams = extraParams;
    }

    @Updatable
    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    public void setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    @Updatable
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Updatable
    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Updatable
    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    @Updatable
    public String getOnAuthenticatedRequest() {
        return onAuthenticatedRequest;
    }

    public void setOnAuthenticatedRequest(String onAuthenticatedRequest) {
        this.onAuthenticatedRequest = onAuthenticatedRequest;
    }

    @Updatable
    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Updatable
    public String getSessionCookieName() {
        return sessionCookieName;
    }

    public void setSessionCookieName(String sessionCookieName) {
        this.sessionCookieName = sessionCookieName;
    }

    @Updatable
    public Long getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(Long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    @Updatable
    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    @Updatable
    public String getUserInfoEndpoint() {
        return userInfoEndpoint;
    }

    public void setUserInfoEndpoint(String userInfoEndpoint) {
        this.userInfoEndpoint = userInfoEndpoint;
    }

    public String primaryKey() {
        return String.format("%s/%s/%s", getClientId(), getUserInfoEndpoint(), getTokenEndpoint());
    }

    @Override
    public void copyFrom(AuthenticateOidcActionConfig oidc) {
        setExtraParams(oidc.authenticationRequestExtraParams());
        setAuthorizationEndpoint(oidc.authorizationEndpoint());
        setClientId(oidc.clientId());
        setClientSecret(oidc.clientSecret());
        setIssuer(oidc.issuer());
        setOnAuthenticatedRequest(oidc.onUnauthenticatedRequestAsString());
        setScope(oidc.scope());
        setSessionCookieName(oidc.sessionCookieName());
        setSessionTimeout(oidc.sessionTimeout());
        setTokenEndpoint(oidc.tokenEndpoint());
        setUserInfoEndpoint(oidc.userInfoEndpoint());
    }

    public String toDisplayString() {
        return "authenticate oidc action";
    }

    public AuthenticateOidcActionConfig toOidc() {
        return AuthenticateOidcActionConfig.builder()
                .authenticationRequestExtraParams(getExtraParams())
                .authorizationEndpoint(getAuthorizationEndpoint())
                .clientId(getClientId())
                .clientSecret(getClientSecret())
                .issuer(getIssuer())
                .onUnauthenticatedRequest(getOnAuthenticatedRequest())
                .scope(getScope())
                .sessionCookieName(getSessionCookieName())
                .sessionTimeout(getSessionTimeout())
                .tokenEndpoint(getTokenEndpoint())
                .userInfoEndpoint(getUserInfoEndpoint())
                .build();
    }
}
