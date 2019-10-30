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

package gyro.aws.elasticache;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticache.model.CacheCluster;
import software.amazon.awssdk.services.elasticache.model.CacheClusterNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query cache cluster.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    cache-clusters: $(external-query aws::elasticache-cluster { id: 'cache-cluster-ex-1'})
 */
@Type("elasticache-cluster")
public class CacheClusterFinder extends AwsFinder<ElastiCacheClient, CacheCluster, CacheClusterResource> {

    private String id;

    /**
     * The identifier of the cache cluster.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<CacheCluster> findAllAws(ElastiCacheClient client) {
        return client.describeCacheClustersPaginator(r -> r.showCacheNodeInfo(true)).cacheClusters().stream().collect(Collectors.toList());
    }

    @Override
    protected List<CacheCluster> findAws(ElastiCacheClient client, Map<String, String> filters) {
        try {
            return client.describeCacheClusters(r -> r.cacheClusterId(filters.get("id")).showCacheNodeInfo(true)).cacheClusters();
        } catch (CacheClusterNotFoundException ex) {
            return Collections.emptyList();
        }
    }
}
