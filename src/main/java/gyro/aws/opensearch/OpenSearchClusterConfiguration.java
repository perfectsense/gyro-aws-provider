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
import gyro.core.validation.DependsOn;
import gyro.core.validation.Min;
import gyro.core.validation.Range;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.opensearch.model.ClusterConfig;
import software.amazon.awssdk.services.opensearch.model.OpenSearchPartitionInstanceType;
import software.amazon.awssdk.services.opensearch.model.OpenSearchWarmPartitionInstanceType;

public class OpenSearchClusterConfiguration extends Diffable implements Copyable<ClusterConfig> {

    private Boolean enableZoneAwareness;
    private OpenSearchZoneAwarenessConfiguration zoneAwarenessConfiguration;
    private OpenSearchPartitionInstanceType instanceType;
    private Integer instanceCount;
    private Boolean dedicatedMasterEnabled;
    private OpenSearchPartitionInstanceType dedicatedMasterType;
    private Integer dedicatedMasterCount;
    private Boolean enableWarm;
    private Integer warmCount;
    private OpenSearchWarmPartitionInstanceType warmType;

    /**
     * When set to ``true``, zone awareness is enabled for the OpenSearch domain cluster.
     */
    @Updatable
    public Boolean getEnableZoneAwareness() {
        return enableZoneAwareness;
    }

    public void setEnableZoneAwareness(Boolean enableZoneAwareness) {
        this.enableZoneAwareness = enableZoneAwareness;
    }

    /**
     * The zone awareness options configuration.
     *
     * @subresource gyro.aws.opensearch.OpenSearchZoneAwarenessConfiguration
     */
    @Updatable
    @DependsOn("enable-zone-awareness")
    public OpenSearchZoneAwarenessConfiguration getZoneAwarenessConfiguration() {
        return zoneAwarenessConfiguration;
    }

    public void setZoneAwarenessConfiguration(OpenSearchZoneAwarenessConfiguration zoneAwarenessConfiguration) {
        this.zoneAwarenessConfiguration = zoneAwarenessConfiguration;
    }

    /**
     * The instance type for the OpenSearch domain cluster. Defaults to ``m4.large_elasticsearch``.
     */
    @Updatable
    public OpenSearchPartitionInstanceType getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(OpenSearchPartitionInstanceType instanceType) {
        this.instanceType = instanceType;
    }

    /**
     * The number of nodes in the specified domain cluster. Defaults to ``1``.
     */
    @Updatable
    @Range(min = 0, max = 40)
    public Integer getInstanceCount() {
        return instanceCount;
    }

    public void setInstanceCount(Integer instanceCount) {
        this.instanceCount = instanceCount;
    }

    /**
     * When set to ``true``, master nodes are dedicated to the OpenSearch domain cluster. Defaults to ``false``.
     */
    @Updatable
    public Boolean getDedicatedMasterEnabled() {
        return dedicatedMasterEnabled;
    }

    public void setDedicatedMasterEnabled(Boolean dedicatedMasterEnabled) {
        this.dedicatedMasterEnabled = dedicatedMasterEnabled;
    }

    /**
     * The instance type for the dedicated master nodes. Defaults to ``m4.large_elasticsearch``.
     */
    @Updatable
    @DependsOn("dedicated-master-enabled")
    public OpenSearchPartitionInstanceType getDedicatedMasterType() {
        return dedicatedMasterType;
    }

    public void setDedicatedMasterType(OpenSearchPartitionInstanceType dedicatedMasterType) {
        this.dedicatedMasterType = dedicatedMasterType;
    }

    /**
     * The number of dedicated master nodes for the cluster. Defaults to ``3``.
     */
    @Updatable
    @DependsOn("dedicated-master-enabled")
    @Range(min = 2, max = 5)
    public Integer getDedicatedMasterCount() {
        return dedicatedMasterCount;
    }

    public void setDedicatedMasterCount(Integer dedicatedMasterCount) {
        this.dedicatedMasterCount = dedicatedMasterCount;
    }

    /**
     * When set to ``true``, warm nodes are enabled for the OpenSearch domain cluster. Defaults to ``false``.
     */
    @Updatable
    public Boolean getEnableWarm() {
        return enableWarm;
    }

    public void setEnableWarm(Boolean enableWarm) {
        this.enableWarm = enableWarm;
    }

    /**
     * The number of warm nodes in the cluster. Defaults to ``3``.
     */
    @Min(3)
    @Updatable
    @DependsOn("enable-warm")
    public Integer getWarmCount() {
        return warmCount;
    }

    public void setWarmCount(Integer warmCount) {
        this.warmCount = warmCount;
    }

    /**
     * The instance type for warm nodes. Defaults to ``ultrawarm1.medium.elasticsearch``.
     */
    @Updatable
    @DependsOn("enable-warm")
    @ValidStrings({ "ultrawarm1.medium.elasticsearch", "ultrawarm1.large.elasticsearch", "ultrawarm1.xlarge.search" })
    public OpenSearchWarmPartitionInstanceType getWarmType() {
        return warmType;
    }

    public void setWarmType(OpenSearchWarmPartitionInstanceType warmType) {
        this.warmType = warmType;
    }

    @Override
    public void copyFrom(ClusterConfig model) {
        setDedicatedMasterCount(model.dedicatedMasterCount());
        setDedicatedMasterEnabled(model.dedicatedMasterEnabled());
        setDedicatedMasterType(model.dedicatedMasterType());
        setEnableWarm(model.warmEnabled());
        setWarmCount(model.warmCount());
        setWarmType(model.warmType());
        setEnableZoneAwareness(model.zoneAwarenessEnabled());
        setInstanceCount(model.instanceCount());
        setInstanceType(model.instanceType());

        setZoneAwarenessConfiguration(null);
        if (model.zoneAwarenessConfig() != null) {
            OpenSearchZoneAwarenessConfiguration configuration = newSubresource(
                OpenSearchZoneAwarenessConfiguration.class);
            configuration.copyFrom(model.zoneAwarenessConfig());
            setZoneAwarenessConfiguration(configuration);
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    ClusterConfig toOpenSearchClusterConfig() {
        ClusterConfig.Builder builder = ClusterConfig.builder()
            .zoneAwarenessEnabled(getEnableZoneAwareness())
            .instanceType(getInstanceType())
            .instanceCount(getInstanceCount())
            .dedicatedMasterEnabled(getDedicatedMasterEnabled())
            .dedicatedMasterType(getDedicatedMasterType())
            .dedicatedMasterCount(getDedicatedMasterCount())
            .warmEnabled(getEnableWarm())
            .warmType(getWarmType())
            .warmCount(getWarmCount());

        if (getEnableZoneAwareness() != null) {
            builder = builder.zoneAwarenessConfig(getZoneAwarenessConfiguration().toZoneAwarenessConfig());
        }

        return builder.build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (!Boolean.TRUE.equals(getEnableZoneAwareness()) &&
            configuredFields.contains("zone-awareness-configuration")) {
            errors.add(new ValidationError(
                this,
                null,
                "The 'zone-awareness-configuration' can only be set if 'enable-zone-awareness' is set to 'true'."));
        }

        if (!Boolean.TRUE.equals(getDedicatedMasterEnabled())
            && (configuredFields.contains("dedicated-master-type")
            || configuredFields.contains("dedicated-master-count"))) {
            errors.add(new ValidationError(
                this,
                null,
                "The 'dedicated-master-count' and 'dedicated-master-type' can only be set if 'dedicated-master-enabled' is set to 'true'."));
        }

        if (!Boolean.TRUE.equals(getEnableWarm()) && (
            configuredFields.contains("warm-type") || configuredFields.contains("warm-count"))) {
            errors.add(new ValidationError(
                this,
                null,
                "The 'warm-count' and 'warm-type' can only be set if 'enable-warm' is set to 'true'."));
        }

        return errors;
    }
}
