package gyro.aws.elasticache;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.core.resource.Resource;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticache.model.CacheCluster;
import software.amazon.awssdk.services.elasticache.model.CacheSecurityGroupMembership;
import software.amazon.awssdk.services.elasticache.model.CreateCacheClusterResponse;
import software.amazon.awssdk.services.elasticache.model.DescribeCacheClustersResponse;
import software.amazon.awssdk.services.elasticache.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.elasticache.model.SecurityGroupMembership;
import software.amazon.awssdk.services.elasticache.model.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CacheClusterResource extends AwsResource {
    private Boolean autoMinorVersionUpgrade;
    private String azMode;
    private String cacheClusterId;
    private String cacheNodeType;
    private String cacheParameterGroupName;
    private List<String> cacheSecurityGroupNames;
    private String cacheSubnetGroupName;
    private String engine;
    private String engineVersion;
    private String notificationTopicArn;
    private Integer numCacheNodes;
    private Integer port;
    private String preferredAvailabilityZone;
    private String preferredMaintenanceWindow;
    private String replicationGroupId;
    private List<String> securityGroupIds;
    private List<String> snapshotArns;
    private Integer snapshotRetentionLimit;
    private String snapshotWindow;
    private Map<String, String> tags;

    private String status;

    public Boolean getAutoMinorVersionUpgrade() {
        return autoMinorVersionUpgrade;
    }

    public void setAutoMinorVersionUpgrade(Boolean autoMinorVersionUpgrade) {
        this.autoMinorVersionUpgrade = autoMinorVersionUpgrade;
    }

    public String getAzMode() {
        return azMode;
    }

    public void setAzMode(String azMode) {
        this.azMode = azMode;
    }

    public String getCacheClusterId() {
        return cacheClusterId;
    }

    public void setCacheClusterId(String cacheClusterId) {
        this.cacheClusterId = cacheClusterId;
    }

    public String getCacheNodeType() {
        return cacheNodeType;
    }

    public void setCacheNodeType(String cacheNodeType) {
        this.cacheNodeType = cacheNodeType;
    }

    public String getCacheParameterGroupName() {
        return cacheParameterGroupName;
    }

    public void setCacheParameterGroupName(String cacheParameterGroupName) {
        this.cacheParameterGroupName = cacheParameterGroupName;
    }

    public List<String> getCacheSecurityGroupNames() {
        return cacheSecurityGroupNames;
    }

    public void setCacheSecurityGroupNames(List<String> cacheSecurityGroupNames) {
        this.cacheSecurityGroupNames = cacheSecurityGroupNames;
    }

    public String getCacheSubnetGroupName() {
        return cacheSubnetGroupName;
    }

    public void setCacheSubnetGroupName(String cacheSubnetGroupName) {
        this.cacheSubnetGroupName = cacheSubnetGroupName;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    public String getNotificationTopicArn() {
        return notificationTopicArn;
    }

    public void setNotificationTopicArn(String notificationTopicArn) {
        this.notificationTopicArn = notificationTopicArn;
    }

    public Integer getNumCacheNodes() {
        return numCacheNodes;
    }

    public void setNumCacheNodes(Integer numCacheNodes) {
        this.numCacheNodes = numCacheNodes;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getPreferredAvailabilityZone() {
        return preferredAvailabilityZone;
    }

    public void setPreferredAvailabilityZone(String preferredAvailabilityZone) {
        this.preferredAvailabilityZone = preferredAvailabilityZone;
    }

    public String getPreferredMaintenanceWindow() {
        return preferredMaintenanceWindow;
    }

    public void setPreferredMaintenanceWindow(String preferredMaintenanceWindow) {
        this.preferredMaintenanceWindow = preferredMaintenanceWindow;
    }

    public String getReplicationGroupId() {
        return replicationGroupId;
    }

    public void setReplicationGroupId(String replicationGroupId) {
        this.replicationGroupId = replicationGroupId;
    }

    public List<String> getSecurityGroupIds() {
        return securityGroupIds;
    }

    public void setSecurityGroupIds(List<String> securityGroupIds) {
        this.securityGroupIds = securityGroupIds;
    }

    public List<String> getSnapshotArns() {
        return snapshotArns;
    }

    public void setSnapshotArns(List<String> snapshotArns) {
        this.snapshotArns = snapshotArns;
    }

    public Integer getSnapshotRetentionLimit() {
        return snapshotRetentionLimit;
    }

    public void setSnapshotRetentionLimit(Integer snapshotRetentionLimit) {
        this.snapshotRetentionLimit = snapshotRetentionLimit;
    }

    public String getSnapshotWindow() {
        return snapshotWindow;
    }

    public void setSnapshotWindow(String snapshotWindow) {
        this.snapshotWindow = snapshotWindow;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean refresh() {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        DescribeCacheClustersResponse response = client.describeCacheClusters(
            r -> r.cacheClusterId(getCacheClusterId())
        );

        if (!response.cacheClusters().isEmpty()) {
            CacheCluster cacheCluster = response.cacheClusters().get(0);

            setAutoMinorVersionUpgrade(cacheCluster.autoMinorVersionUpgrade());

            //cacheCluster.azMode();

            setCacheClusterId(cacheCluster.cacheClusterId());
            setCacheNodeType(cacheCluster.cacheNodeType());
            setCacheParameterGroupName(cacheCluster.cacheParameterGroup().cacheParameterGroupName());
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

            ListTagsForResourceResponse tagResponse = client.listTagsForResource(
                r -> r.resourceName(getCacheClusterId())
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

        CreateCacheClusterResponse response = client.createCacheCluster(
            r -> r.autoMinorVersionUpgrade(getAutoMinorVersionUpgrade())
                .azMode(getAzMode())
                .cacheClusterId(getCacheClusterId())
                .cacheNodeType(getCacheNodeType())
                .cacheParameterGroupName(getCacheParameterGroupName())
                .cacheSecurityGroupNames(getCacheSecurityGroupNames())
                .cacheSubnetGroupName(getCacheSubnetGroupName())
                .engine(getEngine())
                .engineVersion(getEngineVersion())
                .notificationTopicArn(getNotificationTopicArn())
                .numCacheNodes(getNumCacheNodes())
                .port(getPort())
                .preferredAvailabilityZone(getPreferredAvailabilityZone())
                .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
                .replicationGroupId(getReplicationGroupId())
                .securityGroupIds(getSecurityGroupIds())
                .snapshotArns(getSnapshotArns())
                .snapshotRetentionLimit(getSnapshotRetentionLimit())
                .snapshotWindow(getSnapshotWindow())
                .tags(toCacheTags(getTags()))
        );

        setStatus(response.cacheCluster().cacheClusterStatus());

    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        client.modifyCacheCluster(
            r -> r.autoMinorVersionUpgrade(getAutoMinorVersionUpgrade())
                .azMode(getAzMode())
                .cacheClusterId(getCacheClusterId())
                .cacheNodeType(getCacheNodeType())
                .cacheParameterGroupName(getCacheParameterGroupName())
                .cacheSecurityGroupNames(getCacheSecurityGroupNames())
                .engineVersion(getEngineVersion())
                .notificationTopicArn(getNotificationTopicArn())
                .numCacheNodes(getNumCacheNodes())
                .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
                .securityGroupIds(getSecurityGroupIds())
                .snapshotRetentionLimit(getSnapshotRetentionLimit())
                .snapshotWindow(getSnapshotWindow())
        );

        if (changedProperties.contains("tags")) {
            Map<String, String> pendingTags = getTags();
            Map<String, String> currentTags = ((CacheClusterResource) current).getTags();

            saveTags(pendingTags, currentTags, client);
        }
    }

    @Override
    public void delete() {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        client.deleteCacheCluster(
            r -> r.cacheClusterId(getCacheClusterId())
        );
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
                r -> r.resourceName(getCacheClusterId())
                    .tagKeys(diff.entriesOnlyOnLeft().keySet())
            );
        }

        // New tags
        if (!diff.entriesOnlyOnRight().isEmpty()) {
            client.addTagsToResource(
                r -> r.resourceName(getCacheClusterId())
                    .tags(toCacheTags(diff.entriesOnlyOnRight()))
            );
        }

        // Old but modified tags
        if (!diff.entriesDiffering().isEmpty()) {
            client.removeTagsFromResource(
                r -> r.resourceName(getCacheClusterId())
                    .tagKeys(diff.entriesDiffering().keySet())
            );

            Map<String, String> addTags = new HashMap<>();
            diff.entriesDiffering().keySet().forEach(o -> addTags.put(o, diff.entriesDiffering().get(o).rightValue()));

            client.addTagsToResource(
                r -> r.resourceName(getCacheClusterId())
                    .tags(toCacheTags(addTags))
            );
        }
    }
}
