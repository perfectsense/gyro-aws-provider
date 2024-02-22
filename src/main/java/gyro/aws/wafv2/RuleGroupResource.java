/*
 * Copyright 2020, Brightspot.
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
import gyro.aws.iam.PolicyResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.CollectionMax;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.CheckCapacityResponse;
import software.amazon.awssdk.services.wafv2.model.CreateRuleGroupRequest;
import software.amazon.awssdk.services.wafv2.model.CreateRuleGroupResponse;
import software.amazon.awssdk.services.wafv2.model.GetPermissionPolicyResponse;
import software.amazon.awssdk.services.wafv2.model.GetRuleGroupResponse;
import software.amazon.awssdk.services.wafv2.model.RuleGroup;
import software.amazon.awssdk.services.wafv2.model.WafNonexistentItemException;
import software.amazon.awssdk.utils.IoUtils;

/**
 * Creates a rule group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::wafv2-rule-group rule-group-example
 *         name: "rule-group-example"
 *         description: "rule-group-example-desc"
 *         scope: "REGIONAL"
 *
 *         rule
 *             name: "rule-group-example-rule-1"
 *             priority: 0
 *             action: "BLOCK"
 *
 *             visibility-config
 *                 metric-name: "rule-group-example-rule-1"
 *                 cloud-watch-metrics-enabled: false
 *                 sampled-requests-enabled: false
 *             end
 *
 *             statement
 *                 xss-match-statement
 *                     field-to-match
 *                         name: "header-field"
 *                         match-type: "SINGLE_HEADER"
 *                     end
 *
 *                     text-transformation
 *                         priority: 0
 *                         type: "NONE"
 *                     end
 *                 end
 *             end
 *         end
 *
 *         rule
 *             name: "rule-group-example-rule-2"
 *             priority: 1
 *             action: "BLOCK"
 *
 *             visibility-config
 *                 metric-name: "rule-group-example-rule-2"
 *                 cloud-watch-metrics-enabled: false
 *                 sampled-requests-enabled: false
 *             end
 *
 *             statement
 *                 byte-match-statement
 *                     field-to-match
 *                         name: "header-field"
 *                         match-type: "SINGLE_HEADER"
 *                     end
 *
 *                     positional-constraint: "EXACTLY"
 *
 *                     text-transformation
 *                         priority: 0
 *                         type: "NONE"
 *                     end
 *
 *                     search-string: "something"
 *                 end
 *             end
 *         end
 *
 *         rule
 *             name: "rule-group-example-rule-3"
 *             priority: 2
 *             action: "BLOCK"
 *
 *             visibility-config
 *                 metric-name: "rule-group-example-rule-3"
 *                 cloud-watch-metrics-enabled: false
 *                 sampled-requests-enabled: false
 *             end
 *
 *             statement
 *                 size-constraint-statement
 *                     field-to-match
 *                         match-type: "BODY"
 *                     end
 *
 *                     comparison-operator: "EQ"
 *
 *                     text-transformation
 *                         priority: 0
 *                         type: "COMPRESS_WHITE_SPACE"
 *                     end
 *
 *                     text-transformation
 *                         priority: 1
 *                         type: "HTML_ENTITY_DECODE"
 *                     end
 *
 *                     size: 3
 *                 end
 *             end
 *         end
 *
 *         rule
 *             name: "rule-group-example-rule-4"
 *             priority: 3
 *             action: "BLOCK"
 *
 *             visibility-config
 *                 metric-name: "rule-group-example-rule-4"
 *                 cloud-watch-metrics-enabled: false
 *                 sampled-requests-enabled: false
 *             end
 *
 *             statement
 *                 and-statement
 *                     statement
 *                         ip-set-reference-statement
 *                             ip-set: $(aws::wafv2-ip-set ip-set-example-ipv4)
 *                         end
 *                     end
 *
 *                     statement
 *                         regex-pattern-set-reference-statement
 *                             field-to-match
 *                                 match-type: "BODY"
 *                             end
 *
 *                             text-transformation
 *                                 priority: 0
 *                                 type: "COMPRESS_WHITE_SPACE"
 *                             end
 *
 *                             regex-pattern-set: $(aws::wafv2-regex-pattern-set regex-pattern-set-example)
 *                         end
 *                     end
 *
 *                     statement
 *                         sqli-match-statement
 *                             field-to-match
 *                                 match-type: "BODY"
 *                             end
 *
 *                             text-transformation
 *                                 priority: 0
 *                                 type: "COMPRESS_WHITE_SPACE"
 *                             end
 *                         end
 *                     end
 *                 end
 *             end
 *         end
 *
 *         rule
 *             name: "rule-group-example-rule-5"
 *             priority: 4
 *             action: "BLOCK"
 *
 *             visibility-config
 *                 metric-name: "rule-group-example-rule-5"
 *                 cloud-watch-metrics-enabled: false
 *                 sampled-requests-enabled: false
 *             end
 *
 *             statement
 *                 or-statement
 *                     statement
 *                         geo-match-statement
 *                             country-codes: [
 *                                 "IN"
 *                             ]
 *                         end
 *                     end
 *
 *                     statement
 *                         sqli-match-statement
 *                             field-to-match
 *                                 match-type: "BODY"
 *                             end
 *
 *                             text-transformation
 *                                 priority: 0
 *                                 type: "COMPRESS_WHITE_SPACE"
 *                             end
 *                         end
 *                     end
 *                 end
 *             end
 *         end
 *
 *         visibility-config
 *             metric-name: "rule-group-example-metric"
 *             cloud-watch-metrics-enabled: false
 *             sampled-requests-enabled: false
 *         end
 *     end
 */
@Type("wafv2-rule-group")
public class RuleGroupResource extends WafTaggableResource implements Copyable<RuleGroup> {

    private String name;
    private String description;
    private Long capacity;
    private Set<RuleResource> rule;
    private VisibilityConfigResource visibilityConfig;
    private Set<CustomResponseBodyResource> customResponseBody;
    private String policy;
    private String arn;
    private String id;

    /**
     * Name of the rule group.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Description of the rule group.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The total WCU capacity for the rule group.
     * If not provided will be auto calculated based on the conditions provided by the rule configuration.
     */
    public Long getCapacity() {
        return capacity;
    }

    public void setCapacity(Long capacity) {
        this.capacity = capacity;
    }

    /**
     * A set of rule configurations that contains the conditions.
     *
     * @subresource gyro.aws.wafv2.RuleResource
     */
    @Required
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

    /**
     * The visibility configuration for the rule group.
     *
     * @subresource gyro.aws.wafv2.VisibilityConfigResource
     */
    @Required
    public VisibilityConfigResource getVisibilityConfig() {
        return visibilityConfig;
    }

    public void setVisibilityConfig(VisibilityConfigResource visibilityConfig) {
        this.visibilityConfig = visibilityConfig;
    }

    /**
     * A set of custom response body for the rule group.
     *
     * @subresource gyro.aws.wafv2.CustomResponseBodyResource
     */
    @Updatable
    public Set<CustomResponseBodyResource> getCustomResponseBody() {
        if (customResponseBody == null) {
            customResponseBody = new HashSet<>();
        }

        return customResponseBody;
    }

    public void setCustomResponseBody(Set<CustomResponseBodyResource> customResponseBody) {
        this.customResponseBody = customResponseBody;
    }

    /**
     * The policy document. A policy path or policy string is allowed.
     */
    @Updatable
    public String getPolicy() {
        if (this.policy != null && this.policy.contains(".json")) {
            try (InputStream input = openInput(this.policy)) {
                this.policy = PolicyResource.formatPolicy(IoUtils.toUtf8String(input));
                return this.policy;
            } catch (IOException err) {
                throw new GyroException(MessageFormat
                    .format("Queue - {0} policy error. Unable to read policy from path [{1}]", getName(), policy));
            }
        } else {
            return PolicyResource.formatPolicy(this.policy);
        }
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    /**
     * The arn of the rule group.
     */
    @Id
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The id of the rule group.
     */
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

        setCustomResponseBody(null);
        if (ruleGroup.customResponseBodies() != null) {
            ruleGroup.customResponseBodies().forEach((key, value) -> {
                CustomResponseBodyResource customResponseBody = newSubresource(CustomResponseBodyResource.class);
                customResponseBody.copyFrom(value);
                customResponseBody.setName(key);
                getCustomResponseBody().add(customResponseBody);
            });
        }

        Wafv2Client client = createClient(Wafv2Client.class);
        try {
            GetPermissionPolicyResponse response = client.getPermissionPolicy(r -> r.resourceArn(getArn()));
            setPolicy(response.policy());
        } catch (WafNonexistentItemException ex) {
            // ignore
            // errors if no policy exists
        }
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

        Long capacity = getCapacity();

        if (capacity == null) {
            CheckCapacityResponse response = client.checkCapacity(r -> r.scope(getScope())
                .rules(getRule().stream().map(RuleResource::toRule).collect(Collectors.toList())));

            capacity = response.capacity();
        }

        CreateRuleGroupResponse response = client.createRuleGroup(CreateRuleGroupRequest.builder()
            .name(getName())
            .scope(getScope())
            .description(getDescription())
            .capacity(capacity)
            .visibilityConfig(getVisibilityConfig().toVisibilityConfig())
            .rules(getRule().stream().map(RuleResource::toRule).collect(Collectors.toList()))
            .customResponseBodies(getCustomResponseBody().stream()
                .collect(Collectors.toMap(CustomResponseBodyResource::getName,
                    CustomResponseBodyResource::toCustomResponseBody)))
            .build());

        setId(response.summary().id());
        setArn(response.summary().arn());

        if (!ObjectUtils.isBlank(getPolicy())) {
            state.save();

            client.putPermissionPolicy(r -> r.resourceArn(getArn()).policy(getPolicy()));
        }
    }

    @Override
    protected void doUpdate(
        GyroUI ui, State state, Resource config, Set<String> changedProperties) {
        Wafv2Client client = createClient(Wafv2Client.class);

        if (changedProperties.contains("rule")
            || changedProperties.contains("description")
            || changedProperties.contains("visibility-config")
            || changedProperties.contains("custom-response-body")) {
            client.updateRuleGroup(r -> r.id(getId())
                .name(getName())
                .scope(getScope())
                .description(getDescription())
                .lockToken(lockToken(client))
                .rules(getRule().stream().map(RuleResource::toRule).collect(Collectors.toList()))
                .visibilityConfig(getVisibilityConfig().toVisibilityConfig())
                    .customResponseBodies(getCustomResponseBody().stream()
                        .collect(Collectors.toMap(CustomResponseBodyResource::getName,
                            CustomResponseBodyResource::toCustomResponseBody))));
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

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (RuleResource.invalidPriority(getRule())) {
            errors.add(new ValidationError(
                this,
                "rule",
                "'priority' exception. 'priority' value starts from 0 without skipping any number"));
        }

        if (getRule().stream()
            .filter(o -> o.getStatement() != null)
            .anyMatch(o -> o.getStatement().getRateBasedStatement() != null)) {
            errors.add(new ValidationError(
                this,
                "rule",
                "rate based rule cannot be configured as part of a rule group."));
        }

        if (getRule().stream()
            .filter(o -> o.getStatement() != null)
            .anyMatch(o -> o.getStatement().getManagedRuleGroupStatement() != null)) {
            errors.add(new ValidationError(
                this,
                "rule",
                "managed rule group cannot be configured as part of a rule group."));
        }

        return errors;
    }
}
