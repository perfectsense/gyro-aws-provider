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
 * .. code-block:: gyro
 *
 *    cache-clusters: $(aws::elasticache-cluster EXTERNAL/* | cache-cluster-id = 'cache-cluster-ex-1')
 */
@Type("elasticache-cluster")
public class CacheClusterFinder extends AwsFinder<ElastiCacheClient, CacheCluster, CacheClusterResource> {

    private String cacheClusterId;

    /**
     * The identifier of the cache cluster.
     */
    public String getCacheClusterId() {
        return cacheClusterId;
    }

    public void setCacheClusterId(String cacheClusterId) {
        this.cacheClusterId = cacheClusterId;
    }

    @Override
    protected List<CacheCluster> findAllAws(ElastiCacheClient client) {
        return client.describeCacheClustersPaginator(r -> r.showCacheNodeInfo(true)).cacheClusters().stream().collect(Collectors.toList());
    }

    @Override
    protected List<CacheCluster> findAws(ElastiCacheClient client, Map<String, String> filters) {
        try {
            return client.describeCacheClusters(r -> r.cacheClusterId(filters.get("cache-cluster-id")).showCacheNodeInfo(true)).cacheClusters();
        } catch (CacheClusterNotFoundException ex) {
            return Collections.emptyList();
        }
    }
}
