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

@Type("cache-param-group")
public class CacheParameterGroupFinder extends AwsFinder<ElastiCacheClient, CacheParameterGroup, CacheParameterGroupResource> {

    private String cacheParamGroupName;

    public String getCacheParamGroupName() {
        return cacheParamGroupName;
    }

    public void setCacheParamGroupName(String cacheParamGroupName) {
        this.cacheParamGroupName = cacheParamGroupName;
    }

    @Override
    protected List<CacheParameterGroup> findAllAws(ElastiCacheClient client) {
        return client.describeCacheParameterGroupsPaginator().cacheParameterGroups().stream().collect(Collectors.toList());
    }

    @Override
    protected List<CacheParameterGroup> findAws(ElastiCacheClient client, Map<String, String> filters) {
        try {
            return client.describeCacheParameterGroups(r -> r.cacheParameterGroupName(filters.get("cache-param-group-name"))).cacheParameterGroups();
        } catch (CacheParameterGroupNotFoundException ex) {
            return Collections.emptyList();
        }
    }
}
