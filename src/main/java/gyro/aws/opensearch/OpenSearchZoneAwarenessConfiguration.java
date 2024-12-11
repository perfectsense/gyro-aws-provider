/*
 * Copyright 2024, Brightspot.
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

package gyro.aws.opensearch;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.opensearch.model.ZoneAwarenessConfig;

public class OpenSearchZoneAwarenessConfiguration extends Diffable implements Copyable<ZoneAwarenessConfig> {

    private Integer availabilityZoneCount;

    /**
     * The number of availability zones for a domain when zone awareness is enabled.
     */
    @Required
    @Updatable
    public Integer getAvailabilityZoneCount() {
        return availabilityZoneCount;
    }

    public void setAvailabilityZoneCount(Integer availabilityZoneCount) {
        this.availabilityZoneCount = availabilityZoneCount;
    }

    @Override
    public void copyFrom(ZoneAwarenessConfig model) {
        setAvailabilityZoneCount(model.availabilityZoneCount());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    ZoneAwarenessConfig toZoneAwarenessConfig() {
        return ZoneAwarenessConfig.builder().availabilityZoneCount(getAvailabilityZoneCount()).build();
    }
}
