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

import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ecs.model.MountPoint;

public class EcsMountPoint extends Diffable {

    private String sourceVolume;
    private String containerPath;
    private Boolean readOnly;

    /**
     * The name of the volume to mount. (Required)
     * Must be the ``name`` of a task definition volume.
     */
    @Required
    public String getSourceVolume() {
        return sourceVolume;
    }

    public void setSourceVolume(String sourceVolume) {
        this.sourceVolume = sourceVolume;
    }

    /**
     * The path on the container at which to mount the host volume. (Required)
     */
    @Required
    public String getContainerPath() {
        return containerPath;
    }

    public void setContainerPath(String containerPath) {
        this.containerPath = containerPath;
    }

    /**
     * If enabled, the container has read-only access to the volume. Otherwise, the container can write to the volume.
     * Defaults to ``false``.
     */
    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public String primaryKey() {
        return getContainerPath();
    }

    public void copyFrom(MountPoint model) {
        setSourceVolume(model.sourceVolume());
        setContainerPath(model.containerPath());
        setReadOnly(model.readOnly());
    }

    public MountPoint copyTo() {
        return MountPoint.builder()
            .sourceVolume(getSourceVolume())
            .containerPath(getContainerPath())
            .readOnly(getReadOnly())
            .build();
    }
}
