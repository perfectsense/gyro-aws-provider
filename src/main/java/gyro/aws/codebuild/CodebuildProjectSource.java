package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.codebuild.model.ProjectSource;

public class CodebuildProjectSource extends Diffable implements Copyable<ProjectSource> {

    private String type;
    private String location;

    @Updatable
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Updatable
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public void copyFrom(ProjectSource model) {
        setType(model.typeAsString());
        setLocation(model.location());
    }

    @Override
    public String primaryKey() {
        return "project source";
    }
}
