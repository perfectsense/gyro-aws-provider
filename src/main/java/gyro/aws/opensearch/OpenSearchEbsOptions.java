/*
 * Copyright 2024, Brightspot.
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

package gyro.aws.opensearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Range;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.opensearch.model.EBSOptions;
import software.amazon.awssdk.services.opensearch.model.VolumeType;

public class OpenSearchEbsOptions extends Diffable implements Copyable<EBSOptions> {

    private Boolean enableEbs;
    private VolumeType volumeType;
    private Integer volumeCount;
    private Integer iops;
    private Integer throughput;

    /**
     * When set to ``true``, EBS volumes are attached to data nodes in an OpenSearch Service domain
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
     * The volume type for the EBS-based storage. Can only be set if ``enable-ebs`` is set to ``true``.
     */
    @Updatable
    @ValidStrings({ "standard", "gp2", "gp3", "io1" })
    public VolumeType getVolumeType() {
        return volumeType;
    }

    public void setVolumeType(VolumeType volumeType) {
        this.volumeType = volumeType;
    }

    /**
     * The size of the EBS volume. Can only be set if ``enable-ebs`` is set to ``true``.
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
     * The baseline I/O performance for the EBS volume. Only used by Provisioned IOPS volumes. Can only be set if ``enable-ebs`` is set to ``true``.
     */
    @Updatable
    @Range(min = 1000, max = 16000)
    public Integer getIops() {
        return iops;
    }

    public void setIops(Integer iops) {
        this.iops = iops;
    }

    /**
     * The throughput for the EBS volume. Can only be set if ``enable-ebs`` is set to ``true``.
     */
    @Updatable
    @Range(min = 125, max = 1000)
    public Integer getThroughput() {
        return throughput;
    }

    public void setThroughput(Integer throughput) {
        this.throughput = throughput;
    }

    @Override
    public void copyFrom(EBSOptions model) {
        setEnableEbs(model.ebsEnabled());
        setIops(model.iops());
        setVolumeCount(model.volumeSize());
        setVolumeType(model.volumeType());
        setThroughput(model.throughput());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    EBSOptions toEBSOptions() {
        return EBSOptions.builder().ebsEnabled(getEnableEbs())
            .volumeType(getVolumeType())
            .volumeSize(getVolumeCount())
            .iops(getIops())
            .throughput(getThroughput())
            .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (!Boolean.TRUE.equals(getEnableEbs()) && (configuredFields.contains("volume-type")
            || configuredFields.contains("volume-count")
            || configuredFields.contains("iops")
            || configuredFields.contains("throughput"))) {
            errors.add(new ValidationError(
                this,
                null,
                "The 'volume-count', 'volume-type', 'throughput' and 'iops' can only be set if 'enable-ebs' is set to 'true'."));
        }

        if (Boolean.TRUE.equals(getEnableEbs()) && !(configuredFields.contains("volume-type")
            && configuredFields.contains("volume-count"))) {
            errors.add(new ValidationError(
                this,
                null,
                "The 'volume-count' and 'volume-type' are required if 'enable-ebs' is set to 'true'."));
        }

        return errors;
    }
}
