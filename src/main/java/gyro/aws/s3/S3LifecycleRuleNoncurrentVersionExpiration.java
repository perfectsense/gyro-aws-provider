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

package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.s3.model.NoncurrentVersionExpiration;

public class S3LifecycleRuleNoncurrentVersionExpiration extends Diffable implements Copyable<NoncurrentVersionExpiration> {
    private Integer days;

    /**
     * Non current version expiration days. Depends on the values set in non current version transition.
     */
    @Updatable
    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    @Override
    public void copyFrom(NoncurrentVersionExpiration noncurrentVersionExpiration) {
        setDays(noncurrentVersionExpiration.noncurrentDays());
    }

    @Override
    public String primaryKey() {
        return "non current version expiration";
    }

    NoncurrentVersionExpiration toNoncurrentVersionExpiration() {
        return NoncurrentVersionExpiration.builder().noncurrentDays(getDays()).build();
    }
}
