/*
 * Copyright 2021, Brightspot.
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

package gyro.aws.globalaccelerator;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.globalaccelerator.model.PortOverride;

public class EndpointGroupPortOverride extends Diffable implements Copyable<PortOverride> {

    private Integer endpointPort;
    private Integer listenerPort;

    /**
     * The port to connect to on the endpoint.
     */
    public Integer getEndpointPort() {
        return endpointPort;
    }

    public void setEndpointPort(Integer endpointPort) {
        this.endpointPort = endpointPort;
    }

    /**
     * The listener port to override.
     */
    public Integer getListenerPort() {
        return listenerPort;
    }

    public void setListenerPort(Integer listenerPort) {
        this.listenerPort = listenerPort;
    }

    @Override
    public String primaryKey() {
        return String.format("%s -> %s", getListenerPort(), getEndpointPort());
    }

    @Override
    public void copyFrom(PortOverride override) {
        setEndpointPort(override.endpointPort());
        setListenerPort(override.listenerPort());
    }

    PortOverride portOverride() {
        return PortOverride.builder()
            .endpointPort(getEndpointPort())
            .listenerPort(getListenerPort())
            .build();
    }
}
