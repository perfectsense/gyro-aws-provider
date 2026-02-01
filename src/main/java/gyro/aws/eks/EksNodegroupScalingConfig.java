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

package gyro.aws.eks;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Range;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.eks.model.NodegroupScalingConfig;

public class EksNodegroupScalingConfig extends Diffable implements Copyable<NodegroupScalingConfig> {

    private Integer desiredSize;
    private Integer maxSize;
    private Integer minSize;

    /**
     * The current number of worker nodes that the managed node group should maintain. Defaults to ``2``. Can be between ``0`` to ``100``.
     */
    @Range(min = 0, max = 100)
    @Updatable
    public Integer getDesiredSize() {
        if (desiredSize == null) {
            return getMinSize();
        }

        return desiredSize;
    }

    public void setDesiredSize(Integer desiredSize) {
        this.desiredSize = desiredSize;
    }

    /**
     * The maximum number of worker nodes that the managed node group can scale in to. Defaults to ``2``. Can be between ``1`` to ``100``.
     */
    @Range(min = 1, max = 100)
    @Updatable
    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * The minimum number of worker nodes that the managed node group can scale in to. Defaults to ``1``. Can be between ``0`` to ``100``.
     */
    @Range(min = 0, max = 100)
    @Updatable
    public Integer getMinSize() {
        return minSize;
    }

    public void setMinSize(Integer minSize) {
        this.minSize = minSize;
    }

    @Override
    public void copyFrom(NodegroupScalingConfig model) {
        setDesiredSize(model.desiredSize());
        setMaxSize(model.maxSize());
        setMinSize(model.minSize());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    NodegroupScalingConfig toNodegroupScalingConfig() {
        return NodegroupScalingConfig.builder()
            .desiredSize(getDesiredSize())
            .maxSize(getMaxSize())
            .minSize(getMinSize())
            .build();
    }
}
