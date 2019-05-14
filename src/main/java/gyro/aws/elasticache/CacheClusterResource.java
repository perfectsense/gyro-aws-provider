package gyro.aws.elasticache;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsCredentials;
import gyro.aws.AwsResource;
import gyro.core.GyroCore;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceOutput;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceUpdatable;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticache.model.CacheCluster;
import software.amazon.awssdk.services.elasticache.model.CacheNode;
import software.amazon.awssdk.services.elasticache.model.CacheSecurityGroupMembership;
import software.amazon.awssdk.services.elasticache.model.CreateCacheClusterRequest;
import software.amazon.awssdk.services.elasticache.model.CreateCacheClusterResponse;
import software.amazon.awssdk.services.elasticache.model.DescribeCacheClustersResponse;
import software.amazon.awssdk.services.elasticache.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.elasticache.model.ModifyCacheClusterRequest;
import software.amazon.awssdk.services.elasticache.model.RebootCacheClusterRequest;
import software.amazon.awssdk.services.elasticache.model.SecurityGroupMembership;
import software.amazon.awssdk.services.elasticache.model.Tag;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a cache cluster.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::cache-cluster cache-cluster-example
 *         az-mode: "cross-az"
 *         cache-cluster-id: "cache-cluster-ex-1"
 *         cache-node-type: "cache.r5.large"
 *         cache-param-group-name: $(aws::cache-param-group cache-param-group-group-cache-cluster-example | cache-param-group-name)
 *         cache-subnet-group-name: $(aws::cache-subnet-group cache-subnet-group-cache-cluster-example | cache-subnet-group-name)
 *         engine: "memcached"
 *         engine-version: "1.5.10"
 *         num-cache-nodes: 1
 *         preferred-availability-zones: [
 *             "us-east-2a"
 *         ]
 *         port: 11211
 *         preferred-maintenance-window: "thu:01:00-thu:02:00"
 *         security-group-ids: [
 *             $(aws::security-group security-group-cache-cluster-example-1 | group-id)
 *         ]
 *         account-number: "242040583208"
 *
 *         tags: {
 *             Name: "cache-cluster-example"
 *         }
 *     end
 */
@ResourceType("cache-cluster")
public class CacheClusterResource extends AwsResource {
    private String azMode;
    private String cacheClusterId;
    private String cacheNodeType;
    private String cacheParamGroupName;
    private List<String> cacheSecurityGroupNames;
    private String cacheSubnetGroupName;
    private String engine;
    private String engineVersion;
    private String notificationTopicArn;
    private Integer numCacheNodes;
    private Integer port;
    private String preferredMaintenanceWindow;
    private String replicationGroupId;
    private List<String> securityGroupIds;
    private List<String> snapshotArns;
    private Integer snapshotRetentionLimit;
    private String snapshotWindow;
    private Map<String, String> tags;
    private List<String> preferredAvailabilityZones;
    private String arn;
    private Boolean applyImmediately;

    private String status;
    private List<String> nodes;
    private String preferredAvailabilityZone;

    /**
     * The Az mode of the cluster. Valid values are ``single-az`` or ``cross-az`` (Required)
     */
    @ResourceUpdatable
    public String getAzMode() {
        return azMode;
    }

    public void setAzMode(String azMode) {
        this.azMode = azMode;
    }

    /**
     * The name of the cache cluster. (Required)
     */
    public String getCacheClusterId() {
        return cacheClusterId;
    }

    public void setCacheClusterId(String cacheClusterId) {
        this.cacheClusterId = cacheClusterId;
    }

    /**
     * The type of the cache cluster. Valid values ``memcached`` or ``redis`` (Required)
     */
    @ResourceUpdatable
    public String getCacheNodeType() {
        return cacheNodeType;
    }

    public void setCacheNodeType(String cacheNodeType) {
        this.cacheNodeType = cacheNodeType;
    }

    /**
     * The name of the cache parameter group to be associated. (Required)
     */
    @ResourceUpdatable
    public String getCacheParamGroupName() {
        return cacheParamGroupName;
    }

    public void setCacheParamGroupName(String cacheParamGroupName) {
        this.cacheParamGroupName = cacheParamGroupName;
    }

    /**
     * The list of cache security groups to be associated.
     */
    @ResourceUpdatable
    public List<String> getCacheSecurityGroupNames() {
        if (cacheSecurityGroupNames == null) {
            cacheSecurityGroupNames = new ArrayList<>();
        }

        return cacheSecurityGroupNames;
    }

    public void setCacheSecurityGroupNames(List<String> cacheSecurityGroupNames) {
        this.cacheSecurityGroupNames = cacheSecurityGroupNames;
    }

    /**
     * The name of the cache parameter group to be associated. (Required)
     */
    public String getCacheSubnetGroupName() {
        return cacheSubnetGroupName;
    }

    public void setCacheSubnetGroupName(String cacheSubnetGroupName) {
        this.cacheSubnetGroupName = cacheSubnetGroupName;
    }

    /**
     * The name of the engine used to create the cluster. (Required)
     */
    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    /**
     * The version of the engine used to create the cluster. (Required)
     */
    @ResourceUpdatable
    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    /**
     * The notification arn to be associated with the cluster.
     */
    @ResourceUpdatable
    public String getNotificationTopicArn() {
        return notificationTopicArn;
    }

    public void setNotificationTopicArn(String notificationTopicArn) {
        this.notificationTopicArn = notificationTopicArn;
    }

    /**
     * The number of nodes to be created. (Required)
     */
    @ResourceUpdatable
    public Integer getNumCacheNodes() {
        return numCacheNodes;
    }

    public void setNumCacheNodes(Integer numCacheNodes) {
        this.numCacheNodes = numCacheNodes;
    }

    /**
     * The port to be used by the cache cluster. (Required)
     */
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * The preferred maintenance window to be used by the cache cluster. (Required)
     */
    @ResourceUpdatable
    public String getPreferredMaintenanceWindow() {
        return preferredMaintenanceWindow;
    }

    public void setPreferredMaintenanceWindow(String preferredMaintenanceWindow) {
        this.preferredMaintenanceWindow = preferredMaintenanceWindow;
    }

    /**
     * The replication group id to be used for redis cache cluster.
     */
    public String getReplicationGroupId() {
        return replicationGroupId;
    }

    public void setReplicationGroupId(String replicationGroupId) {
        this.replicationGroupId = replicationGroupId;
    }

    /**
     * The list of ec2 security groups to be associated.
     */
    @ResourceUpdatable
    public List<String> getSecurityGroupIds() {
        if (securityGroupIds == null) {
            securityGroupIds = new ArrayList<>();
        }

        return securityGroupIds;
    }

    public void setSecurityGroupIds(List<String> securityGroupIds) {
        this.securityGroupIds = securityGroupIds;
    }

    /**
     * A list of snapshot arn's to be used for redis cache cluster.
     */
    public List<String> getSnapshotArns() {
        if (snapshotArns == null) {
            snapshotArns = new ArrayList<>();
        }

        return snapshotArns;
    }

    public void setSnapshotArns(List<String> snapshotArns) {
        this.snapshotArns = snapshotArns;
    }

    /**
     * The snapshot retention limit to be used for redis cache cluster.
     */
    @ResourceUpdatable
    public Integer getSnapshotRetentionLimit() {
        return snapshotRetentionLimit;
    }

    public void setSnapshotRetentionLimit(Integer snapshotRetentionLimit) {
        this.snapshotRetentionLimit = snapshotRetentionLimit;
    }

    /**
     * The snapshot window to be used for redis cache cluster.
     */
    @ResourceUpdatable
    public String getSnapshotWindow() {
        return snapshotWindow;
    }

    public void setSnapshotWindow(String snapshotWindow) {
        this.snapshotWindow = snapshotWindow;
    }

    /**
     * The tags for the cache cluster. (Required)
     */
    @ResourceUpdatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The preferred availability zone for the cluster. (Required)
     */
    @ResourceUpdatable
    public List<String> getPreferredAvailabilityZones() {
        if (preferredAvailabilityZones == null) {
            preferredAvailabilityZones = new ArrayList<>();
        }

        return preferredAvailabilityZones;
    }

    public void setPreferredAvailabilityZones(List<String> preferredAvailabilityZones) {
        this.preferredAvailabilityZones = preferredAvailabilityZones;
    }

    public String getArn() {
        if (arn == null) {
            arn = "arn:aws:elasticache:" + getRegion() + ":" + getAccountNumber() + ":cluster:" + getCacheClusterId();
        }

        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * A flag that updates and restarts node immediately rather than waiting for the maintenance window. Defaults to true.
     */
    public Boolean getApplyImmediately() {
        if (applyImmediately == null) {
            applyImmediately = true;
        }

        return applyImmediately;
    }

    public void setApplyImmediately(Boolean applyImmediately) {
        this.applyImmediately = applyImmediately;
    }

    /**
     * Status of the cluster.
     *
     * @Output
     */
    @ResourceOutput
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * List of nodes under this cluster.
     *
     * @Output
     */
    @ResourceOutput
    public List<String> getNodes() {
        if (nodes == null) {
            nodes = new ArrayList<>();
        }

        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    /**
     * The preferred availability zone of the cluster.
     *
     * @Output
     */
    @ResourceOutput
    public String getPreferredAvailabilityZone() {
        return preferredAvailabilityZone;
    }

    public void setPreferredAvailabilityZone(String preferredAvailabilityZone) {
        this.preferredAvailabilityZone = preferredAvailabilityZone;
    }

    @Override
    public boolean refresh() {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        DescribeCacheClustersResponse response = client.describeCacheClusters(
            r -> r.cacheClusterId(getCacheClusterId())
                .showCacheNodeInfo(true)
        );

        if (!response.cacheClusters().isEmpty()) {
            CacheCluster cacheCluster = response.cacheClusters().get(0);

            setCacheClusterId(cacheCluster.cacheClusterId());
            setCacheNodeType(cacheCluster.cacheNodeType());
            setCacheParamGroupName(cacheCluster.cacheParameterGroup().cacheParameterGroupName());
            setCacheSecurityGroupNames(cacheCluster.cacheSecurityGroups().stream().map(CacheSecurityGroupMembership::cacheSecurityGroupName).collect(Collectors.toList()));
            setCacheSubnetGroupName(cacheCluster.cacheSubnetGroupName());
            setEngine(cacheCluster.engine());
            setEngineVersion(cacheCluster.engineVersion());
            setNotificationTopicArn(cacheCluster.notificationConfiguration() != null ? cacheCluster.notificationConfiguration().topicArn() : null);
            setNumCacheNodes(cacheCluster.numCacheNodes());
            setPort(cacheCluster.configurationEndpoint().port());
            setPreferredAvailabilityZone(cacheCluster.preferredAvailabilityZone());
            setPreferredMaintenanceWindow(cacheCluster.preferredMaintenanceWindow());
            setReplicationGroupId(cacheCluster.replicationGroupId());
            setSecurityGroupIds(cacheCluster.securityGroups().stream().map(SecurityGroupMembership::securityGroupId).collect(Collectors.toList()));
            setSnapshotRetentionLimit(cacheCluster.snapshotRetentionLimit());
            setSnapshotWindow(cacheCluster.snapshotWindow());
            setNodes(cacheCluster.cacheNodes().stream().map(CacheNode::cacheNodeId).collect(Collectors.toList()));
            setAzMode(cacheCluster.preferredAvailabilityZone().equalsIgnoreCase("multiple") ? "cross-az" : "single-az");

            ListTagsForResourceResponse tagResponse = client.listTagsForResource(
                r -> r.resourceName(getArn())
            );

            loadTags(tagResponse.tagList());

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void create() {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        CreateCacheClusterRequest.Builder builder = CreateCacheClusterRequest.builder()
            .azMode(getAzMode())
            .cacheClusterId(getCacheClusterId())
            .cacheNodeType(getCacheNodeType())
            .cacheParameterGroupName(getCacheParamGroupName())
            .cacheSecurityGroupNames(getCacheSecurityGroupNames())
            .cacheSubnetGroupName(getCacheSubnetGroupName())
            .engine(getEngine())
            .engineVersion(getEngineVersion())
            .notificationTopicArn(getNotificationTopicArn())
            .numCacheNodes(getNumCacheNodes())
            .port(getPort())
            .preferredAvailabilityZones(getPreferredAvailabilityZones())
            .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
            .securityGroupIds(getSecurityGroupIds())
            .tags(toCacheTags(getTags()));

        if (("redis").equalsIgnoreCase(getEngine())) {
            builder.replicationGroupId(getReplicationGroupId())
                .snapshotArns(getSnapshotArns())
                .snapshotRetentionLimit(getSnapshotRetentionLimit())
                .snapshotWindow(getSnapshotWindow());
        }

        CreateCacheClusterResponse response = client.createCacheCluster(builder.build());

        setStatus(response.cacheCluster().cacheClusterStatus());

        waitForAvailability(client, false);
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        CacheClusterResource currentCacheClusterResource = (CacheClusterResource) current;

        if (changedProperties.contains("tags")) {
            Map<String, String> pendingTags = getTags();
            Map<String, String> currentTags = currentCacheClusterResource.getTags();

            saveTags(pendingTags, currentTags, client);
        }

        if ((changedProperties.contains("tags") && changedProperties.size() > 1) || !changedProperties.isEmpty()) {

            //Can only specify new availability zones or AZ mode when adding cache nodes. (Condition)

            ModifyCacheClusterRequest.Builder builder = ModifyCacheClusterRequest.builder()
                .cacheClusterId(getCacheClusterId())
                .cacheNodeType(getCacheNodeType())
                .cacheParameterGroupName(getCacheParamGroupName())
                .cacheSecurityGroupNames(getCacheSecurityGroupNames())
                .engineVersion(getEngineVersion())
                .notificationTopicArn(getNotificationTopicArn())
                .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
                .securityGroupIds(getSecurityGroupIds());

            if (changedProperties.contains("num-cache-nodes")) {
                builder = builder.azMode(getAzMode())
                    .numCacheNodes(getNumCacheNodes())
                    .newAvailabilityZones(getPreferredAvailabilityZones());
            }

            if (("redis").equalsIgnoreCase(getEngine())) {
                builder.snapshotRetentionLimit(getSnapshotRetentionLimit())
                    .snapshotWindow(getSnapshotWindow());
            }

            ModifyCacheClusterRequest modifyCacheClusterRequest = builder.applyImmediately(getApplyImmediately()).build();

            client.modifyCacheCluster(modifyCacheClusterRequest);

            if (getApplyImmediately()) {
                waitForAvailability(client, false);
            } else {
                waitForAvailability(client, true);

                RebootCacheClusterRequest rebootCacheClusterRequest = RebootCacheClusterRequest.builder()
                    .cacheNodeIdsToReboot(getNodes())
                    .cacheClusterId(getCacheClusterId()).build();

                client.rebootCacheCluster(rebootCacheClusterRequest);

                waitForAvailability(client, false);
            }
        }
    }

    @Override
    public void delete() {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        client.deleteCacheCluster(
            r -> r.cacheClusterId(getCacheClusterId())
        );

        waitForDelete(client);
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("cache cluster");

        if (!ObjectUtils.isBlank(getCacheClusterId())) {
            sb.append(" - ").append(getCacheClusterId());
        }

        return sb.toString();
    }

    private List<Tag> toCacheTags(Map<String, String> tagMap) {
        List<Tag> tags = new ArrayList<>();
        tagMap.keySet().forEach(
            o -> tags.add(Tag.builder().key(o).value(tagMap.get(o)).build())
        );

        return tags;
    }

    private void loadTags(List<Tag> tags) {
        getTags().clear();
        tags.forEach(o -> getTags().put(o.key(), o.value()));
    }

    private void saveTags(Map<String, String> pendingTags, Map<String, String> currentTags, ElastiCacheClient client) {
        MapDifference<String, String> diff = Maps.difference(currentTags, pendingTags);

        // Old tags
        if (!diff.entriesOnlyOnLeft().isEmpty()) {
            client.removeTagsFromResource(
                r -> r.resourceName(getArn())
                    .tagKeys(diff.entriesOnlyOnLeft().keySet())
            );
        }

        // New tags
        if (!diff.entriesOnlyOnRight().isEmpty()) {
            client.addTagsToResource(
                r -> r.resourceName(getArn())
                    .tags(toCacheTags(diff.entriesOnlyOnRight()))
            );
        }

        // Old but modified tags
        if (!diff.entriesDiffering().isEmpty()) {
            client.removeTagsFromResource(
                r -> r.resourceName(getArn())
                    .tagKeys(diff.entriesDiffering().keySet())
            );

            Map<String, String> addTags = new HashMap<>();
            diff.entriesDiffering().keySet().forEach(o -> addTags.put(o, diff.entriesDiffering().get(o).rightValue()));

            client.addTagsToResource(
                r -> r.resourceName(getArn())
                    .tags(toCacheTags(addTags))
            );
        }
    }

    private void waitForAvailability(ElastiCacheClient client, boolean waitWithoutCount) {
        boolean available = false;
        int count = 0;
        while (!available && count < 20) {
            DescribeCacheClustersResponse response = waitHelper(count, client, 10000);

            available = response.cacheClusters().get(0).cacheClusterStatus().equals("available");
            count++;

            if (waitWithoutCount) {
                count = 0;
            } else if (!available && count == 20) {
                boolean wait = GyroCore.ui().readBoolean(Boolean.FALSE, "\nWait for completion?..... ");
                if (wait) {
                    count = 0;
                }
            }
        }
    }

    private void waitForDelete(ElastiCacheClient client) {
        boolean available = true;
        int count = 0;
        while (available && count < 20) {
            DescribeCacheClustersResponse response = waitHelper(count, client, 10000);

            available = !response.cacheClusters().isEmpty();
            count++;

            if (available && count == 20) {
                boolean wait = GyroCore.ui().readBoolean(Boolean.FALSE, "\nWait for completion?..... ");
                if (wait) {
                    count = 0;
                }
            }
        }
    }

    private DescribeCacheClustersResponse waitHelper(int count, ElastiCacheClient client, long interval) {
        if (count > 0) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return client.describeCacheClusters(
            r -> r.cacheClusterId(getCacheClusterId())
        );
    }

    private String getAccountNumber() {
        StsClient client = createClient(StsClient.class);
        GetCallerIdentityResponse response = client.getCallerIdentity();
        return response.account();
    }

    private String getRegion() {
        AwsCredentials credentials = (AwsCredentials) resourceCredentials();
        return credentials.getRegion();
    }
}
