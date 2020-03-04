/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws.neptune;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.neptune.NeptuneClient;
import software.amazon.awssdk.services.neptune.model.DBCluster;
import software.amazon.awssdk.services.neptune.model.DbClusterNotFoundException;
import software.amazon.awssdk.services.neptune.model.Filter;

/**
 * Query neptune cluster.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    cluster: $(external-query aws::neptune-cluster { db-cluster-id: 'neptune-cluster-example' })
 */
@Type("neptune-cluster")
public class NeptuneClusterFinder extends AwsFinder<NeptuneClient, DBCluster, NeptuneClusterResource> {

    private String dbClusterId;
    private String engine;

    /**
     * The identifier or arn of the cluster.
     */
    public String getDbClusterId() {
        return dbClusterId;
    }

    public void setDbClusterId(String dbClusterId) {
        this.dbClusterId = dbClusterId;
    }

    /**
     * The name of the engine by which the cluster was created.
     */
    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    @Override
    protected List<DBCluster> findAllAws(NeptuneClient client) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.builder().name("engine").values("neptune").build());

        return client.describeDBClusters(r -> r.filters(filters)).dbClusters().stream().collect(Collectors.toList());
    }

    @Override
    protected List<DBCluster> findAws(NeptuneClient client, Map<String, String> filters) {
        if (!filters.containsKey("engine")) {
            filters.put("engine", "neptune");
        }

        try {
            return client.describeDBClusters(r -> r.filters(filters.entrySet()
                .stream()
                .map(e -> Filter.builder().name(e.getKey()).values(e.getValue()).build())
                .collect(Collectors.toList()))).dbClusters().stream().collect(Collectors.toList());

        } catch (DbClusterNotFoundException ex) {
            return Collections.emptyList();
        }
    }
}
