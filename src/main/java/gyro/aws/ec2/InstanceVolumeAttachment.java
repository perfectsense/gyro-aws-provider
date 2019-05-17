package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.core.GyroCore;
import gyro.core.Wait;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceUpdatable;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.VolumeState;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class InstanceVolumeAttachment extends AwsResource {
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

        if (!ObjectUtils.isBlank(getDeviceName())) {
            sb.append(" ").append(getDeviceName());
        }

        if (!ObjectUtils.isBlank(getVolumeId())) {
            sb.append(" with volume id : ").append(getVolumeId());
        }

        return sb.toString();
    }

    @Override
    public String primaryKey() {
        return String.format("%s", getDeviceName());
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void create() {
        Ec2Client client = createClient(Ec2Client.class);

        InstanceResource parent = (InstanceResource) parent();

        client.attachVolume(
            r -> r.device(getDeviceName())
                .volumeId(getVolumeId())
                .instanceId(parent.getInstanceId())
        );
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        Ec2Client client = createClient(Ec2Client.class);

        InstanceResource parent = (InstanceResource) parent();

        InstanceVolumeAttachment currentAttachment = (InstanceVolumeAttachment) current;

        client.detachVolume(
            r -> r.device(getDeviceName())
                .volumeId(currentAttachment.getVolumeId())
                .instanceId(parent.getInstanceId())
        );

        boolean success = Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(5, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> client.describeVolumes(r -> r.volumeIds(Collections.singleton(currentAttachment.getVolumeId())))
                .volumes().get(0).state().equals(VolumeState.AVAILABLE));

        if (success) {
            client.attachVolume(
                r -> r.device(getDeviceName())
                    .volumeId(getVolumeId())
                    .instanceId(parent.getInstanceId())
            );
        } else {
            GyroCore.ui().write("\n@|bold,blue Skipping adding volume since volume delete is still pending for the instance");
        }
    }

    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);

        InstanceResource parent = (InstanceResource) parent();

        client.detachVolume(
            r -> r.device(getDeviceName())
                .volumeId(getVolumeId())
                .instanceId(parent.getInstanceId())
        );
    }
}
