package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
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
        return "project file system location";
    }
}
