package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
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

    /**
     * Device name to attach the volume to. (Required)
     */
    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * The volume id from the volume to be attached. (Required)
     */
    @ResourceUpdatable
    public String getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(String volumeId) {
        this.volumeId = volumeId;
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Device");

        if (ObjectUtils.isBlank(getDeviceName())) {
            sb.append(" ").append(getDeviceName());
        }

        if (!ObjectUtils.isBlank(getVolumeId())) {
            sb.append(" with volume id : ").append(getVolumeId());
        }

        return sb.toString();
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s", getDeviceName(), getVolumeId());
    }
}
