package gyro.aws.elasticache;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceOutput;
import gyro.core.resource.ResourceType;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticache.model.CreateSnapshotResponse;
import software.amazon.awssdk.services.elasticache.model.DescribeSnapshotsResponse;
import software.amazon.awssdk.services.elasticache.model.Snapshot;

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
@ResourceType("cache-snapshot")
public class SnapshotResource extends AwsResource {
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
    @ResourceOutput
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean refresh() {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        DescribeSnapshotsResponse response = client.describeSnapshots(
            r -> r.snapshotName(getSnapshotName())
        );

        if (!response.snapshots().isEmpty()) {
            Snapshot snapshot = response.snapshots().get(0);

            setReplicationGroupId(snapshot.replicationGroupId());
            setCacheClusterId(snapshot.cacheClusterId());
            setStatus(snapshot.snapshotStatus());

            return true;
        } else {
            return false;
        }
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
}
