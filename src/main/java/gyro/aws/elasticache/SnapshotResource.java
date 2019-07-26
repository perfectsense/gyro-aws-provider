package gyro.aws.elasticache;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.Type;
import gyro.core.diff.Context;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticache.model.CreateSnapshotResponse;
import software.amazon.awssdk.services.elasticache.model.DescribeSnapshotsResponse;
import software.amazon.awssdk.services.elasticache.model.Snapshot;
import software.amazon.awssdk.services.elasticache.model.SnapshotNotFoundException;

import java.util.Set;

/**
 * Creates a cache subnet group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::elasticache-snapshot cache-snapshot-example
 *         snapshot-name: "cache-snapshot-example"
 *         replication-group-id: "replication-group-example"
 *         cache-cluster: $(aws::elasticache-cluster cache-cluster-example)
 *     end
 */
@Type("elasticache-snapshot")
public class SnapshotResource extends AwsResource implements Copyable<Snapshot> {
    private String snapshotName;
    private String replicationGroupId;
    private CacheClusterResource cacheCluster;

    private String status;

    /**
     * Name of the snapshot. (Required)
     */
    @Id
    public String getSnapshotName() {
        return snapshotName;
    }

    public void setSnapshotName(String snapshotName) {
        this.snapshotName = snapshotName;
    }

    /**
     * Id of the replication group. (Required)
     */
    public String getReplicationGroupId() {
        return replicationGroupId;
    }

    public void setReplicationGroupId(String replicationGroupId) {
        this.replicationGroupId = replicationGroupId;
    }

    /**
     * The cache cluster to snapshot from. (Required)
     */
    public CacheClusterResource getCacheCluster() {
        return cacheCluster;
    }

    public void setCacheCluster(CacheClusterResource cacheCluster) {
        this.cacheCluster = cacheCluster;
    }

    /**
     * Status of the replication group.
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

    @Override
    public void copyFrom(Snapshot snapshot) {
        setSnapshotName(snapshot.snapshotName());
        setReplicationGroupId(snapshot.replicationGroupId());
        setCacheCluster(snapshot.cacheClusterId() != null ? findById(CacheClusterResource.class, snapshot.cacheClusterId()) : null);
        setStatus(snapshot.snapshotStatus());
    }

    @Override
    public boolean refresh() {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        Snapshot snapshot = getSnapshot(client);
        if (snapshot == null) {
            return false;
        }

        copyFrom(snapshot);
        return true;
    }

    @Override
    public void create(GyroUI ui, Context context) {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        CreateSnapshotResponse response = client.createSnapshot(
            r -> r.snapshotName(getSnapshotName())
                .replicationGroupId(getReplicationGroupId())
                .cacheClusterId(getCacheCluster() != null ? getCacheCluster().getCacheClusterId() : null)
        );

        setStatus(response.snapshot().snapshotStatus());
    }

    @Override
    public void update(GyroUI ui, Context context, Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, Context context) {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        client.deleteSnapshot(
            r -> r.snapshotName(getSnapshotName())
        );
    }

    private Snapshot getSnapshot(ElastiCacheClient client) {
        Snapshot snapshot = null;

        try {
            DescribeSnapshotsResponse response = client.describeSnapshots(
                r -> r.snapshotName(getSnapshotName())
            );

            if (!response.snapshots().isEmpty()) {
                snapshot = response.snapshots().get(0);
            }

        } catch (SnapshotNotFoundException ex) {
            // Ignore
        }

        return snapshot;
    }
}
