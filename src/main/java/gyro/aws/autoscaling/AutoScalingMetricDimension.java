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
import gyro.core.validation.Required;
import software.amazon.awssdk.services.autoscalingplans.model.MetricDimension;

public class AutoScalingMetricDimension extends Diffable implements Copyable<MetricDimension> {

    private String name;
    private String value;

    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Required
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void copyFrom(MetricDimension model) {
        setName(model.name());
        setValue(model.value());
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    public MetricDimension toMetricDimension() {
        return MetricDimension.builder()
            .name(getName())
            .value(getValue())
            .build();
    }
}
