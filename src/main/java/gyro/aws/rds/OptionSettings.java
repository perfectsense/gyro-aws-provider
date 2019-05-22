package gyro.aws.rds;

import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

public class OptionSettings extends Diffable {

    private String name;
    private String value;

    /**
     * The name of the option settings.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The value of the option settings.
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
        return "option setting " + getName();
    }
}
