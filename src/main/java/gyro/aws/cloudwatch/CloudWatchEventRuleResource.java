package gyro.aws.cloudwatch;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.core.GyroException;
import gyro.core.resource.Resource;

import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceUpdatable;
import software.amazon.awssdk.services.cloudwatchevents.CloudWatchEventsClient;
import software.amazon.awssdk.services.cloudwatchevents.model.CloudWatchEventsException;
import software.amazon.awssdk.services.cloudwatchevents.model.ListRulesResponse;
import software.amazon.awssdk.services.cloudwatchevents.model.ListTargetsByRuleResponse;
import software.amazon.awssdk.services.cloudwatchevents.model.PutRuleResponse;
import software.amazon.awssdk.services.cloudwatchevents.model.Rule;
import software.amazon.awssdk.services.cloudwatchevents.model.Target;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ResourceType("event-rule")
public class CloudWatchEventRuleResource extends AwsResource {

    private String ruleName;
    private String description;
    private String eventPattern;
    private String roleArn;
    private String scheduleExpression;
    private String state;
    private String ruleArn;
    private String managedBy;
    private List<CloudWatchRuleTargetResource> target;

    @ResourceUpdatable
    public List<CloudWatchRuleTargetResource> getTarget() {
        if (target == null) {
            target = new ArrayList<>();
        }
        return target;
    }

    public void setTarget(List<CloudWatchRuleTargetResource> target) {
        this.target = target;
    }

    public String getRuleArn() {
        return ruleArn;
    }

    public void setRuleArn(String ruleArn) {
        this.ruleArn = ruleArn;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    @ResourceUpdatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ResourceUpdatable
    public String getEventPattern() {
        return eventPattern;
    }

    public void setEventPattern(String eventPattern) {
        this.eventPattern = eventPattern;
    }

    @ResourceUpdatable
    public String getRoleArn() {
        return roleArn;
    }

    public void setRoleArn(String roleArn) {
        this.roleArn = roleArn;
    }

    @ResourceUpdatable
    public String getScheduleExpression() {
        return scheduleExpression;
    }

    public void setScheduleExpression(String scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
    }

    @ResourceUpdatable
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @ResourceUpdatable
    public String getManagedBy() {
        return managedBy;
    }

    public void setManagedBy(String managedBy) {
        this.managedBy = managedBy;
    }

    @Override
    public boolean refresh() {
        CloudWatchEventsClient client = createClient(CloudWatchEventsClient.class);

        Rule rule = getRule(client);

        if (rule == null) {
            return false;
        }

        loadRule(rule);

        List<Target> targets = getTarget(client, rule);

        getTarget().clear();
        if (targets != null) {
            for (Target target : targets) {
                CloudWatchRuleTargetResource targetResource = new CloudWatchRuleTargetResource(target);
                getTarget().add(targetResource);
            }
        }

        return true;
    }

    @Override
    public void create() {
        CloudWatchEventsClient client = createClient(CloudWatchEventsClient.class);

        saveRule(client);

    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        CloudWatchEventsClient client = createClient(CloudWatchEventsClient.class);

        saveRule(client);
    }

    @Override
    public void delete() {
        CloudWatchEventsClient client = createClient(CloudWatchEventsClient.class);

        client.deleteRule(d -> d.force(true).name(getRuleName()));
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("event ");
        sb.append("rule");

        if (!ObjectUtils.isBlank(getRuleName())) {
            sb.append(" : ").append(getRuleName());
        }

        return sb.toString();
    }

    private void saveRule(CloudWatchEventsClient client) {
        PutRuleResponse ruleResponse = client.putRule(
            r -> r.name(getRuleName())
            .description(getDescription())
            .scheduleExpression(getScheduleExpression())
            .eventPattern(getEventPattern())
            .roleArn(getRoleArn())
            .state(getState())
        );
        setRuleArn(ruleResponse.ruleArn());
    }

    private Rule getRule(CloudWatchEventsClient client) {

        if (ObjectUtils.isBlank(getRuleName())) {
            throw new GyroException("rule-name is missing, unable to load event rule.");
        }

        ListRulesResponse listRulesResponse = client.listRules(r -> r.namePrefix(getRuleName()));

        try {
            if (listRulesResponse.rules().isEmpty()) {
                return null;
            }

            return listRulesResponse.rules().get(0);

        } catch (CloudWatchEventsException ex) {
            if (ex.getLocalizedMessage().contains("does not exist")) {
                return null;
            }

            throw ex;
        }
    }

    private List<Target> getTarget(CloudWatchEventsClient client, Rule rule) {

        if (getTarget().isEmpty()) {
            throw new GyroException("No targets presents, unable to load rule target.");
        }


        ListTargetsByRuleResponse listTargetResponse = client.listTargetsByRule(r -> r.rule(rule.name()));

        try {
            if (listTargetResponse.targets().isEmpty()) {
                return null;
            }
            return listTargetResponse.targets();

        } catch(CloudWatchEventsException ex) {

            if (ex.getLocalizedMessage().contains("does not exist")) {
                return null;
            }

            throw ex;
        }
    }

    private void loadRule(Rule rule) {
        setRuleArn(rule.arn());
        setRuleName(rule.name());
        setDescription(rule.description());
        setEventPattern(rule.eventPattern());
        setScheduleExpression(rule.scheduleExpression());
        setState(rule.state().toString());
        setRoleArn(rule.roleArn());
        setManagedBy(rule.managedBy());
    }
}
