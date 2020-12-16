/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVolumesResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceBlockDeviceMapping;
import software.amazon.awssdk.services.ec2.model.InstanceStateName;
import software.amazon.awssdk.services.ec2.model.Volume;
import software.amazon.awssdk.services.ec2.model.VolumeAttachment;
import software.amazon.awssdk.services.ec2.model.VolumeAttachmentState;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static software.amazon.awssdk.services.ec2.model.VolumeAttachmentState.ATTACHED;
import static software.amazon.awssdk.services.ec2.model.VolumeAttachmentState.DETACHED;

/**
 * Attach a Volume to an Instance.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::instance-volume-attachment attachment
 *         device-name: "/dev/sde"
 *         volume: $(aws::ebs-volume volume)
 *         instance: $(aws::instance instance-example)
 *     end
 */
@Type("instance-volume-attachment")
public class InstanceVolumeAttachmentResource extends AwsResource {

    private String deviceName;
    private EbsVolumeResource volume;
    private InstanceResource instance;

    /**
     * Device name to attach the volume to.
     */
    @Required
    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * The volume to be attached.
     */
    @Required
    public EbsVolumeResource getVolume() {
        return volume;
    }

    public void setVolume(EbsVolumeResource volume) {
        this.volume = volume;
    }

    /**
     * The Instance the volume should attach to.
     */
    @Required
    public InstanceResource getInstance() {
        return instance;
    }

    public void setInstance(InstanceResource instance) {
        this.instance = instance;
    }

    @Override
    public boolean refresh() {
        Ec2Client client = createClient(Ec2Client.class);

        Instance instance = getInstance(client);

        if (instance == null) {
            return false;
        }

        InstanceBlockDeviceMapping blockDeviceMapping = instance.blockDeviceMappings().stream()
            .filter(o -> o.deviceName().equals(getDeviceName()) && o.ebs().volumeId().equals(getVolume().getId()))
            .findFirst().orElse(null);

        return blockDeviceMapping != null;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        client.attachVolume(
            r -> r.device(getDeviceName())
                .volumeId(getVolume().getId())
                .instanceId(getInstance().getId())
        );

        Wait.atMost(120, TimeUnit.SECONDS)
            .prompt(false)
            .checkEvery(2, TimeUnit.SECONDS)
            .until(() -> getAttachmentState(client) == ATTACHED);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {

    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        client.detachVolume(
            r -> r.device(getDeviceName())
                .volumeId(getVolume().getId())
                .instanceId(getInstance().getId())
        );

        Wait.atMost(120, TimeUnit.SECONDS)
            .prompt(false)
            .checkEvery(2, TimeUnit.SECONDS)
            .until(() -> getAttachmentState(client) == DETACHED);
    }

    private Instance getInstance(Ec2Client client) {
        Instance instance = null;

        try {
            DescribeInstancesResponse response = client.describeInstances(r -> r.instanceIds(getInstance().getId()));

            if (!response.reservations().isEmpty() && !response.reservations().get(0).instances().isEmpty()) {
                instance = response.reservations().get(0).instances().get(0);

                if (instance.state().name().equals(InstanceStateName.TERMINATED) ||  instance.state().name().equals(InstanceStateName.SHUTTING_DOWN)) {
                    instance = null;
                }
            }
        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return instance;
    }

    private VolumeAttachmentState getAttachmentState(Ec2Client client) {
        DescribeVolumesResponse response = client.describeVolumes(r -> r.volumeIds(getVolume().getId()));
        for (Volume volume : response.volumes()) {
            for (VolumeAttachment attachment : volume.attachments()) {
                if (attachment.device().equals(getDeviceName())) {
                    return attachment.state();
                }
            }
        }

        return DETACHED;
    }
}
