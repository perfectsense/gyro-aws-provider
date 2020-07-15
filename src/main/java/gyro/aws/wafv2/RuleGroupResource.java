/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws.wafv2;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.CollectionMax;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.CreateRuleGroupResponse;
import software.amazon.awssdk.services.wafv2.model.GetPermissionPolicyResponse;
import software.amazon.awssdk.services.wafv2.model.GetRuleGroupResponse;
import software.amazon.awssdk.services.wafv2.model.RuleGroup;
import software.amazon.awssdk.services.wafv2.model.WafNonexistentItemException;
import software.amazon.awssdk.utils.IoUtils;

@Type("wafv2-rule-group")
public class RuleGroupResource extends WafTaggableResource implements Copyable<RuleGroup> {

    private String name;
    private String description;
    private Long capacity;
    private Set<RuleResource> rule;
    private VisibilityConfigResource visibilityConfig;
    private String policy;
    private String arn;
    private String id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCapacity() {
        return capacity;
    }

    public void setCapacity(Long capacity) {
        this.capacity = capacity;
    }

    @Updatable
    @CollectionMax(10)
    public Set<RuleResource> getRule() {
        if (rule == null) {
            rule = new HashSet<>();
        }

        return rule;
    }

    public void setRule(Set<RuleResource> rule) {
        this.rule = rule;
    }

    public VisibilityConfigResource getVisibilityConfig() {
        return visibilityConfig;
    }

    public void setVisibilityConfig(VisibilityConfigResource visibilityConfig) {
        this.visibilityConfig = visibilityConfig;
    }

    /**
     * The policy document. A policy path or policy string is allowed. (Required)
     */
    @Updatable
    public String getPolicy() {
        if (this.policy != null && this.policy.contains(".json")) {
            try (InputStream input = openInput(this.policy)) {
                this.policy = formatPolicy(IoUtils.toUtf8String(input));
                return this.policy;
            } catch (IOException err) {
                throw new GyroException(MessageFormat
                    .format("Queue - {0} policy error. Unable to read policy from path [{1}]", getName(), policy));
            }
        } else {
            return this.policy;
        }
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    @Id
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(RuleGroup ruleGroup) {
        setName(ruleGroup.name());
        setDescription(ruleGroup.description());
        setCapacity(ruleGroup.capacity());
        setId(ruleGroup.id());
        setArn(ruleGroup.arn());

        getRule().clear();
        ruleGroup.rules().forEach(o -> {
            RuleResource rule = newSubresource(RuleResource.class);
            rule.copyFrom(o);
            getRule().add(rule);
        });

        VisibilityConfigResource visibilityConfig = newSubresource(VisibilityConfigResource.class);
        visibilityConfig.copyFrom(ruleGroup.visibilityConfig());
        setVisibilityConfig(visibilityConfig);

        Wafv2Client client = createClient(Wafv2Client.class);
        GetPermissionPolicyResponse response = client.getPermissionPolicy(r -> r.resourceArn(getArn()));
        setPolicy(response.policy());
    }

    @Override
    protected String getResourceArn() {
        return getArn();
    }

    @Override
    protected boolean doRefresh() {
        Wafv2Client client = createClient(Wafv2Client.class);

        GetRuleGroupResponse response = getRuleGroupResponse(client);

        if (response != null) {
            copyFrom(response.ruleGroup());

            return true;
        }

        return false;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        Wafv2Client client = createClient(Wafv2Client.class);

        CreateRuleGroupResponse response = client.createRuleGroup(r -> r.name(getName())
            .scope(getScope())
            .description(getDescription())
            .capacity(getCapacity())
            .visibilityConfig(getVisibilityConfig().toVisibilityConfig())
            .rules(getRule().stream().map(RuleResource::toRule).collect(Collectors.toList())));

        setId(response.summary().id());
        setArn(response.summary().arn());

        state.save();

        if (!ObjectUtils.isBlank(getPolicy())) {
            client.putPermissionPolicy(r -> r.resourceArn(getArn()).policy(getPolicy()));
        }
    }

    @Override
    protected void doUpdate(
        GyroUI ui, State state, Resource config, Set<String> changedProperties) {
        Wafv2Client client = createClient(Wafv2Client.class);

        if (changedProperties.contains("rule") || changedProperties.contains("visibility-config")) {
            client.updateRuleGroup(r -> r.id(getId())
                .name(getName())
                .scope(getScope())
                .lockToken(lockToken(client))
                .rules(getRule().stream().map(RuleResource::toRule).collect(Collectors.toList()))
                .visibilityConfig(getVisibilityConfig().toVisibilityConfig()));
        }

        if (changedProperties.contains("policy")) {
            if (ObjectUtils.isBlank(getPolicy())) {
                client.deletePermissionPolicy(r -> r.resourceArn(getArn()));
            } else {
                client.putPermissionPolicy(r -> r.resourceArn(getArn()).policy(getPolicy()));
            }
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Wafv2Client client = createClient(Wafv2Client.class);

        client.deleteRuleGroup(r -> r.id(getId()).name(getName()).scope(getScope()).lockToken(lockToken(client)));
    }

    private GetRuleGroupResponse getRuleGroupResponse(Wafv2Client client) {
        try {
            return client.getRuleGroup(r -> r.id(getId()).name(getName()).scope(getScope()));
        } catch (WafNonexistentItemException ex) {
            return null;
        }
    }

    private String lockToken(Wafv2Client client) {
        String token = null;
        GetRuleGroupResponse response = getRuleGroupResponse(client);

        if (response != null) {
            token = response.lockToken();
        }

        return token;
    }

    private String formatPolicy(String policy) {
        return policy != null ? policy.replaceAll(System.lineSeparator(), " ")
            .replaceAll("\t", " ")
            .trim()
            .replaceAll(" ", "") : policy;
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (RuleResource.invalidPriority(getRule())) {
            errors.add(new ValidationError(
                this,
                "rule",
                "'priority' exception. 'priority' value starts from 0 without skipping any number"));
        }

        return errors;
    }
}
