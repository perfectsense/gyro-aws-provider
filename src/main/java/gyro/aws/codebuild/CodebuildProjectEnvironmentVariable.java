package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.codebuild.model.EnvironmentVariable;

public class CodebuildProjectEnvironmentVariable extends Diffable implements Copyable<EnvironmentVariable> {

    private String name;
    private String value;
    private String type;

    @Updatable
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Updatable
    @Required
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Updatable
    @ValidStrings({ "PLAINTEXT", "PARAMETER_STORE", "SECRETS_MANAGER" })
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void copyFrom(EnvironmentVariable model) {
        setName(model.name());
        setValue(model.value());
        setType(model.typeAsString());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
