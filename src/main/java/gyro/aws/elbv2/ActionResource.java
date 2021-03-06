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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.diff.Create;
import gyro.core.diff.Delete;
import gyro.core.diff.Update;
import gyro.core.resource.DiffableInternals;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Action;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ActionTypeEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.AuthenticateCognitoActionConfig;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.AuthenticateOidcActionConfig;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.FixedResponseActionConfig;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ForwardActionConfig;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RedirectActionConfig;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     action
 *         order: 1
 *         forward-action
 *             target-group-weight
 *                 target-group: $(aws::load-balancer-target-group target-group-example)
 *                 weight: 1
 *             end
 *         end
 *     end
 */
public class ActionResource extends AwsResource implements Copyable<Action> {

    private AuthenticateCognitoAction authenticateCognitoAction;
    private AuthenticateOidcAction authenticateOidcAction;
    private FixedResponseAction fixedResponseAction;
    private ForwardAction forwardAction;
    private Integer order;
    private RedirectAction redirectAction;

    /**
     *  Authentication through user pools supported by Amazon Cognito.
     *
     *  @subresource gyro.aws.elbv2.AuthenticateCognitoAction
     */
    @ConflictsWith({
        "authenticate-oidc-action",
        "fixed-response-action",
        "forward-action",
        "redirect-action" })
    @Updatable
    public AuthenticateCognitoAction getAuthenticateCognitoAction() {
        return authenticateCognitoAction;
    }

    public void setAuthenticateCognitoAction(AuthenticateCognitoAction authenticateCognitoAction) {
        this.authenticateCognitoAction = authenticateCognitoAction;
    }

    /**
     *  Authentication through provider that is OpenID Connect (OIDC) compliant.
     *
     *  @subresource gyro.aws.elbv2.AuthenticateOidcAction
     */
    @ConflictsWith({
        "authenticate-cognito-action",
        "fixed-response-action",
        "forward-action",
        "redirect-action" })
    @Updatable
    public AuthenticateOidcAction getAuthenticateOidcAction() {
        return authenticateOidcAction;
    }

    public void setAuthenticateOidcAction(AuthenticateOidcAction authenticateOidcAction) {
        this.authenticateOidcAction = authenticateOidcAction;
    }

    /**
     *  Action to support multiple ALB Target groups. If both this field and {@link TargetGroupResource}
     *  are defined, they must match and only will support a single target. This field should be used
     *  when forward weights should be used.
     *
     * @subresource gyro.aws.elbv2.ForwardAction
     */
    @ConflictsWith({
        "authenticate-cognito-action",
        "authenticate-oidc-action",
        "fixed-response-action",
        "redirect-action" })
    @Updatable
    public ForwardAction getForwardAction() {
        return forwardAction;
    }

    public void setForwardAction(ForwardAction forwardAction) {
        this.forwardAction = forwardAction;
    }

    /**
     *  Used to specify a custom response for an action.
     *
     *  @subresource gyro.aws.elbv2.FixedResponseAction
     */
    @ConflictsWith({
        "authenticate-cognito-action",
        "authenticate-oidc-action",
        "forward-action",
        "redirect-action" })
    @Updatable
    public FixedResponseAction getFixedResponseAction() {
        return fixedResponseAction;
    }

    public void setFixedResponseAction(FixedResponseAction fixedResponseAction) {
        this.fixedResponseAction = fixedResponseAction;
    }

    /**
     *  The order in which the action should take place.
     */
    @Updatable
    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    /**
     *  Redirect requests from one URL to another.
     *
     *  @subresource gyro.aws.elbv2.RedirectAction
     */
    @ConflictsWith({
        "authenticate-cognito-action",
        "authenticate-oidc-action",
        "fixed-response-action",
        "forward-action" })
    @Updatable
    public RedirectAction getRedirectAction() {
        return redirectAction;
    }

    public void setRedirectAction(RedirectAction redirectAction) {
        this.redirectAction = redirectAction;
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

        ForwardActionConfig forwardActionConfig = action.forwardConfig();
        if (action.targetGroupArn() != null || forwardActionConfig != null) {
            ForwardAction forward = newSubresource(ForwardAction.class);

            if (forwardActionConfig != null) {
                forward.copyFrom(forwardActionConfig);
            }

            forward.addTargets(action.targetGroupArn());
            setForwardAction(forward);
        }

        setOrder(action.order());
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        if (DiffableInternals.getChange(parentResource()) instanceof Create) {
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

    public String detectType() {
        if (getAuthenticateCognitoAction() != null) {
            return ActionTypeEnum.AUTHENTICATE_COGNITO.toString();
        } else if (getAuthenticateOidcAction() != null) {
            return ActionTypeEnum.AUTHENTICATE_OIDC.toString();
        } else if (getFixedResponseAction() != null) {
            return ActionTypeEnum.FIXED_RESPONSE.toString();
        } else if (getForwardAction() != null) {
            return ActionTypeEnum.FORWARD.toString();
        } else if (getRedirectAction() != null) {
            return ActionTypeEnum.REDIRECT.toString();
        } else {
            return ActionTypeEnum.UNKNOWN_TO_SDK_VERSION.toString();
        }
    }

    public Action toAction() {
        return Action.builder()
                .authenticateCognitoConfig(getAuthenticateCognitoAction() != null ? getAuthenticateCognitoAction().toCognito() : null)
                .authenticateOidcConfig(getAuthenticateOidcAction() != null ? getAuthenticateOidcAction().toOidc() : null)
                .fixedResponseConfig(getFixedResponseAction() != null ? getFixedResponseAction().toFixedAction() : null)
                .redirectConfig(getRedirectAction() != null ? getRedirectAction().toRedirect() : null)
                .forwardConfig(getForwardAction() != null ? getForwardAction().toForwardActionConfig() : null)
                .order(getOrder())
                .type(detectType())
                .build();
    }

    @Override
    public String primaryKey() {
        return String.format("%s/%s", getOrder(), detectType());
    }

    /**
     * Actions can only have a single type configured.
     */
    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getAuthenticateCognitoAction() == null && getAuthenticateOidcAction() == null
            && getFixedResponseAction() == null && getForwardAction() == null && getRedirectAction() == null) {
            errors.add(new ValidationError(
                this,
                null,
                "Exactly one of 'authenticate-cognito-action' or 'authenticate-oidc-action' or 'fixed-response-action' or 'forward-action' or 'redirect-action' is required."));
        }

        return errors;
    }
}


