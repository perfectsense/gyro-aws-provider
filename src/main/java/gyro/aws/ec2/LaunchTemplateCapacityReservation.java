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
