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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.cloudfront.model.OriginGroupMember;

public class CloudFrontOriginGroupMember extends Diffable implements Copyable<OriginGroupMember> {

    private String originId;

    /**
     * The ID of the origin that is a member of this origin group.
     */
    @Required
    public String getOriginId() {
        return originId;
    }

    public void setOriginId(String originId) {
        this.originId = originId;
    }

    @Override
    public void copyFrom(OriginGroupMember originGroupMember) {
        setOriginId(originGroupMember.originId());
    }

    @Override
    public String primaryKey() {
        return getOriginId();
    }

    OriginGroupMember toOriginGroupMember() {
        return OriginGroupMember.builder()
            .originId(getOriginId())
            .build();
    }
}