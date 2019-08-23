package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CapacityReservation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query ec2 capacity reservation.
 *
 * .. code-block:: gyro
 *
 *    capacity-reservations: $(aws::ec2-capacity-reservation EXTERNAL/* | capacity-reservation-id = 'cr-071f2771deb1ea5d4')
 */
@Type("ec2-capacity-reservation")
public class CapacityReservationFinder extends Ec2TaggableAwsFinder<Ec2Client, CapacityReservation, CapacityReservationResource> {
    private String capacityReservationId;

    /**
     * The ID of the capacity reservation.
     */
    public String getCapacityReservationId() {
        return capacityReservationId;
    }

    public void setCapacityReservationId(String capacityReservationId) {
        this.capacityReservationId = capacityReservationId;
    }

    @Override
    protected List<CapacityReservation> findAllAws(Ec2Client client) {
        return client.describeCapacityReservationsPaginator().capacityReservations().stream().collect(Collectors.toList());
    }

    @Override
    protected List<CapacityReservation> findAws(Ec2Client client, Map<String, String> filters) {
        if (filters.containsKey("capacity-reservation-id")) {
            return client.describeCapacityReservationsPaginator(r -> r.capacityReservationIds(filters.get("capacity-reservation-id"))).capacityReservations().stream().collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }
}
