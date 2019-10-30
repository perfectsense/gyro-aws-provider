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

package gyro.aws.lambda;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.GetEventSourceMappingResponse;
import software.amazon.awssdk.services.lambda.model.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query lambda event source mapping.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    event-source-mapping: $(external-query aws::lambda-event-source-mapping { id: ''})
 */
@Type("lambda-event-source-mapping")
public class EventSourceMappingFinder extends AwsFinder<LambdaClient, GetEventSourceMappingResponse, EventSourceMappingResource> {
    private String id;

    /**
     * The id of the event source mapping.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<GetEventSourceMappingResponse> findAllAws(LambdaClient client) {
        List<GetEventSourceMappingResponse> getEventSourceMappingResponses = new ArrayList<>();
        client.listEventSourceMappingsPaginator().eventSourceMappings()
            .forEach(o -> getEventSourceMappingResponses.add(client.getEventSourceMapping(r -> r.uuid(o.uuid()))));
        return getEventSourceMappingResponses;
    }

    @Override
    protected List<GetEventSourceMappingResponse> findAws(LambdaClient client, Map<String, String> filters) {
        List<GetEventSourceMappingResponse> getEventSourceMappingResponses = new ArrayList<>();

        if (!filters.containsKey("id")) {
            throw new IllegalArgumentException("'id' is required.");
        }

        try {
            getEventSourceMappingResponses.add(client.getEventSourceMapping(r -> r.uuid(filters.get("id"))));
        } catch (ResourceNotFoundException ignore) {
            // ignore
        }

        return getEventSourceMappingResponses;
    }
}
