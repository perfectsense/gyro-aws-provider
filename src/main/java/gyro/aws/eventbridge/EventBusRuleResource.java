/*
 * Copyright 2022, Brightspot.
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

package gyro.aws.eventbridge;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.PolicyResource;
import gyro.aws.iam.RoleResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.CollectionMin;
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.Required;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.DescribeRuleResponse;
import software.amazon.awssdk.services.eventbridge.model.ListRulesResponse;
import software.amazon.awssdk.services.eventbridge.model.ListTargetsByRuleRequest;
import software.amazon.awssdk.services.eventbridge.model.ListTargetsByRuleResponse;
import software.amazon.awssdk.services.eventbridge.model.PutRuleRequest;
import software.amazon.awssdk.services.eventbridge.model.PutRuleResponse;
import software.amazon.awssdk.services.eventbridge.model.ResourceNotFoundException;
import software.amazon.awssdk.services.eventbridge.model.Rule;
import software.amazon.awssdk.services.eventbridge.model.RuleState;
import software.amazon.awssdk.services.eventbridge.model.Target;
import software.amazon.awssdk.utils.IoUtils;

@Type("event-bus-rule")
public class EventBusRuleResource extends EventBridgeTaggableResource implements Copyable<Rule> {

    private EventBusResource eventBus;
    private String name;
    private String description;
    private String eventPattern;
    private RoleResource role;
    private String scheduledExpression;
    private Set<TargetResource> target;
    private RuleState ruleState;

    private String arn;

    /**
     * The event bus this rule will belong to.
     */
    @Required
    public EventBusResource getEventBus() {
        return eventBus;
    }

    public void setEventBus(EventBusResource eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * The name of the rule.
     */
    @Required
    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The description of the rule.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The event pattern for the rule.
     */
    @Updatable
    @ConflictsWith("scheduled-expression")
    public String getEventPattern() {
        if (this.eventPattern != null && this.eventPattern.contains(".json")) {
            try (InputStream input = openInput(this.eventPattern)) {
                this.eventPattern = PolicyResource.formatPolicy(IoUtils.toUtf8String(input));
                return this.eventPattern;
            } catch (IOException err) {
                throw new GyroException(MessageFormat
                    .format("Event Bus Rule - {0} event pattern error. Unable to read event pattern from path [{1}]", getName(), eventPattern));
            }
        } else {
            return PolicyResource.formatPolicy(this.eventPattern);
        }
    }

    public void setEventPattern(String eventPattern) {
        this.eventPattern = eventPattern;
    }

    /**
     * The iam role to be associated with the rule.
     */
    @Updatable
    public RoleResource getRole() {
        return role;
    }

    public void setRole(RoleResource role) {
        this.role = role;
    }

    /**
     * The scheduled expression for the rule.
     */
    @Updatable
    @ConflictsWith("event-pattern")
    public String getScheduledExpression() {
        return scheduledExpression;
    }

    public void setScheduledExpression(String scheduledExpression) {
        this.scheduledExpression = scheduledExpression;
    }

    /**
     * One or more targets for the rule.
     *
     * @subresource gyro.aws.eventbridge.TargetResource
     */
    @Updatable
    @CollectionMin(1)
    public Set<TargetResource> getTarget() {
        if (target == null) {
            target = new HashSet<>();
        }

        return target;
    }

    public void setTarget(Set<TargetResource> target) {
        this.target = target;
    }

    /**
     * Enable/Disable the rule. Defaults to ``ENABLED``.
     */
    @Updatable
    public RuleState getRuleState() {
        if (ruleState == null) {
            ruleState = RuleState.ENABLED;
        }

        return ruleState;
    }

    public void setRuleState(RuleState ruleState) {
        this.ruleState = ruleState;
    }

    /**
     * The arn of the rule.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public void copyFrom(Rule model) {
        setName(model.name());
        setArn(model.arn());
        setDescription(model.description());
        setEventBus(findById(EventBusResource.class, model.eventBusName()));
        setEventPattern(model.eventPattern());
        setArn(model.roleArn());
        setScheduledExpression(model.scheduleExpression());
        setRuleState(model.state());

        getTarget().clear();
        loadTargets();

        refreshTags();
    }

    @Override
    public boolean refresh() {
        EventBridgeClient client = createClient(EventBridgeClient.class);
        boolean isAvailable = false;

        try {
            ListRulesResponse response = client.listRules(r -> r.eventBusName(getEventBus().getName())
                .namePrefix(getName()));

            if (response.rules() != null && response.rules().stream().anyMatch(o -> o.name().equals(getName()))) {
                isAvailable = true;
                copyFrom(response.rules().stream().filter(o -> o.name().equals(getName())).findFirst().get());
            }

        } catch (ResourceNotFoundException ex) {
            // ignore
        }

        return isAvailable;
    }

    @Override
    public void doCreate(GyroUI ui, State state) throws Exception {
        EventBridgeClient client = createClient(EventBridgeClient.class);

        PutRuleResponse response = client.putRule(r -> r.name(getName())
            .eventBusName(getEventBus().getName())
            .description(getDescription())
            .roleArn(getRole().getArn())
            .eventPattern(getEventPattern())
            .scheduleExpression(getScheduledExpression())
            .state(getRuleState()));

        setArn(response.ruleArn());

        if (!getTarget().isEmpty()) {
            HashSet<TargetResource> targetResources = new HashSet<>(getTarget());
            getTarget().clear();
            state.save();

            setTarget(targetResources);
            client.putTargets(r -> r
                .eventBusName(getEventBus().getName())
                .rule(getName())
                .targets(getTarget().stream()
                    .map(TargetResource::toTarget)
                    .collect(Collectors.toList())));
        }
    }

    @Override
    public void doUpdate(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        EventBridgeClient client = createClient(EventBridgeClient.class);

        HashSet<String> newChangedFields = new HashSet<>(changedFieldNames);
        newChangedFields.remove("tags");
        newChangedFields.remove("target");

        if (!newChangedFields.isEmpty()) {
            client.putRule(r -> r.name(getName())
                .eventBusName(getEventBus().getName())
                .description(getDescription())
                .roleArn(getRole().getArn())
                .eventPattern(getEventPattern())
                .scheduleExpression(getScheduledExpression())
                .state(getRuleState()));
        }

        if (changedFieldNames.contains("target")) {
            EventBusRuleResource eventBusRuleResource = (EventBusRuleResource) current;

            Set<String> removeTargetIds = eventBusRuleResource.getTarget()
                .stream()
                .map(TargetResource::getId)
                .collect(Collectors.toSet());

            removeTargetIds.removeAll(getTarget().stream()
                .map(TargetResource::getId)
                .collect(Collectors.toList()));

            if (!removeTargetIds.isEmpty()) {
                client.removeTargets(r -> r.eventBusName(getEventBus().getName()).rule(getName()).ids(removeTargetIds));
            }

            client.putTargets(r -> r
                .eventBusName(getEventBus().getName())
                .rule(getName())
                .targets(getTarget().stream()
                    .map(TargetResource::toTarget)
                    .collect(Collectors.toList())));
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        EventBridgeClient client = createClient(EventBridgeClient.class);

        try {
            client.deleteRule(r -> r.eventBusName(getEventBus().getName()).name(getName()));
        } catch (ResourceNotFoundException ex) {
            // ignore
        }
    }

    @Override
    public String resourceArn() {
        return getArn();
    }

    private void loadTargets() {
        EventBridgeClient client = createClient(EventBridgeClient.class);
        ListTargetsByRuleResponse response;
        List<Target> targets = new ArrayList<>();
        String token = "";

        do {
            ListTargetsByRuleRequest.Builder builder = ListTargetsByRuleRequest.builder()
                .eventBusName(getEventBus().getName())
                .rule(getName());

            if (!StringUtils.isBlank(token)) {
                builder = builder.nextToken(token);
            }

            response = client.listTargetsByRule(builder.build());
            token = response.nextToken();

            if (response.targets() != null) {
                targets.addAll(response.targets());
            }
        } while (!StringUtils.isBlank(token));

        targets.forEach(o -> {
            TargetResource targetResource = newSubresource(TargetResource.class);
            targetResource.copyFrom(o);
            getTarget().add(targetResource);
        });
    }
}
