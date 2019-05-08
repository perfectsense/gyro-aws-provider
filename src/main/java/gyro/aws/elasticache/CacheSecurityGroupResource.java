package gyro.aws.elasticache;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.core.GyroException;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceOutput;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceUpdatable;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticache.model.CacheSecurityGroup;
import software.amazon.awssdk.services.elasticache.model.CreateCacheSecurityGroupResponse;
import software.amazon.awssdk.services.elasticache.model.DescribeCacheSecurityGroupsResponse;
import software.amazon.awssdk.services.elasticache.model.EC2SecurityGroup;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ResourceType("cache-security-group")
public class CacheSecurityGroupResource extends AwsResource {
    private String cacheSecurityGroupName;
    private String description;
    private Map<String, String> ec2SecurityGroups;

    private String ownerId;

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

    @ResourceUpdatable
    public Map<String, String> getEc2SecurityGroups() {
        if (ec2SecurityGroups == null) {
            ec2SecurityGroups = new HashMap<>();
        }

        return ec2SecurityGroups;
    }

    public void setEc2SecurityGroups(Map<String, String> ec2SecurityGroups) {
        this.ec2SecurityGroups = ec2SecurityGroups;
    }

    @ResourceOutput
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
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
            setOwnerId(cacheSecurityGroup.ownerId());

            getEc2SecurityGroups().clear();
            setEc2SecurityGroups(cacheSecurityGroup.ec2SecurityGroups().stream()
                .collect(
                    Collectors.toMap(EC2SecurityGroup::ec2SecurityGroupName, EC2SecurityGroup::ec2SecurityGroupOwnerId)
                ));

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

        setOwnerId(response.cacheSecurityGroup().ownerId());

        try {
            if (!getEc2SecurityGroups().isEmpty()) {
                for (String key : getEc2SecurityGroups().keySet()) {
                    client.authorizeCacheSecurityGroupIngress(
                        r -> r.cacheSecurityGroupName(getCacheSecurityGroupName())
                            .ec2SecurityGroupName(key)
                            .ec2SecurityGroupOwnerId(getEc2SecurityGroups().get(key))
                    );
                }
            }
        } catch (Exception ex) {
            throw new GyroException(String.format("Error adding ec2 security groups to cache security group %s",
                getCacheSecurityGroupName()), ex);
        }
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        Map<String, String> currentEc2SecurityGroups = ((CacheSecurityGroupResource) current).getEc2SecurityGroups();

        MapDifference<String, String> diff = Maps.difference(currentEc2SecurityGroups, getEc2SecurityGroups());

        if (!diff.entriesOnlyOnLeft().isEmpty()) {
            for (String key: diff.entriesOnlyOnLeft().keySet()) {
                client.revokeCacheSecurityGroupIngress(
                    r -> r.cacheSecurityGroupName(getCacheSecurityGroupName())
                        .ec2SecurityGroupName(key)
                        .ec2SecurityGroupOwnerId(diff.entriesOnlyOnLeft().get(key))
                );
            }
        }

        if (!diff.entriesOnlyOnRight().isEmpty()) {
            for (String key : diff.entriesOnlyOnRight().keySet())
            client.authorizeCacheSecurityGroupIngress(
                r -> r.cacheSecurityGroupName(getCacheSecurityGroupName())
                    .ec2SecurityGroupName(key)
                    .ec2SecurityGroupOwnerId(diff.entriesOnlyOnRight().get(key))
            );
        }

        if (!diff.entriesDiffering().isEmpty()) {
            for (String key: diff.entriesDiffering().keySet()) {
                client.revokeCacheSecurityGroupIngress(
                    r -> r.cacheSecurityGroupName(getCacheSecurityGroupName())
                        .ec2SecurityGroupName(key)
                        .ec2SecurityGroupOwnerId(diff.entriesDiffering().get(key).leftValue())
                );
            }

            for (String key: diff.entriesDiffering().keySet()) {
                client.authorizeCacheSecurityGroupIngress(
                    r -> r.cacheSecurityGroupName(getCacheSecurityGroupName())
                        .ec2SecurityGroupName(key)
                        .ec2SecurityGroupOwnerId(diff.entriesDiffering().get(key).rightValue())
                );
            }
        }
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
