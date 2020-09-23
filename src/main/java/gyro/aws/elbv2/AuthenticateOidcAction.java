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
     *  Up to 10 query parameters to include in the redirect request to the authorization endpoint.
     */
    @Updatable
    public Map<String, String> getExtraParams() {
        return extraParams;
    }

    public void setExtraParams(Map<String, String> extraParams) {
        this.extraParams = extraParams;
    }

    /**
     *  The authorization endpoint of the IdP.
     */
    @Updatable
    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    public void setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    /**
     *  The OAuth 2.0 client identifier.
     */
    @Updatable
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     *  The OAuth 2.0 client secret. Required if creating a rule.
     */
    @Updatable
    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    /**
     *  The OIDC issuer identifier of the IdP.
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
     *  Defaults to ``authenticate``.
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
     *  The set of user claims to be request from th IdP. Defaults to ``openid``.
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
     *  The name of the cookie used to maintain session information. Defaults to ``AWSELBAuthSessionCookie``.
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
     *  The maximum duration of the authentication session. Defaults to 604800 seconds.
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
     *  The token endpoint of the IdP.
     */
    @Updatable
    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    /**
     *  The user token endpoint of the IdP.
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
