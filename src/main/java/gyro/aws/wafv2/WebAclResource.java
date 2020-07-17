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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.aws.elbv2.ApplicationLoadBalancerResource;
import gyro.aws.elbv2.LoadBalancerResource;
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
import software.amazon.awssdk.services.wafv2.model.AllowAction;
import software.amazon.awssdk.services.wafv2.model.BlockAction;
import software.amazon.awssdk.services.wafv2.model.CreateWebAclResponse;
import software.amazon.awssdk.services.wafv2.model.DefaultAction;
import software.amazon.awssdk.services.wafv2.model.GetLoggingConfigurationResponse;
import software.amazon.awssdk.services.wafv2.model.GetWebAclResponse;
import software.amazon.awssdk.services.wafv2.model.ListResourcesForWebAclResponse;
import software.amazon.awssdk.services.wafv2.model.ResourceType;
import software.amazon.awssdk.services.wafv2.model.WafNonexistentItemException;
import software.amazon.awssdk.services.wafv2.model.WebACL;

@Type("wafv2-web-acl")
public class WebAclResource extends WafTaggableResource implements Copyable<WebACL> {

    private String name;
    private String description;
    private WafDefaultAction.DefaultAction defaultAction;
    private Set<RuleResource> rule;
    private VisibilityConfigResource visibilityConfig;
    private Set<ApplicationLoadBalancerResource> loadBalancers;
    private LoggingConfigurationResource loggingConfiguration;
    private String id;
    private String arn;
    private Long capacity;

    @Required
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

    @Required
    @Updatable
    public WafDefaultAction.DefaultAction getDefaultAction() {
        return defaultAction;
    }

    public void setDefaultAction(WafDefaultAction.DefaultAction defaultAction) {
        this.defaultAction = defaultAction;
    }

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

    @Required
    @Updatable
    public VisibilityConfigResource getVisibilityConfig() {
        return visibilityConfig;
    }

    public void setVisibilityConfig(VisibilityConfigResource visibilityConfig) {
        this.visibilityConfig = visibilityConfig;
    }

    /**
     * A set of Application Load Balancer that will be associated with the web acl.
     */
    @Updatable
    public Set<ApplicationLoadBalancerResource> getLoadBalancers() {
        if (loadBalancers == null) {
            loadBalancers = new HashSet<>();
        }

        return loadBalancers;
    }

    public void setLoadBalancers(Set<ApplicationLoadBalancerResource> loadBalancers) {
        this.loadBalancers = loadBalancers;
    }

    public LoggingConfigurationResource getLoggingConfiguration() {
        return loggingConfiguration;
    }

    public void setLoggingConfiguration(LoggingConfigurationResource loggingConfiguration) {
        this.loggingConfiguration = loggingConfiguration;
    }

    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
    public Long getCapacity() {
        return capacity;
    }

    public void setCapacity(Long capacity) {
        this.capacity = capacity;
    }

    @Override
    protected String getResourceArn() {
        return getArn();
    }

    @Override
    public void copyFrom(WebACL webACL) {
        setArn(webACL.arn());
        setCapacity(webACL.capacity());
        setDescription(webACL.description());
        setName(webACL.name());
        setDefaultAction(webACL.defaultAction().allow() != null
            ? WafDefaultAction.DefaultAction.ALLOW
            : WafDefaultAction.DefaultAction.BLOCK);

        getRule().clear();
        webACL.rules().forEach(o -> {
            RuleResource rule = newSubresource(RuleResource.class);
            rule.copyFrom(o);
            getRule().add(rule);
        });

        VisibilityConfigResource visibilityConfig = newSubresource(VisibilityConfigResource.class);
        visibilityConfig.copyFrom(webACL.visibilityConfig());
        setVisibilityConfig(visibilityConfig);

        // Load associated ALB's
        Wafv2Client client = createClient(Wafv2Client.class);
        getLoadBalancers().clear();
        getAssociatedAlbArns(client).forEach(
            r -> getLoadBalancers().add(findById(ApplicationLoadBalancerResource.class, r))
        );

        // Load logging configuration
        setLoggingConfiguration(null);

        try {
            GetLoggingConfigurationResponse response = client.getLoggingConfiguration(r -> r.resourceArn(getArn()));
            if (response.loggingConfiguration() != null) {
                LoggingConfigurationResource loggingConfiguration = newSubresource(LoggingConfigurationResource.class);
                loggingConfiguration.copyFrom(response.loggingConfiguration());
                setLoggingConfiguration(loggingConfiguration);
            }
        } catch (WafNonexistentItemException ex) {
            // Ignore
            // Occurs if no logging config exists
        }

    }

    @Override
    public boolean doRefresh() {
        Wafv2Client client = createClient(Wafv2Client.class);

        GetWebAclResponse response = getWebACL(client);

        if (response != null) {
            copyFrom(response.webACL());

            return true;
        }

        return false;
    }

    @Override
    public void doCreate(GyroUI ui, State state) {
        Wafv2Client client = createClient(Wafv2Client.class);

        CreateWebAclResponse response = client.createWebACL(
            r -> r.name(getName())
                .description(getDescription())
                .scope(getScope())
                .defaultAction(toDefaultAction())
                .rules(getRule().stream().map(RuleResource::toRule).collect(Collectors.toList()))
                .visibilityConfig(getVisibilityConfig().toVisibilityConfig())
        );

        setArn(response.summary().arn());
        setId(response.summary().id());

        if (!getLoadBalancers().isEmpty()) {
            state.save();

            for (ApplicationLoadBalancerResource loadBalancer : getLoadBalancers()) {
                client.associateWebACL(r -> r.webACLArn(getArn())
                    .resourceArn(loadBalancer.getArn()));
            }
        }

        if (getLoggingConfiguration() != null) {
            state.save();

            client.putLoggingConfiguration(
                r -> r.loggingConfiguration(getLoggingConfiguration().toLoggingConfiguration())
            );
        }
    }

    @Override
    public void doUpdate(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Wafv2Client client = createClient(Wafv2Client.class);

        if (changedFieldNames.contains("rule")
            || changedFieldNames.contains("visibility-config")
            || changedFieldNames.contains("default-action")) {
            client.updateWebACL(r -> r.id(getId())
                .name(getName())
                .scope(getScope())
                .lockToken(lockToken(client))
                .defaultAction(toDefaultAction())
                .rules(getRule().stream().map(RuleResource::toRule).collect(Collectors.toList()))
                .visibilityConfig(getVisibilityConfig().toVisibilityConfig()));
        }

        if (changedFieldNames.contains("load-balancers")) {

            WebAclResource aclResource = (WebAclResource) current;

            Set<String> currentAlbArns = aclResource.getLoadBalancers()
                .stream()
                .map(LoadBalancerResource::getArn)
                .collect(Collectors.toSet());

            Set<String> pendingAlbArns = getLoadBalancers()
                .stream()
                .map(LoadBalancerResource::getArn)
                .collect(Collectors.toSet());

            List<String> removeAlbArns = currentAlbArns.stream()
                .filter(o -> !pendingAlbArns.contains(o))
                .collect(Collectors.toList());

            if (!removeAlbArns.isEmpty()) {
                for (String arn : removeAlbArns) {
                    try {
                        client.disassociateWebACL(r -> r.resourceArn(arn));
                    } catch (Exception ex) {
                        // ignore
                    }
                }
            }

            List<String> addAlbArns = pendingAlbArns.stream()
                .filter(o -> !currentAlbArns.contains(o))
                .collect(Collectors.toList());

            if (!addAlbArns.isEmpty()) {
                for (String arn : addAlbArns) {
                    try {
                        client.associateWebACL(r -> r.webACLArn(getArn()).resourceArn(arn));
                    } catch (Exception ex) {
                        throw new GyroException(String.format("Failed to associate loadbalancer %s", arn));
                    }
                }
            }
        }

        if (changedFieldNames.contains("logging-configuration")) {
            if (getLoggingConfiguration() != null) {
                client.putLoggingConfiguration(
                    r -> r.loggingConfiguration(getLoggingConfiguration().toLoggingConfiguration())
                );
            } else {
                client.deleteLoggingConfiguration(r -> r.resourceArn(getArn()));
            }
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Wafv2Client client = createClient(Wafv2Client.class);

        // Remove associated ALb before deleting
        List<String> associatedAlbArns = getAssociatedAlbArns(client);

        if (!associatedAlbArns.isEmpty()) {
            for (String arn : associatedAlbArns) {
                try {
                    client.disassociateWebACL(r -> r.resourceArn(arn));
                } catch (Exception ex) {
                    // ignore
                }
            }
        }

        client.deleteWebACL(r -> r.id(getId()).name(getName()).scope(getScope()).lockToken(lockToken(client)));
    }

    DefaultAction toDefaultAction() {
        DefaultAction.Builder builder = DefaultAction.builder();

        if (getDefaultAction() == WafDefaultAction.DefaultAction.ALLOW) {
            builder.allow(AllowAction.builder().build());
        } else if (getDefaultAction() == WafDefaultAction.DefaultAction.BLOCK) {
            builder.block(BlockAction.builder().build());
        }

        return builder.build();
    }

    private GetWebAclResponse getWebACL(Wafv2Client client) {
        try {
            return client.getWebACL(r -> r.id(getId()).name(getName()).scope(getScope()));
        } catch (WafNonexistentItemException ex) {
            return null;
        }
    }

    private String lockToken(Wafv2Client client) {
        String token = null;
        GetWebAclResponse response = getWebACL(client);

        if (response != null) {
            token = response.lockToken();
        }

        return token;
    }

    private List<String> getAssociatedAlbArns(Wafv2Client client) {
        List<String> arns = new ArrayList<>();

        ListResourcesForWebAclResponse response = client.listResourcesForWebACL(r -> r
            .resourceType(ResourceType.APPLICATION_LOAD_BALANCER)
            .webACLArn(getArn()));

        if (response.hasResourceArns()) {
            arns = response.resourceArns();
        }

        return arns;
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (!"REGIONAL".equals(getScope()) && !getLoadBalancers().isEmpty()) {
            errors.add(new ValidationError(
                this,
                "load-balancers",
                "'load-balancers' can only be set when 'scope' is set to 'REGIONAL'"));
        }

        if (RuleResource.invalidPriority(getRule())) {
            errors.add(new ValidationError(
                this,
                "rule",
                "'priority' exception. 'priority' value starts from 0 without skipping any number"));
        }

        if (getRule().stream()
            .filter(o -> o.getStatement() != null)
            .filter(o -> o.getStatement().getRateBasedStatement() != null)
            .count() > 10) {
            errors.add(new ValidationError(
                this,
                "rule",
                "rate based rule limit reached. Maximum of 10 rate based rule can be configured."));
        }

        return errors;
    }
}
