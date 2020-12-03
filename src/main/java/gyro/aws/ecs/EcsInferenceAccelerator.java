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
import software.amazon.awssdk.services.ecs.model.InferenceAccelerator;

public class EcsInferenceAccelerator extends Diffable {

    private String deviceName;
    private String deviceType;

    /**
     * The Elastic Inference accelerator device name.
     * The device name must also be referenced in a container definition as a resource requirement.
     */
    @Required
    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * The Elastic Inference accelerator type to use.
     */
    @Required
    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    @Override
    public String primaryKey() {
        return getDeviceName();
    }

    public void copyFrom(InferenceAccelerator model) {
        setDeviceName(model.deviceName());
        setDeviceType(model.deviceType());
    }

    public InferenceAccelerator copyTo() {
        return InferenceAccelerator.builder()
            .deviceName(getDeviceName())
            .deviceType(getDeviceType())
            .build();
    }
}
