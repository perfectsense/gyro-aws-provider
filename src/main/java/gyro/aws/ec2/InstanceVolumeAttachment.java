package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Wait;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.InstanceBlockDeviceMapping;
import software.amazon.awssdk.services.ec2.model.VolumeState;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class InstanceVolumeAttachment extends AwsResource implements Copyable<InstanceBlockDeviceMapping> {

    private String deviceName;
    private EbsVolumeResource volume;

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
     * The volume to be attached. (Required)
     */
    @Updatable
    public EbsVolumeResource getVolume() {
        return volume;
    }

    public void setVolume(EbsVolumeResource volume) {
        this.volume = volume;
    }

    @Override
    public void copyFrom(InstanceBlockDeviceMapping instanceBlockDeviceMapping) {
        this.deviceName = instanceBlockDeviceMapping.deviceName();
        this.volume = findById(EbsVolumeResource.class,instanceBlockDeviceMapping.ebs().volumeId());
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        InstanceResource parent = (InstanceResource) parent();

        client.attachVolume(
            r -> r.device(getDeviceName())
                .volumeId(getVolume().getVolumeId())
                .instanceId(parent.getInstanceId())
        );
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Ec2Client client = createClient(Ec2Client.class);

        InstanceResource parent = (InstanceResource) parent();

        InstanceVolumeAttachment currentAttachment = (InstanceVolumeAttachment) current;

        client.detachVolume(
            r -> r.device(getDeviceName())
                .volumeId(currentAttachment.getVolume().getVolumeId())
                .instanceId(parent.getInstanceId())
        );

        boolean success = Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(5, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> client.describeVolumes(r -> r.volumeIds(Collections.singleton(currentAttachment.getVolume().getVolumeId())))
                .volumes().get(0).state().equals(VolumeState.AVAILABLE));

        if (success) {
            client.attachVolume(
                r -> r.device(getDeviceName())
                    .volumeId(getVolume().getVolumeId())
                    .instanceId(parent.getInstanceId())
            );
        } else {
            ui.write("\n@|bold,blue Skipping adding volume since volume delete is still pending for the instance");
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        InstanceResource parent = (InstanceResource) parent();

        client.detachVolume(
            r -> r.device(getDeviceName())
                .volumeId(getVolume().getVolumeId())
                .instanceId(parent.getInstanceId())
        );
    }

    @Override
    public String primaryKey() {
        return String.format("%s", getDeviceName());
    }
}
