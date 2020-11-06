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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.autoscalingplans.model.CustomizedLoadMetricSpecification;

public class AutoScalingCustomizedLoadMetricSpecification extends Diffable
    implements Copyable<CustomizedLoadMetricSpecification> {

    List<AutoScalingMetricDimension> dimensions;
    private String name;
    private String namespace;
    private String statistic;
    private String unit;

    /**
     * The dimensions of the metric.
     *
     * @subresource gyro.aws.autoscaling.AutoScalingMetricDimension
     */
    @Updatable
    public List<AutoScalingMetricDimension> getDimensions() {
        if (dimensions == null) {
            dimensions = new ArrayList<>();
        }
        return dimensions;
    }

    public void setDimensions(List<AutoScalingMetricDimension> dimensions) {
        this.dimensions = dimensions;
    }

    /**
     * The name of the metric.
     */
    @Updatable
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The namespace of the metric.
     */
    @Updatable
    @Required
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * The statistic of the metric.
     */
    @Updatable
    @Required
    @ValidStrings("Sum")
    public String getStatistic() {
        return statistic;
    }

    public void setStatistic(String statistic) {
        this.statistic = statistic;
    }

    /**
     * The unit of the metric.
     */
    @Updatable
    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public void copyFrom(CustomizedLoadMetricSpecification model) {
        setName(model.metricName());
        setNamespace(model.namespace());
        setStatistic(model.statisticAsString());
        setUnit(model.unit());

        if (model.dimensions() != null) {
            List<AutoScalingMetricDimension> dimensions = new ArrayList<>();
            model.dimensions().forEach(metricDimension -> {
                AutoScalingMetricDimension autoScalingMetricDimension = newSubresource(AutoScalingMetricDimension.class);
                autoScalingMetricDimension.copyFrom(metricDimension);
                dimensions.add(autoScalingMetricDimension);
            });

            setDimensions(dimensions);
        } else {
            setDimensions(null);
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public CustomizedLoadMetricSpecification toCustomizedLoadMetricSpecification() {
        return CustomizedLoadMetricSpecification.builder()
            .dimensions(getDimensions().stream()
                .map(AutoScalingMetricDimension::toMetricDimension)
                .collect(Collectors.toList()))
            .metricName(getName())
            .namespace(getNamespace())
            .statistic(getStatistic())
            .unit(getUnit())
            .build();
    }
}
