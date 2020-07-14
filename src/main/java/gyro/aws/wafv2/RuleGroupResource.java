package gyro.aws.wafv2;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.CreateRuleGroupResponse;
import software.amazon.awssdk.services.wafv2.model.GetRuleGroupResponse;
import software.amazon.awssdk.services.wafv2.model.RuleGroup;
import software.amazon.awssdk.services.wafv2.model.WafNonexistentItemException;

@Type("waf-rule-group")
public class RuleGroupResource extends WafTaggableResource implements Copyable<RuleGroup> {

    private String name;
    private String description;
    private Long capacity;
    private Set<RuleResource> rule;
    private VisibilityConfigResource visibilityConfig;
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
    }

    @Override
    protected void doUpdate(
        GyroUI ui, State state, Resource config, Set<String> changedProperties) {
        Wafv2Client client = createClient(Wafv2Client.class);

        client.updateRuleGroup(r -> r.id(getId())
            .name(getName())
            .scope(getScope())
            .lockToken(lockToken(client))
            .rules(getRule().stream().map(RuleResource::toRule).collect(Collectors.toList()))
            .visibilityConfig(getVisibilityConfig().toVisibilityConfig()));
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
}
