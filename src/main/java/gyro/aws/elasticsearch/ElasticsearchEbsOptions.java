/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws.elasticsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Range;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.elasticsearch.model.EBSOptions;
import software.amazon.awssdk.services.elasticsearch.model.VolumeType;

public class ElasticsearchEbsOptions extends Diffable implements Copyable<EBSOptions> {

    private Boolean enableEbs;
    private VolumeType volumeType;
    private Integer volumeCount;
    private Integer iops;

    /**
     * Enable the EBS volume, a block level storage device. (Required)
     */
    @Updatable
    @Required
    public Boolean getEnableEbs() {
        return enableEbs;
    }

    public void setEnableEbs(Boolean enableEbs) {
        this.enableEbs = enableEbs;
    }

    /**
     * The volume type for the EBS-based storage.
     */
    @Updatable
    public VolumeType getVolumeType() {
        return volumeType;
    }

    public void setVolumeType(VolumeType volumeType) {
        this.volumeType = volumeType;
    }

    /**
     * The size of the EBS volume. Valid values belong between ``10`` to ``1024``.
     */
    @Updatable
    @Range(min = 10, max = 1024)
    public Integer getVolumeCount() {
        return volumeCount;
    }

    public void setVolumeCount(Integer volumeCount) {
        this.volumeCount = volumeCount;
    }

    /**
     * The baseline I/O performance for the EBS volume. Only used by Provisioned IOPS volumes. Valid values between ``1000`` to ``16000``.
     */
    @Updatable
    @Range(min = 1000, max = 16000)
    public Integer getIops() {
        return iops;
    }

    public void setIops(Integer iops) {
        this.iops = iops;
    }

    @Override
    public void copyFrom(EBSOptions model) {
        setEnableEbs(model.ebsEnabled());
        setIops(model.iops());
        setVolumeCount(model.volumeSize());
        setVolumeType(model.volumeType());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    EBSOptions toEBSOptions() {
        EBSOptions.Builder builder = EBSOptions.builder().ebsEnabled(getEnableEbs());

        if (getVolumeType() != null) {
            builder = builder.volumeType(getVolumeType());
        }

        if (getVolumeCount() != null) {
            builder = builder.volumeSize(getVolumeCount());
        }

        if (getIops() != null) {
            builder = builder.iops(getIops());
        }

        return builder.build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getEnableEbs().equals(Boolean.FALSE) && (configuredFields.contains("volume-type")
            || configuredFields.contains("volume-count")
            || configuredFields.contains("iops"))) {
            errors.add(new ValidationError(
                this,
                null,
                "The 'volume-count', 'volume-type' and 'iops' can only be set if 'enable-ebs' is set to 'true'."));
        }

        if (getEnableEbs().equals(Boolean.TRUE) && !(configuredFields.contains("volume-type")
            && configuredFields.contains("volume-count"))) {
            errors.add(new ValidationError(
                this,
                null,
                "The 'volume-count' and 'volume-type' are required if 'enable-ebs' is set to 'true'."));
        }

        return errors;
    }
}
