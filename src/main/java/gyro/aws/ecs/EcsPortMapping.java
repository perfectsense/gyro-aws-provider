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
import software.amazon.awssdk.services.ecs.model.NetworkMode;
import software.amazon.awssdk.services.ecs.model.PortMapping;
import software.amazon.awssdk.services.ecs.model.TransportProtocol;

public class EcsPortMapping extends Diffable {

    private Integer containerPort;
    private Integer hostPort;
    private TransportProtocol protocol;

    /**
     * The port number on the container that is bound to the user-specified or automatically assigned ``host-port``. (Required)
     */
    @Required
    public Integer getContainerPort() {
        return containerPort;
    }

    public void setContainerPort(Integer containerPort) {
        this.containerPort = containerPort;
    }

    /**
     * The port number on the container instance to reserve for your container.
     * If you are using containers in a task with the ``awsvpc`` or ``host`` ``network-mode``, the ``host-port`` can either be left blank or set to the same value as the ``container-port``.
     * If you are using containers in a task with the ``bridge`` ``network-mode``, you can specify a non-reserved ``host-port`` for your container port mapping, or you can omit the ``host-port`` (or set it to ``0``) while specifying a ``container-port`` and your container automatically receives a port in the ephemeral port range for your container instance operating system and Docker version.
     */
    public Integer getHostPort() {
        return hostPort;
    }

    public void setHostPort(Integer hostPort) {
        this.hostPort = hostPort;
    }

    /**
     * The protocol used for the port mapping.
     * Valid values are ``tcp`` and ``udp``. Defaults to ``tcp``.
     */
    public TransportProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(TransportProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public String primaryKey() {
        // Duplicate entries supported by the API, but not Gyro
        return "";
    }

    public void copyFrom(PortMapping model) {
        setContainerPort(model.containerPort());
        setHostPort(model.hostPort());
        setProtocol(model.protocol());
    }

    public PortMapping copyTo() {
        return PortMapping.builder()
            .containerPort(getContainerPort())
            .hostPort(getHostPort())
            .protocol(getProtocol())
            .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        EcsContainerDefinition containerDefinition = (EcsContainerDefinition) parent();
        EcsTaskDefinitionResource taskDefinition = containerDefinition.getParentTaskDefinition();

        if (taskDefinition.getNetworkMode() == NetworkMode.AWSVPC && configuredFields.contains("host-port") && !getHostPort().equals(getContainerPort())) {
            errors.add(new ValidationError(
                this,
                "host-port",
                "When the task definition's 'network-mode' is 'awsvpc', the 'host-port' must either be blank or hold the same value as the 'container-port'."
            ));
        }

        return errors;
    }
}
