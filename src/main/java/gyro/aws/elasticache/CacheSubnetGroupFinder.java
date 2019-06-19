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

@Type("cache-subnet-group")
public class CacheSubnetGroupFinder extends AwsFinder<ElastiCacheClient, CacheSubnetGroup, CacheSubnetGroupResource> {

    private String cacheSubnetGroupName;

    public String getCacheSubnetGroupName() {
        return cacheSubnetGroupName;
    }

    public void setCacheSubnetGroupName(String cacheSubnetGroupName) {
        this.cacheSubnetGroupName = cacheSubnetGroupName;
    }

    @Override
    protected List<CacheSubnetGroup> findAllAws(ElastiCacheClient client) {
        return client.describeCacheSubnetGroupsPaginator().cacheSubnetGroups().stream().collect(Collectors.toList());
    }

    @Override
    protected List<CacheSubnetGroup> findAws(ElastiCacheClient client, Map<String, String> filters) {
        try {
            return client.describeCacheSubnetGroups(r -> r.cacheSubnetGroupName(filters.get("cache-subnet-group-name"))).cacheSubnetGroups();
        } catch (CacheSubnetGroupNotFoundException ex) {
            return Collections.emptyList();
        }
    }
}
