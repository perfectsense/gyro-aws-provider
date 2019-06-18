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
    private String onUnauthenticatedRequest;
    private String scope;
    private String sessionCookieName;
    private Long sessionTimeout;
    private String tokenEndpoint;
    private String userInfoEndpoint;

    /**
     *  Up to 10 query parameters to include in the redirect request to the authorization endpoint. (Optional)
     */
    @Updatable
    public Map<String, String> getExtraParams() {
        return extraParams;
    }

    public void setExtraParams(Map<String, String> extraParams) {
        this.extraParams = extraParams;
    }

    /**
     *  The authorization endpoint of the IdP. (Required)
     */
    @Updatable
    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    public void setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    /**
     *  The OAuth 2.0 client identifier. (Required)
     */
    @Updatable
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     *  The OAuth 2.0 client secret. Required if creating a rule. (Required)
     */
    @Updatable
    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    /**
     *  The OIDC issuer identifier of the IdP. (Required)
     */
    @Updatable
    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /**
     *  The behavior if the use is not authenticated. Valid values are ``deny``, ``allow``, and ``authenticate``.
     *  Defaults to ``authenticate``. (Optional)
     */
    @Updatable
    public String getOnUnauthenticatedRequest() {
        if (onUnauthenticatedRequest == null) {
            onUnauthenticatedRequest = "authenticate";
        }

        return onUnauthenticatedRequest;
    }

    public void setOnUnauthenticatedRequest(String onUnauthenticatedRequest) {
        this.onUnauthenticatedRequest = onUnauthenticatedRequest;
    }

    /**
     *  The set of user claims to be request from th IdP. Defaults to ``openid``. (Optional)
     */
    @Updatable
    public String getScope() {
        if (scope == null) {
            scope = "openid";
        }

        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     *  The name of the cookie used to maintain session information. Defaults to ``AWSELBAuthSessionCookie``. (Optional)
     */
    @Updatable
    public String getSessionCookieName() {
        if (sessionCookieName == null) {
            sessionCookieName = "AWSELBAuthSessionCookie";
        }

        return sessionCookieName;
    }

    public void setSessionCookieName(String sessionCookieName) {
        this.sessionCookieName = sessionCookieName;
    }

    /**
     *  The maximum duration of the authentication session. Defaults to 604800 seconds. (Optional)
     */
    @Updatable
    public Long getSessionTimeout() {
        if (sessionTimeout == null) {
            sessionTimeout = 604800L;
        }

        return sessionTimeout;
    }

    public void setSessionTimeout(Long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    /**
     *  The token endpoint of the IdP. (Required)
     */
    @Updatable
    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    /**
     *  The user token endpoint of the IdP. (Required)
     */
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
        setIssuer(oidc.issuer());
        setOnUnauthenticatedRequest(oidc.onUnauthenticatedRequestAsString());
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
                .onUnauthenticatedRequest(getOnUnauthenticatedRequest())
                .scope(getScope())
                .sessionCookieName(getSessionCookieName())
                .sessionTimeout(getSessionTimeout())
                .tokenEndpoint(getTokenEndpoint())
                .userInfoEndpoint(getUserInfoEndpoint())
                .build();
    }
}
