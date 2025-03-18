package gyro.aws.wafv2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.wafv2.model.RuleAction;

public class RuleActionOverride
    extends Diffable implements Copyable<software.amazon.awssdk.services.wafv2.model.RuleActionOverride> {

    private String name;
    private RuleAction actionToUse;

    /**
     * The name of the rule to override
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The override action to use, in place of the configured action of the rule in the rule group.
     */
    public RuleAction getActionToUse() {
        return actionToUse;
    }

    public void setActionToUse(RuleAction actionToUse) {
        this.actionToUse = actionToUse;
    }

    @Override
    public String primaryKey() {
        return String.format("Rule '%s', Action: '%s'", getName(), getActionToUse());
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.wafv2.model.RuleActionOverride ruleActionOverride) {
        setName(ruleActionOverride.name());
        setActionToUse(ruleActionOverride.actionToUse());
    }

    software.amazon.awssdk.services.wafv2.model.RuleActionOverride toRuleActionOverride() {
        return software.amazon.awssdk.services.wafv2.model.RuleActionOverride.builder()
            .name(getName())
            .actionToUse(getActionToUse())
            .build();
    }
}
