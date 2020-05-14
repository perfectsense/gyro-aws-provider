package gyro.aws.ecs;

import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ecs.model.MountPoint;

public class EcsMountPoint extends Diffable {

    private String sourceVolume;
    private String containerPath;
    private Boolean readOnly;

    @Required
    public String getSourceVolume() {
        return sourceVolume;
    }

    public void setSourceVolume(String sourceVolume) {
        this.sourceVolume = sourceVolume;
    }

    @Required
    public String getContainerPath() {
        return containerPath;
    }

    public void setContainerPath(String containerPath) {
        this.containerPath = containerPath;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public String primaryKey() {
        return getContainerPath();
    }

    public void copyFrom(MountPoint model) {
        setSourceVolume(model.sourceVolume());
        setContainerPath(model.containerPath());
        setReadOnly(model.readOnly());
    }

    public MountPoint copyTo() {
        return MountPoint.builder()
            .sourceVolume(getSourceVolume())
            .containerPath(getContainerPath())
            .readOnly(getReadOnly())
            .build();
    }
}
