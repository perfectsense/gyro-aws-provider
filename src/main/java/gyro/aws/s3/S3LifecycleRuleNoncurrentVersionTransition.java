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
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.s3.model.NoncurrentVersionTransition;

public class S3LifecycleRuleNoncurrentVersionTransition extends Diffable implements Copyable<NoncurrentVersionTransition> {
    private Integer days;
    private String storageClass;

    /**
     * Days after creation that versioning would start. Min value 30.
     */
    @Required
    @Updatable
    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    /**
     * Type of transition. Valid values are ``GLACIER`` or ``STANDARD_IA`` or ``ONEZONE_IA`` or ``INTELLIGENT_TIERING``.
     */
    @Required
    @ValidStrings({"GLACIER", "STANDARD_IA", "ONEZONE_IA", "INTELLIGENT_TIERING"})
    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s", getDays(), getStorageClass());
    }

    @Override
    public void copyFrom(NoncurrentVersionTransition noncurrentVersionTransition) {
        setDays(noncurrentVersionTransition.noncurrentDays());
        setStorageClass(noncurrentVersionTransition.storageClassAsString());
    }

    NoncurrentVersionTransition toNoncurrentVersionTransition() {
        return NoncurrentVersionTransition.builder()
            .noncurrentDays(getDays())
            .storageClass(getStorageClass())
            .build();
    }
}
