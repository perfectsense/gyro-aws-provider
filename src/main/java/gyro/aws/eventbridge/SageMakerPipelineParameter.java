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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.eventbridge.model.SageMakerPipelineParameters;

public class SageMakerPipelineParameter extends Diffable implements Copyable<SageMakerPipelineParameters> {

    private List<PipelineParameter> pipelineParameters;

    /**
     * A list of pipeline parameter configs.
     *
     * @subresource gyro.aws.eventbridge.PipelineParameter
     */
    @Required
    @Updatable
    public List<PipelineParameter> getPipelineParameters() {
        if (pipelineParameters == null) {
            pipelineParameters = new ArrayList<>();
        }

        return pipelineParameters;
    }

    public void setPipelineParameters(List<PipelineParameter> pipelineParameters) {
        this.pipelineParameters = pipelineParameters;
    }

    @Override
    public void copyFrom(SageMakerPipelineParameters model) {
        getPipelineParameters().clear();
        if (model.pipelineParameterList() != null) {
            model.pipelineParameterList().forEach( param -> {
                PipelineParameter pipelineParameter = newSubresource(PipelineParameter.class);
                pipelineParameter.copyFrom(param);
                getPipelineParameters().add(pipelineParameter);
            });
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    protected SageMakerPipelineParameters toSageMakerPipelineParameters() {
        return SageMakerPipelineParameters.builder()
            .pipelineParameterList(getPipelineParameters().stream()
                .map(PipelineParameter::toSageMakerPipelineParameter)
                .collect(Collectors.toList()))
            .build();
    }
}
