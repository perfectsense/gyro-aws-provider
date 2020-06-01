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

    /**
     * The path for the device on the host container instance. (Required)
     */
    @Required
    public String getHostPath() {
        return hostPath;
    }

    public void setHostPath(String hostPath) {
        this.hostPath = hostPath;
    }

    /**
     * The path inside the container at which to expose the host device.
     */
    public String getContainerPath() {
        return containerPath;
    }

    public void setContainerPath(String containerPath) {
        this.containerPath = containerPath;
    }

    /**
     * The explicit permissions to provide to the container for the device.
     * By default, the container has permissions for ``read``, ``write``, and ``mknod`` for the device.
     * Valid values are ``read``, ``write``, and ``mknod``.
     */
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
        // Duplicate entries supported by the API, but not Gyro
        return "";
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
