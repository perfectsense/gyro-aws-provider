package gyro.aws.waf.common;

import gyro.aws.Copyable;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.waf.model.ActivatedRule;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.ExcludedRule;
import software.amazon.awssdk.services.waf.model.UpdateWebAclRequest;
import software.amazon.awssdk.services.waf.model.WafRuleType;
import software.amazon.awssdk.services.waf.model.WebACLUpdate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ActivatedRuleResource extends AbstractWafResource implements Copyable<ActivatedRule> {
    private CommonRuleResource rule;
    private String action;
    private Integer priority;
    private List<String> excludedRules;

    /**
     * The rule to be attached. (Required)
     */
    public CommonRuleResource getRule() {
        return rule;
    }

    public void setRule(CommonRuleResource rule) {
        this.rule = rule;
    }

    /**
     * The default action for the rule under this waf. valid values are ``ALLOW`` or ``BLOCK``. (Required)
     */
    @Updatable
    public String getAction() {
        return action != null ? action.toUpperCase() : null;
    }

    public void setAction(String action) {
        this.action = action;
    }

    /**
     * The priority of the rule when attached to the acl. Valid values integer 1 through 10 without skipping. (Required)
     */
    @Updatable
    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
    * A list of rule id's to be excluded from this activated rule.
    */
    public List<String> getExcludedRules() {
        if (excludedRules == null) {
            excludedRules = new ArrayList<>();
        }

        return excludedRules;
    }

    public void setExcludedRules(List<String> excludedRules) {
        this.excludedRules = excludedRules;
    }

    @Override
    public void copyFrom(ActivatedRule activatedRule) {
        setAction(activatedRule.action().typeAsString());
        setPriority(activatedRule.priority());
        setRule(findById(CommonRuleResource.class, activatedRule.ruleId()));
        //setType(activatedRule.typeAsString());
        setExcludedRules(activatedRule.excludedRules().stream().map(ExcludedRule::ruleId).collect(Collectors.toList()));
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {
        // Priority check
        handlePriority();

        saveActivatedRule(getActivatedRule(), false);
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        // Priority check
        handlePriority();

        // Remove old activated rule
        saveActivatedRule(((ActivatedRuleResource) current).getActivatedRule(), true);

        // Add updated activated rule
        saveActivatedRule(getActivatedRule(), false);
    }

    @Override
    public void delete() {
        saveActivatedRule(getActivatedRule(), true);
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("activated rule");

        if (getRule() != null) {
            sb.append(" - ").append(getRule().getRuleId());
        }

        if (getType() != null) {
            sb.append(" - ").append(getType().toString());
        }

        return sb.toString();
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s", getRule() != null ? getRule().getRuleId() : null, getType());
    }

    protected ActivatedRule getActivatedRule() {
        return ActivatedRule.builder()
            .action(wa -> wa.type(getAction()))
            .priority(getPriority())
            .type(getType())
            .ruleId(getRule().getRuleId())
            .excludedRules(
                getExcludedRules().stream()
                    .map(
                        o -> ExcludedRule.builder().ruleId(o).build()
                    )
                    .collect(Collectors.toList())
            )
            .build();
    }

    protected abstract void saveActivatedRule(ActivatedRule activatedRule, boolean isDelete);

    protected abstract void handlePriority();

    protected UpdateWebAclRequest.Builder getUpdateWebAclRequest(gyro.aws.waf.common.WebAclResource parent, ActivatedRule activatedRule, boolean isDelete) {
        WebACLUpdate webAclUpdate = WebACLUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .activatedRule(activatedRule)
            .build();

        return UpdateWebAclRequest.builder()
            .webACLId(parent.getWebAclId())
            .defaultAction(da -> da.type(parent.getDefaultAction()))
            .updates(Collections.singleton(webAclUpdate));
    }

    private WafRuleType getType() {
        WafRuleType ruleType = null;

        if (getRule() instanceof RuleResource) {
            ruleType = WafRuleType.REGULAR;
        } else if (getRule() instanceof RateRuleResource) {
            ruleType = WafRuleType.RATE_BASED;
        }

        return ruleType;
    }
}
