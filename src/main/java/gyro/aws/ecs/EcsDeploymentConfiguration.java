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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Min;
import software.amazon.awssdk.services.ecs.model.DeploymentConfiguration;

public class EcsDeploymentConfiguration extends Diffable implements Copyable<DeploymentConfiguration> {

    public Integer maximumPercent;
    public Integer minimumHealthyPercent;

    /**
     * The upper limit on the number of tasks in a service that are allowed in the ``RUNNING`` or ``PENDING`` state during a deployment. Valid values are ``0`` or any positive integer. Defaults to ``200``.
     */
    @Min(0)
    @Updatable
    public Integer getMaximumPercent() {
        return maximumPercent;
    }

    public void setMaximumPercent(Integer maximumPercent) {
        this.maximumPercent = maximumPercent;
    }

    /**
     * The lower limit on the number of tasks in a service that are allowed in the ``RUNNING`` state during a deployment. Valid values are ``0`` or any positive integer. Defaults to ``100``.
     */
    @Min(0)
    @Updatable
    public Integer getMinimumHealthyPercent() {
        return minimumHealthyPercent;
    }

    public void setMinimumHealthyPercent(Integer minimumHealthyPercent) {
        this.minimumHealthyPercent = minimumHealthyPercent;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(DeploymentConfiguration model) {
        setMaximumPercent(model.maximumPercent());
        setMinimumHealthyPercent(model.minimumHealthyPercent());
    }

    public DeploymentConfiguration toDeploymentConfiguration() {
        return DeploymentConfiguration.builder()
            .maximumPercent(getMaximumPercent())
            .minimumHealthyPercent(getMinimumHealthyPercent())
            .build();
    }
}
