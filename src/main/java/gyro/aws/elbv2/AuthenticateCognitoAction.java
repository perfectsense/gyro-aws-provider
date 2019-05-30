package gyro.aws.elbv2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.AuthenticateCognitoActionConfig;

import java.util.Map;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     action
 *         type: "authenticate-cognito"
 *
 *         authenticate-cognito-action
 *             user-pool-arn: $(aws::authenticate-cognito-user-pool cognito | user-pool-arn)
 *             user-pool-client-id: $(aws::authenticate-cognito-user-pool-client client | user-pool-client-id)
 *             user-pool-domain: $(aws::authenticate-cognito-user-pool-domain domain | domain)
 *         end
 *     end
 *
 *
 */
public class AuthenticateCognitoAction extends Diffable implements Copyable<AuthenticateCognitoActionConfig> {

    private Map<String, String> extraParams;
    private String onUnauthenticatedRequest;
    private String scope;
    private String sessionCookieName;
    private Long sessionTimeout;
    private String userPoolArn;
    private String userPoolClientId;
    private String userPoolDomain;

    /**
     *  Up to 10 query parameters to include in the redirect request to the authorization endpoint. (Optional)
     */
    public Map<String, String> getExtraParams() {
        return extraParams;
    }

    public void setExtraParams(Map<String, String> extraParams) {
        this.extraParams = extraParams;
    }

    /**
     *  The behavior if the use is not authenticated. Valid values are ``deny``, ``allow``, and ``authenticate``.
     *  Defaults to ``authenticate``. (Optional)
     */
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
    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     *  The name of the cookie used to maintain session information. Defaults to ``AWSELBAuthSessionCookie``. (Optional)
     */
    public String getSessionCookieName() {
        return sessionCookieName;
    }

    public void setSessionCookieName(String sessionCookieName) {
        this.sessionCookieName = sessionCookieName;
    }

    /**
     *  The maximum duration of the authentication session. Defaults to 604800 seconds. (Optional)
     */
    public Long getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(Long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    /**
     *  The arn of the cognito user pool associated with the action. (Required)
     */
    public String getUserPoolArn() {
        return userPoolArn;
    }

    public void setUserPoolArn(String userPoolArn) {
        this.userPoolArn = userPoolArn;
    }

    /**
     *  The id of the cognito user pool client associated with the action. (Required)
     */
    public String getUserPoolClientId() {
        return userPoolClientId;
    }

    public void setUserPoolClientId(String userPoolClientId) {
        this.userPoolClientId = userPoolClientId;
    }

    /**
     *  The domain prefix or fully-qualified domain name of the user pool. (Required)
     */
    public String getUserPoolDomain() {
        return userPoolDomain;
    }

    public void setUserPoolDomain(String userPoolDomain) {
        this.userPoolDomain = userPoolDomain;
    }

    public String primaryKey() {
        return String.format("%s/%s/%s", getUserPoolArn(), getUserPoolClientId(), getUserPoolDomain());
    }

    @Override
    public void copyFrom(AuthenticateCognitoActionConfig cognito) {
        setExtraParams(cognito.authenticationRequestExtraParams());
        setOnUnauthenticatedRequest(cognito.onUnauthenticatedRequestAsString());
        setScope(cognito.scope());
        setSessionCookieName(cognito.sessionCookieName());
        setSessionTimeout(cognito.sessionTimeout());
        setUserPoolArn(cognito.userPoolArn());
        setUserPoolClientId(cognito.userPoolClientId());
        setUserPoolDomain(cognito.userPoolDomain());
    }

    public String toDisplayString() {
        return "authenticate cognito action";
    }

    public AuthenticateCognitoActionConfig toCognito() {
        return AuthenticateCognitoActionConfig.builder()
                .authenticationRequestExtraParams(getExtraParams())
                .onUnauthenticatedRequest(getOnUnauthenticatedRequest())
                .scope(getScope())
                .sessionCookieName(getSessionCookieName())
                .sessionTimeout(getSessionTimeout())
                .userPoolArn(getUserPoolArn())
                .userPoolClientId(getUserPoolClientId())
                .userPoolDomain(getUserPoolDomain())
                .build();
    }
}
