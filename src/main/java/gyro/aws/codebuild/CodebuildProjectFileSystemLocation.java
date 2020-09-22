package gyro.aws.codebuild;

import java.util.ArrayList;
import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.codebuild.model.ProjectFileSystemLocation;

public class CodebuildProjectFileSystemLocation extends Diffable implements Copyable<ProjectFileSystemLocation> {

    private String identifier;
    private String location;
    private String mountOptions;
    private String mountPoint;
    private String type;

    @Updatable
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Updatable
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Updatable
    public String getMountOptions() {
        return mountOptions;
    }

    public void setMountOptions(String mountOptions) {
        this.mountOptions = mountOptions;
    }

    @Updatable
    public String getMountPoint() {
        return mountPoint;
    }

    public void setMountPoint(String mountPoint) {
        this.mountPoint = mountPoint;
    }

    @Updatable
    @ValidStrings("EFS")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void copyFrom(ProjectFileSystemLocation model) {
        setIdentifier(model.identifier());
        setLocation(model.location());
        setMountOptions(model.mountOptions());
        setMountPoint(model.mountPoint());
        setType(model.typeAsString());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public ProjectFileSystemLocation toProjectFileSystemLocation() {
        return ProjectFileSystemLocation.builder()
            .identifier(getIdentifier())
            .location(getLocation())
            .mountOptions(getMountOptions())
            .mountPoint(getMountPoint())
            .type(getType())
            .build();
    }
}
