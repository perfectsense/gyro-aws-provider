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
import software.amazon.awssdk.services.elasticache.model.CacheSubnetGroup;
import software.amazon.awssdk.services.elasticache.model.CacheSubnetGroupNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query cache subnet group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    cache-subnet-groups: $(external-query aws::elasticache-subnet-group { name: 'cache-subnet-group-example'})
 */
@Type("elasticache-subnet-group")
public class CacheSubnetGroupFinder extends AwsFinder<ElastiCacheClient, CacheSubnetGroup, CacheSubnetGroupResource> {

    private String name;

    /**
     * The name of the cache subnet group.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<CacheSubnetGroup> findAllAws(ElastiCacheClient client) {
        return client.describeCacheSubnetGroupsPaginator().cacheSubnetGroups().stream().collect(Collectors.toList());
    }

    @Override
    protected List<CacheSubnetGroup> findAws(ElastiCacheClient client, Map<String, String> filters) {
        try {
            return client.describeCacheSubnetGroups(r -> r.cacheSubnetGroupName(filters.get("name"))).cacheSubnetGroups();
        } catch (CacheSubnetGroupNotFoundException ex) {
            return Collections.emptyList();
        }
    }
}
