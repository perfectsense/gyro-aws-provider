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

package gyro.aws.rds;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.GlobalCluster;
import software.amazon.awssdk.services.rds.model.GlobalClusterNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query global cluster.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    global-clusters: $(external-query aws::db-global-cluster { db-cluster-id: 'aurora-global-cluster'})
 */
@Type("db-global-cluster")
public class DbGlobalClusterFinder extends AwsFinder<RdsClient, GlobalCluster, DbGlobalClusterResource> {

    private String dbClusterId;

    /**
     * The identifier or arn of the global cluster.
     */
    public String getDbClusterId() {
        return dbClusterId;
    }

    public void setDbClusterId(String dbClusterId) {
        this.dbClusterId = dbClusterId;
    }

    @Override
    protected List<GlobalCluster> findAws(RdsClient client, Map<String, String> filters) {
        try {
            return client.describeGlobalClusters(r -> r.filters(createRdsFilters(filters))).globalClusters();
        } catch (GlobalClusterNotFoundException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    protected List<GlobalCluster> findAllAws(RdsClient client) {
        return client.describeGlobalClustersPaginator().globalClusters().stream().collect(Collectors.toList());
    }

}
