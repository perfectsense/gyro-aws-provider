package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.core.resource.ResourceUpdatable;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ActivatedRule;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.ExcludedRule;
import software.amazon.awssdk.services.waf.model.WebACLUpdate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ActivatedRuleResource extends AwsResource {
    private String ruleId;
    private String action;
    private String type;
    private Integer priority;
    private List<String> excludedRules;

    /**
     * The id of the rule. (Required)
     */
    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    /**
     * The default action for the rule under this waf. valid values are ``ALLOW`` or ``BLOCK``. (Required)
     */
    @ResourceUpdatable
    public String getAction() {
        return action != null ? action.toUpperCase() : null;
    }

    public void setAction(String action) {
        this.action = action;
    }

    /**
     * The type of rule being attached. Valid values are ``REGULAR`` or ``RATE_BASED``. (Required)
     */
    public String getType() {
        return type != null ? type.toUpperCase() : null;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The priority of the rule when attached to the acl. Valid values integer 1 through 10 without skipping. (Required)
     */
    @ResourceUpdatable
    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public List<String> getExcludedRules() {
        if (excludedRules == null) {
            excludedRules = new ArrayList<>();
        }

        return excludedRules;
    }

    public void setExcludedRules(List<String> excludedRules) {
        this.excludedRules = excludedRules;
    }

    public ActivatedRuleResource() {

    }

    public ActivatedRuleResource(ActivatedRule activatedRule) {
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
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        WebAclResource parent = (WebAclResource) parent();

        // Priority check
        handlePriority(client, parent);

        saveActivatedRule(client, parent, getActivatedRule(), false);
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        WebAclResource parent = (WebAclResource) parent();

        // Priority check
        handlePriority(client, parent);

        // Remove old activated rule
        saveActivatedRule(client, parent, ((ActivatedRuleResource) current).getActivatedRule(), true);

        // Add updated activated rule
        saveActivatedRule(client, parent, getActivatedRule(), false);
    }

    @Override
    public void delete() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        WebAclResource parent = (WebAclResource) parent();

        saveActivatedRule(client, parent, getActivatedRule(), true);
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("activated rule");

        if (!ObjectUtils.isBlank(getRuleId())) {
            sb.append(" - ").append(getRuleId());
        }

        if (!ObjectUtils.isBlank(getType())) {
            sb.append(" - ").append(getType());
        }

        return sb.toString();
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s", getRuleId(), getType());
    }

    private ActivatedRule getActivatedRule() {
        return ActivatedRule.builder()
            .action(wa -> wa.type(getAction()))
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

    private void saveActivatedRule(WafClient client, WebAclResource parent, ActivatedRule activatedRule, boolean isDelete) {
        if (!isDelete || parent.isActivatedRulePresent(activatedRule)) {

            WebACLUpdate webAclUpdate = WebACLUpdate.builder()
                .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
                .activatedRule(activatedRule)
                .build();

            client.updateWebACL(
                r -> r.webACLId(parent.getWebAclId())
                    .defaultAction(da -> da.type(parent.getDefaultAction()))
                    .changeToken(client.getChangeToken().changeToken())
                    .updates(Collections.singleton(webAclUpdate))
            );
        }
    }

    private void handlePriority(WafClient client, WebAclResource parent) {
        ActivatedRule activatedRule = parent.getActivatedRuleWithPriority(getPriority());
        if (activatedRule != null) {
            //delete conflicting activated rule with same priority
            saveActivatedRule(client, parent, activatedRule, true);
        }
    }
}
