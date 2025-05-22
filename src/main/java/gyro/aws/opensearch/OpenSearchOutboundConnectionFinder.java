/*
 * Copyright 2025, Perfect Sense, Inc.
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

package gyro.aws.opensearch;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.opensearch.OpenSearchClient;
import software.amazon.awssdk.services.opensearch.model.DescribeOutboundConnectionsRequest;
import software.amazon.awssdk.services.opensearch.model.OutboundConnection;

/**
 * Query opensearch outbound connection.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    opensearch-outbound-connection: $(external-query aws::opensearch-outbound-connection {connection-id: 'example-connection-id'})
 */
@Type("opensearch-outbound-connection")
public class OpenSearchOutboundConnectionFinder
    extends AwsFinder<OpenSearchClient, OutboundConnection, OpenSearchOutboundConnectionResource> {

    @Override
    protected List<OutboundConnection> findAllAws(OpenSearchClient client) {
        return client.describeOutboundConnectionsPaginator(DescribeOutboundConnectionsRequest.builder().build())
            .stream()
            .flatMap(r -> r.connections().stream())
            .collect(Collectors.toList());
    }

    @Override
    protected List<OutboundConnection> findAws(OpenSearchClient client, Map<String, String> filters) {
        return client.describeOutboundConnectionsPaginator(r -> r.filters(createOpensearchFilters(filters))).stream()
            .flatMap(r -> r.connections().stream())
            .collect(Collectors.toList());
    }
}

