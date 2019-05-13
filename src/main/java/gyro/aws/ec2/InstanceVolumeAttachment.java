package gyro.aws.ec2;

import gyro.core.resource.Diffable;
import gyro.core.resource.ResourceUpdatable;

public class InstanceVolumeAttachment extends Diffable {
    private String deviceName;

    private String volumeId;

    public InstanceVolumeAttachment() {

    }

    public InstanceVolumeAttachment(String deviceName, String volumeId) {
        this.deviceName = deviceName;

        this.volumeId = volumeId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @ResourceUpdatable
    public String getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(String volumeId) {
        this.volumeId = volumeId;
    }

    @Override
    public String toDisplayString() {
        return String.format("Device %s with volume id : %s", getDeviceName(), getVolumeId());
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s", getDeviceName(), getVolumeId());
    }
}
