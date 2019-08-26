package gyro.aws.elasticache;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticache.model.CacheParameterGroup;
import software.amazon.awssdk.services.elasticache.model.CacheParameterGroupNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query cache parameter group.
 *
 * .. code-block:: gyro
 *
 *    cache-param-groups: $(external-query aws::elasticache-parameter-group { name: 'cache-param-group-example'})
 */
@Type("elasticache-parameter-group")
public class CacheParameterGroupFinder extends AwsFinder<ElastiCacheClient, CacheParameterGroup, CacheParameterGroupResource> {

    private String name;

    /**
     * The name of the cache parameter group.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<CacheParameterGroup> findAllAws(ElastiCacheClient client) {
        return client.describeCacheParameterGroupsPaginator().cacheParameterGroups().stream().collect(Collectors.toList());
    }

    @Override
    protected List<CacheParameterGroup> findAws(ElastiCacheClient client, Map<String, String> filters) {
        try {
            return client.describeCacheParameterGroups(r -> r.cacheParameterGroupName(filters.get("name"))).cacheParameterGroups();
        } catch (CacheParameterGroupNotFoundException ex) {
            return Collections.emptyList();
        }
    }
}
