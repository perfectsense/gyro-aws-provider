package gyro.aws.dax;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.dax.model.ParameterNameValue;

public class DaxParameterNameValue extends Diffable implements Copyable<ParameterNameValue> {

    private String name;
    private String value;

    /**
     * The name of the parameter.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The value of the parameter.
     */
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void copyFrom(ParameterNameValue model) {
        setName(model.parameterName());
        setValue(model.parameterValue());
    }

    @Override
    public String primaryKey() {
        return String.format("%s", getName());
    }

    public static ParameterNameValue toParameterNameValues(DaxParameterNameValue daxParameterNameValue) {
        return ParameterNameValue.builder()
            .parameterName(daxParameterNameValue.getName())
            .parameterValue(daxParameterNameValue.getValue())
            .build();
    }
}
