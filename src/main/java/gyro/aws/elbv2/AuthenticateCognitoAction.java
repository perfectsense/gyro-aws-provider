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

    public Map<String, String> getExtraParams() {
        return extraParams;
    }

    public void setExtraParams(Map<String, String> extraParams) {
        this.extraParams = extraParams;
    }

    public String getOnUnauthenticatedRequest() {
        return onUnauthenticatedRequest;
    }

    public void setOnUnauthenticatedRequest(String onUnauthenticatedRequest) {
        this.onUnauthenticatedRequest = onUnauthenticatedRequest;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getSessionCookieName() {
        return sessionCookieName;
    }

    public void setSessionCookieName(String sessionCookieName) {
        this.sessionCookieName = sessionCookieName;
    }

    public Long getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(Long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public String getUserPoolArn() {
        return userPoolArn;
    }

    public void setUserPoolArn(String userPoolArn) {
        this.userPoolArn = userPoolArn;
    }

    public String getUserPoolClientId() {
        return userPoolClientId;
    }

    public void setUserPoolClientId(String userPoolClientId) {
        this.userPoolClientId = userPoolClientId;
    }

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
