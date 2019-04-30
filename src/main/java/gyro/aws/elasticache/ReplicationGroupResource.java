package gyro.aws.elasticache;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.core.resource.Resource;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticache.model.AutomaticFailoverStatus;
import software.amazon.awssdk.services.elasticache.model.CreateReplicationGroupResponse;
import software.amazon.awssdk.services.elasticache.model.DescribeReplicationGroupsResponse;
import software.amazon.awssdk.services.elasticache.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.elasticache.model.ReplicationGroup;
import software.amazon.awssdk.services.elasticache.model.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ReplicationGroupResource extends AwsResource {
    private Boolean enableRestEncrytion;
    private Boolean enableAutomaticFailOver;
    private Boolean enableAutoMinorVersionUpgrade;
    private String cacheNodeType;
    private String cacheParameterGroupName;
    private List<String> cacheSecurityGroupNames;
    private String engine;
    private String engineVersion;
    private String cacheSubnetGroupName;
    private List<NodeGroupConfigurationResource> nodeGroupConfiguration;
    private String notificationTopicArn;
    private Integer numCacheClusters;
    private Integer port;
    private List<String> preferredCacheClusterAZs;
    private String preferredMaintenanceWindow;
    private String primaryClusterId;
    private Integer numNodeGroups;
    private Integer replicasPerNodeGroup;
    private String replicationGroupId;
    private List<String> securityGroupIds;
    private List<String> snapshotArns;
    private String snapshotName;
    private Integer snapshotRetentionLimit;
    private String snapshotWindow;
    private String replicationGroupDescription;
    private Boolean enableTransitEncryption;
    private Map<String, String> tags;

    private String status;

    public Boolean getEnableRestEncrytion() {
        return enableRestEncrytion;
    }

    public void setEnableRestEncrytion(Boolean enableRestEncrytion) {
        this.enableRestEncrytion = enableRestEncrytion;
    }

    public Boolean getEnableAutomaticFailOver() {
        return enableAutomaticFailOver;
    }

    public void setEnableAutomaticFailOver(Boolean enableAutomaticFailOver) {
        this.enableAutomaticFailOver = enableAutomaticFailOver;
    }

    public Boolean getEnableAutoMinorVersionUpgrade() {
        return enableAutoMinorVersionUpgrade;
    }

    public void setEnableAutoMinorVersionUpgrade(Boolean enableAutoMinorVersionUpgrade) {
        this.enableAutoMinorVersionUpgrade = enableAutoMinorVersionUpgrade;
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

    public String getCacheSubnetGroupName() {
        return cacheSubnetGroupName;
    }

    public void setCacheSubnetGroupName(String cacheSubnetGroupName) {
        this.cacheSubnetGroupName = cacheSubnetGroupName;
    }

    public List<NodeGroupConfigurationResource> getNodeGroupConfiguration() {
        return nodeGroupConfiguration;
    }

    public void setNodeGroupConfiguration(List<NodeGroupConfigurationResource> nodeGroupConfiguration) {
        this.nodeGroupConfiguration = nodeGroupConfiguration;
    }

    public String getNotificationTopicArn() {
        return notificationTopicArn;
    }

    public void setNotificationTopicArn(String notificationTopicArn) {
        this.notificationTopicArn = notificationTopicArn;
    }

    public Integer getNumCacheClusters() {
        return numCacheClusters;
    }

    public void setNumCacheClusters(Integer numCacheClusters) {
        this.numCacheClusters = numCacheClusters;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public List<String> getPreferredCacheClusterAZs() {
        return preferredCacheClusterAZs;
    }

    public void setPreferredCacheClusterAZs(List<String> preferredCacheClusterAZs) {
        this.preferredCacheClusterAZs = preferredCacheClusterAZs;
    }

    public String getPreferredMaintenanceWindow() {
        return preferredMaintenanceWindow;
    }

    public void setPreferredMaintenanceWindow(String preferredMaintenanceWindow) {
        this.preferredMaintenanceWindow = preferredMaintenanceWindow;
    }

    public String getPrimaryClusterId() {
        return primaryClusterId;
    }

    public void setPrimaryClusterId(String primaryClusterId) {
        this.primaryClusterId = primaryClusterId;
    }

    public Integer getNumNodeGroups() {
        return numNodeGroups;
    }

    public void setNumNodeGroups(Integer numNodeGroups) {
        this.numNodeGroups = numNodeGroups;
    }

    public Integer getReplicasPerNodeGroup() {
        return replicasPerNodeGroup;
    }

    public void setReplicasPerNodeGroup(Integer replicasPerNodeGroup) {
        this.replicasPerNodeGroup = replicasPerNodeGroup;
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

    public String getSnapshotName() {
        return snapshotName;
    }

    public void setSnapshotName(String snapshotName) {
        this.snapshotName = snapshotName;
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

    public String getReplicationGroupDescription() {
        return replicationGroupDescription;
    }

    public void setReplicationGroupDescription(String replicationGroupDescription) {
        this.replicationGroupDescription = replicationGroupDescription;
    }

    public Boolean getEnableTransitEncryption() {
        return enableTransitEncryption;
    }

    public void setEnableTransitEncryption(Boolean enableTransitEncryption) {
        this.enableTransitEncryption = enableTransitEncryption;
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

        DescribeReplicationGroupsResponse response = client.describeReplicationGroups(
            r -> r.replicationGroupId(getReplicationGroupId())
        );

        if (!response.replicationGroups().isEmpty()) {
            /*ReplicationGroup replicationGroup = response.replicationGroups().get(0);

            replicationGroup.atRestEncryptionEnabled();
            replicationGroup.automaticFailover().equals(AutomaticFailoverStatus.ENABLED);
            replicationGroup.autoMinorVersionUpgrade();
            replicationGroup.cacheNodeType();
            replicationGroup.cacheParameterGroupName();
            replicationGroup.cacheSecurityGroupNames();
            replicationGroup.engine();
            replicationGroup.engineVersion();
            replicationGroup.cacheSubnetGroupName();
            replicationGroup.nodeGroupConfiguration();
            replicationGroup.notificationTopicArn();
            replicationGroup.memberClusters().size();
            replicationGroup.configurationEndpoint().port();
            replicationGroup.preferredCacheClusterAZs();
            replicationGroup.preferredMaintenanceWindow();
            replicationGroup.snapshottingClusterId();
            replicationGroup.nodeGroups().size();
            //replicationGroup.replicasPerNodeGroup();
            replicationGroup.replicationGroupId();
            //replicationGroup.securityGroupIds();
            //replicationGroup.snapshotArns();
            //replicationGroup.snapshotName();
            replicationGroup.snapshotRetentionLimit();
            replicationGroup.snapshotWindow();
            replicationGroup.description();
            replicationGroup.transitEncryptionEnabled();

            replicationGroup.nodeGroups().get(0).nodeGroupId();

            client.decreaseReplicaCount(
                r -> r.newReplicaCount()
                    .replicationGroupId()
                    .replicaConfiguration(
                        o -> o.newReplicaCount()
                            .nodeGroupId()
                            .preferredAvailabilityZones()
                    )
            );

            client.increaseReplicaCount(
                r -> r.newReplicaCount()
                    .replicationGroupId()
                    .replicaConfiguration(
                        r -> r.nodeGroupId()
                            .preferredAvailabilityZones()
                    )
            );*/

            ListTagsForResourceResponse tagResponse = client.listTagsForResource(
                r -> r.resourceName(getReplicationGroupId())
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

        CreateReplicationGroupResponse response = client.createReplicationGroup(
            r -> r.atRestEncryptionEnabled(getEnableRestEncrytion())
                .automaticFailoverEnabled(getEnableAutomaticFailOver())
                .autoMinorVersionUpgrade(getEnableAutoMinorVersionUpgrade())
                .cacheNodeType(getCacheNodeType())
                .cacheParameterGroupName(getCacheParameterGroupName())
                .cacheSecurityGroupNames(getCacheSecurityGroupNames())
                .engine(getEngine())
                .engineVersion(getEngineVersion())
                .cacheSubnetGroupName(getCacheSubnetGroupName())
                .nodeGroupConfiguration(getNodeGroupConfiguration().stream().map(NodeGroupConfigurationResource::getNodeGroupConfiguration).collect(Collectors.toList()))
                .notificationTopicArn(getNotificationTopicArn())
                .numCacheClusters(getNumCacheClusters())
                .port(getPort())
                .preferredCacheClusterAZs(getPreferredCacheClusterAZs())
                .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
                .primaryClusterId(getPrimaryClusterId())
                .numNodeGroups(getNumNodeGroups())
                .replicasPerNodeGroup(getNumNodeGroups())
                .replicationGroupId(getReplicationGroupId())
                .securityGroupIds(getSecurityGroupIds())
                .snapshotArns(getSnapshotArns())
                .snapshotName(getSnapshotName())
                .snapshotRetentionLimit(getSnapshotRetentionLimit())
                .snapshotWindow(getSnapshotWindow())
                .tags(toCacheTags(getTags()))
                .replicationGroupDescription(getReplicationGroupDescription())
                .transitEncryptionEnabled(getEnableTransitEncryption())
        );

        setStatus(response.replicationGroup().status());

    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        client.modifyReplicationGroup(
            r -> r.automaticFailoverEnabled(getEnableAutomaticFailOver())
                .autoMinorVersionUpgrade(getEnableAutoMinorVersionUpgrade())
                .cacheNodeType(getCacheNodeType())
                .cacheParameterGroupName(getCacheParameterGroupName())
                .cacheSecurityGroupNames(getCacheSecurityGroupNames())
                .engineVersion(getEngineVersion())
                .notificationTopicArn(getNotificationTopicArn())
                .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
                .primaryClusterId(getPrimaryClusterId())
                .replicationGroupId(getReplicationGroupId())
                .securityGroupIds(getSecurityGroupIds())
                .snapshotRetentionLimit(getSnapshotRetentionLimit())
                .snapshotWindow(getSnapshotWindow())
                .replicationGroupDescription(getReplicationGroupDescription())
        );

        if (changedProperties.contains("tags")) {
            Map<String, String> pendingTags = getTags();
            Map<String, String> currentTags = ((ReplicationGroupResource) current).getTags();

            saveTags(pendingTags, currentTags, client);
        }
    }

    @Override
    public void delete() {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        //more options

        client.deleteReplicationGroup(
            r -> r.replicationGroupId(getReplicationGroupId())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("replication group");

        if (!ObjectUtils.isBlank(getReplicationGroupId())) {
            sb.append(" - ").append(getReplicationGroupId());
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
                r -> r.resourceName(getReplicationGroupId())
                    .tagKeys(diff.entriesOnlyOnLeft().keySet())
            );
        }

        // New tags
        if (!diff.entriesOnlyOnRight().isEmpty()) {
            client.addTagsToResource(
                r -> r.resourceName(getReplicationGroupId())
                    .tags(toCacheTags(diff.entriesOnlyOnRight()))
            );
        }

        // Old but modified tags
        if (!diff.entriesDiffering().isEmpty()) {
            client.removeTagsFromResource(
                r -> r.resourceName(getReplicationGroupId())
                    .tagKeys(diff.entriesDiffering().keySet())
            );

            Map<String, String> addTags = new HashMap<>();
            diff.entriesDiffering().keySet().forEach(o -> addTags.put(o, diff.entriesDiffering().get(o).rightValue()));

            client.addTagsToResource(
                r -> r.resourceName(getReplicationGroupId())
                    .tags(toCacheTags(addTags))
            );
        }
    }
}
