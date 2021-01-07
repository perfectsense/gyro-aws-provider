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
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.autoscalingplans.model.LoadMetricType;
import software.amazon.awssdk.services.autoscalingplans.model.PredefinedLoadMetricSpecification;

public class AutoScalingPredefinedLoadMetricSpecification extends Diffable
    implements Copyable<PredefinedLoadMetricSpecification> {

    private LoadMetricType predefinedLoadMetricType;
    private String resourceLabel;

    /**
     * The metric type.
     */
    @Updatable
    @Required
    @ValidStrings({ "ASGTotalCPUUtilization", "ASGTotalNetworkIn", "ASGTotalNetworkOut", "ALBTargetGroupRequestCount" })
    public LoadMetricType getPredefinedLoadMetricType() {
        return predefinedLoadMetricType;
    }

    public void setPredefinedLoadMetricType(LoadMetricType predefinedLoadMetricType) {
        this.predefinedLoadMetricType = predefinedLoadMetricType;
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
    public void copyFrom(PredefinedLoadMetricSpecification model) {
        setPredefinedLoadMetricType(model.predefinedLoadMetricType());
        setResourceLabel(model.resourceLabel());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public PredefinedLoadMetricSpecification toPredefinedLoadMetricSpecification() {
        return PredefinedLoadMetricSpecification.builder()
            .predefinedLoadMetricType(getPredefinedLoadMetricType())
            .resourceLabel(getResourceLabel())
            .build();
    }
}
