/*
 * Copyright 2022, Brightspot.
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

package gyro.aws.eventbridge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.DescribeEventBusResponse;
import software.amazon.awssdk.services.eventbridge.model.EventBus;
import software.amazon.awssdk.services.eventbridge.model.ListEventBusesRequest;
import software.amazon.awssdk.services.eventbridge.model.ListEventBusesResponse;
import software.amazon.awssdk.services.eventbridge.model.ResourceNotFoundException;

/**
 * Query Event Bus .
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    event-bus: $(external-query aws::event-bus { name: ''})
 */
@Type("event-bus")
public class EventBusFinder extends AwsFinder<EventBridgeClient, DescribeEventBusResponse, EventBusResource> {

    @Override
    protected List<DescribeEventBusResponse> findAllAws(EventBridgeClient client) {
        List<DescribeEventBusResponse> busResponses = new ArrayList<>();
        List<EventBus> eventBuses = new ArrayList<>();
        ListEventBusesResponse response;
        String token = null;

        do {
            if (StringUtils.isBlank(token)) {
                response = client.listEventBuses(ListEventBusesRequest.builder().limit(50).build());
            } else {
                response = client.listEventBuses(ListEventBusesRequest.builder().nextToken(token).limit(50).build());
            }

            token = response.nextToken();
            eventBuses.addAll(response.eventBuses());

        } while (token != null);

        eventBuses.forEach(o -> busResponses.add(client.describeEventBus(r -> r.name(o.name()))));

        return busResponses;
    }

    @Override
    protected List<DescribeEventBusResponse> findAws(
        EventBridgeClient client, Map<String, String> filters) {

        List<DescribeEventBusResponse> busResponses = new ArrayList<>();

        try {
            busResponses.add(client.describeEventBus(r -> r.name(filters.get("name"))));
        } catch (ResourceNotFoundException ex) {
            // ignore
        }

        return busResponses;
    }
}
