package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.lang.Resource;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ActivatedRule;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.ExcludedRule;
import software.amazon.awssdk.services.waf.model.WafAction;
import software.amazon.awssdk.services.waf.model.WebACLUpdate;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ActivatedRuleResource extends AwsResource {

    private String ruleId;
    private String action;
    private String overrideAction;
    private String defaultAction;
    private String type;
    private Integer priority;
    private List<String> excludedRules;

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOverrideAction() {
        return overrideAction;
    }

    public void setOverrideAction(String overrideAction) {
        this.overrideAction = overrideAction;
    }

    public String getDefaultAction() {
        return defaultAction;
    }

    public void setDefaultAction(String defaultAction) {
        this.defaultAction = defaultAction;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public List<String> getExcludedRules() {
        return excludedRules;
    }

    public void setExcludedRules(List<String> excludedRules) {
        this.excludedRules = excludedRules;
    }

    public ActivatedRuleResource() {

    }

    public ActivatedRuleResource(ActivatedRule activatedRule) {
        setOverrideAction(activatedRule.overrideAction().typeAsString());
        setAction(activatedRule.action().typeAsString());
        setPriority(activatedRule.priority());
        setRuleId(activatedRule.ruleId());
        setType(activatedRule.typeAsString());
        setExcludedRules(activatedRule.excludedRules().stream().map(ExcludedRule::ruleId).collect(Collectors.toList()));
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {
        WafClient client = createClient(WafClient.class);

        WebAclResource parent = (WebAclResource) parent();

        WebACLUpdate webACLUpdate = WebACLUpdate.builder().action(ChangeAction.INSERT)
            .activatedRule(getActivatedRule())
            .build();

        client.updateWebACL(
            r -> r.webACLId(parent.getWebAclId())
                .defaultAction(da -> da.type(parent.getDefaultAction()))
                .changeToken(UUID.randomUUID().toString())
                .updates(webACLUpdate)
        );
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        WafClient client = createClient(WafClient.class);

        WebAclResource parent = (WebAclResource) parent();

        WebACLUpdate webACLUpdate = WebACLUpdate.builder().action(ChangeAction.DELETE)
            .activatedRule(getActivatedRule())
            .build();

        client.updateWebACL(
            r -> r.webACLId(parent.getWebAclId())
                .defaultAction(da -> da.type(parent.getDefaultAction()))
                .changeToken(UUID.randomUUID().toString())
                .updates(webACLUpdate)
        );
    }

    @Override
    public String toDisplayString() {
        return null;
    }

    private ActivatedRule getActivatedRule() {
        return ActivatedRule.builder()
            .action(wa -> wa.type(getAction()))
            .overrideAction(oa -> oa.type(getOverrideAction()))
            .priority(getPriority())
            .type(getType())
            .ruleId(getRuleId())
            .excludedRules(
                getExcludedRules().stream()
                    .map(
                        o -> ExcludedRule.builder().ruleId(o).build()
                    )
                    .collect(Collectors.toList())
            )
            .build();
    }
}
