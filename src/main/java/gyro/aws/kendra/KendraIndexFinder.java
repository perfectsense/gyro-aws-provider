/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.kendra;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.kendra.KendraClient;
import software.amazon.awssdk.services.kendra.model.DescribeIndexResponse;
import software.amazon.awssdk.services.kendra.model.ListIndicesRequest;

/**
 * Query kendra index.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    kendra-index: $(external-query aws::kendra-index { name: "example-index" })
 */
@Type("kendra-index")
public class KendraIndexFinder extends AwsFinder<KendraClient, DescribeIndexResponse, KendraIndexResource> {

    private String name;

    /**
     * The name of the index.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<DescribeIndexResponse> findAllAws(KendraClient client) {
        return client.listIndicesPaginator(ListIndicesRequest.builder().build())
            .stream()
            .flatMap(r -> r.indexConfigurationSummaryItems().stream())
            .map(r -> client.describeIndex(i -> i.id(r.id())))
            .collect(Collectors.toList());
    }

    @Override
    protected List<DescribeIndexResponse> findAws(KendraClient client, Map<String, String> filters) {
        return client.listIndicesPaginator(ListIndicesRequest.builder().build())
            .stream()
            .flatMap(r -> r.indexConfigurationSummaryItems().stream())
            .filter(r -> r.name().equals(filters.get("name")))
            .map(r -> client.describeIndex(i -> i.id(r.id())))
            .collect(Collectors.toList());
    }
}
