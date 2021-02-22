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

package gyro.aws.ec2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.model.LaunchPermission;

public class AmiLaunchPermission extends Diffable implements Copyable<LaunchPermission> {
    private String userId;

    /**
     * The AWS Account ID for the permission.
     */
    @Required
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String primaryKey() {
        return getUserId();
    }

    @Override
    public void copyFrom(LaunchPermission permission) {
        setUserId(permission.userId());
    }

    LaunchPermission toLaunchPermission() {
        return LaunchPermission.builder()
            .userId(getUserId())
            .build();
    }
}
