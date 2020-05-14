package gyro.aws.ecs;

import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ecs.model.VolumeFrom;

public class EcsVolumeFrom extends Diffable {

    private String sourceContainer;
    private Boolean readOnly;

    @Required
    public String getSourceContainer() {
        return sourceContainer;
    }

    public void setSourceContainer(String sourceContainer) {
        this.sourceContainer = sourceContainer;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public String primaryKey() {
        return null;
    }

    public void copyFrom(VolumeFrom model) {
        setSourceContainer(model.sourceContainer());
        setReadOnly(model.readOnly());
    }

    public VolumeFrom copyTo() {
        return VolumeFrom.builder()
            .sourceContainer(getSourceContainer())
            .readOnly(getReadOnly())
            .build();
    }
}
