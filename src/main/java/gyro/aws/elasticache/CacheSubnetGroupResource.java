package gyro.aws.elasticache;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceUpdatable;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticache.model.CacheSubnetGroup;
import software.amazon.awssdk.services.elasticache.model.DescribeCacheSubnetGroupsResponse;
import software.amazon.awssdk.services.elasticache.model.Subnet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a cache subnet group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::cache-subnet-group cache-subnet-group-example
 *         cache-subnet-group-name: "cache-subnet-group-example"
 *         description: "cache-subnet-group-desc"
 *         subnets: [
 *             $(aws::subnet subnet-cache-subnet-group-example-1 | subnet-id),
 *             $(aws::subnet subnet-cache-subnet-group-example-2 | subnet-id),
 *             $(aws::subnet subnet-cache-subnet-group-example-3 | subnet-id)
 *         ]
 *     end
 */
@ResourceType("cache-subnet-group")
public class CacheSubnetGroupResource extends AwsResource {
    private String cacheSubnetGroupName;
    private String description;
    private List<String> subnets;

    /**
     * The name of the cache subnet group. (Required)
     */
    public String getCacheSubnetGroupName() {
        return cacheSubnetGroupName;
    }

    public void setCacheSubnetGroupName(String cacheSubnetGroupName) {
        this.cacheSubnetGroupName = cacheSubnetGroupName;
    }

    /**
     * The description of the cache subnet group.
     */
    @ResourceUpdatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * A list of subnet id's. (Required)
     */
    @ResourceUpdatable
    public List<String> getSubnets() {
        if (subnets == null) {
            subnets = new ArrayList<>();
        }

        if (!subnets.isEmpty() && !subnets.contains(null)) {
            Collections.sort(subnets);
        }

        return subnets;
    }

    public void setSubnets(List<String> subnets) {
        this.subnets = subnets;
    }

    @Override
    public boolean refresh() {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        DescribeCacheSubnetGroupsResponse response = client.describeCacheSubnetGroups(
            r -> r.cacheSubnetGroupName(getCacheSubnetGroupName())
        );

        if (!response.cacheSubnetGroups().isEmpty()) {
            CacheSubnetGroup cacheSubnetGroup = response.cacheSubnetGroups().get(0);

            setDescription(cacheSubnetGroup.cacheSubnetGroupDescription());
            setSubnets(cacheSubnetGroup.subnets().stream().map(Subnet::subnetIdentifier).collect(Collectors.toList()));

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void create() {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        client.createCacheSubnetGroup(
            r -> r.cacheSubnetGroupName(getCacheSubnetGroupName())
                .cacheSubnetGroupDescription(getDescription())
                .subnetIds(getSubnets())
        );

    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        client.modifyCacheSubnetGroup(
            r -> r.cacheSubnetGroupName(getCacheSubnetGroupName())
                .cacheSubnetGroupDescription(getDescription())
                .subnetIds(getSubnets())
        );
    }

    @Override
    public void delete() {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        client.deleteCacheSubnetGroup(
            r -> r.cacheSubnetGroupName(getCacheSubnetGroupName())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("cache subnet group");

        if (!ObjectUtils.isBlank(getCacheSubnetGroupName())) {
            sb.append(" - ").append(getCacheSubnetGroupName());
        }

        return sb.toString();
    }
}
