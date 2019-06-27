package gyro.aws.waf.common;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

public class WafAction extends Diffable implements Copyable<software.amazon.awssdk.services.waf.model.WafAction> {
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
    public void copyFrom(software.amazon.awssdk.services.waf.model.WafAction wafAction) {
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

    software.amazon.awssdk.services.waf.model.WafAction toWafAction() {
        return software.amazon.awssdk.services.waf.model.WafAction.builder().type(getType()).build();
    }
}
