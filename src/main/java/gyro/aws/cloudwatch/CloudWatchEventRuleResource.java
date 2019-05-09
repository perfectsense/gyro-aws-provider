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

import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ResourceType("event-rule")
public class CloudWatchEventRuleResource extends AwsResource {

    private String description;
    private String eventPattern;
    private String eventPatternPath;
    private String managedBy;
    private String roleArn;
    private String ruleArn;
    private String ruleName;
    private String scheduleExpression;
    private Boolean scheduleEvent;
    private String state;
    private List<CloudWatchRuleTargetResource> target;

    /**
     * The name of the rule associated that matches incoming events. (Required)
     */
    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    /**
     * The description of the rule associated with the event.(Optional)
     */
    @ResourceUpdatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The iam role arn that gives permissions to invoke actions on the targets.
     */
    @ResourceUpdatable
    public String getRoleArn() {
        return roleArn;
    }

    public void setRoleArn(String roleArn) {
        this.roleArn = roleArn;
    }

    /**
     * The Boolean value that indicates if the rule is a scheduled type.
     */
    public Boolean getScheduleEvent() {
        if (scheduleEvent == null) {
            scheduleEvent = false;
        }
        return scheduleEvent;
    }

    public void setScheduleEvent(Boolean scheduleEvent) {
        this.scheduleEvent = scheduleEvent;
    }

    /**
     * The event pattern which activates target actions when matched.
     */
    @ResourceUpdatable
    public String getEventPattern() {
        return eventPattern;
    }

    public void setEventPattern(String eventPattern) {
        this.eventPattern = eventPattern;
    }

    /**
     * The location to the json file which contains the event pattern.
     */
    @ResourceUpdatable
    public String getEventPatternPath() {
        return eventPatternPath;
    }

    public void setEventPatternPath(String eventPatternPath) {
        this.eventPatternPath = eventPatternPath;
    }

    /**
     * The schedule expression that triggers an automated rule action.
     *
     * See `AWS Services Schedule Expressions <https://docs.aws.amazon.com/AmazonCloudWatch/latest/events/ScheduledEvents.html/>`_.
     */
    @ResourceUpdatable
    public String getScheduleExpression() {
        return scheduleExpression;
    }

    public void setScheduleExpression(String scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
    }

    /**
     * This value indicates if the rule is enabled to invoke target actions.
     *
     * Valid values are ``ENABLED`` and ``DISABLED``
     */
    @ResourceUpdatable
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * The list of target resources which can be invoked when an event pattern is matched or scheduled
     *
     * See `AWS Services CloudWatch events <https://docs.aws.amazon.com/AmazonCloudWatch/latest/events/WhatIsCloudWatchEvents.html/>`_.
     *
     * @subresource gyro.aws.cloudwatch.CloudWatchRuleTargetResource
     */
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

    /**
     * If role arn is given, this field specifies which aws service can manage the rules.
     */
    @ResourceUpdatable
    public String getManagedBy() {
        return managedBy;
    }

    public void setManagedBy(String managedBy) {
        this.managedBy = managedBy;
    }

    public String getRuleArn() {
        return ruleArn;
    }

    public void setRuleArn(String ruleArn) {
        this.ruleArn = ruleArn;
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

    private void loadRule(Rule rule) {
        setRuleArn(rule.arn());
        setRuleName(rule.name());
        setDescription(rule.description());
        setEventPattern(rule.eventPattern());
        setScheduleExpression(rule.scheduleExpression());
        setState(rule.state().toString());
        setRoleArn(rule.roleArn());
        setManagedBy(rule.managedBy());

        if (rule.scheduleExpression() != null) {
            setScheduleEvent(true);
        }
    }

    private void saveRule(CloudWatchEventsClient client) {

        if (getScheduleEvent()) {
            PutRuleResponse ruleResponse = client.putRule(
                    r -> r.name(getRuleName())
                            .description(getDescription())
                            .scheduleExpression(getScheduleExpression())
                            .roleArn(getRoleArn())
                            .state(getState())
            );
            setRuleArn(ruleResponse.ruleArn());
        } else {

            if (!getEventPatternPath().isEmpty()) {
                setEventPatternFromPath();
            }

            PutRuleResponse ruleResponse = client.putRule(
                    r -> r.name(getRuleName())
                            .description(getDescription())
                            .eventPattern(getEventPattern())
                            .roleArn(getRoleArn())
                            .state(getState())
            );
            setRuleArn(ruleResponse.ruleArn());
        }
    }

    private void setEventPatternFromPath() {
        try (InputStream input = openInput(getEventPatternPath())) {
            setEventPattern(IoUtils.toUtf8String(input));

        } catch (IOException ioex) {
            throw new GyroException(MessageFormat
                    .format("Event Pattern - {0} file error. Unable to read event pattern from path [{1}]", getRuleName(), getEventPatternPath()));
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
}
