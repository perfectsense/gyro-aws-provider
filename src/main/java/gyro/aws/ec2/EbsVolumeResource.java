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

import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.kms.KmsKeyResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.TimeoutSettings;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateVolumeResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVolumeAttributeResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVolumesResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Volume;
import software.amazon.awssdk.services.ec2.model.VolumeAttributeName;
import software.amazon.awssdk.services.ec2.model.VolumeState;

/**
 * Creates a EBS Volume.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::ebs-volume ebs-volume-example
 *         availability-zone: "us-east-2a"
 *         size: 100
 *         auto-enable-io: false
 *         tags: {
 *             Name: "ebs-volume-example"
 *         }
 *     end
 */
@Type("ebs-volume")
public class EbsVolumeResource extends Ec2TaggableResource<Volume> implements Copyable<Volume> {

    private String availabilityZone;
    private Boolean encrypted;
    private Integer iops;
    private KmsKeyResource kms;
    private Integer size;
    private EbsSnapshotResource snapshot;
    private String volumeType;
    private Boolean autoEnableIo;

    // Read-only
    private Date createTime;
    private String id;
    private String state;

    /**
     * The availability zone for the volume being created.
     */
    @Required
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * The creation time for the volume.
     */
    @Output
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * Should the volume be encrypted. Defaults to false.
     */
    public Boolean getEncrypted() {
        if (encrypted == null) {
            encrypted = false;
        }

        return encrypted;
    }

    public void setEncrypted(Boolean encrypted) {
        this.encrypted = encrypted;
    }

    /**
     * The number of I/O operations per second (IOPS) to provision for the volume. Only allowed when 'volume-type' set to ``io1``.
     */
    @Updatable
    public Integer getIops() {
        return iops;
    }

    public void setIops(Integer iops) {
        this.iops = iops;
    }

    /**
     * The kms, when using encrypted volume.
     */
    public KmsKeyResource getKms() {
        return kms;
    }

    public void setKms(KmsKeyResource kms) {
        this.kms = kms;
    }

    /**
     * The size of the volume in GiBs.
     */
    @Updatable
    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * The snapshot from which to create the volume. Required if size is not mentioned.
     */
    public EbsSnapshotResource getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(EbsSnapshotResource snapshot) {
        this.snapshot = snapshot;
    }

    /**
     * The state of the volume.
     */
    @Output
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * The ID of the volume.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The type of volume being created. Defaults to 'gp3'.
     */
    @Updatable
    @ValidStrings({"gp2", "gp3", "io1", "st1", "sc1", "standard"})
    public String getVolumeType() {
        if (volumeType == null) {
            volumeType = "gp3";
        }

        return volumeType;
    }

    public void setVolumeType(String volumeType) {
        this.volumeType = volumeType;
    }

    /**
     * When set to ``true``, IO is auto enabled. Defaults to false.
     */
    @Updatable
    public Boolean getAutoEnableIo() {
        if (autoEnableIo == null) {
            autoEnableIo = false;
        }

        return autoEnableIo;
    }

    public void setAutoEnableIo(Boolean autoEnableIo) {
        this.autoEnableIo = autoEnableIo;
    }

    @Override
    public void copyFrom(Volume volume) {
        setId(volume.volumeId());
        setAvailabilityZone(volume.availabilityZone());
        setCreateTime(Date.from(volume.createTime()));
        setEncrypted(volume.encrypted());
        setIops(volume.iops());
        setKms(!ObjectUtils.isBlank(volume.kmsKeyId()) ? findById(KmsKeyResource.class, volume.kmsKeyId()) : null);
        setSize(volume.size());
        setSnapshot(!ObjectUtils.isBlank(volume.snapshotId())
            ? findById(EbsSnapshotResource.class, volume.snapshotId()) : null);
        setState(volume.stateAsString());
        setVolumeType(volume.volumeTypeAsString());

        Ec2Client client = createClient(Ec2Client.class);

        DescribeVolumeAttributeResponse responseAutoEnableIo = client.describeVolumeAttribute(
            r -> r.volumeId(getId())
                .attribute(VolumeAttributeName.AUTO_ENABLE_IO)
        );

        setAutoEnableIo(responseAutoEnableIo.autoEnableIO().value());

        refreshTags();
    }

    @Override
    protected boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        Volume volume = getVolume(client);

        if (volume == null) {
            return false;
        }

        copyFrom(volume);

        return true;
    }

    @Override
    protected String getResourceId() {
        return getId();
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        validate(true);

        CreateVolumeResponse response = client.createVolume(
            r -> r.availabilityZone(getAvailabilityZone()).encrypted(getEncrypted())
                .iops(getVolumeType().equals("io1") ? getIops() : null)
                .kmsKeyId(getKms() != null ? getKms().getId() : null)
                .size(getSize()).snapshotId(getSnapshot() != null ? getSnapshot().getId() : null)
                .volumeType(getVolumeType())
        );

        setId(response.volumeId());
        setCreateTime(Date.from(response.createTime()));
        setState(response.stateAsString());

        state.save();

        if (getAutoEnableIo()) {
            try {
                client.modifyVolumeAttribute(r -> r.volumeId(getId()).autoEnableIO(a -> a.value(getAutoEnableIo())));
            } catch (Exception ex) {
                ui.write("\n@|bold,blue EBS Volume resource - error enabling "
                    + "'auto enable io' to volume with Id - %s. |@", getId());
                ui.write("\n@|bold,blue Error message - %s |@", ex.getMessage());
                ui.write("\n@|bold,blue Please retry to enable 'auto enable io' again. |@");
            }
        }

        boolean waitResult = Wait.atMost(40, TimeUnit.SECONDS).checkEvery(5, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.CREATE)
            .prompt(false).until(() -> isAvailable(client));

        if (!waitResult) {
            throw new GyroException("Unable to reach 'available' state for ebs volume - " + getId());
        }
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        validate(false);

        if (changedProperties.contains("iops") || changedProperties.contains("size")
            || changedProperties.contains("volume-type")) {
            client.modifyVolume(r -> r.volumeId(getId()).iops(getVolumeType().equals("io1") ? getIops() : null)
                .size(getSize()).volumeType(getVolumeType())
            );
        }

        if (changedProperties.contains("auto-enable-io")) {
            client.modifyVolumeAttribute(r -> r.volumeId(getId()).autoEnableIO(a -> a.value(getAutoEnableIo())));
        }

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.UPDATE)
            .prompt(true)
            .until(() -> isAvailable(client));
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteVolume(
            r -> r.volumeId(getId())
        );
    }

    private Volume getVolume(Ec2Client client) {
        Volume volume = null;

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load volume.");
        }

        try {
            DescribeVolumesResponse response = client.describeVolumes(
                r -> r.volumeIds(Collections.singleton(getId()))
            );

            if (!response.volumes().isEmpty()) {
                volume = response.volumes().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return volume;
    }

    private void validate(boolean isCreate) {
        if (!getVolumeType().equals("io1") && isCreate && getIops() != null) {
            throw new GyroException("The param 'iops' can only be set when param 'volume-type' is set to 'io1'.");
        }
    }

    private boolean isAvailable(Ec2Client client) {
        Volume volume = getEc2Volume(client);

        return volume != null && volume.state().equals(VolumeState.AVAILABLE);
    }

    private Volume getEc2Volume(Ec2Client client) {
        Volume volume = null;

        try {
            DescribeVolumesResponse response = client.describeVolumes(r -> r.volumeIds(Collections.singletonList(getId())));

            if (!response.volumes().isEmpty()) {
                volume = response.volumes().get(0);
            }
        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return volume;
    }
}
