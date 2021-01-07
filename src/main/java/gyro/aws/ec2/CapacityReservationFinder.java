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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CapacityReservation;

/**
 * Query ec2 capacity reservation.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    capacity-reservations: $(external-query aws::ec2-capacity-reservation { capacity-reservation-id: 'cr-071f2771deb1ea5d4'})
 */
@Type("ec2-capacity-reservation")
public class CapacityReservationFinder extends
    Ec2TaggableAwsFinder<Ec2Client, CapacityReservation, CapacityReservationResource> {

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
        return client.describeCapacityReservationsPaginator().capacityReservations().stream()
            .collect(Collectors.toList());
    }

    @Override
    protected List<CapacityReservation> findAws(Ec2Client client, Map<String, String> filters) {
        if (filters.containsKey("capacity-reservation-id")) {
            return client.describeCapacityReservationsPaginator(r -> r.capacityReservationIds(
                filters.get("capacity-reservation-id"))).capacityReservations().stream().collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }
}
