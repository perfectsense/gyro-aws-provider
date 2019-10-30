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
import software.amazon.awssdk.services.elasticloadbalancing.model.ConnectionDraining;

public class LoadBalancerConnectionDraining extends Diffable implements Copyable<ConnectionDraining> {
    private Boolean enabled;
    private Integer timeout;

    /**
     * If set to ``true``, the load balancer allows existing requests to complete before the load balancer shifts traffic away from a deregistered or unhealthy instance. Defaults to ``false``.
     */
    @Updatable
    public Boolean getEnabled() {
        if (enabled == null) {
            enabled = false;
        }

        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * The maximum time, in seconds, to keep the existing connections open before deregistering the instances. Defaults to ``300``.
     */
    @Updatable
    public Integer getTimeout() {
        if (timeout == null) {
            timeout = 300;
        }

        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    @Override
    public void copyFrom(ConnectionDraining connectionDraining) {
        setEnabled(connectionDraining.enabled());
        setTimeout(connectionDraining.timeout());
    }

    @Override
    public String primaryKey() {
        return "connection draining";
    }

    ConnectionDraining toConnectionDraining(boolean overrideEnabled) {
        return ConnectionDraining.builder().enabled(overrideEnabled ? true : getEnabled()).timeout(getTimeout()).build();
    }
}
