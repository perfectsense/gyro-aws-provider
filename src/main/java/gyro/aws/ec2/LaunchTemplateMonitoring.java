/*
 * Copyright 2021, Perfect Sense.
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

package gyro.aws.ec2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.model.LaunchTemplatesMonitoring;
import software.amazon.awssdk.services.ec2.model.LaunchTemplatesMonitoringRequest;

public class LaunchTemplateMonitoring extends Diffable implements Copyable<LaunchTemplatesMonitoring> {

    private Boolean enabled;

    /**
     * When set to ``true``, monitoring for your instance is enabled.
     */
    @Required
    @Updatable
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void copyFrom(LaunchTemplatesMonitoring model) {
        setEnabled(model.enabled());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    LaunchTemplatesMonitoringRequest toLaunchTemplatesMonitoringRequest() {
        return LaunchTemplatesMonitoringRequest.builder().enabled(getEnabled()).build();
    }
}
