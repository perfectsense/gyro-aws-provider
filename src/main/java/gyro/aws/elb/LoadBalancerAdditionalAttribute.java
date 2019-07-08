package gyro.aws.elb;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.elasticloadbalancing.model.AdditionalAttribute;

public class LoadBalancerAdditionalAttribute extends Diffable implements Copyable<AdditionalAttribute> {
    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void copyFrom(AdditionalAttribute additionalAttribute) {
        setKey(additionalAttribute.key());
        setValue(additionalAttribute.value());
    }

    @Override
    public String toDisplayString() {
        return String.format("additional attribute: key - %s, value - %s", getKey(), getValue());
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s", getKey(), getValue());
    }

    AdditionalAttribute toAdditionalAttribute() {
        return AdditionalAttribute.builder().key(getKey()).value(getValue()).build();
    }
}
