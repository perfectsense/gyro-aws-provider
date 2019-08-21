package gyro.aws.elbv2;

import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.diff.Create;
import gyro.core.diff.Delete;
import gyro.core.resource.DiffableInternals;
import gyro.core.resource.Resource;
import gyro.core.diff.Update;
import gyro.core.resource.Updatable;

import gyro.core.scope.State;
import org.apache.commons.lang.NotImplementedException;
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
 *         target-group: $(aws::load-balancer-target-group target-group-example)
 *         type: "forward"
 *     end
 */
public class ActionResource extends NetworkActionResource implements Copyable<Action> {

    private AuthenticateCognitoAction authenticateCognitoAction;
    private AuthenticateOidcAction authenticateOidcAction;
    private FixedResponseAction fixedResponseAction;
    private Integer order;
    private RedirectAction redirectAction;
    private TargetGroupResource targetGroup;
    private String type;

    /**
     *  Authentication through user pools supported by Amazon Cognito. (Optional)
     */
    @Updatable
    public AuthenticateCognitoAction getAuthenticateCognitoAction() {
        return authenticateCognitoAction;
    }

    public void setAuthenticateCognitoAction(AuthenticateCognitoAction authenticateCognitoAction) {
        this.authenticateCognitoAction = authenticateCognitoAction;
    }

    /**
     *  Authentication through provider that is OpenID Connect (OIDC) compliant. (Optional)
     */
    @Updatable
    public AuthenticateOidcAction getAuthenticateOidcAction() {
        return authenticateOidcAction;
    }

    public void setAuthenticateOidcAction(AuthenticateOidcAction authenticateOidcAction) {
        this.authenticateOidcAction = authenticateOidcAction;
    }

    /**
     *  Used to specify a custom response for an action. (Optional)
     */
    @Updatable
    public FixedResponseAction getFixedResponseAction() {
        return fixedResponseAction;
    }

    public void setFixedResponseAction(FixedResponseAction fixedResponseAction) {
        this.fixedResponseAction = fixedResponseAction;
    }

    /**
     *  The order in which the action should take place. (Optional)
     */
    @Updatable
    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    /**
     *  Redirect requests from one URL to another. (Optional)
     */
    @Updatable
    public RedirectAction getRedirectAction() {
        return redirectAction;
    }

    public void setRedirectAction(RedirectAction redirectAction) {
        this.redirectAction = redirectAction;
    }

    /**
     *  The target group that this action is associated with. (Required)
     */
    public TargetGroupResource getTargetGroup() {
        return targetGroup;
    }

    public void setTargetGroup(TargetGroupResource targetGroup) {
        this.targetGroup = targetGroup;
    }

    /**
     *  The type of action to perform. (Required)
     */
    @Updatable
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
    public void copyFrom(Action action) {
        AuthenticateCognitoActionConfig cognitoConfig = action.authenticateCognitoConfig();
        if (cognitoConfig != null) {
            AuthenticateCognitoAction cognito = newSubresource(AuthenticateCognitoAction.class);
            cognito.copyFrom(cognitoConfig);
            setAuthenticateCognitoAction(cognito);
        }

        AuthenticateOidcActionConfig oidcConfig = action.authenticateOidcConfig();
        if (oidcConfig != null) {
            AuthenticateOidcAction oidc = newSubresource(AuthenticateOidcAction.class);
            oidc.copyFrom(oidcConfig);
            setAuthenticateOidcAction(oidc);
        }

        FixedResponseActionConfig fixedConfig = action.fixedResponseConfig();
        if (fixedConfig != null) {
            FixedResponseAction fixed = newSubresource(FixedResponseAction.class);
            fixed.copyFrom(fixedConfig);
            setFixedResponseAction(fixed);
        }

        RedirectActionConfig redirectConfig = action.redirectConfig();
        if (redirectConfig != null) {
            RedirectAction redirect = newSubresource(RedirectAction.class);
            redirect.copyFrom(redirectConfig);
            setRedirectAction(redirect);
        }

        setOrder(action.order());
        setTargetGroup(action.targetGroupArn() != null ? parentResource().findById(TargetGroupResource.class, action.targetGroupArn()) : null);
        setType(action.typeAsString());
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        throw new NotImplementedException();
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        if (DiffableInternals.getChange(parentResource()) instanceof Update) {
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
    public void delete(GyroUI ui, State state) {
        if (DiffableInternals.getChange(parentResource()) instanceof Delete) {
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

    public Action toAction() {
        return Action.builder()
                .authenticateCognitoConfig(getAuthenticateCognitoAction() != null ? getAuthenticateCognitoAction().toCognito() : null)
                .authenticateOidcConfig(getAuthenticateOidcAction() != null ? getAuthenticateOidcAction().toOidc() : null)
                .fixedResponseConfig(getFixedResponseAction() != null ? getFixedResponseAction().toFixedAction() : null)
                .redirectConfig(getRedirectAction() != null ? getRedirectAction().toRedirect() : null)
                .order(getOrder())
                .targetGroupArn(getTargetGroup() != null ? getTargetGroup().getArn() : null)
                .type(getType())
                .build();
    }
}


