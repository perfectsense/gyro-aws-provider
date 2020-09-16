package gyro.aws.codebuild;

import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.codebuild.model.ProjectCache;

public class CodebuildProjectCache extends Diffable implements Copyable<ProjectCache> {

    private String type;
    private String location;
    private List<String> modes;

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

    @Updatable
    public List<String> getModes() {
        return modes;
    }

    public void setModes(List<String> modes) {
        this.modes = modes;
    }

    @Override
    public void copyFrom(ProjectCache model) {
        setType(model.typeAsString());
        setLocation(model.location());
        setModes(model.modesAsStrings());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public ProjectCache toProjectCache() {
        return ProjectCache.builder()
            .type(getType())
            .location(getLocation())
            .modesWithStrings(getModes())
            .build();
    }
}
