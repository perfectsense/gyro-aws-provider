package gyro.aws.elbv2;

import gyro.aws.Copyable;
import gyro.aws.cognitoidp.UserPoolClientResource;
import gyro.aws.cognitoidp.UserPoolDomainResource;
import gyro.aws.cognitoidp.UserPoolResource;
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
 *             user-pool: $(aws::authenticate-cognito-user-pool cognito)
 *             user-pool-client: $(aws::authenticate-cognito-user-pool-client client)
 *             user-pool-domain: $(aws::authenticate-cognito-user-pool-domain domain)
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
    private UserPoolResource userPool;
    private UserPoolClientResource userPoolClient;
    private UserPoolDomainResource userPoolDomain;

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
     *  The cognito user pool resource associated with the action. (Required)
     */
    public UserPoolResource getUserPool() {
        return userPool;
    }

    public void setUserPool(UserPoolResource userPool) {
        this.userPool = userPool;
    }

    /**
     *  The cognito user pool client resource associated with the action. (Required)
     */
    public UserPoolClientResource getUserPoolClient() {
        return userPoolClient;
    }

    public void setUserPoolClient(UserPoolClientResource userPoolClient) {
        this.userPoolClient = userPoolClient;
    }

    /**
     *  The cognito user pool domain resource associated with the the user pool. (Required)
     */
    public UserPoolDomainResource getUserPoolDomain() {
        return userPoolDomain;
    }

    public void setUserPoolDomain(UserPoolDomainResource userPoolDomain) {
        this.userPoolDomain = userPoolDomain;
    }

    public String primaryKey() {
        return String.format("%s/%s/%s", getUserPool(), getUserPoolClient(), getUserPoolDomain());
    }

    @Override
    public void copyFrom(AuthenticateCognitoActionConfig cognito) {
        setExtraParams(cognito.authenticationRequestExtraParams());
        setOnUnauthenticatedRequest(cognito.onUnauthenticatedRequestAsString());
        setScope(cognito.scope());
        setSessionCookieName(cognito.sessionCookieName());
        setSessionTimeout(cognito.sessionTimeout());
        setUserPool(findById(UserPoolResource.class, cognito.userPoolArn()));
        setUserPoolClient(findById(UserPoolClientResource.class, cognito.userPoolClientId()));
        setUserPoolDomain(findById(UserPoolDomainResource.class, cognito.userPoolDomain()));
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
                .userPoolArn(getUserPool().getArn())
                .userPoolClientId(getUserPoolClient().getId())
                .userPoolDomain(getUserPoolDomain().getDomain())
                .build();
    }
}
