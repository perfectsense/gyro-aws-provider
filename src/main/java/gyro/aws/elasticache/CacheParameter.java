package gyro.aws.elasticache;

import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
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

    /**
     * The name of the cache parameter variable. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The value of the cache parameter variable. (Required)
     */
    @Updatable
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
}
