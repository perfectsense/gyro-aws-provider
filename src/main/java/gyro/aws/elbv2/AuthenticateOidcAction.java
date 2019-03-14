package gyro.aws.elbv2;

import gyro.core.diff.Diffable;
import gyro.core.diff.ResourceDiffProperty;

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
public class AuthenticateOidcAction extends Diffable {

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

    public AuthenticateOidcAction() {

    }

    public AuthenticateOidcAction(AuthenticateOidcActionConfig oidc) {
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

    @ResourceDiffProperty(updatable = true)
    public Map<String, String> getExtraParams() {
        return extraParams;
    }

    public void setExtraParams(Map<String, String> extraParams) {
        this.extraParams = extraParams;
    }

    @ResourceDiffProperty(updatable = true)
    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    public void setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    @ResourceDiffProperty(updatable = true)
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @ResourceDiffProperty(updatable = true)
    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @ResourceDiffProperty(updatable = true)
    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    @ResourceDiffProperty(updatable = true)
    public String getOnAuthenticatedRequest() {
        return onAuthenticatedRequest;
    }

    public void setOnAuthenticatedRequest(String onAuthenticatedRequest) {
        this.onAuthenticatedRequest = onAuthenticatedRequest;
    }

    @ResourceDiffProperty(updatable = true)
    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @ResourceDiffProperty(updatable = true)
    public String getSessionCookieName() {
        return sessionCookieName;
    }

    public void setSessionCookieName(String sessionCookieName) {
        this.sessionCookieName = sessionCookieName;
    }

    @ResourceDiffProperty(updatable = true)
    public Long getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(Long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    @ResourceDiffProperty(updatable = true)
    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    @ResourceDiffProperty(updatable = true)
    public String getUserInfoEndpoint() {
        return userInfoEndpoint;
    }

    public void setUserInfoEndpoint(String userInfoEndpoint) {
        this.userInfoEndpoint = userInfoEndpoint;
    }

    public String primaryKey() {
        return String.format("%s/%s/%s", getClientId(), getUserInfoEndpoint(), getTokenEndpoint());
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
