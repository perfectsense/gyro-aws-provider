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

package gyro.aws.ecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.core.resource.Diffable;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ecs.model.Compatibility;
import software.amazon.awssdk.services.ecs.model.Volume;

public class EcsVolume extends Diffable {

    private String name;
    private String hostSourcePath;
    private EcsDockerVolumeConfiguration dockerVolumeConfiguration;

    /**
     * The name of the volume.
     */
    @Required
    @Regex(value = "[-a-zA-Z0-9]{1,255}", message = "a string 1 to 255 characters long containing letters, numbers, and hyphens")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The path on the host container instance that is presented to the container.
     * When the task definition's 'requires-compatibilities' parameter contains 'FARGATE', this parameter is not supported.
     */
    public String getHostSourcePath() {
        return hostSourcePath;
    }

    public void setHostSourcePath(String hostSourcePath) {
        this.hostSourcePath = hostSourcePath;
    }

    /**
     * This parameter is specified when using Docker volumes.
     *
     * @subresource gyro.aws.ecs.EcsDockerVolumeConfiguration
     */
    public EcsDockerVolumeConfiguration getDockerVolumeConfiguration() {
        return dockerVolumeConfiguration;
    }

    public void setDockerVolumeConfiguration(EcsDockerVolumeConfiguration dockerVolumeConfiguration) {
        this.dockerVolumeConfiguration = dockerVolumeConfiguration;
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    public void copyFrom(Volume model) {
        setName(model.name());
        setHostSourcePath(model.host() != null ? model.host().sourcePath() : null);

        EcsDockerVolumeConfiguration volumeConfiguration = null;

        if (model.dockerVolumeConfiguration() != null) {
            volumeConfiguration = newSubresource(EcsDockerVolumeConfiguration.class);
            volumeConfiguration.copyFrom(model.dockerVolumeConfiguration());
        }

        setDockerVolumeConfiguration(volumeConfiguration);
    }

    public Volume copyTo() {
        Volume.Builder volumeBuilder = Volume.builder().name(getName());

        if (getHostSourcePath() != null) {
            volumeBuilder.host(o -> o.sourcePath(getHostSourcePath()));

        } else if (getDockerVolumeConfiguration() != null) {
            volumeBuilder.dockerVolumeConfiguration(getDockerVolumeConfiguration().copyTo());
        }

        return volumeBuilder.build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        EcsTaskDefinitionResource taskDefinition = (EcsTaskDefinitionResource) parent();

        if (taskDefinition.getRequiresCompatibilities().contains(Compatibility.FARGATE.toString())) {
            if (configuredFields.contains("host-source-path")) {
                errors.add(new ValidationError(
                    this,
                    "host-source-path",
                    "A 'host-source-path' may not be specified when the task definition's 'requires-compatibilities' parameter contains 'FARGATE'."
                ));
            }
        }

        if (configuredFields.contains("host-source-path") && configuredFields.contains("docker-volume-configuration")) {
            errors.add(new ValidationError(
                this,
                "host-source-path",
                "Only one of 'host-source-path' or 'docker-volume-configuration' may be specified."
            ));
        }

        return errors;
    }
}
