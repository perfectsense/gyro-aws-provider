package gyro.aws.ec2;

import gyro.core.diff.Diffable;
import gyro.core.resource.ResourceDiffProperty;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.BlockDeviceMapping;
import software.amazon.awssdk.services.ec2.model.InstanceBlockDeviceMapping;
import software.amazon.awssdk.services.ec2.model.Volume;

public class BlockDeviceMappingResource extends Diffable {
    private String deviceName;
    private Boolean deleteOnTermination;
    private Boolean encrypted;
    private Integer iops;
    private String kmsKeyId;
    private Integer volumeSize;
    private String snapshotId;
    private String state;
    private String volumeId;
    private String volumeType;
    private Boolean autoEnableIo;
    private Boolean rootDevice;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Boolean getDeleteOnTermination() {
        if (deleteOnTermination == null) {
            deleteOnTermination = true;
        }

        return deleteOnTermination;
    }

    public void setDeleteOnTermination(Boolean deleteOnTermination) {
        this.deleteOnTermination = deleteOnTermination;
    }

    public Boolean getEncrypted() {
        if (encrypted == null) {
            encrypted = false;
        }

        return encrypted;
    }

    public void setEncrypted(Boolean encrypted) {
        this.encrypted = encrypted;
    }

    public Integer getIops() {
        return iops;
    }

    public void setIops(Integer iops) {
        this.iops = iops;
    }

    public String getKmsKeyId() {
        return kmsKeyId;
    }

    public void setKmsKeyId(String kmsKeyId) {
        this.kmsKeyId = kmsKeyId;
    }

    @ResourceDiffProperty(updatable = true)
    public Integer getVolumeSize() {
        return volumeSize;
    }

    public void setVolumeSize(Integer volumeSize) {
        this.volumeSize = volumeSize;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(String volumeId) {
        this.volumeId = volumeId;
    }

    @ResourceDiffProperty(updatable = true)
    public String getVolumeType() {
        if (volumeType == null) {
            volumeType = "gp2";
        }

        return volumeType;
    }

    public void setVolumeType(String volumeType) {
        this.volumeType = volumeType;
    }

    @ResourceDiffProperty(updatable = true)
    public Boolean getAutoEnableIo() {
        if (autoEnableIo == null) {
            autoEnableIo = false;
        }

        return autoEnableIo;
    }

    public void setAutoEnableIo(Boolean autoEnableIo) {
        this.autoEnableIo = autoEnableIo;
    }

    public Boolean getRootDevice() {
        return rootDevice;
    }

    public void setRootDevice(Boolean rootDevice) {
        this.rootDevice = rootDevice;
    }

    public BlockDeviceMappingResource() {

    }

    public BlockDeviceMappingResource(InstanceBlockDeviceMapping blockDeviceMapping, Volume volume, boolean isRootDevice) {
        setDeviceName(blockDeviceMapping.deviceName());
        setDeleteOnTermination(blockDeviceMapping.ebs().deleteOnTermination());
        setVolumeId(volume.volumeId());
        setEncrypted(volume.encrypted());
        setIops(volume.iops());
        setKmsKeyId(volume.kmsKeyId());
        setVolumeSize(volume.size());
        setSnapshotId(volume.snapshotId());
        setState(volume.stateAsString());
        setVolumeType(volume.volumeTypeAsString());
        setRootDevice(isRootDevice);
    }

    @Override
    public String primaryKey() {
        return getDeviceName();
    }

    @Override
    public String toDisplayString() {
        return String.format("Device%s - %s", getRootDevice()? " (root)" : "", getDeviceName());
    }

    BlockDeviceMapping getBlockDeviceMapping() {
        return BlockDeviceMapping.builder()
            .deviceName(getDeviceName())
            .ebs(
                e -> e.encrypted(getEncrypted())
                    .iops(getVolumeType().equals("io1") ? getIops() : null)
                    .kmsKeyId(getKmsKeyId())
                    .volumeSize(getVolumeSize())
                    .snapshotId(getSnapshotId())
                    .volumeType(getVolumeType())
                    .deleteOnTermination(getDeleteOnTermination())
            )
            .build();
    }

    software.amazon.awssdk.services.autoscaling.model.BlockDeviceMapping getBlockDeviceMappingAutoScale() {
        return software.amazon.awssdk.services.autoscaling.model.BlockDeviceMapping.builder()
            .deviceName(getDeviceName())
            .ebs(
                e -> e.encrypted(getEncrypted())
                    .iops(getVolumeType().equals("io1") ? getIops() : null)
                    .volumeSize(getVolumeSize())
                    .snapshotId(getSnapshotId())
                    .volumeType(getVolumeType())
                    .deleteOnTermination(getDeleteOnTermination())
            )
            .build();
    }

    void modifyVolume(Ec2Client client) {
        //Do something if this is the root device.

        client.modifyVolume(
            r -> r.volumeId(getVolumeId())
                .iops(getVolumeType().equals("io1") ? getIops() : null)
                .size(getVolumeSize())
                .volumeType(getVolumeType())
        );

        client.modifyVolumeAttribute(
            r -> r.volumeId(getVolumeId())
                .autoEnableIO(a -> a.value(getAutoEnableIo()))
        );
    }

    void removeClient(Ec2Client client, String instanceId) {
        //Do something if this is the root device.

        client.detachVolume(
            o -> o.device(getDeviceName())
                .instanceId(instanceId)
                .volumeId(getVolumeId())
        );

        if (!getDeleteOnTermination()) {
            client.deleteVolume(
                o -> o.volumeId(getVolumeId())
            );
        }
    }
}
