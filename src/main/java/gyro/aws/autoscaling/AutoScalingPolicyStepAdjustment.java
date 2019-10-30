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

package gyro.aws.autoscaling;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.autoscaling.model.StepAdjustment;

public class AutoScalingPolicyStepAdjustment extends Diffable implements Copyable<StepAdjustment> {
    private Integer scalingAdjustment;
    private Double metricIntervalLowerBound;
    private Double metricIntervalUpperBound;

    /**
     * The amount of scaling adjustment.
     */
    public Integer getScalingAdjustment() {
        return scalingAdjustment;
    }

    public void setScalingAdjustment(Integer scalingAdjustment) {
        this.scalingAdjustment = scalingAdjustment;
    }

    /**
     * The lower bound for the scaling adjustment.
     */
    public Double getMetricIntervalLowerBound() {
        return metricIntervalLowerBound;
    }

    public void setMetricIntervalLowerBound(Double metricIntervalLowerBound) {
        this.metricIntervalLowerBound = metricIntervalLowerBound;
    }

    /**
     * The upper bound for the scaling adjustment.
     */
    public Double getMetricIntervalUpperBound() {
        return metricIntervalUpperBound;
    }

    public void setMetricIntervalUpperBound(Double metricIntervalUpperBound) {
        this.metricIntervalUpperBound = metricIntervalUpperBound;
    }

    @Override
    public void copyFrom(StepAdjustment stepAdjustment) {
        setScalingAdjustment(stepAdjustment.scalingAdjustment());
        setMetricIntervalLowerBound(stepAdjustment.metricIntervalLowerBound());
        setMetricIntervalUpperBound(stepAdjustment.metricIntervalUpperBound());
    }

    public StepAdjustment getStepPolicyStep() {
        return StepAdjustment.builder()
            .scalingAdjustment(getScalingAdjustment())
            .metricIntervalLowerBound(getMetricIntervalLowerBound())
            .metricIntervalUpperBound(getMetricIntervalUpperBound())
            .build();
    }
}
