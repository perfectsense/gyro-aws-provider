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
import software.amazon.awssdk.services.globalaccelerator.model.PortRange;

public class ListenerPortRange extends Diffable implements Copyable<PortRange> {

    private Integer fromPort;
    private Integer toPort;

    @Override
    public void copyFrom(PortRange portRange) {
        setFromPort(portRange.fromPort());
        setToPort(portRange.toPort());
    }

    @Override
    public String primaryKey() {
        return String.format("%s:%s", fromPort, toPort);
    }

    public Integer getFromPort() {
        return fromPort;
    }

    public void setFromPort(Integer fromPort) {
        this.fromPort = fromPort;
    }

    public Integer getToPort() {
        return toPort;
    }

    public void setToPort(Integer toPort) {
        this.toPort = toPort;
    }

    PortRange portRange() {
        return PortRange.builder()
            .fromPort(getFromPort())
            .toPort(getToPort())
            .build();
    }
}
