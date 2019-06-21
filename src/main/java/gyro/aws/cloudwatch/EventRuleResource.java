package gyro.aws.cloudwatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.RoleResource;
import gyro.core.GyroException;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;

import gyro.core.Type;
import gyro.core.resource.Updatable;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Create a CloudWatch rule and targets
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *      aws::event-rule event-pattern-example
 *        rule-name: "event-pattern-test"
 *        description: "first rule test"
 *        event-pattern-path: 'event-pattern.json'
 *        schedule-event: true
 *        schedule-expression: "rate(5 minutes)"
 *        state: "ENABLED"
 *
 *        target
 *           target-id: "event-pattern-match-target1"
 *           target-arn: "arn:aws:sqs:us-east-2:242040583208:cloudwatch-test.fifo"
 *           message-group-id: "test123"
 *        end
 * end
 *
 */
@Type("event-rule")
public class EventRuleResource extends AwsResource implements Copyable<Rule> {

    private String description;
    private String eventPattern;
    private String managedBy;
    private RoleResource role;
    private String ruleArn;
    private String ruleName;
    private String scheduleExpression;
    private String state;
    private Set<RuleTargetResource> target;

    /**
     * The name of the rule associated that matches incoming events. (Required)
     */
    @Id
    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    /**
     * The description of the rule associated with the event.(Optional)
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The IAM role that gives permissions to invoke actions on the targets.
     */
    @Updatable
    public RoleResource getRole() {
        return role;
    }

    public void setRole(RoleResource role) {
        this.role = role;
    }

    /**
     * The event pattern which activates target actions when matched.
     */
    @Updatable
    public String getEventPattern() {
        return getProcessedEventPattern(eventPattern);
    }

    public void setEventPattern(String eventPattern) {
        this.eventPattern = eventPattern;
    }

    /**
     * The schedule expression that triggers an automated rule action. See `AWS Services Schedule Expressions <https://docs.aws.amazon.com/AmazonCloudWatch/latest/events/ScheduledEvents.html/>`_.
     */
    @Updatable
    public String getScheduleExpression() {
        return scheduleExpression;
    }

    public void setScheduleExpression(String scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
    }

    /**
     * This value indicates if the rule is enabled to invoke target actions. Valid values are ``ENABLED`` or ``DISABLED``
     */
    @Updatable
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * The list of target resources which can be invoked when an event pattern is matched or scheduled. See `AWS Services CloudWatch events <https://docs.aws.amazon.com/AmazonCloudWatch/latest/events/WhatIsCloudWatchEvents.html/>`_.
     *
     * @subresource gyro.aws.cloudwatch.RuleTargetResource
     */
    @Updatable
    public Set<RuleTargetResource> getTarget() {
        if (target == null) {
            target = new HashSet<>();
        }
        return target;
    }

    public void setTarget(Set<RuleTargetResource> target) {
        this.target = target;
    }

    /**
     * If role arn is given, this field specifies which aws service can manage the rules.
     */
    @Output
    public String getManagedBy() {
        return managedBy;
    }

    public void setManagedBy(String managedBy) {
        this.managedBy = managedBy;
    }

    /**
     * The arn for the cloudwatch rule.
     */
    @Output
    public String getRuleArn() {
        return ruleArn;
    }

    public void setRuleArn(String ruleArn) {
        this.ruleArn = ruleArn;
    }

    @Override
    public void copyFrom(Rule rule) {
        setRuleArn(rule.arn());
        setRuleName(rule.name());
        setDescription(rule.description());
        setEventPattern(rule.eventPattern());
        setScheduleExpression(rule.scheduleExpression());
        setState(rule.state().toString());
        setRole(!ObjectUtils.isBlank(rule.roleArn()) ? findById(RoleResource.class, rule.roleArn()) : null);
        setManagedBy(rule.managedBy());

        CloudWatchEventsClient client = createClient(CloudWatchEventsClient.class);

        List<Target> targets = getTarget(client, rule);

        getTarget().clear();
        for (Target target : targets) {
            RuleTargetResource targetResource = newSubresource(RuleTargetResource.class);
            targetResource.copyFrom(target);
            getTarget().add(targetResource);
        }
    }

    @Override
    public boolean refresh() {
        CloudWatchEventsClient client = createClient(CloudWatchEventsClient.class);

        Rule rule = getRule(client);

        if (rule == null) {
            return false;
        }

        copyFrom(rule);

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
            sb.append(getRuleName());
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

    private void saveRule(CloudWatchEventsClient client) {

        if (!ObjectUtils.isBlank(getScheduleExpression())) {
            PutRuleResponse ruleResponse = client.putRule(
                    r -> r.name(getRuleName())
                            .description(getDescription())
                            .scheduleExpression(getScheduleExpression())
                            .roleArn(getRole() != null ? getRole().getArn() : null)
                            .state(getState())
            );
            setRuleArn(ruleResponse.ruleArn());
        } else {

            PutRuleResponse ruleResponse = client.putRule(
                    r -> r.name(getRuleName())
                            .description(getDescription())
                            .eventPattern(getEventPattern())
                            .roleArn(getRole() != null ? getRole().getArn() : null)
                            .state(getState())
            );
            setRuleArn(ruleResponse.ruleArn());
        }
    }

    private List<Target> getTarget(CloudWatchEventsClient client, Rule rule) {
        List<Target> targets = new ArrayList<>();
        ListTargetsByRuleResponse listTargetResponse = client.listTargetsByRule(r -> r.rule(rule.name()));

        try {
            if (!listTargetResponse.targets().isEmpty()) {
                targets.addAll(listTargetResponse.targets());
            }
        } catch(CloudWatchEventsException ex) {

            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return targets;
    }

    private String getProcessedEventPattern(String eventPattern) {
        if (eventPattern == null) {
            return null;
        } else if (eventPattern.endsWith(".json")) {
            try (InputStream input = openInput(eventPattern)) {
                eventPattern = IoUtils.toUtf8String(input);

            } catch (IOException ex) {
                throw new GyroException(String.format("File at path '%s' not found.", eventPattern));
            }
        }

        ObjectMapper obj = new ObjectMapper();
        try {
            JsonNode jsonNode = obj.readTree(eventPattern);
            return jsonNode.toString();
        } catch (IOException ex) {
            throw new GyroException(String.format("Could not read the json `%s`",eventPattern),ex);
        }
    }
}