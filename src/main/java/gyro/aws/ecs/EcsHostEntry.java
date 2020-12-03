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
import software.amazon.awssdk.services.ecs.model.HostEntry;

public class EcsHostEntry extends Diffable {

    private String hostname;
    private String ipAddress;

    /**
     * The hostname to use in the /etc/hosts entry.
     */
    @Required
    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * The IP address to use in the /etc/hosts entry.
     */
    @Required
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public String primaryKey() {
        return getHostname();
    }

    public void copyFrom(HostEntry model) {
        setHostname(model.hostname());
        setIpAddress(model.ipAddress());
    }

    public HostEntry copyTo() {
        return HostEntry.builder()
            .hostname(getHostname())
            .ipAddress(getIpAddress())
            .build();
    }
}
