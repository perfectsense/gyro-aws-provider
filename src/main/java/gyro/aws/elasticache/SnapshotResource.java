package gyro.aws.elasticache;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.Type;
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
 *     aws::cache-snapshot cache-snapshot-example
 *         snapshot-name: "cache-snapshot-example"
 *         replication-group-id: "replication-group-example"
 *         cache-cluster-id: $(aws::cache-cluster cache-cluster-example | cache-cluster-id)
 *     end
 */
@Type("cache-snapshot")
public class SnapshotResource extends AwsResource implements Copyable<Snapshot> {
    private String snapshotName;
    private String replicationGroupId;
    private String cacheClusterId;

    private String status;

    /**
     * Name of the snapshot. (Required)
     */
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
     * Id of the cache cluster. (Required)
     */
    public String getCacheClusterId() {
        return cacheClusterId;
    }

    public void setCacheClusterId(String cacheClusterId) {
        this.cacheClusterId = cacheClusterId;
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
        setCacheClusterId(snapshot.cacheClusterId());
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
    public void create() {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        CreateSnapshotResponse response = client.createSnapshot(
            r -> r.snapshotName(getSnapshotName())
                .replicationGroupId(getReplicationGroupId())
                .cacheClusterId(getCacheClusterId())
        );

        setStatus(response.snapshot().snapshotStatus());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        client.deleteSnapshot(
            r -> r.snapshotName(getSnapshotName())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("snapshot");

        if (!ObjectUtils.isBlank(getSnapshotName())) {
            sb.append(" - ").append(getSnapshotName());
        }

        return sb.toString();
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

        }

        return snapshot;
    }
}
