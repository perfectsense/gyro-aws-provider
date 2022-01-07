/*
 * Copyright 2022, Brightspot.
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

package gyro.aws.eventbridge;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.eventbridge.model.SageMakerPipelineParameter;

public class PipelineParameter extends Diffable implements Copyable<SageMakerPipelineParameter> {

    private String name;
    private String value;

    /**
     * Name of parameter to start execution of a SageMaker Model Building Pipeline.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Value of parameter to start execution of a SageMaker Model Building Pipeline.
     */
    @Required
    @Updatable
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void copyFrom(SageMakerPipelineParameter model) {
        setName(model.name());
        setValue(model.value());
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    protected SageMakerPipelineParameter toSageMakerPipelineParameter() {
        return SageMakerPipelineParameter.builder()
            .name(getName())
            .value(getValue())
            .build();
    }
}
