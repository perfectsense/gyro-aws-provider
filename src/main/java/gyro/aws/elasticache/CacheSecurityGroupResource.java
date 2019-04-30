package gyro.aws.elasticache;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.core.resource.Resource;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticache.model.CacheSecurityGroup;
import software.amazon.awssdk.services.elasticache.model.CreateCacheSecurityGroupResponse;
import software.amazon.awssdk.services.elasticache.model.DescribeCacheSecurityGroupsResponse;

import java.util.Set;

public class CacheSecurityGroupResource extends AwsResource {
    private String cacheSecurityGroupName;
    private String description;

    public String getCacheSecurityGroupName() {
        return cacheSecurityGroupName;
    }

    public void setCacheSecurityGroupName(String cacheSecurityGroupName) {
        this.cacheSecurityGroupName = cacheSecurityGroupName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean refresh() {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        DescribeCacheSecurityGroupsResponse response = client.describeCacheSecurityGroups(
            r -> r.cacheSecurityGroupName(getCacheSecurityGroupName())
        );

        if (!response.cacheSecurityGroups().isEmpty()) {
            CacheSecurityGroup cacheSecurityGroup = response.cacheSecurityGroups().get(0);

            setDescription(cacheSecurityGroup.description());
            cacheSecurityGroup.ec2SecurityGroups();

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void create() {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        CreateCacheSecurityGroupResponse response = client.createCacheSecurityGroup(
            r -> r.description(getDescription())
                .cacheSecurityGroupName(getCacheSecurityGroupName())
        );
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        client.authorizeCacheSecurityGroupIngress(
            r -> r.cacheSecurityGroupName(getCacheSecurityGroupName())
        );
    }

    @Override
    public void delete() {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        client.deleteCacheSubnetGroup(
            r -> r.cacheSubnetGroupName(getCacheSecurityGroupName())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("cache security group");

        if (!ObjectUtils.isBlank(getCacheSecurityGroupName())) {
            sb.append(" - ").append(getCacheSecurityGroupName());
        }

        return sb.toString();
    }
}
