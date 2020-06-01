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
import software.amazon.awssdk.services.ecs.model.VolumeFrom;

public class EcsVolumeFrom extends Diffable {

    private String sourceContainer;
    private Boolean readOnly;

    /**
     * The ``name`` of another container within the same task definition from which to mount volumes. (Required)
     */
    @Required
    public String getSourceContainer() {
        return sourceContainer;
    }

    public void setSourceContainer(String sourceContainer) {
        this.sourceContainer = sourceContainer;
    }

    /**
     * If ``true``, the container has read-only access to the volume. Otherwise, the container can write to the volume.
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
        // Duplicate entries supported by the API, but not Gyro
        return "";
    }

    public void copyFrom(VolumeFrom model) {
        setSourceContainer(model.sourceContainer());
        setReadOnly(model.readOnly());
    }

    public VolumeFrom copyTo() {
        return VolumeFrom.builder()
            .sourceContainer(getSourceContainer())
            .readOnly(getReadOnly())
            .build();
    }
}
