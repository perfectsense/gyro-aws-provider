package gyro.aws.elbv2;

import gyro.core.diff.Create;
import gyro.core.diff.Delete;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import gyro.core.diff.Update;
import gyro.lang.Resource;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Action;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.AuthenticateCognitoActionConfig;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.AuthenticateOidcActionConfig;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.FixedResponseActionConfig;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RedirectActionConfig;

import java.util.Set;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     action
 *         target-group-arn: $(aws::target-group target-group-example | target-group-arn)
 *         type: "forward"
 *     end
 */

@ResourceName(parent = "alb-listener", value = "default-action")
@ResourceName(parent = "alb-listener-rule", value = "action")
public class ActionResource extends NetworkActionResource {

    private AuthenticateCognitoAction cognitoAction;
    private AuthenticateOidcAction oidc;
    private FixedResponseAction fixedResponseAction;
    private Integer order;
    private RedirectAction redirectAction;
    private String targetGroupArn;
    private String type;

    public ActionResource() {

    }

    public ActionResource(Action action) {

        AuthenticateCognitoActionConfig cognitoConfig = action.authenticateCognitoConfig();
        if (cognitoConfig != null) {
            AuthenticateCognitoAction cognito = new AuthenticateCognitoAction(cognitoConfig);
            setCognitoAction(cognito);
        }

        AuthenticateOidcActionConfig oidcConfig = action.authenticateOidcConfig();
        if (oidcConfig != null) {
            AuthenticateOidcAction oidc = new AuthenticateOidcAction(oidcConfig);
            setOidc(oidc);
        }

        FixedResponseActionConfig fixedConfig = action.fixedResponseConfig();
        if (fixedConfig != null) {
            FixedResponseAction fixed = new FixedResponseAction(fixedConfig);
            setFixedResponse(fixed);
        }

        RedirectActionConfig redirectConfig = action.redirectConfig();
        if (redirectConfig != null) {
            RedirectAction redirect = new RedirectAction(redirectConfig);
            setRedirect(redirect);
        }

        setOrder(action.order());
        setTargetGroupArn(action.targetGroupArn());
        setType(action.typeAsString());
    }

    /**
     *  Authentication through user pools supported by Amazon Cognito (Optional)
     */
    @ResourceDiffProperty(updatable = true)
    public AuthenticateCognitoAction getCognitoAction() {
        return cognitoAction;
    }

    public void setCognitoAction(AuthenticateCognitoAction cognitoAction) {
        this.cognitoAction = cognitoAction;
    }

    /**
     *  Authentication through provider that is OpenID Connect (OIDC) compliant (Optional)
     */
    @ResourceDiffProperty(updatable = true)
    public AuthenticateOidcAction getOidc() {
        return oidc;
    }

    public void setOidc(AuthenticateOidcAction oidc) {
        this.oidc = oidc;
    }

    /**
     *  Used to specify a custom response for an action  (Optional)
     */
    @ResourceDiffProperty(updatable = true)
    public FixedResponseAction getFixedResponse() {
        return fixedResponseAction;
    }

    public void setFixedResponse(FixedResponseAction fixedResponseAction) {
        this.fixedResponseAction = fixedResponseAction;
    }

    /**
     *  The order in which the action should take place (Optional)
     */
    @ResourceDiffProperty(updatable = true)
    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    /**
     *  Redirect requests from one URL to another (Optional)
     */
    @ResourceDiffProperty(updatable = true)
    public RedirectAction getRedirect() {
        return redirectAction;
    }

    public void setRedirect(RedirectAction redirectAction) {
        this.redirectAction = redirectAction;
    }

    /**
     *  The target group arn that this action is associated with  (Optional)
     */
    public String getTargetGroupArn() {
        return targetGroupArn;
    }

    public void setTargetGroupArn(String targetGroupArn) {
        this.targetGroupArn = targetGroupArn;
    }

    /**
     *  The type of action to perform  (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String primaryKey() {
        return String.format("%d %s", getOrder(), getType());
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void create() {
        if (parentResource().change() instanceof Create) {
            return;
        }

        if (parentResource() instanceof ApplicationLoadBalancerListenerRuleResource) {
            ApplicationLoadBalancerListenerRuleResource parent = (ApplicationLoadBalancerListenerRuleResource) parentResource();
            parent.createAction(this);
        } else {
            ApplicationLoadBalancerListenerResource parent = (ApplicationLoadBalancerListenerResource) parentResource();
            parent.createDefaultAction(this);
        }
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        if (parentResource().change() instanceof Update) {
            return;
        }

        if (parentResource() instanceof ApplicationLoadBalancerListenerRuleResource) {
            ApplicationLoadBalancerListenerRuleResource parent = (ApplicationLoadBalancerListenerRuleResource) parentResource();
            parent.updateAction();
        } else {
            ApplicationLoadBalancerListenerResource parent = (ApplicationLoadBalancerListenerResource) parentResource();
            parent.updateDefaultAction();
        }
    }

    @Override
    public void delete() {
        if (parentResource().change() instanceof Delete) {
            return;
        }

        if (parentResource() instanceof ApplicationLoadBalancerListenerRuleResource) {
            ApplicationLoadBalancerListenerRuleResource parent = (ApplicationLoadBalancerListenerRuleResource) parentResource();
            parent.deleteAction(this);
        } else {
            ApplicationLoadBalancerListenerResource parent = (ApplicationLoadBalancerListenerResource) parentResource();
            parent.deleteDefaultAction(this);
        }
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        if (parentResource() instanceof ListenerResource) {
            sb.append("default action");
        } else {
            sb.append("rule action - type: " + getType());
        }

        return sb.toString();
    }

    public Action toAction() {
        return Action.builder()
                .authenticateCognitoConfig(getCognitoAction() != null ? getCognitoAction().toCognito() : null)
                .authenticateOidcConfig(getOidc() != null ? getOidc().toOidc() : null)
                .fixedResponseConfig(getFixedResponse() != null ? getFixedResponse().toFixedAction() : null)
                .redirectConfig(getRedirect() != null ? getRedirect().toRedirect() : null)
                .order(getOrder())
                .targetGroupArn(getTargetGroupArn())
                .type(getType())
                .build();
    }
}


