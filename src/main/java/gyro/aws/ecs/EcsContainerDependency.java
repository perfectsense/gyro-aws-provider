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

package gyro.aws.ecs;

import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ecs.model.ContainerCondition;
import software.amazon.awssdk.services.ecs.model.ContainerDependency;

public class EcsContainerDependency extends Diffable {

    private String containerName;
    private ContainerCondition condition;

    /**
     * The ``name`` of a container. (Required)
     */
    @Required
    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    /**
     * The dependency condition of the container. (Required)
     * Valid values are ``START``, ``COMPLETE``, ``SUCCESS``, and ``HEALTHY``.
     */
    @Required
    public ContainerCondition getCondition() {
        return condition;
    }

    public void setCondition(ContainerCondition condition) {
        this.condition = condition;
    }

    @Override
    public String primaryKey() {
        // Duplicate entries supported by the API, but not Gyro
        return "";
    }

    public void copyFrom(ContainerDependency model) {
        setContainerName(model.containerName());
        setCondition(model.condition());
    }

    public ContainerDependency copyTo() {
        return ContainerDependency.builder()
            .containerName(getContainerName())
            .condition(getCondition())
            .build();
    }
}
