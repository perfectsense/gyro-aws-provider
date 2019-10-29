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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.ec2.model.BlockDeviceMapping;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateBlockDeviceMapping;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateBlockDeviceMappingRequest;

public class BlockDeviceMappingResource extends Diffable implements Copyable<BlockDeviceMapping> {
    private String deviceName;
    private Boolean deleteOnTermination;
    private Boolean encrypted;
    private Integer iops;
    private String kmsKeyId;
    private Integer volumeSize;
    private String snapshotId;
    private String state;
    private String volumeType;
    private Boolean autoEnableIo;

    /**
     * Name of the drive to attach this volume to. (Required)
     */
    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * Delete volume on instance termination. Defaults to true.
     */
    public Boolean getDeleteOnTermination() {
        if (deleteOnTermination == null) {
            deleteOnTermination = true;
        }

        return deleteOnTermination;
    }

    public void setDeleteOnTermination(Boolean deleteOnTermination) {
        this.deleteOnTermination = deleteOnTermination;
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
     * The number of I/O operations per second (IOPS) to provision for the volume.
     * Only allowed when 'volume-type' set to 'iops'.
     */
    public Integer getIops() {
        return iops;
    }

    public void setIops(Integer iops) {
        this.iops = iops;
    }

    /**
     * The kms key id, when using encrypted volume.
     */
    public String getKmsKeyId() {
        return kmsKeyId;
    }

    public void setKmsKeyId(String kmsKeyId) {
        this.kmsKeyId = kmsKeyId;
    }

    /**
     * The size of the volume in GiBs.
     */
    @Updatable
    public Integer getVolumeSize() {
        return volumeSize;
    }

    public void setVolumeSize(Integer volumeSize) {
        this.volumeSize = volumeSize;
    }

    /**
     * The snapshot from which to create the volume.
     */
    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    /**
     * The state of the drive.
     */
    @Output
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * The type of volume being created. Defaults to 'gp2'.
     * Valid values are ``gp2``, ``io1``, ``st1``, ``sc1`` or ``standard``.
     */
    @Updatable
    public String getVolumeType() {
        if (volumeType == null) {
            volumeType = "gp2";
        }

        return volumeType;
    }

    public void setVolumeType(String volumeType) {
        this.volumeType = volumeType;
    }

    /**
     * Auto Enable IO. Defaults to false.
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
    public String primaryKey() {
        return getDeviceName();
    }

    @Override
    public void copyFrom(BlockDeviceMapping blockDeviceMapping) {
        setDeviceName(blockDeviceMapping.deviceName());
        setDeleteOnTermination(blockDeviceMapping.ebs().deleteOnTermination());
        setEncrypted(blockDeviceMapping.ebs().encrypted());
        setIops(blockDeviceMapping.ebs().iops());
        setKmsKeyId(blockDeviceMapping.ebs().kmsKeyId());
        setVolumeSize(blockDeviceMapping.ebs().volumeSize());
        setSnapshotId(blockDeviceMapping.ebs().snapshotId());
        setVolumeType(blockDeviceMapping.ebs().volumeTypeAsString());
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

    LaunchTemplateBlockDeviceMappingRequest getLaunchTemplateBlockDeviceMapping() {
        return LaunchTemplateBlockDeviceMappingRequest.builder()
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

    public software.amazon.awssdk.services.autoscaling.model.BlockDeviceMapping getAutoscalingBlockDeviceMapping() {
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
}
