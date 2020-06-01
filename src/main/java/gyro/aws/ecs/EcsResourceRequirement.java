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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ecs.model.ResourceRequirement;
import software.amazon.awssdk.services.ecs.model.ResourceType;

public class EcsResourceRequirement extends Diffable {

    private ResourceType type;
    private String value;

    /**
     * The type of resource to assign to a container. (Required)
     * Valid values are ``GPU`` and ``InferenceAccelerator``.
     */
    @Required
    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    /**
     * The value for the specified resource ``type``. (Required)
     * If the ``GPU`` ``type`` is used, the ``value`` is the number of physical GPUs the Amazon ECS container agent will reserve for the container. The total of all GPU resource requirements' values across a task definition may not exceed ``16``.
     * If the ``InferenceAccelerator`` ``type`` is used, the ``value`` should match the ``device-name`` for an ``inference-accelerator`` specified in a task definition.
     */
    @Required
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String primaryKey() {
        return getType().toString();
    }

    public void copyFrom(ResourceRequirement model) {
        setType(model.type());
        setValue(model.value());
    }

    public ResourceRequirement copyTo() {
        return ResourceRequirement.builder()
            .type(getType())
            .value(getValue())
            .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getType() == ResourceType.GPU && Integer.valueOf(getValue()) < 1) {
            errors.add(new ValidationError(
                this,
                "value",
                "The 'value' for the 'gpu' 'resource-type' must be a positive integer"
            ));
        }

        return errors;
    }
}
