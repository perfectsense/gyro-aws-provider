/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.ecs;

import gyro.core.resource.Diffable;
import gyro.core.validation.Range;
import software.amazon.awssdk.services.ecs.model.ManagedScaling;
import software.amazon.awssdk.services.ecs.model.ManagedScalingStatus;

public class EcsManagedScaling extends Diffable {

    private ManagedScalingStatus status;
    private Integer targetCapacity;
    private Integer minimumScalingStepSize;
    private Integer maximumScalingStepSize;

    /**
     * Whether or not to enable managed scaling for the capacity provider.
     * Valid values are ``ENABLED`` and ``DISABLED``.
     */
    public ManagedScalingStatus getStatus() {
        return status;
    }

    public void setStatus(ManagedScalingStatus status) {
        this.status = status;
    }

    /**
     * The target capacity value for the capacity provider. A value of ``100`` will result in the Amazon EC2 instances in your Auto Scaling group being completely utilized.
     * Valid values are integers greater than ``0`` and less than or equal to ``100``.
     */
    @Range(min = 1, max = 100)
    public Integer getTargetCapacity() {
        return targetCapacity;
    }

    public void setTargetCapacity(Integer targetCapacity) {
        this.targetCapacity = targetCapacity;
    }

    /**
     * The minimum number of container instances that Amazon ECS will scale in or scale out at one time.
     * Defaults to ``1``.
     */
    public Integer getMinimumScalingStepSize() {
        return minimumScalingStepSize;
    }

    public void setMinimumScalingStepSize(Integer minimumScalingStepSize) {
        this.minimumScalingStepSize = minimumScalingStepSize;
    }

    /**
     * The maximum number of container instances that Amazon ECS will scale in or scale out at one time.
     * Defaults to ``10000``.
     */
    public Integer getMaximumScalingStepSize() {
        return maximumScalingStepSize;
    }

    public void setMaximumScalingStepSize(Integer maximumScalingStepSize) {
        this.maximumScalingStepSize = maximumScalingStepSize;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public void copyFrom(ManagedScaling model) {
        setStatus(model.status());
        setTargetCapacity(model.targetCapacity());
        setMinimumScalingStepSize(model.minimumScalingStepSize());
        setMaximumScalingStepSize(model.maximumScalingStepSize());
    }

    public ManagedScaling copyTo() {
        return ManagedScaling.builder()
            .status(getStatus())
            .targetCapacity(getTargetCapacity())
            .minimumScalingStepSize(getMinimumScalingStepSize())
            .maximumScalingStepSize(getMaximumScalingStepSize())
            .build();
    }
}
