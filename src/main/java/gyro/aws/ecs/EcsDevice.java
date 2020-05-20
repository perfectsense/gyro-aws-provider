package gyro.aws.ecs;

import java.util.ArrayList;
import java.util.List;

import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.ecs.model.Device;

public class EcsDevice extends Diffable {

    private String hostPath;
    private String containerPath;
    private List<String> permissions;

    @Required
    public String getHostPath() {
        return hostPath;
    }

    public void setHostPath(String hostPath) {
        this.hostPath = hostPath;
    }

    public String getContainerPath() {
        return containerPath;
    }

    public void setContainerPath(String containerPath) {
        this.containerPath = containerPath;
    }

    @ValidStrings({"read", "write", "mknod"})
    public List<String> getPermissions() {
        if (permissions == null) {
            permissions = new ArrayList<>();
        }

        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    @Override
    public String primaryKey() {
        return null;
    }

    public void copyFrom(Device model) {
        setHostPath(model.hostPath());
        setContainerPath(model.containerPath());
        setPermissions(model.permissionsAsStrings());
    }

    public Device copyTo() {
        return Device.builder()
            .hostPath(getHostPath())
            .containerPath(getContainerPath())
            .permissionsWithStrings(getPermissions())
            .build();
    }
}
