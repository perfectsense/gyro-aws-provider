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

import java.util.ArrayList;
import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.s3.model.LifecycleExpiration;

public class S3LifecycleRuleExpiration extends Diffable implements Copyable<LifecycleExpiration> {
    private Integer days;
    private Boolean expiredObjectDeleteMarker;

    /**
     * The lifetime, in days, of the objects that are subject to the rule. Valid values integers ``1`` and above.
     */
    @Updatable
    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;

        validate();
    }

    /**
     * Indicates whether Amazon S3 will remove a delete marker with no noncurrent versions. If set to true, the delete marker will be expired. Cannot be set with 'days' or lifecyclerule 'tags'.
     */
    @Updatable
    public Boolean getExpiredObjectDeleteMarker() {
        return expiredObjectDeleteMarker;
    }

    public void setExpiredObjectDeleteMarker(Boolean expiredObjectDeleteMarker) {
        this.expiredObjectDeleteMarker = expiredObjectDeleteMarker;

        validate();
    }

    @Override
    public void copyFrom(LifecycleExpiration lifecycleExpiration) {
        setDays(lifecycleExpiration.days());
        setExpiredObjectDeleteMarker(lifecycleExpiration.expiredObjectDeleteMarker());
    }

    @Override
    public String primaryKey() {
        return "lifecycle expiration";
    }

    LifecycleExpiration toLifecycleExpiration() {
        return LifecycleExpiration.builder().date(null)
            .days(getDays())
            .expiredObjectDeleteMarker(getExpiredObjectDeleteMarker())
            .build();
    }

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (getDays() != null && getExpiredObjectDeleteMarker() != null) {
            errors.add(new ValidationError(this, null, "Param 'days' and 'expired-object-delete-marker' cannot be both set together."));
        }

        return errors;
    }
}
