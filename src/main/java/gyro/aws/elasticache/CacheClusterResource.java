package gyro.aws.elasticache;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.core.GyroCore;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ResourceName("cache-cluster")
public class CacheClusterResource extends AwsResource {
    private Boolean autoMinorVersionUpgrade;
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
    private String preferredAvailabilityZone;
    private String preferredMaintenanceWindow;
    private String replicationGroupId;
    private List<String> securityGroupIds;
    private List<String> snapshotArns;
    private Integer snapshotRetentionLimit;
    private String snapshotWindow;
    private Map<String, String> tags;
    private List<String> nodeAvailabilityZone;
    private String accountNumber;
    private String arn;
    private Boolean applyImmediately;

    private String status;
    private List<String> nodes;

    @ResourceDiffProperty(updatable = true)
    public Boolean getAutoMinorVersionUpgrade() {
        if (autoMinorVersionUpgrade == null) {
            autoMinorVersionUpgrade = true;
        }

        return autoMinorVersionUpgrade;
    }

    public void setAutoMinorVersionUpgrade(Boolean autoMinorVersionUpgrade) {
        this.autoMinorVersionUpgrade = autoMinorVersionUpgrade;
    }

    @ResourceDiffProperty(updatable = true)
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

    @ResourceDiffProperty(updatable = true)
    public String getCacheNodeType() {
        return cacheNodeType;
    }

    public void setCacheNodeType(String cacheNodeType) {
        this.cacheNodeType = cacheNodeType;
    }

    @ResourceDiffProperty(updatable = true)
    public String getCacheParamGroupName() {
        return cacheParamGroupName;
    }

    public void setCacheParamGroupName(String cacheParamGroupName) {
        this.cacheParamGroupName = cacheParamGroupName;
    }

    @ResourceDiffProperty(updatable = true)
    public List<String> getCacheSecurityGroupNames() {
        if (cacheSecurityGroupNames == null) {
            cacheSecurityGroupNames = new ArrayList<>();
        }

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

    @ResourceDiffProperty(updatable = true)
    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    @ResourceDiffProperty(updatable = true)
    public String getNotificationTopicArn() {
        return notificationTopicArn;
    }

    public void setNotificationTopicArn(String notificationTopicArn) {
        this.notificationTopicArn = notificationTopicArn;
    }

    @ResourceDiffProperty(updatable = true)
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

    @ResourceDiffProperty(updatable = true)
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

    @ResourceDiffProperty(updatable = true)
    public List<String> getSecurityGroupIds() {
        if (securityGroupIds == null) {
            securityGroupIds = new ArrayList<>();
        }

        return securityGroupIds;
    }

    public void setSecurityGroupIds(List<String> securityGroupIds) {
        this.securityGroupIds = securityGroupIds;
    }

    public List<String> getSnapshotArns() {
        if (snapshotArns == null) {
            snapshotArns = new ArrayList<>();
        }

        return snapshotArns;
    }

    public void setSnapshotArns(List<String> snapshotArns) {
        this.snapshotArns = snapshotArns;
    }

    @ResourceDiffProperty(updatable = true)
    public Integer getSnapshotRetentionLimit() {
        return snapshotRetentionLimit;
    }

    public void setSnapshotRetentionLimit(Integer snapshotRetentionLimit) {
        this.snapshotRetentionLimit = snapshotRetentionLimit;
    }

    @ResourceDiffProperty(updatable = true)
    public String getSnapshotWindow() {
        return snapshotWindow;
    }

    public void setSnapshotWindow(String snapshotWindow) {
        this.snapshotWindow = snapshotWindow;
    }

    @ResourceDiffProperty(updatable = true)
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    @ResourceDiffProperty(updatable = true)
    public List<String> getNodeAvailabilityZone() {
        if (nodeAvailabilityZone == null) {
            nodeAvailabilityZone = new ArrayList<>();
        }

        return nodeAvailabilityZone;
    }

    public void setNodeAvailabilityZone(List<String> nodeAvailabilityZone) {
        this.nodeAvailabilityZone = nodeAvailabilityZone;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
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

    public Boolean getApplyImmediately() {
        if (applyImmediately == null) {
            applyImmediately = true;
        }

        return applyImmediately;
    }

    public void setApplyImmediately(Boolean applyImmediately) {
        this.applyImmediately = applyImmediately;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getNodes() {
        if (nodes == null) {
            nodes = new ArrayList<>();
        }

        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
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

            setAutoMinorVersionUpgrade(cacheCluster.autoMinorVersionUpgrade());

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
            .autoMinorVersionUpgrade(getAutoMinorVersionUpgrade())
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
            .preferredAvailabilityZone(getPreferredAvailabilityZone())
            .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
            .securityGroupIds(getSecurityGroupIds())
            .tags(toCacheTags(getTags()));

        if (getEngine().equalsIgnoreCase("redis")) {
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
                .autoMinorVersionUpgrade(getAutoMinorVersionUpgrade())
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
                    .newAvailabilityZones(getNodeAvailabilityZone());
            }

            if (engine.equalsIgnoreCase("redis")) {
                builder.snapshotRetentionLimit(getSnapshotRetentionLimit())
                    .snapshotWindow(getSnapshotWindow());
            }

            ModifyCacheClusterRequest modifyCacheClusterRequest = builder.applyImmediately(getApplyImmediately()).build();

            if (getApplyImmediately()) {
                client.modifyCacheCluster(modifyCacheClusterRequest);

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
}
