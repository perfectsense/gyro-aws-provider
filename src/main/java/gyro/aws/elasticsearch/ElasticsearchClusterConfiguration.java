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
import gyro.core.validation.DependsOn;
import gyro.core.validation.Min;
import gyro.core.validation.Range;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.elasticsearch.model.ESPartitionInstanceType;
import software.amazon.awssdk.services.elasticsearch.model.ESWarmPartitionInstanceType;
import software.amazon.awssdk.services.elasticsearch.model.ElasticsearchClusterConfig;

public class ElasticsearchClusterConfiguration extends Diffable implements Copyable<ElasticsearchClusterConfig> {

    private Boolean enableZoneAwareness;
    private ElasticsearchZoneAwarenessConfiguration zoneAwarenessConfiguration;
    private ESPartitionInstanceType instanceType;
    private Integer instanceCount;
    private Boolean dedicatedMasterEnabled;
    private ESPartitionInstanceType dedicatedMasterType;
    private Integer dedicatedMasterCount;
    private Boolean enableWarm;
    private Integer warmCount;
    private ESWarmPartitionInstanceType warmType;

    /**
     * Enable zone awareness configuration.
     */
    @Updatable
    public Boolean getEnableZoneAwareness() {
        return enableZoneAwareness;
    }

    public void setEnableZoneAwareness(Boolean enableZoneAwareness) {
        this.enableZoneAwareness = enableZoneAwareness;
    }

    /**
     * The zone awareness configuration for a domain when zone awareness is enabled.
     *
     * @subresource gyro.aws.elasticsearch.ElasticsearchZoneAwarenessConfiguration
     */
    @Updatable
    @DependsOn("enable-zone-awareness")
    public ElasticsearchZoneAwarenessConfiguration getZoneAwarenessConfiguration() {
        return zoneAwarenessConfiguration;
    }

    public void setZoneAwarenessConfiguration(ElasticsearchZoneAwarenessConfiguration zoneAwarenessConfiguration) {
        this.zoneAwarenessConfiguration = zoneAwarenessConfiguration;
    }

    /**
     * The type of instance for the Elasticsearch domain cluster. Defaults to ``m4.large_elasticsearch``.
     */
    @Updatable
    public ESPartitionInstanceType getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(ESPartitionInstanceType instanceType) {
        this.instanceType = instanceType;
    }

    /**
     * Number of nodes in the specified domain cluster. Defaults to ``1``. Valid values between ``0`` to ``40``.
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
     * Enable to dedicate a master node to the domain cluster. Defaults to ``false``.
     */
    @Updatable
    public Boolean getDedicatedMasterEnabled() {
        return dedicatedMasterEnabled;
    }

    public void setDedicatedMasterEnabled(Boolean dedicatedMasterEnabled) {
        this.dedicatedMasterEnabled = dedicatedMasterEnabled;
    }

    /**
     * Instance type for the dedicated master node if dedicated master is enabled. Defaults to ``m4.large_elasticsearch``.
     */
    @Updatable
    @DependsOn("dedicated-master-enabled")
    public ESPartitionInstanceType getDedicatedMasterType() {
        return dedicatedMasterType;
    }

    public void setDedicatedMasterType(ESPartitionInstanceType dedicatedMasterType) {
        this.dedicatedMasterType = dedicatedMasterType;
    }

    /**
     * Number of dedicated master nodes for the cluster. Defaults to ``3``. Valid values between ``2`` to ``5``
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
     * Enable warm storage. Defaults to ``false``.
     */
    @Updatable
    public Boolean getEnableWarm() {
        return enableWarm;
    }

    public void setEnableWarm(Boolean enableWarm) {
        this.enableWarm = enableWarm;
    }

    /**
     * Number of warm nodes in the cluster. Defaults to ``3``. Minimum ``3`` nodes.
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
     * The instance type for the cluster's warm nodes. Defaults to ``ultrawarm1.medium.elasticsearch``.
     */
    @Updatable
    @DependsOn("enable-warm")
    public ESWarmPartitionInstanceType getWarmType() {
        return warmType;
    }

    public void setWarmType(ESWarmPartitionInstanceType warmType) {
        this.warmType = warmType;
    }

    @Override
    public void copyFrom(ElasticsearchClusterConfig model) {
        setDedicatedMasterCount(model.dedicatedMasterCount());
        setDedicatedMasterEnabled(model.dedicatedMasterEnabled());
        setDedicatedMasterType(model.dedicatedMasterType());
        setEnableWarm(model.warmEnabled());
        setWarmCount(model.warmCount());
        setWarmType(model.warmType());
        setEnableZoneAwareness(model.zoneAwarenessEnabled());
        ElasticsearchZoneAwarenessConfiguration configuration = newSubresource(
            ElasticsearchZoneAwarenessConfiguration.class);
        configuration.copyFrom(model.zoneAwarenessConfig());
        setZoneAwarenessConfiguration(configuration);
        setInstanceCount(model.instanceCount());
        setInstanceType(model.instanceType());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    ElasticsearchClusterConfig toElasticSearchClusterConfig() {
        ElasticsearchClusterConfig.Builder builder = ElasticsearchClusterConfig.builder()
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

        if (configuredFields.contains("enable-zone-awareness") && getEnableZoneAwareness().equals(Boolean.FALSE)
            && configuredFields.contains("zone-awareness-configuration")) {
            errors.add(new ValidationError(
                this,
                null,
                "The 'zone-awareness-configuration' can only be set if 'enable-zone-awareness' is set to 'true'."));
        }

        if (configuredFields.contains("dedicated-master-enabled") && getDedicatedMasterEnabled().equals(Boolean.FALSE)
            && (configuredFields.contains("dedicated-master-type")
            || configuredFields.contains("dedicated-master-count"))) {
            errors.add(new ValidationError(
                this,
                null,
                "The 'dedicated-master-count' and 'dedicated-master-type' can only be set if 'dedicated-master-enabled' is set to 'true'."));
        }

        if (configuredFields.contains("enable-warm") && getEnableWarm().equals(Boolean.FALSE) && (
            configuredFields.contains("warm-type") || configuredFields.contains("warm-count"))) {
            errors.add(new ValidationError(
                this,
                null,
                "The 'warm-count' and 'warm-type' can only be set if 'enable-warm' is set to 'true'."));
        }

        return errors;
    }
}
