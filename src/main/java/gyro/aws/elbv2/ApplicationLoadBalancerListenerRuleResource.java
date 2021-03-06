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
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Range;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Action;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateRuleResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeRulesResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RuleCondition;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RuleNotFoundException;

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
 *             forward-action
 *                 target-group-weight
 *                     target-group: $(aws::load-balancer-target-group target-group-example)
 *                     weight: 1
 *                 end
 *             end
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
     *  List of actions associated with the rule.
     *
     *  @subresource gyro.aws.elbv2.ActionResource
     */
    @Required
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
     *  List of conditions associated with the rule.
     *
     *  @subresource gyro.aws.elbv2.ConditionResource
     */
    @Required
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
     *  The alb associated with this listener rule.
     */
    @Required
    public ApplicationLoadBalancerListenerResource getAlbListener() {
        return albListener;
    }

    public void setAlbListener(ApplicationLoadBalancerListenerResource albListener) {
        this.albListener = albListener;
    }

    /**
     *  Priority of the rule. No two rules can have the same priority. ``-1`` points to the default rule.
     */
    @Required
    @Range(min = 1, max = 50000)
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

            List<String> conditionsWithFields = new ArrayList<>();
            for (ConditionResource c : getCondition()) {
                if (c.getField() != null) {
                    conditionsWithFields.add(c.getField());
                }
            }

            this.copyFrom(rule);

            for (ConditionResource c : getCondition()) {
                if (conditionsWithFields.contains(c.getField())) {
                    c.resetConfigs();
                } else {
                    c.setField(null);
                    c.setValue(null);
                }
            }

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
