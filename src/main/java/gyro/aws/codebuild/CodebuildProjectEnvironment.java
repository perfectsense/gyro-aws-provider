package gyro.aws.codebuild;

import java.util.ArrayList;
import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.codebuild.model.ProjectEnvironment;

public class CodebuildProjectEnvironment extends Diffable implements Copyable<ProjectEnvironment> {

    private String computeType;
    private String image;
    private String type;

    @Updatable
    public String getComputeType() {
        return computeType;
    }

    public void setComputeType(String computeType) {
        this.computeType = computeType;
    }

    @Updatable
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Updatable
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void copyFrom(ProjectEnvironment model) {
        setComputeType(model.computeTypeAsString());
        setImage(model.image());
        setType(model.typeAsString());
    }

    @Override
    public String primaryKey() {
        return null;
    }
}
