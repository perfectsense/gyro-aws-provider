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
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.AllowAction;
import software.amazon.awssdk.services.wafv2.model.BlockAction;
import software.amazon.awssdk.services.wafv2.model.CreateWebAclResponse;
import software.amazon.awssdk.services.wafv2.model.DefaultAction;
import software.amazon.awssdk.services.wafv2.model.GetWebAclResponse;
import software.amazon.awssdk.services.wafv2.model.WafNonexistentItemException;
import software.amazon.awssdk.services.wafv2.model.WebACL;

@Type("waf-web-acl")
public class WebAclResource extends WafTaggableResource implements Copyable<WebACL> {

    private String name;
    private String description;
    private WafDefaultAction.DefaultAction defaultAction;
    private Set<RuleResource> rule;
    private VisibilityConfigResource visibilityConfig;
    private String id;
    private String arn;
    private Long capacity;

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

    @Updatable
    public WafDefaultAction.DefaultAction getDefaultAction() {
        return defaultAction;
    }

    public void setDefaultAction(WafDefaultAction.DefaultAction defaultAction) {
        this.defaultAction = defaultAction;
    }

    @Updatable
    public Set<RuleResource> getRule() {
        if (rule == null) {
            rule = new HashSet<>();
        }

        return rule;
    }

    public void setRule(Set<RuleResource> rule) {
        this.rule = rule;
    }

    @Updatable
    public VisibilityConfigResource getVisibilityConfig() {
        return visibilityConfig;
    }

    public void setVisibilityConfig(VisibilityConfigResource visibilityConfig) {
        this.visibilityConfig = visibilityConfig;
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
    }

    @Override
    public void doUpdate(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Wafv2Client client = createClient(Wafv2Client.class);

        client.updateWebACL(r -> r.id(getId())
            .name(getName())
            .scope(getScope())
            .lockToken(lockToken(client))
            .defaultAction(toDefaultAction())
            .rules(getRule().stream().map(RuleResource::toRule).collect(Collectors.toList()))
            .visibilityConfig(getVisibilityConfig().toVisibilityConfig()));
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Wafv2Client client = createClient(Wafv2Client.class);

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
}
