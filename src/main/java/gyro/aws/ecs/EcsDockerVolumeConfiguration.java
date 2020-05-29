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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ecs.model.DockerVolumeConfiguration;
import software.amazon.awssdk.services.ecs.model.Scope;

public class EcsDockerVolumeConfiguration extends Diffable {

    private Scope scope;
    private Boolean autoprovision;
    private String driver;
    private Map<String, String> driverOpts;
    private Map<String, String> labels;

    /**
     * The scope for the Docker volume that determines its lifecycle. (Required)
     * Valid values are ``task`` and ``shared``.
     * Docker volumes that are scoped to a task are automatically provisioned when the task starts and destroyed when the task stops. Docker volumes that are scoped as shared persist after the task stops.
     */
    @Required
    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    /**
     * If this value is true, the Docker volume is created if it does not already exist.
     * This field is only used if the scope is ``shared``.
     */
    public Boolean getAutoprovision() {
        return autoprovision;
    }

    public void setAutoprovision(Boolean autoprovision) {
        this.autoprovision = autoprovision;
    }

    /**
     * The Docker volume driver to use. The driver value must match the driver name provided by Docker. (Required)
     */
    @Required
    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    /**
     * A map of Docker driver-specific options.
     */
    public Map<String, String> getDriverOpts() {
        if (driverOpts.isEmpty()) {
            driverOpts = new HashMap<>();
        }

        return driverOpts;
    }

    public void setDriverOpts(Map<String, String> driverOpts) {
        this.driverOpts = driverOpts;
    }

    /**
     * Custom metadata to add to your Docker volume.
     */
    public Map<String, String> getLabels() {
        if (labels.isEmpty()) {
            labels = new HashMap<>();
        }

        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public void copyFrom(DockerVolumeConfiguration model) {
        setScope(model.scope());
        setAutoprovision(model.autoprovision());
        setDriver(model.driver());
        setDriverOpts(model.driverOpts());
        setLabels(model.labels());
    }

    public DockerVolumeConfiguration copyTo() {
        return DockerVolumeConfiguration.builder().scope(getScope())
            .autoprovision(getAutoprovision())
            .driver(getDriver())
            .driverOpts(getDriverOpts())
            .labels(getLabels())
            .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (configuredFields.contains("autoprovision") && !getScope().equals(Scope.SHARED)) {
            errors.add(new ValidationError(
                this,
                "autoprovision",
                "'autoprovision' may only be configured when using the 'scope' is set to 'shared'."
            ));
        }

        return errors;
    }
}
