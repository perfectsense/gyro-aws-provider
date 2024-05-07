package gyro.aws.eks;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.eks.model.Taint;
import software.amazon.awssdk.services.eks.model.TaintEffect;

public class EksNodegroupTaint extends Diffable implements Copyable<Taint> {

    private String key;
    private String value;
    private TaintEffect taintEffect;

    @Required
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Required
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @ValidStrings({"NO_SCHEDULE", "NO_EXECUTE", "PREFER_NO_SCHEDULE"})
    @Required
    public TaintEffect getTaintEffect() {
        return taintEffect;
    }

    public void setTaintEffect(TaintEffect taintEffect) {
        this.taintEffect = taintEffect;
    }

    @Override
    public void copyFrom(Taint taint) {
        setKey(taint.key());
        setValue(taint.value());
        setTaintEffect(taint.effect());
    }

    public Taint toTaint() {
        return Taint.builder()
                .key(getKey())
                .value(getValue())
                .effect(getTaintEffect())
                .build();
    }

    @Override
    public String primaryKey() {
        return getKey() + " : " + getValue() + " : " + getTaintEffect();
    }
}
