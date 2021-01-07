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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.kendra.KendraClient;
import software.amazon.awssdk.services.kendra.model.DataSourceSummary;
import software.amazon.awssdk.services.kendra.model.DescribeDataSourceResponse;

/**
 * Query kendra data source.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    kendra-data-source: $(external-query aws::kendra-data-source { id: "", index-id: ""})
 */
@Type("kendra-data-source")
public class KendraDataSourceFinder
    extends AwsFinder<KendraClient, DescribeDataSourceResponse, KendraDataSourceResource> {

    private String id;
    private String indexId;

    /**
     * The id of the data source.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The id of the index associated with the data source.
     */
    public String getIndexId() {
        return indexId;
    }

    public void setIndexId(String indexId) {
        this.indexId = indexId;
    }

    @Override
    protected List<DescribeDataSourceResponse> findAllAws(KendraClient client) {
        throw new IllegalArgumentException("Cannot query data sources without 'index-id'.");
    }

    @Override
    protected List<DescribeDataSourceResponse> findAws(KendraClient client, Map<String, String> filters) {
        List<DescribeDataSourceResponse> dataSources = new ArrayList<>();

        if (filters.containsKey("index-id")) {
            String indexId = filters.get("index-id");

            if (!filters.containsKey("id")) {
                List<DataSourceSummary> dataSourceSummaries = client.listDataSourcesPaginator(r -> r.indexId(
                    indexId)).stream().flatMap(r -> r.summaryItems().stream()).collect(Collectors.toList());

                if (!dataSourceSummaries.isEmpty()) {
                    dataSources = dataSourceSummaries
                        .stream()
                        .map(r -> client.describeDataSource(d -> d.id(r.id()).indexId(indexId)))
                        .collect(Collectors.toList());
                }

            } else {
                DescribeDataSourceResponse dataSource = client.describeDataSource(d -> d.id(filters.get("id"))
                    .indexId(indexId));

                if (dataSource != null) {
                    dataSources = Collections.singletonList(dataSource);
                }
            }

        } else {
            throw new IllegalArgumentException("Cannot query faqs without 'index-id'.");
        }

        return dataSources;
    }
}
