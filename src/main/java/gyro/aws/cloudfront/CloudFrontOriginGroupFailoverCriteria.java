/*
 * Copyright 2025, Perfect Sense, Inc.
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

package gyro.aws.cloudfront;

import java.util.HashSet;
import java.util.Set;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.cloudfront.model.OriginGroupFailoverCriteria;

public class CloudFrontOriginGroupFailoverCriteria extends Diffable implements Copyable<OriginGroupFailoverCriteria> {

    private Set<Integer> statusCodes;

    /**
     * The status codes that will trigger a failover to the secondary origin.
     */
    @Updatable
    public Set<Integer> getStatusCodes() {
        if (statusCodes == null) {
            statusCodes = new HashSet<>();
        }

        return statusCodes;
    }

    public void setStatusCodes(Set<Integer> statusCodes) {
        this.statusCodes = statusCodes;
    }

    @Override
    public void copyFrom(OriginGroupFailoverCriteria originGroupFailoverCriteria) {
        getStatusCodes().clear();
        if (originGroupFailoverCriteria.statusCodes() != null && originGroupFailoverCriteria.statusCodes().items() != null) {
            getStatusCodes().addAll(originGroupFailoverCriteria.statusCodes().items());
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    OriginGroupFailoverCriteria toOriginGroupFailoverCriteria() {
        return OriginGroupFailoverCriteria.builder()
            .statusCodes(r -> r.items(getStatusCodes()))
            .build();
    }
}