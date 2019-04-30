package gyro.aws.elasticache;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.diff.Diffable;
import gyro.core.resource.ResourceDiffProperty;
import software.amazon.awssdk.services.elasticache.model.ParameterNameValue;

public class CacheParameter extends Diffable {
    private String name;
    private String value;

    public CacheParameter() {

    }

    public CacheParameter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ResourceDiffProperty(updatable = true)
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    @Override
    public String toDisplayString() {
        return String.format("cache param - %s", getName()) ;
    }

    ParameterNameValue getParameterNameValue() {
        return ParameterNameValue.builder()
            .parameterName(getName())
            .parameterValue(getValue())
            .build();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass().equals(CacheParameter.class)) {
            CacheParameter obj1 = (CacheParameter) obj;
            return (this.getName().equals(obj1.getName())
                && (this.getValue().equals(obj1.getValue())
                || (ObjectUtils.isBlank(this.getValue()) && ObjectUtils.isBlank(obj1.getValue()))));
        } else {
            return false;
        }
    }
}
