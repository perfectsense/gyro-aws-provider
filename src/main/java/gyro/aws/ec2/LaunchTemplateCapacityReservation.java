/*
 * Copyright 2021, Perfect Sense.
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
import gyro.core.resource.Updatable;
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.ec2.model.CapacityReservationPreference;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateCapacityReservationSpecificationRequest;

public class LaunchTemplateCapacityReservation extends Diffable
    implements Copyable<LaunchTemplateCapacityReservationSpecificationRequest> {

    private CapacityReservationPreference preference;
    private LaunchTemplateCapacityReservationTarget target;

    /**
     * The instance's Capacity Reservation preferences.
     */
    @Updatable
    @ValidStrings({ "NONE", "OPEN" })
    @ConflictsWith("target")
    public CapacityReservationPreference getPreference() {
        return preference;
    }

    public void setPreference(CapacityReservationPreference preference) {
        this.preference = preference;
    }

    /**
     * The information about the target Capacity Reservation or Capacity Reservation group.
     */
    @Updatable
    @ConflictsWith("preference")
    public LaunchTemplateCapacityReservationTarget getTarget() {
        return target;
    }

    public void setTarget(LaunchTemplateCapacityReservationTarget target) {
        this.target = target;
    }

    @Override
    public void copyFrom(LaunchTemplateCapacityReservationSpecificationRequest model) {
        setPreference(model.capacityReservationPreference());

        setTarget(null);
        if (model.capacityReservationTarget() != null) {
            LaunchTemplateCapacityReservationTarget capacityReservationTarget = newSubresource(
                LaunchTemplateCapacityReservationTarget.class);
            capacityReservationTarget.copyFrom(model.capacityReservationTarget());
            setTarget(capacityReservationTarget);
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    LaunchTemplateCapacityReservationSpecificationRequest toLaunchTemplateCapacityReservationSpecificationRequest() {
        return LaunchTemplateCapacityReservationSpecificationRequest.builder()
            .capacityReservationPreference(getPreference())
            .capacityReservationTarget(getTarget() == null ? null : getTarget().toCapacityReservationTarget())
            .build();
    }
}
