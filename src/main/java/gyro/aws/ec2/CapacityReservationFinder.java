package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CapacityReservation;
import software.amazon.awssdk.services.ec2.model.DescribeCapacityReservationsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeCapacityReservationsResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query ec2 capacity reservation.
 *
 * .. code-block:: gyro
 *
 *    capacity-reservations: $(aws::ec2-capacity-reservation EXTERNAL/* | capacity-reservation-id = 'cr-071f2771deb1ea5d4')
 */
@Type("ec2-capacity-reservation")
public class CapacityReservationFinder extends AwsFinder<Ec2Client, CapacityReservation, CapacityReservationResource> {
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
        return getCapacityReservations(client, null);
    }

    @Override
    protected List<CapacityReservation> findAws(Ec2Client client, Map<String, String> filters) {
        if (filters.containsKey("capacity-reservation-id")) {
            return getCapacityReservations(client, filters);
        } else {
            return Collections.emptyList();
        }
    }

    private List<CapacityReservation> getCapacityReservations(Ec2Client client, Map<String, String> filters) {
        List<CapacityReservation> capacityReservations = new ArrayList<>();

        DescribeCapacityReservationsRequest.Builder builder = DescribeCapacityReservationsRequest.builder();

        if (filters != null) {
            builder = builder.capacityReservationIds(filters.get("capacity-reservation-id"));
        }

        String marker = null;
        DescribeCapacityReservationsResponse response;

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.describeCapacityReservations(builder.build());
            } else {
                response = client.describeCapacityReservations(builder.nextToken(marker).build());
            }

            marker = response.nextToken();
            capacityReservations.addAll(response.capacityReservations());
        } while (!ObjectUtils.isBlank(marker));

        return capacityReservations;
    }
}
