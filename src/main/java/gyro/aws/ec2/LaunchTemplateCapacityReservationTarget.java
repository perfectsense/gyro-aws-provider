package gyro.aws.ec2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.ec2.model.CapacityReservationTarget;

public class LaunchTemplateCapacityReservationTarget extends Diffable implements Copyable<CapacityReservationTarget> {

    private CapacityReservationResource capacityReservation;
    private String capacityReservationResourceGroupArn;

    /**
     * The capacity reservation in which to run the instance.
     */
    public CapacityReservationResource getCapacityReservation() {
        return capacityReservation;
    }

    public void setCapacityReservation(CapacityReservationResource capacityReservation) {
        this.capacityReservation = capacityReservation;
    }

    /**
     * The arn of the capacity reservation resource group in which to run the instance.
     */
    public String getCapacityReservationResourceGroupArn() {
        return capacityReservationResourceGroupArn;
    }

    public void setCapacityReservationResourceGroupArn(String capacityReservationResourceGroupArn) {
        this.capacityReservationResourceGroupArn = capacityReservationResourceGroupArn;
    }

    @Override
    public void copyFrom(CapacityReservationTarget model) {
        setCapacityReservation(findById(CapacityReservationResource.class, model.capacityReservationId()));

    }

    @Override
    public String primaryKey() {
        return "";
    }

    CapacityReservationTarget toCapacityReservationTarget() {
        return CapacityReservationTarget.builder().capacityReservationId(getCapacityReservation().getId())
            .capacityReservationResourceGroupArn(getCapacityReservationResourceGroupArn()).build();
    }
}
