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
import software.amazon.awssdk.services.eventbridge.model.RunCommandParameters;

public class RunCommandParameter extends Diffable implements Copyable<RunCommandParameters> {

    private List<RunCommandTarget> runCommandTargets;

    /**
     * The run command target configs.
     *
     * @subresource gyro.aws.eventbridge.RunCommandTarget
     */
    @Required
    @Updatable
    public List<RunCommandTarget> getRunCommandTargets() {
        if (runCommandTargets == null) {
            runCommandTargets = new ArrayList<>();
        }

        return runCommandTargets;
    }

    public void setRunCommandTargets(List<RunCommandTarget> runCommandTargets) {
        this.runCommandTargets = runCommandTargets;
    }

    @Override
    public void copyFrom(RunCommandParameters model) {
        getRunCommandTargets().clear();
        if (model.runCommandTargets() != null) {
            model.runCommandTargets().forEach( target -> {
                RunCommandTarget commandTarget = newSubresource(RunCommandTarget.class);
                commandTarget.copyFrom(target);
                getRunCommandTargets().add(commandTarget);
            });
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    protected RunCommandParameters toRunCommandParameters() {
        return RunCommandParameters.builder()
            .runCommandTargets(getRunCommandTargets().stream()
                .map(RunCommandTarget::toRunCommandTarget)
                .collect(Collectors.toList()))
            .build();
    }
}
