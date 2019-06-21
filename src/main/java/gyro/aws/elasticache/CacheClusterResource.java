package gyro.aws.elasticache;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsCredentials;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.aws.sns.TopicResource;
import gyro.core.GyroException;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticache.model.CacheCluster;
import software.amazon.awssdk.services.elasticache.model.CacheClusterNotFoundException;
import software.amazon.awssdk.services.elasticache.model.CacheNode;
import software.amazon.awssdk.services.elasticache.model.CacheSecurityGroupMembership;
import software.amazon.awssdk.services.elasticache.model.CreateCacheClusterRequest;
import software.amazon.awssdk.services.elasticache.model.CreateCacheClusterResponse;
import software.amazon.awssdk.services.elasticache.model.DescribeCacheClustersResponse;
import software.amazon.awssdk.services.elasticache.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.elasticache.model.ModifyCacheClusterRequest;
import software.amazon.awssdk.services.elasticache.model.Tag;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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
 *         cache-node-type: "cache.t2.micro"
 *         cache-param-group: $(aws::cache-param-group cache-param-group-group-cache-cluster-example)
 *         cache-subnet-group: $(aws::cache-subnet-group cache-subnet-group-cache-cluster-example)
 *         engine: "memcached"
 *         engine-version: "1.5.10"
 *         num-cache-nodes: 1
 *         preferred-availability-zones: [
 *             "us-east-2a"
 *         ]
 *         port: 11211
 *         preferred-maintenance-window: "thu:01:00-thu:02:00"
 *         security-groups: [
 *             $(aws::security-group security-group-cache-cluster-example-1)
 *         ]
 *         account-number: "242040583208"
 *
 *         tags: {
 *             Name: "cache-cluster-example"
 *         }
 *     end
 */
@Type("cache-cluster")
public class CacheClusterResource extends AwsResource implements Copyable<CacheCluster> {
    private String azMode;
    private String cacheClusterId;
    private String cacheNodeType;
    private CacheParameterGroupResource cacheParamGroup;
    private List<String> cacheSecurityGroupNames;
    private CacheSubnetGroupResource cacheSubnetGroup;
    private String engine;
    private String engineVersion;
    private TopicResource notificationTopic;
    private Integer numCacheNodes;
    private Integer port;
    private String preferredMaintenanceWindow;
    private String replicationGroupId;
    private Set<SecurityGroupResource> securityGroups;
    private List<String> snapshotArns;
    private Integer snapshotRetentionLimit;
    private String snapshotWindow;
    private Map<String, String> tags;
    private List<String> preferredAvailabilityZones;
    private Boolean applyImmediately;

    private String arn;
    private String status;
    private List<CacheClusterNode> nodes;
    private String preferredAvailabilityZone;

    /**
     * The Az mode of the cluster. Valid value is ``single-az`` or ``cross-az`` (Required)
     */
    @Updatable
    public String getAzMode() {
        return azMode;
    }

    public void setAzMode(String azMode) {
        this.azMode = azMode;
    }

    /**
     * The name of the cache cluster. (Required)
     */
    @Id
    public String getCacheClusterId() {
        return cacheClusterId;
    }

    public void setCacheClusterId(String cacheClusterId) {
        this.cacheClusterId = cacheClusterId;
    }

    /**
     * The type of the cache cluster nodes. (Required)
     */
    @Updatable
    public String getCacheNodeType() {
        return cacheNodeType;
    }

    public void setCacheNodeType(String cacheNodeType) {
        this.cacheNodeType = cacheNodeType;
    }

    /**
     * The cache parameter group to be associated. (Required)
     */
    @Updatable
    public CacheParameterGroupResource getCacheParamGroup() {
        return cacheParamGroup;
    }

    public void setCacheParamGroup(CacheParameterGroupResource cacheParamGroup) {
        this.cacheParamGroup = cacheParamGroup;
    }

    /**
     * The list of cache security groups to be associated.
     */
    @Updatable
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
     * The cache subnet group to be associated. (Required)
     */
    public CacheSubnetGroupResource getCacheSubnetGroup() {
        return cacheSubnetGroup;
    }

    public void setCacheSubnetGroup(CacheSubnetGroupResource cacheSubnetGroup) {
        this.cacheSubnetGroup = cacheSubnetGroup;
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
    @Updatable
    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    /**
     * The sns topic to be associated with the cluster.
     */
    @Updatable
    public TopicResource getNotificationTopic() {
        return notificationTopic;
    }

    public void setNotificationTopic(TopicResource notificationTopic) {
        this.notificationTopic = notificationTopic;
    }

    /**
     * The number of nodes to be created. (Required)
     */
    @Updatable
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
    @Updatable
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
    @Updatable
    public Set<SecurityGroupResource> getSecurityGroups() {
        if (securityGroups == null) {
            securityGroups = new LinkedHashSet<>();
        }

        return securityGroups;
    }

    public void setSecurityGroups(Set<SecurityGroupResource> securityGroups) {
        this.securityGroups = securityGroups;
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
    @Updatable
    public Integer getSnapshotRetentionLimit() {
        return snapshotRetentionLimit;
    }

    public void setSnapshotRetentionLimit(Integer snapshotRetentionLimit) {
        this.snapshotRetentionLimit = snapshotRetentionLimit;
    }

    /**
     * The snapshot window to be used for redis cache cluster.
     */
    @Updatable
    public String getSnapshotWindow() {
        return snapshotWindow;
    }

    public void setSnapshotWindow(String snapshotWindow) {
        this.snapshotWindow = snapshotWindow;
    }

    /**
     * The tags for the cache cluster. (Required)
     */
    @Updatable
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
    @Updatable
    public List<String> getPreferredAvailabilityZones() {
        if (preferredAvailabilityZones == null) {
            preferredAvailabilityZones = new ArrayList<>();
        }

        return preferredAvailabilityZones;
    }

    public void setPreferredAvailabilityZones(List<String> preferredAvailabilityZones) {
        this.preferredAvailabilityZones = preferredAvailabilityZones;
    }

    /**
     * A flag that updates and restarts node immediately rather than waiting for the maintenance window. Defaults to true.
     */
    @Updatable
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
     * Arn of the cluster.
     *
     * @Output
     */
    @Output
    public String getArn() {
        return this.arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * Status of the cluster.
     *
     * @Output
     */
    @Output
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
    @Output
    public List<CacheClusterNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<CacheClusterNode> nodes) {
        this.nodes = nodes;
    }

    /**
     * The preferred availability zone of the cluster.
     *
     * @Output
     */
    @Output
    public String getPreferredAvailabilityZone() {
        return preferredAvailabilityZone;
    }

    public void setPreferredAvailabilityZone(String preferredAvailabilityZone) {
        this.preferredAvailabilityZone = preferredAvailabilityZone;
    }

    @Override
    public void copyFrom(CacheCluster cacheCluster) {
        setCacheClusterId(cacheCluster.cacheClusterId());
        setCacheNodeType(cacheCluster.cacheNodeType());
        setCacheParamGroup(findById(CacheParameterGroupResource.class, cacheCluster.cacheParameterGroup().cacheParameterGroupName()));
        setCacheSecurityGroupNames(cacheCluster.cacheSecurityGroups().stream().map(CacheSecurityGroupMembership::cacheSecurityGroupName).collect(Collectors.toList()));
        setCacheSubnetGroup(findById(CacheSubnetGroupResource.class, cacheCluster.cacheSubnetGroupName()));
        setEngine(cacheCluster.engine());
        setEngineVersion(cacheCluster.engineVersion());
        setNotificationTopic(cacheCluster.notificationConfiguration() != null ? findById(TopicResource.class, cacheCluster.notificationConfiguration().topicArn()) : null);
        setNumCacheNodes(cacheCluster.pendingModifiedValues().numCacheNodes() != null ? cacheCluster.pendingModifiedValues().numCacheNodes() : cacheCluster.numCacheNodes());
        setPort(cacheCluster.configurationEndpoint().port());
        setPreferredAvailabilityZone(cacheCluster.preferredAvailabilityZone());
        setPreferredMaintenanceWindow(cacheCluster.preferredMaintenanceWindow());
        setReplicationGroupId(cacheCluster.replicationGroupId());
        setSecurityGroups(cacheCluster.securityGroups().stream().map(s -> findById(SecurityGroupResource.class, s.securityGroupId())).collect(Collectors.toSet()));
        setSnapshotRetentionLimit(cacheCluster.snapshotRetentionLimit());
        setSnapshotWindow(cacheCluster.snapshotWindow());

        List<CacheClusterNode> nodes = new ArrayList<>();
        for (CacheNode model : cacheCluster.cacheNodes()) {
            CacheClusterNode node = newSubresource(CacheClusterNode.class);
            node.copyFrom(model);
            nodes.add(node);
        }

        setNodes(nodes);
        setAzMode(cacheCluster.preferredAvailabilityZone().equalsIgnoreCase("multiple") ? "cross-az" : "single-az");
        setArn("arn:aws:elasticache:" + getRegion() + ":" + getAccountNumber() + ":cluster:" + getCacheClusterId());
        setStatus(cacheCluster.cacheClusterStatus());

        ElastiCacheClient client = createClient(ElastiCacheClient.class);
        ListTagsForResourceResponse tagResponse = client.listTagsForResource(
            r -> r.resourceName(getArn())
        );

        loadTags(tagResponse.tagList());
    }

    @Override
    public boolean refresh() {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        CacheCluster cacheCluster = getCacheCluster(client);

        if (cacheCluster == null) {
            return false;
        }

        copyFrom(cacheCluster);
        return true;
    }

    @Override
    public void create() {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        CreateCacheClusterRequest.Builder builder = CreateCacheClusterRequest.builder()
            .azMode(getAzMode())
            .cacheClusterId(getCacheClusterId())
            .cacheNodeType(getCacheNodeType())
            .cacheParameterGroupName(getCacheParamGroup().getCacheParamGroupName())
            .cacheSecurityGroupNames(getCacheSecurityGroupNames())
            .cacheSubnetGroupName(getCacheSubnetGroup().getCacheSubnetGroupName())
            .engine(getEngine())
            .engineVersion(getEngineVersion())
            .notificationTopicArn(getNotificationTopic() != null ? getNotificationTopic().getTopicArn() : null)
            .numCacheNodes(getNumCacheNodes())
            .port(getPort())
            .preferredAvailabilityZones(getPreferredAvailabilityZones())
            .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
            .securityGroupIds(getSecurityGroups().stream().map(SecurityGroupResource::getGroupId).collect(Collectors.toList()))
            .tags(toCacheTags(getTags()));

        if (("redis").equalsIgnoreCase(getEngine())) {
            builder.replicationGroupId(getReplicationGroupId())
                .snapshotArns(getSnapshotArns())
                .snapshotRetentionLimit(getSnapshotRetentionLimit())
                .snapshotWindow(getSnapshotWindow());
        }

        CreateCacheClusterResponse response = client.createCacheCluster(builder.build());

        setStatus(response.cacheCluster().cacheClusterStatus());
        setArn("arn:aws:elasticache:" + getRegion() + ":" + getAccountNumber() + ":cluster:" + getCacheClusterId());

        Wait.atMost(3, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> isAvailable(client));

        CacheCluster cacheCluster = getCacheCluster(client);
        List<CacheClusterNode> nodes = new ArrayList<>();
        if (cacheCluster != null) {
            for (CacheNode model : cacheCluster.cacheNodes()) {
                CacheClusterNode node = newSubresource(CacheClusterNode.class);
                node.copyFrom(model);
                nodes.add(node);
            }

            setNodes(nodes);
        }
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);
        Set<String> properties = new HashSet<>(changedProperties);

        CacheClusterResource currentCacheClusterResource = (CacheClusterResource) current;

        if (properties.contains("tags")) {
            Map<String, String> pendingTags = getTags();
            Map<String, String> currentTags = currentCacheClusterResource.getTags();

            saveTags(pendingTags, currentTags, client);

            properties.remove("tags");
        }

        if (!properties.isEmpty()) {
            ModifyCacheClusterRequest.Builder builder = ModifyCacheClusterRequest.builder()
                .cacheClusterId(getCacheClusterId())
                .cacheNodeType(getCacheNodeType())
                .cacheParameterGroupName(getCacheParamGroup().getCacheParamGroupName())
                .cacheSecurityGroupNames(getCacheSecurityGroupNames())
                .engineVersion(getEngineVersion())
                .notificationTopicArn(getNotificationTopic() != null ? getNotificationTopic().getTopicArn() : null)
                .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
                .securityGroupIds(getSecurityGroups().stream().map(SecurityGroupResource::getGroupId).collect(Collectors.toList()))
                .numCacheNodes(getNumCacheNodes());

            if (properties.contains("preferred-availability-zones")) {
                List<String> newAvailabilityZones = new ArrayList<>(getPreferredAvailabilityZones());
                newAvailabilityZones.removeAll(currentCacheClusterResource.getPreferredAvailabilityZones());

                if (newAvailabilityZones.size() > 0) {
                    builder.newAvailabilityZones(newAvailabilityZones);
                }
            }

            if (("redis").equalsIgnoreCase(getEngine())) {
                builder.snapshotRetentionLimit(getSnapshotRetentionLimit())
                    .snapshotWindow(getSnapshotWindow());
            }

            ModifyCacheClusterRequest modifyCacheClusterRequest = builder.applyImmediately(getApplyImmediately()).build();

            client.modifyCacheCluster(modifyCacheClusterRequest);

            Wait.atMost(3, TimeUnit.MINUTES)
                .checkEvery(10, TimeUnit.SECONDS)
                .prompt(true)
                .until(() -> isAvailable(client));
        }
    }

    @Override
    public void delete() {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        client.deleteCacheCluster(
            r -> r.cacheClusterId(getCacheClusterId())
        );

        Wait.atMost(3, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> getCacheCluster(client) == null);
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

    private String getAccountNumber() {
        StsClient client = createClient(StsClient.class);
        GetCallerIdentityResponse response = client.getCallerIdentity();
        return response.account();
    }

    private String getRegion() {
        AwsCredentials credentials = credentials(AwsCredentials.class);
        return credentials.getRegion();
    }

    private boolean isAvailable(ElastiCacheClient client) {
        CacheCluster cacheCluster = getCacheCluster(client);

        return cacheCluster != null && cacheCluster.cacheClusterStatus().equals("available");
    }

    private CacheCluster getCacheCluster(ElastiCacheClient client) {
        CacheCluster cacheCluster = null;

        if (ObjectUtils.isBlank(getCacheClusterId())) {
            throw new GyroException("cache-cluster-id is missing, unable to load cache cluster.");
        }

        try {
            DescribeCacheClustersResponse response = client.describeCacheClusters(
                r -> r.cacheClusterId(getCacheClusterId()).showCacheNodeInfo(true)
            );

            if (!response.cacheClusters().isEmpty()) {
                cacheCluster = response.cacheClusters().get(0);
            }

        } catch (CacheClusterNotFoundException ex) {
            // Ignore
        }

        return cacheCluster;
    }
}
