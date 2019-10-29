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
import software.amazon.awssdk.services.elasticloadbalancing.model.CrossZoneLoadBalancing;

public class LoadBalancerCrossZoneLoadBalancing extends Diffable implements Copyable<CrossZoneLoadBalancing> {
    private Boolean enabled;

    /**
     * If enabled, the load balancer routes the request traffic evenly across all instances regardless of the Availability Zones. Defaults to false.
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

    @Override
    public void copyFrom(CrossZoneLoadBalancing crossZoneLoadBalancing) {
        setEnabled(crossZoneLoadBalancing.enabled());
    }

    @Override
    public String primaryKey() {
        return "cross zone load balancing";
    }

    CrossZoneLoadBalancing toCrossZoneLoadBalancing() {
        return CrossZoneLoadBalancing.builder()
            .enabled(getEnabled())
            .build();
    }
}
