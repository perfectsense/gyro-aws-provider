/*
 * Copyright 2019, Perfect Sense, Inc.
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

package gyro.aws.elb;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.elasticloadbalancing.model.ConnectionSettings;

public class LoadBalancerConnectionSettings extends Diffable implements Copyable<ConnectionSettings> {
    private Integer idleTimeout;

    /**
     * The amount in seconds the load balancer allows the connections to remain idle (no data is sent over the connection) for the specified duration. Defaults to ``600``.
     */
    @Updatable
    public Integer getIdleTimeout() {
        if (idleTimeout == null) {
            idleTimeout = 600;
        }

        return idleTimeout;
    }

    public void setIdleTimeout(Integer idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    @Override
    public void copyFrom(ConnectionSettings connectionSettings) {
        setIdleTimeout(connectionSettings.idleTimeout());
    }

    @Override
    public String primaryKey() {
        return "connection settings";
    }

    ConnectionSettings toConnectionSettings() {
        return ConnectionSettings.builder().idleTimeout(getIdleTimeout()).build();
    }
}
