package gyro.aws.codebuild;

import java.util.ArrayList;
import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.codebuild.model.ProjectCache;

public class CodebuildProjectCache extends Diffable implements Copyable<ProjectCache> {

    private String type;
    private String location;
    private List<String> modes;

    @Updatable
    @Required
    @ValidStrings({ "NO_CACHE", "S3", "LOCAL" })
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
    @ValidStrings({"LOCAL_DOCKER_LAYER_CACHE", "LOCAL_SOURCE_CACHE", "LOCAL_CUSTOM_CACHE"})
    public List<String> getModes() {
        if (modes == null) {
            modes = new ArrayList<>();
        }
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
