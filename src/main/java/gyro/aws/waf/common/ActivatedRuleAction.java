package gyro.aws.waf.common;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.waf.model.WafAction;

public class ActivatedRuleAction extends Diffable implements Copyable<WafAction> {
    private String type;

    /**
     * The action for a rule under a waf. valid values are ``ALLOW`` or ``BLOCK``. (Required)
     */
    @Updatable
    public String getType() {
        return type != null ? type.toUpperCase() : null;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void copyFrom(WafAction wafAction) {
        setType(wafAction.typeAsString());
    }

    @Override
    public String primaryKey() {
        return getType();
    }

    @Override
    public String toDisplayString() {
        return getType();
    }

    WafAction toWafAction() {
        return WafAction.builder().type(getType()).build();
    }
}
