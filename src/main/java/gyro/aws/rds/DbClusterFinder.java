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
import software.amazon.awssdk.services.rds.model.DBCluster;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query db cluster.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    clusters: $(external-query aws::db-cluster { db-cluster-id: 'aurora-mysql-cluster'})
 */
@Type("db-cluster")
public class DbClusterFinder extends AwsFinder<RdsClient, DBCluster, DbClusterResource> {

    private String dbClusterId;

    /**
     * The identifier or arn of the cluster.
     */
    public String getDbClusterId() {
        return dbClusterId;
    }

    public void setDbClusterId(String dbClusterId) {
        this.dbClusterId = dbClusterId;
    }

    @Override
    protected List<DBCluster> findAws(RdsClient client, Map<String, String> filters) {
        return client.describeDBClustersPaginator(r -> r.filters(createRdsFilters(filters))).dbClusters().stream().collect(Collectors.toList());
    }

    @Override
    protected List<DBCluster> findAllAws(RdsClient client) {
        return client.describeDBClustersPaginator().dbClusters().stream().collect(Collectors.toList());
    }

}
