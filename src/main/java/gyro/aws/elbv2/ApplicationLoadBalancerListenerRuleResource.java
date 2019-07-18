package gyro.aws.elbv2;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;

import gyro.core.scope.State;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Action;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateRuleResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeRulesResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RuleCondition;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RuleNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::application-load-balancer-listener-rule listener-rule-example
 *         alb-listener: $(aws::application-load-balancer-listener listener-example)
 *         priority: "1"
 *
 *         action
 *             target-group: $(aws::load-balancer-target-group target-group-example | target-group-arn)
 *             type: "forward"
 *         end
 *
 *         condition
 *             field: "path-pattern"
 *             value: ["/applespice"]
 *         end
 *
 *         condition
 *             field: "host-header"
 *             value: ["www.example.net"]
 *         end
 *     end
 */
@Type("application-load-balancer-listener-rule")
public class ApplicationLoadBalancerListenerRuleResource extends AwsResource implements Copyable<Rule> {

    private List<ActionResource> action;
    private List<ConditionResource> condition;
    private ApplicationLoadBalancerListenerResource albListener;
    private Integer priority;
    private String arn;

    /**
     *  List of actions associated with the rule. (Required)
     *
     *  @subresource gyro.aws.elbv2.ActionResource
     */
    @Updatable
    public List<ActionResource> getAction() {
        if (action == null) {
            action = new ArrayList<>();
        }

        return action;
    }

    public void setAction(List<ActionResource> action) {
        this.action = action;
    }

    /**
     *  List of conditions associated with the rule. (Required)
     *
     *  @subresource gyro.aws.elbv2.ConditionResource
     */
    @Updatable
    public List<ConditionResource> getCondition() {
        if (condition == null) {
            condition = new ArrayList<>();
        }

        return condition;
    }

    public void setCondition(List<ConditionResource> condition) {
        this.condition = condition;
    }

    /**
     *  The alb associated with this listener rule. (Required)
     */
    public ApplicationLoadBalancerListenerResource getAlbListener() {
        return albListener;
    }

    public void setAlbListener(ApplicationLoadBalancerListenerResource albListener) {
        this.albListener = albListener;
    }

    /**
     *  Priority of the rule. Valid values between ``1`` and ``50000``. No two rules can have the same priority. ``-1`` points to the default rule. (Required)
     */
    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }


    /**
     *  The arn of the rule.
     */
    @Id
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public void copyFrom(Rule rule) {
        setAction(fromActions(rule.actions()));
        setCondition(fromCondition(rule.conditions()));
        setArn(rule.ruleArn());

        if (rule.priority().equalsIgnoreCase("default")) {
            setPriority(-1);
        } else {
            setPriority(Integer.valueOf(rule.priority()));
        }
    }

    @Override
    public boolean refresh() {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        try {
            DescribeRulesResponse response = client.describeRules(r -> r.ruleArns(getArn()));

            Rule rule = response.rules().get(0);

            this.copyFrom(rule);

            return true;

        } catch (RuleNotFoundException ex) {
            return false;
        }
    }

    @Override
    public void create(GyroUI ui, State state) {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        CreateRuleResponse response = client.createRule(r -> r.actions(toActions())
                .conditions(toConditions())
                .listenerArn(getAlbListener().getArn())
                .priority(getPriority()));

        setArn(response.rules().get(0).ruleArn());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        client.modifyRule(r -> r.actions(toActions())
                                .conditions(toConditions())
                                .ruleArn(getArn()));
    }

    @Override
    public void delete(GyroUI ui, State state) {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        client.deleteRule(r -> r.ruleArn(getArn()));
    }

    private List<Action> toActions() {
        List<Action> actionsList = new ArrayList<>();

        for (ActionResource act : getAction()) {
            actionsList.add(act.toAction());
        }

        return actionsList;
    }

    private List<RuleCondition> toConditions() {
        List<RuleCondition> conditionList = new ArrayList<>();

        for (ConditionResource ruleCondition : getCondition()) {
            conditionList.add(ruleCondition.toCondition());
        }

        return conditionList;
    }

    private List<ActionResource> fromActions(List<Action> actionList) {
        List<ActionResource> actions = new ArrayList<>();

        for (Action action : actionList) {
            ActionResource actionResource = newSubresource(ActionResource.class);
            actionResource.copyFrom(action);
            actions.add(actionResource);
        }
        return actions;
    }

    private List<ConditionResource> fromCondition(List<RuleCondition> conditionsList) {
        List<ConditionResource> conditions = new ArrayList<>();

        for (RuleCondition rc : conditionsList) {
            ConditionResource condition = new ConditionResource();
            condition.copyFrom(rc);
            conditions.add(condition);
        }

        return conditions;
    }

    public void createAction(ActionResource action) {
        if (!getAction().contains(action)) {
            getAction().add(action);
        }

        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        client.modifyRule(r -> r.actions(toActions())
                .conditions(toConditions())
                .ruleArn(getArn()));
    }

    public void updateAction() {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        client.modifyRule(r -> r.actions(toActions())
                .conditions(toConditions())
                .ruleArn(getArn()));
    }

    public void deleteAction(ActionResource action) {
        getAction().remove(action);

        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        client.modifyRule(r -> r.actions(toActions())
                .conditions(toConditions())
                .ruleArn(getArn()));
    }

    public void createCondition(ConditionResource condition) {
        if (!getCondition().contains(condition)) {
            getCondition().add(condition);
        }

        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        client.modifyRule(r -> r.actions(toActions())
                .conditions(toConditions())
                .ruleArn(getArn()));
    }

    public void updateCondition() {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        client.modifyRule(r -> r.actions(toActions())
                .conditions(toConditions())
                .ruleArn(getArn()));
    }

    public void deleteCondition(ConditionResource condition) {
        getCondition().remove(condition);

        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        client.modifyRule(r -> r.actions(toActions())
                .conditions(toConditions())
                .ruleArn(getArn()));
    }
}
