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

package gyro.aws.autoscaling;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.autoscalingplans.model.PredefinedScalingMetricSpecification;
import software.amazon.awssdk.services.autoscalingplans.model.ScalingMetricType;

public class AutoScalingPredefinedScalingMetricSpecification extends Diffable
    implements Copyable<PredefinedScalingMetricSpecification> {

    private ScalingMetricType predefinedScalingMetricType;
    private String resourceLabel;

    /**
     * The metric type.
     */
    @Updatable
    @Required
    public ScalingMetricType getPredefinedScalingMetricType() {
        return predefinedScalingMetricType;
    }

    public void setPredefinedScalingMetricType(ScalingMetricType predefinedScalingMetricType) {
        this.predefinedScalingMetricType = predefinedScalingMetricType;
    }

    /**
     * The resource associated with the metric type.
     */
    @Updatable
    public String getResourceLabel() {
        return resourceLabel;
    }

    public void setResourceLabel(String resourceLabel) {
        this.resourceLabel = resourceLabel;
    }

    @Override
    public void copyFrom(PredefinedScalingMetricSpecification model) {
        setPredefinedScalingMetricType(model.predefinedScalingMetricType());
        setResourceLabel(model.resourceLabel());
    }

    @Override
    public String primaryKey() {
        return null;
    }

    public PredefinedScalingMetricSpecification toPredefinedScalingMetricSpecification() {
        return PredefinedScalingMetricSpecification.builder()
            .predefinedScalingMetricType(getPredefinedScalingMetricType())
            .resourceLabel(getResourceLabel())
            .build();
    }
}
